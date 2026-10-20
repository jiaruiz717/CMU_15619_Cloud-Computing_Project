#!/usr/bin/env bash
set -euo pipefail
: "${RESOURCE_GROUP:?}" "${AKS_NAME:?}" "${ACR_NAME:?}" "${K8S_NAMESPACE:?}"
: "${STACK:?}" "${IMAGE_TAG:?}" "${TARGET_ENV:?}" "${MYSQL_HOST:?}" "${MYSQL_USER:?}" "${MYSQL_PASSWORD:?}"
: "${RELEASE_NAME:=dwendwen}" "${INCLUDE_MIX:=false}"
for key in MYSQL_HOST MYSQL_USER MYSQL_PASSWORD; do
  [[ "${!key}" != '$('* ]] || { echo "Missing pipeline variable: $key" >&2; exit 2; }
done
case "$TARGET_ENV" in dev|prod) ;; *) exit 2 ;; esac
case "$STACK" in dotnet|vertx|jooby) ;; *) exit 2 ;; esac
INCLUDE_MIX=${INCLUDE_MIX,,}
[[ "$STACK" != dotnet || "$INCLUDE_MIX" == false ]] || exit 2
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)
chart="$root/phase3-deployment/AzureDevOps/helm/dwendwen"
work_dir=$(mktemp -d)
trap 'rm -rf "$work_dir"' EXIT
export KUBECONFIG="$work_dir/kubeconfig"
k8s_version=$(az aks show --name "$AKS_NAME" --resource-group "$RESOURCE_GROUP" --query kubernetesVersion -o tsv)
az aks install-cli --client-version "v${k8s_version#v}" \
  --install-location "$work_dir/kubectl" --kubelogin-install-location "$work_dir/kubelogin"
export PATH="$work_dir:$PATH"
az aks get-credentials --name "$AKS_NAME" --resource-group "$RESOURCE_GROUP" --file "$KUBECONFIG" --overwrite-existing
# Reuse the AzureCLI@2 authenticated session; never request admin kubeconfig credentials.
kubelogin convert-kubeconfig -l azurecli
kubectl create namespace "$K8S_NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -

# One Secret per build means a Helm rollback continues using that revision's DB configuration.
export MYSQL_SECRET="$RELEASE_NAME-mysql-$IMAGE_TAG"
export K8S_NAMESPACE
python3 - <<'PY' | kubectl apply -f -
import json, os
print(json.dumps({
    'apiVersion': 'v1', 'kind': 'Secret', 'type': 'Opaque',
    'metadata': {'name': os.environ['MYSQL_SECRET'], 'namespace': os.environ['K8S_NAMESPACE'],
                 'labels': {'app.kubernetes.io/part-of': 'dwendwen'}},
    'stringData': {'mysqlHost': os.environ['MYSQL_HOST'], 'mysqlUser': os.environ['MYSQL_USER'],
                   'mysqlPassword': os.environ['MYSQL_PASSWORD']}
}))
PY
registry=$(az acr show --name "$ACR_NAME" --query loginServer -o tsv)
helm upgrade --install "$RELEASE_NAME" "$chart" \
  --namespace "$K8S_NAMESPACE" \
  --values "$chart/values.$TARGET_ENV.yaml" \
  --set-string image.registry="$registry" \
  --set-string image.tag="$IMAGE_TAG" \
  --set-string image.stack="$STACK" \
  --set-string database.existingSecret="$MYSQL_SECRET" \
  --set services.mix.enabled="$INCLUDE_MIX" \
  --atomic --wait --timeout 10m --history-max 10
kubectl get deployments,services --namespace "$K8S_NAMESPACE"

# Check routing via port-forward; no dependency on public IP allocation or external DNS.
kubectl port-forward --namespace "$K8S_NAMESPACE" "service/$RELEASE_NAME-gateway" 18080:80 \
  > "$work_dir/port-forward.log" 2>&1 &
forward_pid=$!
trap 'kill "$forward_pid" 2>/dev/null || true; rm -rf "$work_dir"' EXIT
ready=false
for attempt in {1..20}; do
  if curl --silent --fail http://127.0.0.1:18080/healthz > /dev/null; then
    ready=true
    break
  fi
  sleep 1
done
[[ "$ready" == true ]] || { echo 'Gateway port-forward did not become ready.' >&2; exit 1; }
BASE_URL=http://127.0.0.1:18080 bash "$root/phase3-deployment/AzureDevOps/scripts/smoke-test.sh"
