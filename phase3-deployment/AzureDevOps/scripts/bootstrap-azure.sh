#!/usr/bin/env bash
# Run only from the manual infrastructure pipeline, or after an explicit az login.
set -euo pipefail
: "${RESOURCE_GROUP:?}" "${LOCATION:?}" "${ACR_NAME:?}" "${AKS_NAME:?}"
: "${PIPELINE_PRINCIPAL_OBJECT_ID:?}" "${NODE_COUNT:=2}"
[[ "$PIPELINE_PRINCIPAL_OBJECT_ID" =~ ^[0-9a-fA-F-]{36}$ ]] || {
  echo 'Set pipelinePrincipalObjectId to the service principal object ID.' >&2
  exit 2
}
[[ "$ACR_NAME" != replace* ]] || { echo 'Choose a unique ACR name in config.' >&2; exit 2; }

az group create --name "$RESOURCE_GROUP" --location "$LOCATION" --output none
if ! az acr show --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP" --output none 2>/dev/null; then
  az acr create --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP" \
    --sku Basic --admin-enabled false --output none
fi
acr_id=$(az acr show --name "$ACR_NAME" --resource-group "$RESOURCE_GROUP" --query id -o tsv)
if ! az aks show --name "$AKS_NAME" --resource-group "$RESOURCE_GROUP" --output none 2>/dev/null; then
  az aks create --name "$AKS_NAME" --resource-group "$RESOURCE_GROUP" \
    --node-count "$NODE_COUNT" --node-vm-size Standard_D2s_v5 \
    --enable-managed-identity --enable-aad --enable-azure-rbac --disable-local-accounts \
    --network-plugin azure --network-plugin-mode overlay \
    --attach-acr "$acr_id" --generate-ssh-keys --output none
else
  # Existing clusters must already use managed Entra ID and Azure RBAC.
  az aks update --name "$AKS_NAME" --resource-group "$RESOURCE_GROUP" \
    --attach-acr "$acr_id" --output none
fi
aks_id=$(az aks show --name "$AKS_NAME" --resource-group "$RESOURCE_GROUP" --query id -o tsv)
assign_role() {
  local role="$1" scope="$2"
  local count
  count=$(az role assignment list --scope "$scope" \
    --query "[?principalId=='$PIPELINE_PRINCIPAL_OBJECT_ID' && roleDefinitionName=='$role'] | length(@)" -o tsv)
  if [[ "$count" == 0 ]]; then
    az role assignment create --assignee-object-id "$PIPELINE_PRINCIPAL_OBJECT_ID" \
      --assignee-principal-type ServicePrincipal --role "$role" --scope "$scope" --output none
  fi
}
assign_role AcrPush "$acr_id"
assign_role 'Azure Kubernetes Service Cluster User Role' "$aks_id"
# Demonstration setup: namespace creation and Helm installs require cluster-wide permissions.
assign_role 'Azure Kubernetes Service RBAC Cluster Admin' "$aks_id"
echo 'ACR/AKS provisioning complete. Allow role assignments to propagate before deploying.'
