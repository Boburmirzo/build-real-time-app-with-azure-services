---
page_type: sample
languages:
- java
products:
- azure
- azure-functions
- azure-event-hubs
- azure-cosmos-db
description: "Shows how to use build a real-time event-driven Java solution in Azure."
urlFragment: "sample"
---

# Build a real-time event-driven Java solution in Azure

This repo shows wit the simple example how to build serverless Java solutions using the power of serverless event-driven computing, Azure Functions, and send event-based telemetric data in real time to Azure Cosmos DB.

This sample accompanies [Tutorial: Create an Azure function in Java with an Event Hub trigger and Cosmos DB output binding](https://docs.microsoft.com/azure/azure-functions/functions-event-hub-cosmos-db).

The commands below use a Bash environment. Equivalent commands for the Windows Cmd environment are provided in the tutorial.

## Prerequisites

* [Azure CLI](https://docs.microsoft.com/cli/azure/install-azure-cli)
* [Java Developer Kit](https://aka.ms/azure-jdks), version 8
* [Maven](https://maven.apache.org)
* [Azure Functions Core Tools](https://www.npmjs.com/package/azure-functions-core-tools)

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



## Create a new eventhubs namespace
az eventhubs namespace create --resource-group java-event-based --name device-event-telemetry-ns

## Create a new evenhub inside device-event-telemetry-ns namespace
az eventhubs eventhub create --resource-group java-event-based --name device-event-hub --namespace-name device-event-telemetry-ns --message-retention 1
 
az eventhubs eventhub authorization-rule create --resource-group java-event-based --name device-event-receive-ar --eventhub-name device-event-hub --namespace-name device-event-telemetry-ns --rights Listen Send

az cosmosdb create --resource-group java-event-based --name events-data-account

az cosmosdb sql database create --resource-group java-event-based --account-name events-data-account --name TelemetryDb
 
az cosmosdb sql container create --resource-group java-event-based --account-name events-data-account --database-name TelemetryDb --name TelemetryInfo --partition-key-path '/temperatureStatus'

## Storage Account

az storage account create --resource-group java-event-based --name eventsstoragejava --sku Standard_LRS

## Create a new Function APP
 
az functionapp create --resource-group java-event-based --name event-trigger-java-func-app --storage-account eventsstoragejava --consumption-plan-location westeurope --runtime java --functions-version 3

## Get the storage account connection string and set it to env variable
SET AZURE_WEB_JOBS_STORAGE=$(az storage account show-connection-string --name eventsstoragejava --query connectionString --output tsv)

echo $AZURE_WEB_JOBS_STORAGE
  
echo $AZURE_WEB_JOBS_STORAGE EVENT_HUB_CONNECTION_STRING=$(az eventhubs eventhub authorization-rule keys list --resource-group java-event-based --name device-event-receive-ar --eventhub-name device-event-hub --namespace-name device-event-telemetry-ns --query primaryConnectionString --output tsv)
  
echo $EVENT_HUB_CONNECTION_STRING 

COSMOS_DB_CONNECTION_STRING=$(az cosmosdb keys list --resource-group java-event-based --name events-data-account --type connection-strings --query connectionStrings[0].connectionString --output tsv)

echo $COSMOS_DB_CONNECTION_STRING

## Update the function app's settings with all connection strings
az functionapp config appsettings set --resource-group java-event-based --name event-trigger-java-func-app --settings AzureWebJobsStorage=$AZURE_WEB_JOBS_STORAGE EventHubConnectionString=$EVENT_HUB_CONNECTION_STRING CosmosDBConnectionString=$COSMOS_DB_CONNECTION_STRING

mvn archetype:generate --batch-mode -DarchetypeGroupId=com.microsoft.azure -DarchetypeArtifactId=azure-functions-archetype -DappName=event-trigger-java-func-app -DresourceGroup=java-event-based -DgroupId=com.example -DartifactId=telemetry-functions

func azure functionapp fetch-app-settings event-trigger-java-func-app

mvn archetype:generate --batch-mode -DarchetypeGroupId=com.microsoft.azure -DarchetypeArtifactId=azure-functions-archetype -DappName=event-trigger-java-func-app -DresourceGroup=java-event-based -DgroupId=com.example -DartifactId=telemetry-functions

```

## Running the sample

Run the sample locally:

``` bash
mvn clean package
mvn azure-functions:run
```

Deploy to Azure:

```bash
mvn azure-functions:deploy
```

Clean up Azure resources when you are finished:

``` bash
az group delete --name $RESOURCE_GROUP
```

## Key concepts

For details, see [Tutorial: Create an Azure function in Java with an Event Hub trigger and Cosmos DB output binding](https://docs.microsoft.com/azure/azure-functions/functions-eventhub-cosmosdb).
