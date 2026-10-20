output "db_password" {
  description = "password for primary user of db"
  value       = module.db.password
  sensitive   = true
}