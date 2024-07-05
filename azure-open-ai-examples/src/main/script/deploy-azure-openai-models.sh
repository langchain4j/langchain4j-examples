#!/usr/bin/env bash

# Execute this script to deploy the needed Azure OpenAI models to execute the samples.
#
# For this, you need to have Azure CLI installed: https://learn.microsoft.com/cli/azure/install-azure-cli
#
# Azure CLI runs on:
# - Windows (using Windows Command Prompt (CMD), PowerShell, or Windows Subsystem for Linux (WSL)): https://learn.microsoft.com/cli/azure/install-azure-cli-windows 
# - macOS: https://learn.microsoft.com/cli/azure/install-azure-cli-macos
# - Linux: https://learn.microsoft.com/cli/azure/install-azure-cli-linux
# - Docker: https://learn.microsoft.com/cli/azure/run-azure-cli-docker
#
# Once installed, you can run the following commands to check your installation is correct:
# az --version
# az --help

echo "Setting up environment variables..."
echo "----------------------------------"
PROJECT="langchain4j-samples"
RESOURCE_GROUP="rg-$PROJECT"
LOCATION="swedencentral"
TAG="$PROJECT"
AI_SERVICE="ai-$PROJECT"
AZURE_OPENAI_DEPLOYMENT_NAME="gpt-35-turbo"
AZURE_OPENAI_DALLE_DEPLOYMENT_NAME="dall-e-3"
AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME="text-embedding-ada-002"

echo "Creating the resource group..."
echo "------------------------------"
az group create \
  --name "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags system="$TAG"

echo "Creating the Cognitive Service..."
echo "---------------------------------"
az cognitiveservices account create \
  --name "$AI_SERVICE" \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --custom-domain "$AI_SERVICE" \
  --tags system="$TAG" \
  --kind "OpenAI" \
  --sku "S0"
  
# If you want to know the available models, run the following Azure CLI command:
# az cognitiveservices account list-models --resource-group "$RESOURCE_GROUP" --name "$AI_SERVICE" -o table  

echo "Deploying a gpt-35-turbo model..."
echo "----------------------"
az cognitiveservices account deployment create \
  --name "$AI_SERVICE" \
  --resource-group "$RESOURCE_GROUP" \
  --deployment-name "$AZURE_OPENAI_DEPLOYMENT_NAME" \
  --model-name "gpt-35-turbo" \
  --model-version "1106"  \
  --model-format "OpenAI" \
  --sku-capacity 120 \
  --sku-name "Standard"

echo "Deploying a dall-e-3 model..."
echo "----------------------"
az cognitiveservices account deployment create \
  --name "$AI_SERVICE" \
  --resource-group "$RESOURCE_GROUP" \
  --deployment-name "$AZURE_OPENAI_DALLE_DEPLOYMENT_NAME" \
  --model-name "dall-e-3" \
  --model-version "3.0"  \
  --model-format "OpenAI" \
  --sku-capacity 1 \
  --sku-name "Standard"

echo "Deploying a text-embedding-ada-002 model..."
echo "----------------------"
az cognitiveservices account deployment create \
  --name "$AI_SERVICE" \
  --resource-group "$RESOURCE_GROUP" \
  --deployment-name "$AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME" \
  --model-name "text-embedding-ada-002" \
  --model-version "2"  \
  --model-format "OpenAI" \
  --sku-capacity 1 \
  --sku-name "Standard"

echo "Storing the key and endpoint in environment variables..."
echo "--------------------------------------------------------"
AZURE_OPENAI_KEY=$(
  az cognitiveservices account keys list \
    --name "$AI_SERVICE" \
    --resource-group "$RESOURCE_GROUP" \
    | jq -r .key1
  )
AZURE_OPENAI_ENDPOINT=$(
  az cognitiveservices account show \
    --name "$AI_SERVICE" \
    --resource-group "$RESOURCE_GROUP" \
    | jq -r .properties.endpoint
  )

echo "AZURE_OPENAI_KEY=$AZURE_OPENAI_KEY"
echo "AZURE_OPENAI_ENDPOINT=$AZURE_OPENAI_ENDPOINT"
echo "AZURE_OPENAI_DEPLOYMENT_NAME=$AZURE_OPENAI_DEPLOYMENT_NAME"
echo "AZURE_OPENAI_DALLE_DEPLOYMENT_NAME=$AZURE_OPENAI_DALLE_DEPLOYMENT_NAME"
echo "AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME=$AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME"
