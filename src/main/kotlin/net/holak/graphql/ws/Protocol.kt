@file:Suppress("unused")

package net.holak.graphql.ws

import graphql.ExecutionResult
import graphql.GraphQLError

typealias JsonMap = Map<String, Any?>

object Type {
    /** @see ConnectionInit */
    val GQL_CONNECTION_INIT = "connection_init"

    /** @see ConnectionAck */
    val GQL_CONNECTION_ACK = "connection_ack"

    /** @see ConnectionError */
    val GQL_CONNECTION_ERROR = "connection_error"

    /** @see ConnectionKeepAlive */
    val GQL_CONNECTION_KEEP_ALIVE = "ka"

    /** @see ConnectionTerminate */
    val GQL_CONNECTION_TERMINATE = "connection_terminate"

    /** @see Start */
    val GQL_START = "start"

    /** @see Data */
    val GQL_DATA = "data"

    /** @see Error */
    val GQL_ERROR = "error"

    /** @see Complete */
    val GQL_COMPLETE = "complete"

    /** @see Stop */
    val GQL_STOP = "stop"
}

fun classForMessageType(type: String): Class<out OperationMessage<*>>? {
    return when (type) {
        Type.GQL_CONNECTION_INIT -> ConnectionInit::class.java
        Type.GQL_CONNECTION_ACK -> ConnectionAck::class.java
        Type.GQL_CONNECTION_ERROR -> ConnectionError::class.java
        Type.GQL_CONNECTION_KEEP_ALIVE -> ConnectionKeepAlive::class.java
        Type.GQL_CONNECTION_TERMINATE -> ConnectionTerminate::class.java
        Type.GQL_START -> Start::class.java
        Type.GQL_DATA -> Data::class.java
        Type.GQL_ERROR -> Error::class.java
        Type.GQL_COMPLETE -> Complete::class.java
        Type.GQL_STOP -> Stop::class.java
        else -> null
    }
}


interface OperationMessage<out T> {
    val id: String?
    val type: String
    val payload: T?
}

sealed class ClientToServerMessage<out T> : OperationMessage<T>
sealed class ServerToClientMessage<out T> : OperationMessage<T>

/**
 * The message description docs are taken from:
 * https://github.com/apollographql/subscriptions-transport-ws/blob/master/PROTOCOL.md
 * and modified to be more accurate for this project.
 *
 * Used under terms of the MIT license:
 * https://github.com/apollographql/subscriptions-transport-ws/blob/master/LICENSE
 */

/**
 * Client sends this message after plain WebSocket connection to start the communication with the server.
 *
 * The server will respond only with GQL_CONNECTION_ACK or GQL_CONNECTION_ERROR to this message.
 *
 * @param payload optional parameters that the client specifies in connectionParams
 */
class ConnectionInit(
        override val payload: JsonMap = emptyMap()
) : ClientToServerMessage<JsonMap>() {

    override val type = Type.GQL_CONNECTION_INIT
    override val id = null

}

/**
 * Client sends this message to execute GraphQL operation.
 *
 * @param id The id of the GraphQL operation to start.
 */
class Start(
        override val id: String,
        override val payload: Start.Payload
) : ClientToServerMessage<Start.Payload>() {

    override val type = Type.GQL_START
    constructor(id: String, query: String) : this(id, Start.Payload(query))

    class Payload(
            /** GraphQL operation as string. */
            val query: String,

            /** Object with GraphQL variables. */
            val variables: JsonMap? = null,

            /** GraphQL operation name */
            val operationName: String? = null
    )

}

/**
 * Client sends this message in order to stop a running GraphQL operation execution (for example: unsubscribe).
 *
 * @param id operation id
 */
class Stop(
        override val id: String
) : ClientToServerMessage<Nothing>() {
    override val type = Type.GQL_STOP
    override val payload = null
}

/** Client sends this message to terminate the connection. */
class ConnectionTerminate : ClientToServerMessage<Nothing>() {
    override val type = Type.GQL_CONNECTION_TERMINATE
    override val id = null
    override val payload = null
}

/**
 * The server may respond with this message to a GQL_CONNECTION_INIT, this indicates that the server rejected the connection.
 *
 * The server may also respond with this message in case of a parse error in the message
 * (which should not disconnect the client, just ignore the message).
 *
 * @param payload the server side error
 */
class ConnectionError(override val payload: JsonMap) : ServerToClientMessage<JsonMap>() {
    override val type = Type.GQL_CONNECTION_ERROR
    override val id = null
}

/**
 * The server may respond with this message to a GQL_CONNECTION_INIT from the client,
 * this indicates that the server accepted the connection.
 */
class ConnectionAck : ServerToClientMessage<Nothing>() {
    override val type = Type.GQL_CONNECTION_ACK
    override val id = null
    override val payload = null
}

/**
 * The server sends this message to transfer the GraphQL execution result from the server to the client,
 * this message is a response to a GQL_START message.
 *
 * @param id ID of the operation that was successfully set up
 * @param payload
 */
class Data(
        override val id: String,
        override val payload: Data.Payload
) : ServerToClientMessage<Data.Payload>() {
    override val type = Type.GQL_DATA

    /**
     * This can be removed and `graphql.ExecutionResult` used directly once
     * https://github.com/graphql-java/graphql-java/issues/473 is resolved
     */
    class Payload(
            val data: Any?,
            val errors: List<GraphQLError>? = null,
            val extensions: Any? = null
    )

    constructor(id: String, data: Any = emptyMap<String, Any?>())
            : this(id, Payload(data))

    constructor(id: String, errors: List<GraphQLError>)
            : this(id, Payload(null, errors))

    constructor(id: String, result: ExecutionResult)
            : this(id, Payload(
                    result.getData(),
                    if (result.errors?.isEmpty() ?: true) null else result.errors,
                    result.extensions
            ))
}

/**
 * Server sends this message upon a failing operation, before the GraphQL execution,
 * usually due to GraphQL validation errors (resolver errors are part of a GQL_DATA message,
 * and will be added to the errors array)
 *
 * @param id operation ID of the operation that failed on the server
 * @param payload payload with the error attributed to the operation failing on the server
 */
class Error(
        override val id: String,
        override val payload: JsonMap
) : ServerToClientMessage<JsonMap>() {
    override val type = Type.GQL_ERROR
}

/**
 * Server sends this message to indicate that a GraphQL operation is done,
 * and no more data will arrive for the specific operation.
 */
class Complete(override val id: String) : ServerToClientMessage<Nothing>() {
    override val type = Type.GQL_COMPLETE
    override val payload = null
}

/**
 * Server message sent periodically to keep the client connection alive.
 */
class ConnectionKeepAlive : ServerToClientMessage<Nothing>() {
    override val type = Type.GQL_CONNECTION_KEEP_ALIVE
    override val id = null
    override val payload = null
}
