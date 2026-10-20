#!/usr/bin/env bash
set -euo pipefail
: "${BASE_URL:?}"
expected='0x66d92b800x5bc76d830x121a7fa60x51c111870x3a5f3ca30x8be36a130xedb223a0xfc8e98780x33bf50de0x2e8709700x545a2d0f0xecef7ae0x461175cd0xff132a'
actual=$(curl --fail --silent --show-error --max-time 30 --get "$BASE_URL/qrcode" \
  --data-urlencode type=encode --data-urlencode 'data=CC Team')
[[ "$actual" == "$expected" ]] || { echo 'QR response differs from the original project vector.' >&2; exit 1; }
# This checks the INVALID response, not DB correctness. The legacy Vert.x worker
# replies INVALID but then continues into Data.serve; do not assume zero DB activity.
actual=$(curl --fail --silent --show-error --max-time 30 --get "$BASE_URL/twitter" \
  --data-urlencode user_id=123 --data-urlencode type=unknown \
  --data-urlencode phrase=hello --data-urlencode hashtag=cloud)
[[ "$actual" == $'dwendwen,144331934093\nINVALID' ]] || { echo 'Twitter route failed.' >&2; exit 1; }
# Valid zlib/JSON envelope with a missing required field -> INVALID in every implementation.
cc=$(python3 - <<'PY'
import base64, zlib
print(base64.urlsafe_b64encode(zlib.compress(b'{}')).decode())
PY
)
actual=$(curl --fail --silent --show-error --max-time 30 --get "$BASE_URL/blockchain" --data-urlencode "cc=$cc")
[[ "$actual" == $'dwendwen,144331934093\nINVALID' ]] || { echo 'Blockchain route failed.' >&2; exit 1; }
echo 'Gateway and three application route checks passed.'
