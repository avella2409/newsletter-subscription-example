#!/bin/bash
set -e

PROJECT_NAME=$1
PROJECT_JAVA_HOME_PATH=$2
AWS_ECR_REGION=$3
AWS_ACCOUNT_ID=$4
AWS_ECR_REPOSITORY_PATH=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_ECR_REGION.amazonaws.com

echo "Push $PROJECT_NAME"

export JAVA_HOME=$PROJECT_JAVA_HOME_PATH
mvn clean package
aws ecr get-login-password --region "$AWS_ECR_REGION" | docker login --username AWS --password-stdin "$AWS_ECR_REPOSITORY_PATH"
docker build -t "$PROJECT_NAME" .
docker tag "$PROJECT_NAME" "$AWS_ECR_REPOSITORY_PATH"/"$PROJECT_NAME"
docker push "$AWS_ECR_REPOSITORY_PATH"/"$PROJECT_NAME"
