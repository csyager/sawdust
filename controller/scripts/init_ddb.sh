#!/bin/bash

# delete tables
aws dynamodb delete-table \
  --table-name workflows-table \
  --endpoint http://localhost:8000;

aws dynamodb delete-table \
  --table-name compute-registrations-table \
  --endpoint http://localhost:8000;

aws dynamodb delete-table \
  --table-name activities-table \
  --endpoint http://localhost:8000;

aws dynamodb create-table \
  --table-name workflows-table \
  --attribute-definitions AttributeName=workflowId,AttributeType=S \
  --key-schema AttributeName=workflowId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint http://localhost:8000;

aws dynamodb create-table \
  --table-name  compute-registrations-table \
  --attribute-definitions AttributeName=computeId,AttributeType=S \
  --key-schema AttributeName=computeId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint http://localhost:8000;

aws dynamodb create-table \
  --table-name activities-table \
  --attribute-definitions AttributeName=activityId,AttributeType=S AttributeName=incompleteState,AttributeType=S \
  --key-schema AttributeName=computeId,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint http://localhost:8000 \
  --global-secondary-indexes \
          "[
              {
                  \"IndexName\": \"IncompleteActivityIndex\",
                  \"KeySchema\": [
                      {\"AttributeName\":\"incompleteState\",\"KeyType\":\"HASH\"},
                  ],
                  \"Projection\": {
                      \"ProjectionType\":\"ALL\"
                  }
              }
          ]";