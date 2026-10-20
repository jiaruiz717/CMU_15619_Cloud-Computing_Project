resource "random_password" "password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

module "db" {
  source  = "terraform-aws-modules/rds/aws"

  identifier = "twitter-mysql-rds"

  engine            = "mysql"
  engine_version    = "8.0.28"
  instance_class    = "db.r6g.large"
  allocated_storage = 50

  db_name  = "twitter-mysql-rds"
  username = "root"
  port     = "3306"
  password = random_password.password.result
  iam_database_authentication_enabled = false

  vpc_security_group_ids = ["sg-12345678"] # change after EKS created

  maintenance_window = "Mon:00:00-Mon:03:00"
  backup_window      = "03:00-06:00"

  # Enhanced Monitoring - see example for details on how to create the role
  # by yourself, in case you don't want to create it automatically
  monitoring_interval = "30"
  monitoring_role_name = "MyRDSMonitoringRole"
  create_monitoring_role = true

  tags = {
    project  = "twitter-phase-3"

  }

  # DB subnet group
  create_db_subnet_group = true
  subnet_ids             = ["subnet-12345678", "subnet-87654321"] # change after EKS created

  # DB parameter group
  family = "mysql8.0"

  # DB option group
  major_engine_version = "8.0"

  # Database Deletion Protection
  deletion_protection = true

  parameters = [
    {
      name = "character_set_client"
      value = "utf8mb4"
    },
    {
      name = "character_set_server"
      value = "utf8mb4"
    }
  ]

}