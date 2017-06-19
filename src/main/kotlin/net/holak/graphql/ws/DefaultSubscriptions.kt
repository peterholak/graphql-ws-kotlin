package net.holak.graphql.ws

import graphql.ErrorType
import graphql.GraphQLError
import graphql.language.Document
import graphql.language.Field
import graphql.language.OperationDefinition
import graphql.parser.Parser
import graphql.schema.GraphQLSchema
import graphql.validation.Validator
import mu.KLogging
import org.antlr.v4.runtime.misc.ParseCancellationException
import java.lang.IllegalArgumentException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

typealias Identifier<T> = Subscriptions.Identifier<T>

/**
 * TODO: this is not currently thread safe
 *
 * Places the data directly into context when running the subscription "queries",
 * which should probably be configurable.
 */
@Suppress("unused")
class DefaultSubscriptions<Client>(val schema: GraphQLSchema) : Subscriptions<Client> {
    companion object: KLogging()

    override val subscriptions = ConcurrentHashMap<String, ConcurrentLinkedQueue<Identifier<Client>>>()
    override val subscriptionsByClient = ConcurrentHashMap<Client, ConcurrentHashMap<String, Subscription<Client>>>()

    override fun subscribe(client: Client, start: Start): List<GraphQLError>? {
        try {
            logger.debug { "Subscribe: id=${start.id} query=${start.payload.query}" }
            val document = Parser().parseDocument(start.payload.query)
            val errors = Validator().validateDocument(schema, document)
            if (errors.isNotEmpty()) {
                return errors
            }

            val id = Identifier(client, start.id)

            val toNotify = subscriptionToNotify(document, start.payload.operationName)
            subscriptions
                    .getOrPut(toNotify, { ConcurrentLinkedQueue() })
                    .add(id)
            subscriptionsByClient
                    .getOrPut(client, { ConcurrentHashMap() })
                    .put(start.id, Subscription(client, start, toNotify, null))

            return null
        }catch(e: ParseCancellationException) {
            logger.error { "Error during subscription: $e" }
            return listOf(SubscriptionDocumentError(e.message ?: e.javaClass.simpleName))
        }catch(e: IllegalArgumentException) {
            logger.error { "Error during subscription: $e" }
            return listOf(SubscriptionDocumentError(e.message ?: e.javaClass.simpleName))
        }
    }

    override fun unsubscribe(client: Client, subscriptionId: String) {
        logger.debug { "Unsubscribe: id=$subscriptionId" }
        val id = Identifier(client, subscriptionId)

        val subscription = subscriptionsByClient[client]?.remove(subscriptionId) ?: throw NoSuchSubscriptionException("No such subscription.")
        subscriptions[subscription.subscriptionName]?.remove(id)
    }

    override fun disconnected(client: Client) {
        logger.debug { "Client with ${subscriptionsByClient[client]?.size ?: 0} subscriptions disconnected." }
        val ids = subscriptionsByClient.remove(client) ?: return
        ids.forEach { (subscriptionId, subscription) ->
            subscriptions[subscription.subscriptionName]?.remove(Identifier(client, subscriptionId))
        }
    }
}

// TODO: a better way to obtain this information?
fun subscriptionToNotify(document: Document, operationName: String? = null): String {
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

class SubscriptionDocumentError(private val description: String) : GraphQLError {
    override fun getMessage() = description
    override fun getErrorType() = ErrorType.ValidationError
    override fun getLocations() = null
}
