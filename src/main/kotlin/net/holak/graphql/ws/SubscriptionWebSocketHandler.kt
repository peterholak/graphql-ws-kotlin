package net.holak.graphql.ws

import com.google.gson.GsonBuilder
import mu.KLogging
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketException
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import java.util.concurrent.ConcurrentLinkedQueue

/** TODO: thread safety */
@Suppress("unused", "UNUSED_PARAMETER")
@WebSocket
class SubscriptionWebSocketHandler(val subscriptions: Subscriptions<Session>, val publisher: Publisher) {
    val sessions = ConcurrentLinkedQueue<Session>()
    val gson = GsonBuilder().registerTypeAdapter(OperationMessage::class.java, MessageDeserializer()).create()!!
    companion object: KLogging()

    @OnWebSocketClose
    fun onWebSocketClose(session: Session, statusCode: Int, reason: String) {
        logger.info { "Closed connection with client: statusCode=$statusCode reason=$reason." }
        subscriptions.disconnected(session)
        sessions.remove(session)
    }

    @OnWebSocketConnect
    fun onWebSocketConnect(session: Session) {
        logger.info { "Client connected. "}
        sessions.add(session)
    }

    @OnWebSocketMessage
    fun onWebSocketText(session: Session, message: String) {

        val operation = gson.fromJson(message, OperationMessage::class.java)
                as? ClientToServerMessage<*>
                ?: return

        when (operation) {
            is ConnectionInit -> sendOperation(session, ConnectionAck())
            // TODO: technically, this should support queries and mutations as well
            is Start -> {
                val errors = subscriptions.subscribe(session, operation)
                if (errors != null) {
                    sendOperation(session, Data(operation.id, errors))
                }
            }
            is Stop -> {
                subscriptions.unsubscribe(session, operation.id)
                sendOperation(session, Complete(operation.id))
            }
            is ConnectionTerminate -> session.close()
        }

    }

    fun sendOperation(session: Session, message: ServerToClientMessage<*>) {
        try {
            session.remote.sendString(gson.toJson(message))
        }catch(e: WebSocketException) {
            // TODO: this exception doesn't necessarily mean that the client has disconnected
            // (it does in the current Jetty implementation in getRemote() though).
            logger.info { "Found out that a client is already disconnected while attempting to send a ${message.type} message." }
            subscriptions.disconnected(session)
        }
    }

    val transport: Transport<Session> = { session, data -> sendOperation(session, data) }

}
