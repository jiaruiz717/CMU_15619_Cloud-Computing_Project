#!/bin/bash

# Scripts to deploy m1 microservice

# Build the QRCode docker image and push it to AWS ECR
docker build -f mix_jooby/src/main/docker/Dockerfile -t mix:jooby .
docker tag mix:jooby 144331934093.dkr.ecr.us-east-1.amazonaws.com/mix:jooby
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 144331934093.dkr.ecr.us-east-1.amazonaws.com/m3
docker push 144331934093.dkr.ecr.us-east-1.amazonaws.com/mix:jooby
# docker run -e mysqlHost -e mysqlUser -e mysqlPassword -p 80:8080 144331934093.dkr.ecr.us-east-1.amazonaws.com/m3:jooby


# Documentation: https://kops.sigs.k8s.io/getting_started/aws/
# Create cluster state storage named "s3bucket-dwendwen"
aws s3api create-bucket --bucket s3bucket-dwendwen --region us-east-1
# Version the S3 bucket
aws s3api put-bucket-versioning --bucket s3bucket-dwendwen  --versioning-configuration Status=Enabled
# Use S3 default bucket encryption
aws s3api put-bucket-encryption --bucket s3bucket-dwendwen --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'


export KOPS_STATE_STORE=s3://s3bucket-dwendwen
# Create a cluster
cd k8s/cluster
kops create -f cluster.yaml,ig-master.yaml,ig-nodes.yaml
# Edit the cluster definition to add the EC2 key pair for SSH access into the nodes
kops edit cluster cluster.k8s.local
# Build the cluster
kops update cluster --name cluster.k8s.local --yes --admin
# Wait for the cluster to settle down.
kops validate cluster --wait 10m

# Install services and MySQL
cd k8s/helm
helm install mix mix
helm install mysql mysql

# Expose the MySQL pod to the outside
kubectl expose deployment mysql-m3 --type=LoadBalancer --name=mysql-service

# Go into mysql pod
kubectl exec -it mysql-0 -- /bin/bash
apt update
apt install vim
service --status-all

# MySQL my.cnf
vim /etc/mysql/my.cnf
[mysqld]
innodb_doublewrite=0
innodb_flush_log_at_trx_commit=2
secure-file-priv = ""
innodb_buffer_pool_size=8G
innodb_log_file_size=512M
innodb_read_io_threads=64
innodb_write_io_threads=64
innodb_io_capacity=5000
innodb_thread_concurrency=0

# Delete the cluster
kops delete cluster --name cluster.k8s.local --yes

# Initialize the new volume
sudo fio --filename=/dev/nvme2n1 --rw=read --bs=128k --iodepth=32 --ioengine=libaio --direct=1 --name=volume-initialize
