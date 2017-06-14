package net.holak.graphql.ws

import graphql.GraphQL

data class Subscription<out Client>(
        val client: Client,
        val start: Start,
        val subscriptionName: String,
        val input: Any?
)

interface SubscriptionHandler<Client> {
    data class Identifier<out Client>(val client: Client, val subscriptionId: String)

    val subscriptions: Map<String, Collection<Identifier<Client>>>
    val subscriptionsByClient: Map<Client, Map<String, Subscription<Client>>>

    fun subscribe(client: Client, start: Start)
    fun unsubscribe(client: Client, subscriptionId: String)
    fun disconnected(client: Client)
}

interface Publisher {
    fun publish(subscriptionName: String, data: Any? = null)
}

abstract class TypedPublisher : Publisher {
    /** A shorthand version that simply uses the context's "class name with the first letter converted to lowercase"as the subscription name. */
    inline fun <reified T> publish(context: T?) {
        // TODO: this obviously only supports that one special convention of class names right now, which is not enough.
        val className = T::class.java.simpleName
        val camelCased = className[0].toLowerCase() + className.substring(1)
        publish(camelCased, context)
    }
}

/** A hack to deal with the cyclical dependency. */
class LatePublisher : Publisher {
    override fun publish(subscriptionName: String, data: Any?) = publisher.publish(subscriptionName, data)
    lateinit var publisher: Publisher
}
