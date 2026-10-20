# m3_dotnet — Twitter 联系人评分查询

使用 **C# 12 / .NET 8 / ASP.NET Core Minimal API + MySqlConnector 2.3.7** 实现 `m3_jooby`、`m3_vertx` 对应的项目。沿用原项目已有 ETL 结果，不依赖在线 Twitter API。

## 接口

`GET /twitter?user_id=123&type=reply|retweet|both&phrase=hello&hashtag=cloud`

四个参数均必需。`phrase` 和 `hashtag` 必须非空，避免原 Java 实现对空 phrase 的无限循环；参数中的特殊字符应 URL 编码。

响应为 UTF-8 纯文本：第一行 `dwendwen,144331934093`，随后每行 `user_id\tscreen_name\tdescription\tlatest_tweet_text`。无联系人或非法参数返回前缀和 `INVALID`；有联系人但所选类型为空或分数全部为零，仅返回前缀。数据库/数据错误返回 HTTP 503、前缀和 `ERROR`，详细异常只记到日志。

保留 `GET /` → `Welcome!`。

## 评分与排序

1. 查询 `contact_tweets` 中对应用户的预计算 JSON，分别建立 reply 和 retweet 联系人集合。
2. `both` 合并两类文本及 hashtag，按最大 tweet ID 选择最新文本。与 Java 相同，重合联系人的基础分数和资料保留 reply 侧数据。
3. `hashtag_score = 1`，当 hashtag_count > 10 且联系人不是自己时，改为 `1 + ln(1 + hashtag_count - 10)`。
4. `keyword_score = 1 + ln(phrase_matches + hashtag_matches + 1)`。
5. `score = interaction_score × hashtag_score × keyword_score`。
6. 分数降序，同分按数值 user ID 降序，只输出分数大于零的用户。

phrase 匹配区分大小写，使用 KMP 并计入重叠匹配；hashtag 使用不区分大小写的完整标签匹配。数据库中的文本/标签按 `U+001F` 分隔。

## 数据库配置

优先读取 `ConnectionStrings:Twitter`，可通过环境变量设置：

```bash
export ConnectionStrings__Twitter='Server=localhost;Port=3306;Database=twitter_db;User ID=twitter_app;Password=YOUR_PASSWORD'
dotnet run --urls http://localhost:8083
```

若连接字符串未提供，则兼容原项目的 `mysqlHost`、`mysqlUser`、`mysqlPassword` 环境变量。每次请求获取独立的池化连接，使用参数化 SQL 和异步读写，避免复用 Java 原版的全局连接。

`Data/schema.sql` 是从 Java 查询代码推导的**最小查询表结构**，不会删除已有数据库。已有数据库无需重新建表；本次未迁移 Spark/Zeppelin ETL，也未附带原始 Twitter 数据。

`contact_tweet` 是 JSON 数组，每项格式如下：

```json
[
  {
    "other_user_id": 456,
    "all_texts": "hello cloud\u001fhello again",
    "maxtid": 1002,
    "maxtid_text": "hello again",
    "all_hashtags": "cloud\u001fdistributed",
    "hashtag_count": 12,
    "interaction_score": 2.5,
    "other_screen_name": "example_user",
    "other_description": "Example profile"
  }
]
```

`hashtag_count` 缺省/null 视为 0，名称和简介缺省/null 视为空字符串；数值字段也可由 JSON 数字字符串输入。

## 文件

| 文件 / 目录 | 职责 |
| --- | --- |
| `Program.cs` | 路由、参数检查与错误响应 |
| `Data/ContactRepository.cs` | MySQL 参数化查询与 JSON 解析 |
| `Models/` | JSON DTO 和联系人评分模型 |
| `Services/TwitterService.cs` | 分类、合并、评分、排序和响应 |
| `Services/KmpMatcher.cs` | 重叠子串匹配 |

本次按要求交付源码及静态检查，未还原 NuGet 依赖、编译、连接数据库或启动服务。
