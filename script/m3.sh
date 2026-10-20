#!/bin/bash

# Scripts to deploy m1 microservice

# Build the QRCode docker image and push it to AWS ECR
docker build -f m3_jooby/src/main/docker/Dockerfile -t m3:jooby .
docker tag m3:jooby 144331934093.dkr.ecr.us-east-1.amazonaws.com/m3:jooby
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 144331934093.dkr.ecr.us-east-1.amazonaws.com/m3
docker push 144331934093.dkr.ecr.us-east-1.amazonaws.com/m3:jooby
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

# Install services
helm install m3 k8s/helm/m3/

# Install MySQL
helm repo add stable https://charts.helm.sh/stable
helm repo update
export mysqlPassword=CHANGE_ME
helm install mysql-m3 --set mysqlRootPassword=${mysqlPassword} --set persistence.storageClass=gp2 --set persistence.size=250Gi stable/mysql

# Expose the MySQL pod to the outside
kubectl expose deployment mysql-m3 --type=LoadBalancer --name=mysql-service

# Load data into MySQL
cd m3_jooby
export mysqlHost=127.0.0.1
kubectl port-forward svc/mysql-m3 3306 &
mysql -h ${mysqlHost} -u root -p${mysqlPassword} twitter_db

# Delete the cluster
kops delete cluster --name cluster.k8s.local --yes