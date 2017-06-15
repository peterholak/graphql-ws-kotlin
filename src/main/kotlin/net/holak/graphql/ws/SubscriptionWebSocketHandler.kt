package net.holak.graphql.ws

import com.google.gson.GsonBuilder
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import java.util.concurrent.ConcurrentLinkedQueue

/** TODO: thread safety */
@Suppress("unused", "UNUSED_PARAMETER")
@WebSocket
class SubscriptionWebSocketHandler(val subscriptions: SubscriptionHandler<Session>) {
    val sessions = ConcurrentLinkedQueue<Session>()

    val gson = GsonBuilder().registerTypeAdapter(OperationMessage::class.java, MessageDeserializer()).create()!!

    @OnWebSocketClose
    fun onWebSocketClose(session: Session, statusCode: Int, reason: String) {
        println("Disconnected: " + reason)
        subscriptions.disconnected(session)
        sessions.remove(session)
    }

    @OnWebSocketConnect
    fun onWebSocketConnect(session: Session) {
        sessions.add(session)
    }

    @OnWebSocketMessage
    fun onWebSocketText(session: Session, message: String) {

        val operation = gson.fromJson(message, OperationMessage::class.java)
                as? ClientToServerMessage<*>
                ?: return

        when (operation) {
            is ConnectionInit -> sendOperation(session, ConnectionAck())
            is Start -> subscriptions.subscribe(session, operation)
            is Stop -> {
                subscriptions.unsubscribe(session, operation.id)
                sendOperation(session, Complete(operation.id))
            }
            is ConnectionTerminate -> session.close()
        }

    }

    fun sendOperation(session: Session, message: ServerToClientMessage<*>) {
        session.remote.sendString(gson.toJson(message))
    }

    val transport: Transport<Session> = { session, data -> sendOperation(session, data) }

}