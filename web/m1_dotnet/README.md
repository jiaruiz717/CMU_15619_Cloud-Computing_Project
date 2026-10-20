# m1_dotnet — 自定义二维码编解码

使用 **C# 12 / .NET 8 / ASP.NET Core Minimal API** 实现 `m1_jooby`、`m1_vertx` 对应的项目。没有第三方依赖。核心逻辑参考 `m1_jooby/src/main/java/app`，不是标准二维码库的替代示例。

## 接口

`GET /qrcode?type=encode|decode&data=...`

- `encode`：1–13 个字符使用 21×21，14–22 个字符使用 25×25。字符值限制为 0–255，保持原算法的一字节字符表示；不支持把中文直接当作 UTF-8 多字节输入。
- 数据依次为长度字节、各字符字节和对应奇偶校验字节，之后以 `1110110000010001` 填充。
- 保留三处定位图案、时序图案、25×25 对齐图案、双列折返填充、Logistic Map 异或、`0x` 分隔的无前导零十六进制输出。
- `decode`：接收 **32 个以 `0x` 开头的 32 位十六进制整数**，表示完整 32×32 加密画布。支持平移以及 0/90/180/270 度旋转。
- **编码结果不能直接作为解码输入**：这是原项目的接口约定；编码输出是 21×21 或 25×25 矩阵，解码输入是 32×32 画布。
- 解码与原实现一致，跳过校验字节，不实现纠错。无效数据返回 HTTP 400、空正文；未知类型返回 `No such type!`。成功响应为 UTF-8 纯文本，不加团队前缀。

## 代码对应

| C# 文件 | 职责 / Java 来源 |
| --- | --- |
| `Program.cs` | HTTP 路由，对应 `App.java` |
| `Services/QrMatrix.cs` | 模板与数据坐标，对应 `Matrix.java` 和双列扫描 |
| `Services/LogisticMap.cs` | `x0=0.1, r=4` 序列和位异或 |
| `Services/QrCodeService.cs` | 对应 `QREncoding.java`、`QRDecoding.java` |

解码改用四个方向的模板搜索，避免依赖原代码“恰好找到三个定位点”的假设；编码和解码共用数据坐标。

## 可选运行方式

在本目录执行 `dotnet run --urls http://localhost:8081`。`requests.http` 保留了原 Java 测试的编码、解码样例和期望值，供以后联调。

本次按要求交付源码及静态检查，未安装 SDK、编译或启动服务，也未声明通过运行测试。
