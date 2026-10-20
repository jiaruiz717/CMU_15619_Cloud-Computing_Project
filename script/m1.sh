#!/bin/bash

# Scripts to deploy m1 microservice
# Execute the following commands outside the m1_jooby folder

# Build the QRCode docker image and push it to AWS ECR
docker build -f m1_jooby/src/main/docker/Dockerfile -t m1:jooby .
docker tag m1:jooby 144331934093.dkr.ecr.us-east-1.amazonaws.com/m1:jooby
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 144331934093.dkr.ecr.us-east-1.amazonaws.com/m1
docker push 144331934093.dkr.ecr.us-east-1.amazonaws.com/m1:jooby


# Documentation: https://kops.sigs.k8s.io/getting_started/aws/
# Create cluster state storage named "s3bucket-dwendwen"
aws s3api create-bucket --bucket s3bucket-dwendwen --region us-east-1
# Version the S3 bucket
aws s3api put-bucket-versioning --bucket s3bucket-dwendwen  --versioning-configuration Status=Enabled
# Use S3 default bucket encryption
aws s3api put-bucket-encryption --bucket s3bucket-dwendwen --server-side-encryption-configuration '{"Rules":[{"ApplyServerSideEncryptionByDefault":{"SSEAlgorithm":"AES256"}}]}'


export KOPS_STATE_STORE=s3://s3bucket-dwendwen
# Create a cluster
cd cluster-definition
kops create -f cluster.yaml,ig-master.yaml,ig-nodes.yaml
# Edit the cluster definition to add the EC2 key pair for SSH access into the nodes
kops edit cluster cluster.k8s.local
# Build the cluster
kops update cluster --name cluster.k8s.local --yes --admin
# Wait for the cluster to settle down.
kops validate cluster --wait 10m


# Install services
helm install m1 m1_jooby/src/main/helm/m1/

# Delete the cluster
kops delete cluster --name cluster.k8s.local --yes