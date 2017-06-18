package net.holak.graphql.ws.test

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import net.holak.graphql.ws.Publisher
import net.holak.graphql.ws.SubscriptionWebSocketHandler
import net.holak.graphql.ws.Subscriptions
import org.eclipse.jetty.websocket.api.Session
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object SubscriptionWebSocketHandlerSpec : Spek({

    @Suppress("UNCHECKED_CAST")
    val subscriptions = mock<Subscriptions<Session>>()
    val publisher = mock<Publisher>()
    val handler = SubscriptionWebSocketHandler(subscriptions, publisher)

    describe("when a client disconnects") {
        it("unsubscribes the client from everything") {
            val client = mock<Session>()
            handler.onWebSocketClose(client, 0, "")
            verify(subscriptions).disconnected(client)
        }
    }

    describe("when a client subscribes with GQL_START") {
        it("sends a GQL_DATA with an error if there are errors during subscribing") {

        }
    }

    describe("when a client unsubscribes with GQL_STOP") {
        it("unsubscribes the client") {

        }

        it("sends a GQL_COMPLETE if there were no errors during unsubscribing") {

        }

        it("sends a GQL_ERROR followed by a GQL_COMPLETE if there were errors during unsubscribing") {

        }
    }
})
