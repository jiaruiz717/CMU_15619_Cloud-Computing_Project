# m2_dotnet — 区块链校验与挖矿

使用 **C# 12 / .NET 8 / ASP.NET Core Minimal API** 实现 `m2_jooby`、`m2_vertx` 对应的项目。仅使用 .NET 内置的 JSON、SHA-256、BigInteger、ZLibStream，无第三方依赖。

## 接口与格式

`GET /blockchain?cc=<URL-safe Base64 of zlib-compressed UTF-8 JSON>`

请求 JSON 包含 `chain`、`new_tx`、`new_target` 三个字段。`chain` 为已有区块数组；`new_tx` 为新交易数组；`new_target` 为八位小写十六进制目标值。

成功返回 `dwendwen,144331934093\n`，后接压缩编码的 `{"chain":[...]}`；业务输入无效则返回相同前缀和 `INVALID`。压缩结果无需和 Java 的字节串完全一致，解压后的 JSON 含义一致即可。

## 实现内容

- URL-safe Base64 与 **zlib** 编解码，保留 Base64 填充；解压大小上限 4 MiB。
- 校验区块编号、前块哈希参与计算的区块哈希、目标值、PoW、交易时间、余额、金额、手续费和奖励。
- 交易哈希：`SHA256(time|send|recv|amt|fee)` 的前八位，缺失字段留空。
- 区块哈希：`SHA256(SHA256(id|previousHash|txHash...) + pow)` 的前八位。
- 三字段交易 `time/recv/amt` 自动补全团队发送账号、零手续费、交易哈希和 RSA 签名。
- 奖励金额 `500000000 / 2^(blockId/2)`，奖励时间为前块最后一笔交易时间加 `600000000000`。
- 保留原版手续费从发送者扣除、奖励不额外累加手续费的行为。
- 金额和时间使用 `BigInteger`，JSON 数字保持整数精度；金额、账号、签名输出为 JSON 数字，时间和 PoW 为字符串。
- 挖矿支持请求取消，最多尝试 `Mining:MaxAttempts` 次（默认 50,000,000），超限返回 HTTP 503 和 `MINING_LIMIT_EXCEEDED`。

## 与 Java 版本的明确差异

原 `Verification.java` 没有核对声明的区块 hash，也没有实际验证 sig；这里补上两项校验，使用源代码的 RSA 模数和发送账号作为公钥指数。因此，原实现误放行的错误签名/哈希会被拒绝。

同时拒绝空交易区块、无法挖出的零目标，以及会使新奖励时间早于新交易的输入。空链以时间 0、前块哈希 `00000000` 为起点，可创建首块，修复原奖励函数对空链的越界访问。上述属于明确的输入边界修正，不是完全复制原实现的缺陷。

账号、公私钥常数与团队标识沿用课程项目；这是该项目协议实现，不是面向真实资金的区块链。

## 文件

| 文件 | 职责 |
| --- | --- |
| `Program.cs` | 路由、纯文本响应、错误处理 |
| `Services/PayloadCodec.cs` | Base64 / zlib |
| `Services/ChainProtocol.cs` | 哈希、签名、奖励、JSON 整数工具 |
| `Services/ChainValidator.cs` | 区块与交易校验、余额计算 |
| `Services/BlockchainService.cs` | 补全交易、追加奖励、PoW 挖矿 |

可选运行：在本目录执行 `dotnet run --urls http://localhost:8082`。`requests.http` 提供空链挖矿样例。

本次按要求交付源码及静态检查，未安装 SDK、编译、启动服务或验证端到端兼容性。
