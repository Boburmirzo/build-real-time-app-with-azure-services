package com.function;

import java.io.IOException;
import java.util.Optional;

import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.CosmosDBInput;
import com.microsoft.azure.functions.annotation.CosmosDBTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.signalr.SignalRConnectionInfo;
import com.microsoft.azure.functions.signalr.SignalRMessage;
import com.microsoft.azure.functions.signalr.annotation.SignalRConnectionInfoInput;
import com.microsoft.azure.functions.signalr.annotation.SignalROutput;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

public class Function {

        @FunctionName("getStocks")
        public HttpResponseMessage run(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.GET }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
                        @CosmosDBInput(name = "stocks", databaseName = "stocksdb", collectionName = "stocks", connectionStringSetting = "AzureCosmosDBConnectionString") StockPrice[] stockPrices,
                        final ExecutionContext context) throws IOException {

                if (stockPrices == null) {
                        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                                        .body("Document not found.")
                                        .build();
                } else {
                        return request.createResponseBuilder(HttpStatus.OK)
                                        .header("Content-Type", "application/json")
                                        .body(stockPrices)
                                        .build();
                }
        }

        @FunctionName("negotiate")
        public SignalRConnectionInfo negotiate(
                        @HttpTrigger(name = "req", methods = {
                                        HttpMethod.POST }, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> req,
                        @SignalRConnectionInfoInput(name = "connectionInfo", hubName = "stocks", connectionStringSetting = "AzureSignalRConnectionString") SignalRConnectionInfo connectionInfo) {

                return connectionInfo;
        }

        @FunctionName("stocksChanged")
        @SignalROutput(name = "$return", hubName = "stocks", connectionStringSetting = "AzureSignalRConnectionString")
        public SignalRMessage cosmosDbProcessor(
                        @CosmosDBTrigger(name = "items", databaseName = "stocksdb", collectionName = "stocks", leaseCollectionName = "leases", createLeaseCollectionIfNotExists = true, feedPollDelay = 500, connectionStringSetting = "AzureCosmosDBConnectionString") StockPrice[] stockPrices,
                        final ExecutionContext context) {

                SignalRMessage message = new SignalRMessage();
                message.target = "updated";
                for (StockPrice stockPrice : stockPrices) {
                        message.arguments.add(stockPrice);
                }
                return message;
        }
}