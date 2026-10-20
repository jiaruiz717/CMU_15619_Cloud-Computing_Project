#!/usr/bin/env bash
# Use after authenticating to AKS; no infrastructure or database rollback is performed.
set -euo pipefail
: "${KUBE_CONTEXT:?}" "${K8S_NAMESPACE:?}" "${HELM_REVISION:?}"
: "${RELEASE_NAME:=dwendwen}"
[[ "$HELM_REVISION" =~ ^[1-9][0-9]*$ ]] || exit 2
helm rollback "$RELEASE_NAME" "$HELM_REVISION" \
  --kube-context "$KUBE_CONTEXT" --namespace "$K8S_NAMESPACE" --wait --timeout 10m
