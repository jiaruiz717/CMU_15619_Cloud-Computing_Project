#!/bin/bash

# Scripts to deploy EKS with managed node groups

# Create an EKS cluster with managed nodes
eksctl create cluster -f cluster.yaml

# Create an IAM Policy that allows the AWS Load Balancer Controller to manage ELB (only need to run once)
curl -o iam-policy.json https://raw.githubusercontent.com/kubernetes-sigs/aws-load-balancer-controller/main/docs/install/iam_policy.json
aws iam create-policy --policy-name AWSLoadBalancerControllerIAMPolicy --policy-document file://iam-policy.json

# Install the AWS Load Balancer Controller. You can also reference the examples at the end of the document.
CLUSTER_NAME=twitter6
TEAM_AWS_ID=144331934093

eksctl utils associate-iam-oidc-provider --region us-east-1 --cluster $CLUSTER_NAME --approve
eksctl create iamserviceaccount --cluster=$CLUSTER_NAME --namespace=kube-system --name=aws-load-balancer-controller --attach-policy-arn=arn:aws:iam::$TEAM_AWS_ID:policy/AWSLoadBalancerControllerIAMPolicy --override-existing-serviceaccounts --approve

kubectl apply -k "github.com/aws/eks-charts/stable/aws-load-balancer-controller//crds?ref=master"

helm repo add eks https://aws.github.io/eks-charts

helm upgrade -i aws-load-balancer-controller eks/aws-load-balancer-controller --set clusterName=$CLUSTER_NAME --set serviceAccount.create=false --set region=us-east-1 --set serviceAccount.name=aws-load-balancer-controller -n kube-system

# Verify the intallation is successful by checking there is a pod named aws-load-balancer-controller running
kubectl get pods -A

# Install services and ingress
helm install m1 helm/m1/
helm install m2 helm/m2/
helm install m3 helm/m3/
kubectl create -f helm/ingress/ingress.yaml

helm install mix helm/mix

# Delete the cluster
eksctl delete cluster --region=us-east-1 --name=$CLUSTER_NAME --wait