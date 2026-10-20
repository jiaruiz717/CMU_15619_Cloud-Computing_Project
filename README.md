# dwendwen-S22

Team **dwendwen**'s cloud computing project for **Carnegie Mellon University 15-619, Spring 2022**.

The project builds a cloud-native web service with three APIs: QR-code processing, blockchain validation and mining, and Twitter contact recommendations. It focuses on processing large datasets, deploying containerized services, and improving throughput within a cloud budget. See the [Spring 2022 course syllabus](https://www.cs.cmu.edu/~msakr/15619-s22/15319_15619_s22_syllabus.pdf) for the course and team-project objectives.

## Services

| Service | Endpoint | Function |
| --- | --- | --- |
| M1: QR Code | `GET /qrcode` | Encode and decode messages using the project's QR-code format and Logistic Map transformation. |
| M2: Blockchain | `GET /blockchain` | Validate a simplified blockchain and its transactions, then mine a new block using proof of work. |
| M3: Twitter | `GET /twitter` | Rank a user's contacts using replies, retweets, shared hashtags, and keyword matches in preprocessed Twitter data. |

The repository includes separate Java and C# service implementations. The Java `mix` applications combine all three endpoints for deployment experiments.

## Tech Stack

| Area | Technologies |
| --- | --- |
| Web services | Java, Vert.x, Jooby/Netty; C#, .NET 8, ASP.NET Core Minimal APIs |
| Database | MySQL, Amazon RDS, JDBC/HikariCP, MySqlConnector |
| Data processing | Scala, Apache Spark, Spark SQL, Apache Zeppelin, Google Cloud Dataproc and Cloud Storage |
| Containers and orchestration | Docker, Kubernetes, Helm, kOps, eksctl |
| AWS deployment | EC2, EKS, ECR, S3, Elastic Load Balancing; ECS/EKS Fargate experiments |
| Azure deployment and CI/CD | Azure DevOps / Azure Pipelines, Azure Container Registry (ACR), Azure Kubernetes Service (AKS), NGINX |
| Infrastructure and development | Terraform, Bash, Maven, Git for version control, GitHub for team collaboration |
| Testing and monitoring | JUnit, Apache JMeter, Amazon CloudWatch, Kubernetes Dashboard |

## Design Highlights

- **ETL:** Spark notebooks clean and deduplicate Twitter data, extract user interactions, and prepare data for MySQL.
- **Query optimization:** Precomputed contact data reduces database round trips; the services calculate request-specific scores and rank results.
- **Deployment:** Docker images and Helm charts support independent service replicas. AWS configurations cover self-managed Kubernetes and managed-service experiments.
- **CI/CD:** The Azure pipeline validates scripts and Helm charts, builds the selected Java or .NET services, publishes images to ACR, and deploys to AKS.
- **Performance:** The reports compare frameworks, load balancers, instance types, database schemas, and deployment options against throughput and cost.

## Repository Layout

| Path | Contents |
| --- | --- |
| `web/` | M1, M2, M3, and combined Java applications; individual C#/.NET services |
| `etl/` | Zeppelin notebooks for Spark/Scala data processing |
| `k8s/` | Kubernetes cluster definitions and Helm charts |
| `script/` | AWS image-build, deployment, and database setup scripts |
| `phase3-deployment/` | EKS, ECS/Fargate, RDS, and Azure DevOps deployment configurations |
| `*-report.pdf` | Design decisions, experiments, results, and team reflections |

For service details, see the [M1 .NET](web/m1_dotnet/README.md), [M2 .NET](web/m2_dotnet/README.md), and [M3 .NET](web/m3_dotnet/README.md) READMEs. The [Azure deployment guide](phase3-deployment/AzureDevOps/README.md) explains the pipeline setup. M3 requires a MySQL database populated with the ETL output; cloud configurations require environment-specific settings before use.

## Reports

- [Phase 1 checkpoint](phase1-checkpoint-report.pdf)
- [Phase 1 final report](phase1-final-report.pdf)
- [Phase 2 final report](phase2-final-report.pdf)
- [Phase 3 final report](phase3-final-report.pdf)

## Team

Wangshu Li (`wangshul`), Qing Deng (`qingd`), and Jiarui Zhang (`jiaruiz`).
