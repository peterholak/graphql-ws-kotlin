package net.holak.graphql.ws

import graphql.ExecutionInput
import graphql.GraphQL
import mu.KLogging

typealias Transport<Client> = (Client, Data) -> Unit

class DefaultPublisher<Client>(val graphQL: GraphQL, val subscriptions: Subscriptions<Client>, val transport: Transport<Client>) : Publisher {
    companion object: KLogging()

    override fun publish(subscriptionName: String, data: Any?, filter: Filter?) {
        subscriptions.subscriptions[subscriptionName]?.forEach {
            val subscription = subscriptions.subscriptionsByClient[it.client]!![it.subscriptionId]!!

            if ((filter?.invoke(subscription.arguments) ?: true) == false) {
                return@forEach
            }

            // TODO: Some deduplication would be useful here?
            // TODO: also conversion of all those variables every time can be pretty slow
            val result = graphQL.execute(ExecutionInput(
                    subscription.start.payload.query,
                    subscription.start.payload.operationName,
                    data,
                    null,
                    subscription.start.payload.variables ?: emptyMap()
            ))

            try {
                transport(it.client, Data(subscription.start.id, result))
            }catch(e: Exception) {
                logger.error { "Failed to transport subscription ${subscription.start.id}: $e" }
            }
        }
    }
}
