#!/usr/bin/env bash
set -euo pipefail
: "${ACR_NAME:?}" "${SERVICE:?}" "${STACK:?}" "${IMAGE_TAG:?}"
registry=$(az acr show --name "$ACR_NAME" --query loginServer --output tsv)
az acr login --name "$ACR_NAME" --output none
image="dwendwen/$SERVICE-$STACK:$IMAGE_TAG"
docker tag "$image" "$registry/$image"
docker push "$registry/$image"
