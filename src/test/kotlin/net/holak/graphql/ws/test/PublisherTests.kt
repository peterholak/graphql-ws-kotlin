package net.holak.graphql.ws.test

import com.nhaarman.mockito_kotlin.*
import graphql.GraphQL
import net.holak.graphql.ws.*
import org.eclipse.jetty.websocket.api.Session
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object DefaultPublisherSpec : Spek({
    describe("publish") {

        val graphQL = mock<GraphQL>()
        val subscriptions = mock<Subscriptions<Session>>()
        val transport: Transport<Session> = { _, _ -> Unit }
        val publisher = DefaultPublisher<Session>(graphQL, subscriptions, transport)

        it("does nothing when no one is subscribed to the subscription name") {

        }

        it("runs the query with the data as context") {
            val client = mock<Session>()
            whenever(subscriptions.subscriptions)
                    .thenReturn(mapOf(
                            "hello" to listOf(
                                    Identifier(client, "1")
                            )
                    ))
            whenever(subscriptions.subscriptionsByClient)
                    .thenReturn(mapOf(
                            client to mapOf("1" to Subscription<Session>(
                                    client = client,
                                    start = Start("1", Start.Payload(
                                            query = "subscription { hello { text } }",
                                            operationName = "S",
                                            variables = mapOf("arg" to 1)
                                    )),
                                    subscriptionName = "hello",
                                    input = null
                            ))
                    ))

            publisher.publish("hello", "data")
            verify(graphQL).execute("subscription { hello { text } }", "S", "data", mapOf("arg" to 1))
        }

        it("sends a GQL_DATA response") {

        }
    }

    describe("inline publish version") {
        it("infers the subscription name from the class name") {

        }
    }
})
