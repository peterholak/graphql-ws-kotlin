package net.holak.graphql.ws

import graphql.GraphQL
import graphql.language.Field
import graphql.language.OperationDefinition
import graphql.parser.Parser
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

data class Subscription<out Client>(
        val client: Client,
        val start: Start,
        val subscriptionName: String
)

class NoSuchSubscriptionException(message: String) : Exception(message)

@Suppress("unused")
/** TODO: this is not currently thread safe */
class SimpleSubscriptionHandler<Client> : SubscriptionServerHandler<Client> {

    data class Identifier<out Client>(val client: Client, val subscriptionId: String)

    val subscriptions = ConcurrentHashMap<String, ConcurrentLinkedQueue<Identifier<Client>>>()
    val subscriptionsByClient = ConcurrentHashMap<Client, ConcurrentHashMap<String, Subscription<Client>>>()

    override fun subscribe(client: Client, start: Start) {
        val id = Identifier(client, start.id)

        val toNotify = subscriptionToNotify(start.payload.query, start.payload.operationName)
        subscriptions
                .getOrPut(toNotify, { ConcurrentLinkedQueue() })
                .add(id)
        subscriptionsByClient
                .getOrPut(client, { ConcurrentHashMap() })
                .put(start.id, Subscription(client, start, toNotify))
    }

    override fun unsubscribe(client: Client, subscriptionId: String) {
        val id = Identifier(client, subscriptionId)

        val subscription = subscriptionsByClient[client]?.remove(subscriptionId) ?: throw NoSuchSubscriptionException("No such subscription.")
        subscriptions[subscription.subscriptionName]?.remove(id)
    }

    override fun disconnected(client: Client) {
        val ids = subscriptionsByClient.remove(client) ?: return
        ids.forEach { (subscriptionId, subscription) ->
            subscriptions[subscription.subscriptionName]?.remove(Identifier(client, subscriptionId))
        }
    }

    /** A shorthand version that simply uses the context's "class name with the first letter converted to lowercase"as the subscription name. */
    inline fun <reified T> publish(context: T?, graphQl: GraphQL, noinline send: (Client, Data) -> Unit) {
        // TODO: this obviously only supports that one special convention of class names right now, which is not enough.
        val className = T::class.java.simpleName
        val camelCased = className[0].toLowerCase() + className.substring(1)
        publish(camelCased, context, graphQl, send)
    }

    inline fun <reified T> publish(noinline executeAndSend: (Client, Subscription<Client>) -> Unit) {
        // TODO: this obviously only supports that one special convention of class names right now, which is not enough.
        val className = T::class.java.simpleName
        val camelCased = className[0].toLowerCase() + className.substring(1)
        publish(camelCased, executeAndSend)
    }

    fun <T> publish(subscriptionName: String, context: T? = null, graphQl: GraphQL, send: (Client, Data) -> Unit) {
        publish(subscriptionName) { client, subscription ->
            // TODO: Some deduplication would be useful here?
            val result = graphQl.execute(
                    subscription.start.payload.query,
                    subscription.start.payload.operationName,
                    context,
                    subscription.start.payload.variables ?: emptyMap()
            )

            send(client, Data(subscription.start.id, result))
        }
    }

    fun publish(subscriptionName: String, executeAndSend: (Client, Subscription<Client>) -> Unit) {
        subscriptions[subscriptionName]?.forEach {
            val subscription = subscriptionsByClient[it.client]!![it.subscriptionId]!!
            executeAndSend(it.client, subscription)
        }
    }

}

// TODO: a better way to obtain this information?
fun subscriptionToNotify(request: String, operationName: String? = null): String {
    val document = Parser().parseDocument(request)
    val definitions = document.definitions
    val definition = when {
        definitions.isEmpty() ->
            throw IllegalArgumentException("Empty request.")

        definitions.size > 1 && operationName == null ->
            throw IllegalArgumentException("Must specify operation name.")

        definitions.size == 0 && operationName == null ->
            definitions[0]

        else ->
            definitions.find { (it as? OperationDefinition)?.name == operationName } ?:
                    throw IllegalArgumentException("Unable to find operation '$operationName'.")
    } as? OperationDefinition ?: throw IllegalArgumentException("Not an operation.")

    if (definition.operation != OperationDefinition.Operation.SUBSCRIPTION)
        throw IllegalArgumentException("The operation is not a subscription.")

    return definition.selectionSet.selections
            .filterIsInstance<Field>()
            .apply { if (size > 1) throw IllegalArgumentException("Subscriptions can only have a single root field") }
            .first()
            .name
}
