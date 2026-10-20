#!/usr/bin/env bash
set -euo pipefail
: "${SERVICE:?}" "${STACK:?}" "${IMAGE_TAG:?}"
case "$SERVICE" in m1|m2|m3|mix) ;; *) exit 2 ;; esac
case "$STACK" in dotnet|vertx|jooby) ;; *) exit 2 ;; esac
root=$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)
source_dir="$root/web/${SERVICE}_${STACK}"
image="dwendwen/$SERVICE-$STACK:$IMAGE_TAG"
test -d "$source_dir"
if [[ "$STACK" == dotnet ]]; then
  [[ "$SERVICE" != mix ]] || { echo 'No mix_dotnet project exists.' >&2; exit 2; }
  docker build --file "$root/phase3-deployment/AzureDevOps/docker/dotnet.Dockerfile" \
    --build-arg "APP_DLL=${SERVICE^^}.DotNet.dll" --tag "$image" "$source_dir"
else
  docker build --file "$root/phase3-deployment/AzureDevOps/docker/java.Dockerfile" \
    --build-arg "STACK=$STACK" --tag "$image" "$source_dir"
fi
