package net.holak.graphql.ws

import graphql.GraphQL

typealias Transport<Client> = (Client, Data) -> Unit

class DefaultPublisher<Client>(val graphQL: GraphQL, val sub: Subscriptions<Client>, val transport: Transport<Client>) : TypedPublisher() {
    override fun publish(subscriptionName: String, data: Any?) {
        sub.subscriptions[subscriptionName]?.forEach {
            val subscription = sub.subscriptionsByClient[it.client]!![it.subscriptionId]!!

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
