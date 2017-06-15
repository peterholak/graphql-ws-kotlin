package net.holak.graphql.ws

import graphql.GraphQLError

data class Subscription<out Client>(
        val client: Client,
        val start: Start,
        val subscriptionName: String,
        val input: Any?
)

interface Subscriptions<Client> {
    data class Identifier<out Client>(val client: Client, val subscriptionId: String)

    val subscriptions: Map<String, Collection<Identifier<Client>>>
    val subscriptionsByClient: Map<Client, Map<String, Subscription<Client>>>

    fun subscribe(client: Client, start: Start): List<GraphQLError>?
    fun unsubscribe(client: Client, subscriptionId: String)
    fun disconnected(client: Client)
}

/** A hack to deal with the cyclical dependency. */
class LateSubscriptions<Client> : Subscriptions<Client> {
    override val subscriptions: Map<String, Collection<Subscriptions.Identifier<Client>>>
        get() = handler.subscriptions

    override val subscriptionsByClient
        get() = handler.subscriptionsByClient

    override fun subscribe(client: Client, start: Start) = handler.subscribe(client, start)
    override fun unsubscribe(client: Client, subscriptionId: String) = handler.unsubscribe(client, subscriptionId)
    override fun disconnected(client: Client) = handler.disconnected(client)

    lateinit var handler: Subscriptions<Client>
}

class NoSuchSubscriptionException(message: String) : Exception(message)
