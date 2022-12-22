---
page_type: sample
languages:
- java
- java-script
products:
- azure
- azure-functions
- azure-event-hubs
- azure-cosmos-db
description: "Shows how to use build a real-time event-driven Java solution in Azure."
urlFragment: "sample"
---

# Build real-time Apps with Azure Functions and SignalR Service

This repo shows with the simple stock price example how to build real-time serverless Java solutions runs only when data changes in an Azure Cosmos DB, broadcast changes to connected web application as a client service using SignalR Service.

This sample taken from [Tutorial: Enable automatic updates in a web application using Azure Functions and SignalR Service](https://learn.microsoft.com/en-us/training/modules/automatic-update-of-a-webapp-using-azure-functions-and-signalr/) and modified to 
use Java programming language to create serverless solutions with Azure Function.

This repo demonstrates followings:

- How to broadcast messages with SignalR Service and Azure Function in serverless.
- How to build serverless functions using Java that interact with no-sql Azure Cosmos DB.
- How to integrate SignalR service into the web app using SignalR SDK, JavaScript and Vue.js.

## Branch and project structure

The repo has two branches _before_ and _after_ that shows two scenarios polling-based and push-based respectively to implement automatic updates of the stock price information

## Prerequisites

* [Azure CLI](https://docs.microsoft.com/cli/azure/install-azure-cli)
* [Java Developer Kit](https://aka.ms/azure-jdks), at least version 8
* [Maven](https://maven.apache.org)
* [Azure Functions Core Tools](https://www.npmjs.com/package/azure-functions-core-tools)
* [Node.js](https://nodejs.org/download/)
* [Visual Studio Code](https://code.visualstudio.com/download)
* [Azure Functions Core Tools (min. version 2.6.666)]()
* [Azure Functions extension for Visual Studio Code](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azurefunctions)
* [Azure Storage extension for Visual Studio Code](https://marketplace.visualstudio.com/items?itemName=ms-azuretools.vscode-azurestorage)

## Setup

You can use the following commands with resource names prepopulated for the demo:

```bash
## Create a new Resource Group
az group create --name real-time-stock-app --location westeurope

## Create a Storage account
az storage account create \
  --name stockstorage \
  --resource-group real-time-stock-app \
  --kind StorageV2 \
  --sku Standard_LRS

## Create an Azure Cosmos DB account
az cosmosdb create  \
  --name msl-sigr-cosmos-stock \
  --resource-group real-time-stock-app

## Get the connection settings for your cloud services and add to the local settings file

STORAGE_CONNECTION_STRING=$(az storage account show-connection-string \
--name $(az storage account list \
  --resource-group real-time-stock-app \
  --query [0].name -o tsv) \
--resource-group real-time-stock-app \
--query "connectionString" -o tsv)

COSMOSDB_ACCOUNT_NAME=$(az cosmosdb list \
    --resource-group real-time-stock-app \
    --query [0].name -o tsv)

COSMOSDB_CONNECTION_STRING=$(az cosmosdb list-connection-strings  \
  --name $COSMOSDB_ACCOUNT_NAME \
  --resource-group real-time-stock-app \
  --query "connectionStrings[?description=='Primary SQL Connection String'].connectionString" -o tsv)

COSMOSDB_MASTER_KEY=$(az cosmosdb list-keys \
--name $COSMOSDB_ACCOUNT_NAME \
--resource-group real-time-stock-app \
--query primaryMasterKey -o tsv)

printf "\n\nReplace <STORAGE_CONNECTION_STRING> with:\n$STORAGE_CONNECTION_STRING\n\nReplace <COSMOSDB_CONNECTION_STRING> with:\n$COSMOSDB_CONNECTION_STRING\n\nReplace <COSMOSDB_MASTER_KEY> with:\n$COSMOSDB_MASTER_KEY\n\n"

## Create a SignalR account
az signalr create \
  --name stock-price-update-signalr \
  --resource-group real-time-stock-app \
  --sku Free_DS2 \
  --unit-count 1

## Update the SignalR account service mode to Serverless
az resource update \
  --resource-type Microsoft.SignalRService/SignalR \
  --name stock-price-update-signalr \
  --resource-group real-time-stock-app \
  --set properties.features[flag=ServiceMode].value=Serverless

## Get the connection strings for the SignalR account

SIGNALR_CONNECTION_STRING=$(az signalr key list \
  --name $(az signalr list \
    --resource-group real-time-stock-app \
    --query [0].name -o tsv) \
  --resource-group real-time-stock-app \
  --query primaryConnectionString -o tsv)

printf "\n\nReplace <SIGNALR_CONNECTION_STRING> with:\n$SIGNALR_CONNECTION_STRING\n\n"

```

## Running the sample

To run the sample locally, go to _before_ or _after_ branch:

Navigate to `/serverless` folder:

``` bash
mvn clean install
mvn azure-functions:run
```

The function app startup is shown in a terminal window.

Then, navigate to `/frontend` folder:

```bash
npm install
npm start
```

The script automatically opens the browser and goes to `http://localhost:8080`.

Clean up Azure resources when you are finished:

``` bash
az group delete --name real-time-stock-app
```

## Key concepts

For details, see [Tutorial: Enable automatic updates in a web application using Azure Functions and SignalR Service](https://learn.microsoft.com/en-us/training/modules/automatic-update-of-a-webapp-using-azure-functions-and-signalr/).
