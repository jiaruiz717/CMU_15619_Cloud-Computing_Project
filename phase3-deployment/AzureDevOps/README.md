# AzureDevOps — Azure Pipelines / ACR / AKS 部署方案

这是与 `ECSfargate`、`EKSfargate`、`EKSmanaged` 并列的新方案，部署对象仍是 `/web` 的项目。

**Azure DevOps 负责 CI/CD，应用实际运行在 AKS，容器镜像存放在 ACR。** 默认使用上一轮新增的 `m1_dotnet`、`m2_dotnet`、`m3_dotnet`；也可选择原有 `vertx` 或 `jooby` 技术栈。

本次只编写配置和脚本，并做静态检查。没有连接 Azure、创建资源、构建镜像、触发流水线或部署服务；不能据此宣称已经跑通。

## 部署结构

| 组成 | 本方案实现 |
| --- | --- |
| CI/CD | Azure Pipelines YAML：Validate → Build → Deploy |
| 镜像仓库 | Azure Container Registry，使用 Build ID 标记镜像 |
| 计算平台 | Azure Kubernetes Service，独立 dev/prod 配置 |
| 发布 | Helm chart，滚动更新、等待就绪、Helm 更新失败自动回退 |
| HTTP 入口 | NGINX gateway + Azure LoadBalancer Service |
| 数据库 | 复用可从 AKS 访问的 MySQL / `twitter_db` |
| 凭据 | Azure DevOps secret variable group → Kubernetes Secret |
| 登录 | Azure Resource Manager service connection + AzureCLI；AKS 使用 Entra / kubelogin |

统一入口保留原 EKS 的路径分流：`/qrcode` → m1，`/blockchain` → m2，`/twitter` → m3。网关不改写查询参数。此示例入口为 HTTP；需要 HTTPS 时，应配置证书和 TLS 终止入口。

可选 `mix` 与三个服务并行部署，只提供集群内部 Service，不接管统一入口；可用端口转发单独访问。

## 文件说明

| 文件 / 目录 | 用途 |
| --- | --- |
| `azure-pipelines.yml` | 应用流水线入口 |
| `azure-infrastructure.yml` | 手动运行的 ACR/AKS 初始化流水线 |
| `templates/build-job.yml` | m1/m2/m3/mix 共用的构建任务 |
| `config/common.yml` | 服务连接、区域、ACR 名称、主体 object ID |
| `config/dev.yml`、`config/prod.yml` | 两套资源组、AKS、namespace 和节点数 |
| `docker/dotnet.Dockerfile` | .NET 8 多阶段构建 |
| `docker/java.Dockerfile` | Java 17 / Maven 构建，适配 Vert.x fat JAR 和 Jooby uber JAR |
| `scripts/bootstrap-azure.sh` | 创建资源组、ACR、AKS 和角色授权 |
| `scripts/build.sh`、`push.sh` | 构建与推送独立镜像 |
| `scripts/deploy.sh` | AKS 登录、创建 Secret、Helm 发布和路由检查 |
| `scripts/smoke-test.sh` | 三个接口的基本路由/响应检查 |
| `scripts/rollback.sh` | 回退指定 Helm revision |
| `helm/dwendwen/` | 应用、Service、gateway 和环境副本配置 |

## 流水线参数

| 参数 | 默认值 | 含义 |
| --- | --- | --- |
| `stack` | `dotnet` | 选择 `dotnet`、`vertx`、`jooby` |
| `environment` | `dev` | 选择 dev 或 prod 配置 |
| `includeMix` | `false` | Java 可设 true；不存在 mix_dotnet，组合不合法会被拒绝 |
| `deploy` | `true` | 是否执行 Deploy 阶段；false 时仍构建，主分支仍会推送镜像 |

`main` / `master` 的相关路径变更触发流水线。PR 只校验和构建，不推送、不部署。Azure Repos Git 的 PR 验证需要在分支策略中添加 Build Validation；YAML 的 `pr` 触发用于支持它的仓库来源，如 GitHub。

镜像路径为 `<registry>/dwendwen/m1-dotnet:<Build.BuildId>` 等。部署使用当前这次构建的标签，不使用 `latest`。dev 和 prod 是分别运行的部署目标，这份示例没有实现跨环境批准后复用同一个构建产物的晋级流水线。

## 使用前需要填写的内容

1. 把仓库根目录导入 Azure Repos 或连接已有仓库。在 Azure Pipelines 中选择本目录的 YAML 文件路径。
2. 修改 `config/common.yml` 的服务连接名和全局唯一 ACR 名称；修改 `config/prod.yml` 的生产 ACR 名称。资源组和集群名称按需要调整。
3. 创建 Azure Resource Manager service connection（示例名 `sc-dwendwen-azure`），推荐使用 workload identity federation。`pipelinePrincipalObjectId` 是对应企业应用/服务主体的 **object ID**，不是 application/client ID。
4. 若需新建基础设施，单独创建 `azure-infrastructure.yml` 对应的流水线并手动运行。首次初始化的服务连接需要创建资源组、ACR、AKS 和角色分配的权限，例如订阅作用域的 Contributor 加 Role Based Access Control Administrator。已有集群可以跳过初始化，配置对应的连接和权限即可。
5. 创建 `dwendwen-dev-secrets` 和/或 `dwendwen-prod-secrets` variable group，按下表设置变量，并授权对应应用流水线。
6. 创建 `dwendwen-dev`、`dwendwen-prod` Azure DevOps Environments。基础设施流水线使用 `dwendwen-dev-infrastructure` / `dwendwen-prod-infrastructure`。需要发布审批时在 Environment 的 Approvals and checks 中设置，YAML 不会自行创建这些审批。
7. 先保证 MySQL DNS、网络访问和 ETL 数据可用，再运行应用流水线。

| Variable group 变量 | 值 | Secret |
| --- | --- | --- |
| `mysqlHost` | 可从 AKS 解析和访问的 MySQL 主机名 | 按需 |
| `mysqlUser` | 查询用户 | 按需 |
| `mysqlPassword` | 数据库密码 | **是** |

运行时变量保持原 Java 名称，.NET 版也兼容同名配置。数据库仍为 `twitter_db`、端口 3306。没有把密码写入仓库、镜像或 Helm values。每次构建产生独立的 `dwendwen-mysql-<BuildId>` Secret，旧 revision 对应的 Secret 保留，回滚时可继续引用；清理旧 Secret 应结合 Helm 历史和数据库凭据轮换策略处理。

初始化脚本为流水线主体赋予 ACR 的 `AcrPush`、AKS 的 `Azure Kubernetes Service Cluster User Role` 和 `Azure Kubernetes Service RBAC Cluster Admin`；最后一个用于本示例创建 namespace 和发布资源。实际多租户环境可预创建 namespace，再收窄部署权限。AKS 节点通过 `--attach-acr` 获取拉取权限，假定使用普通 RBAC 模式的 ACR。角色分配传播需要时间。

初始化脚本可复用已存在资源，但不是完整的基础设施状态管理工具：不会自动调和节点数、网络或认证模式的所有漂移。已有 AKS 必须支持 managed Entra ID 和 Azure RBAC。这里默认公开 AKS API 配合托管 Ubuntu agent；若改成私有 AKS/ACR，则需要有对应网络访问能力的 agent。

## 发布、检查与回滚

应用容器均监听 8080，内部 Service 映射到 80。dev 每个服务一个副本；prod 默认三个副本、两个 gateway 副本，可在 values 中调整。

现有应用没有统一健康接口，因此应用使用 TCP startup/readiness/liveness 探针；gateway 使用 `/healthz`。这只证明进程可接收连接，不代表数据库查询、性能或业务逻辑正确。

发布完成后通过端口转发检查：

- m1：`CC Team` 的编码结果应匹配原 Java 测试向量。
- m2：压缩的空对象请求应返回 `INVALID`，检查格式和路由。
- m3：非法 type 应返回 `INVALID`，检查路由，不验证数据库数据。原 Vert.x worker 回复后仍继续调用 `Data.serve`；只有 .NET/Jooby 在这里直接返回，因此不能一概认为该检查没有数据库访问。

`helm upgrade --atomic --wait` 会在 Helm 发布失败时回退。**之后的 smoke check 失败会使流水线失败，但不会自动回滚已经成功的 Helm revision。** 如需回滚，在已登录目标 AKS 的环境执行：

```bash
export KUBE_CONTEXT='your-aks-context'
export K8S_NAMESPACE='dwendwen-dev'
export RELEASE_NAME='dwendwen'
helm history "$RELEASE_NAME" --kube-context "$KUBE_CONTEXT" -n "$K8S_NAMESPACE"
export HELM_REVISION=1
bash phase3-deployment/AzureDevOps/scripts/rollback.sh
```

回滚仅恢复 Helm 管理的应用配置，不修改数据库或 Azure 基础设施。

## 与现有方案的边界

- 保留原 ECS/EKS 文件。这里用 NGINX gateway 实现相同路径分流，无需安装 AWS ALB controller 或另一个 ingress controller。
- `mix_dotnet` 未包含在上一轮三个项目中，因此本方案也不凭空引用它。
- 不创建或迁移 MySQL、不运行 Zeppelin/Spark ETL，也不导入 Twitter 数据；可复用已有 RDS 或自行配置 Azure Database for MySQL，但必须解决 AKS 到数据库的网络、DNS 和所需 TLS 配置。
- Java 原工程与新 .NET 工程保留各自实现。Java 构建明确跳过旧测试；流水线中没有虚构单元测试通过或测试覆盖率报告。

## 官方参考

- [AzureCLI@2 task](https://learn.microsoft.com/en-us/azure/devops/pipelines/tasks/reference/azure-cli-v2?view=azure-pipelines)
- [AKS kubelogin authentication](https://learn.microsoft.com/en-us/azure/aks/kubelogin-authentication)
- [AKS Azure RBAC](https://learn.microsoft.com/en-us/azure/aks/entra-id-authorization)
- [AKS / ACR integration](https://learn.microsoft.com/en-us/azure/aks/cluster-container-registry-integration)

以上用于核对任务参数和认证方式；实际订阅资源、权限、网络与数据库配置仍需在接入环境中确认。
