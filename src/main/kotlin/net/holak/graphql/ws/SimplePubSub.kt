package net.holak.graphql.ws

import graphql.GraphQL
import graphql.language.Field
import graphql.language.OperationDefinition
import graphql.parser.Parser
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class NoSuchSubscriptionException(message: String) : Exception(message)

typealias Transport<Client> = (Client, Data) -> Unit
typealias Identifier<T> = SubscriptionHandler.Identifier<T>

/**
 * TODO: this is not currently thread safe
 *
 * Places the data directly into context when running the subscription "queries",
 * which should probably be configurable.
 */
@Suppress("unused")
class SimpleSubscriptions<Client> : SubscriptionHandler<Client> {

    override val subscriptions = ConcurrentHashMap<String, ConcurrentLinkedQueue<Identifier<Client>>>()
    override val subscriptionsByClient = ConcurrentHashMap<Client, ConcurrentHashMap<String, Subscription<Client>>>()

    override fun subscribe(client: Client, start: Start) {
        // TODO: validation here, but first graphql-java must support it

        val id = Identifier(client, start.id)

        val toNotify = subscriptionToNotify(start.payload.query, start.payload.operationName)
        subscriptions
                .getOrPut(toNotify, { ConcurrentLinkedQueue() })
                .add(id)
        subscriptionsByClient
                .getOrPut(client, { ConcurrentHashMap() })
                .put(start.id, Subscription(client, start, toNotify, null))
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
}

class SimplePublisher<Client>(val graphQL: GraphQL, val sub: SubscriptionHandler<Client>, val transport: Transport<Client>) : TypedPublisher() {
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
