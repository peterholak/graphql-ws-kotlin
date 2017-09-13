package net.holak.graphql.ws

import graphql.GraphQLError

data class Subscription<out Client>(
        val client: Client,
        val start: Start,
        val subscriptionName: String,
        val arguments: JsonMap = emptyMap()
)

interface Subscriptions<Client> {
    data class Identifier<out Client>(val client: Client, val subscriptionId: String)

    val subscriptions: Map<String, Collection<Identifier<Client>>>
    val subscriptionsByClient: Map<Client, Map<String, Subscription<Client>>>

    fun subscribe(client: Client, start: Start): List<GraphQLError>?
    fun unsubscribe(client: Client, subscriptionId: String)
    fun disconnected(client: Client)
}

class NoSuchSubscriptionException(message: String) : Exception(message)
