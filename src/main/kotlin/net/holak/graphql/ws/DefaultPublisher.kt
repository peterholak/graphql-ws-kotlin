package net.holak.graphql.ws

import graphql.GraphQL

typealias Transport<Client> = (Client, Data) -> Unit

class DefaultPublisher<Client>(val graphQL: GraphQL, val subscriptions: Subscriptions<Client>, val transport: Transport<Client>) : Publisher {
    override fun publish(subscriptionName: String, data: Any?) {
        subscriptions.subscriptions[subscriptionName]?.forEach {
            val subscription = subscriptions.subscriptionsByClient[it.client]!![it.subscriptionId]!!

            // TODO: Some deduplication would be useful here?
            val result = graphQL.execute(
                    subscription.start.payload.query,
                    subscription.start.payload.operationName,
                    data,
                    subscription.start.payload.variables ?: emptyMap()
            )

            try {
                transport(it.client, Data(subscription.start.id, result))
            }catch(e: Exception) {
                // TODO: log transport errors, but it's no reason to not keep going
            }
        }
    }
}
