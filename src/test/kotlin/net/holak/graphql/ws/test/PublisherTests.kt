package net.holak.graphql.ws.test

import com.nhaarman.mockito_kotlin.*
import graphql.ExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import net.holak.graphql.ws.*
import org.eclipse.jetty.websocket.api.Session
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor

@RunWith(JUnitPlatform::class)
object DefaultPublisherSpec : Spek({

    class TransportCall(val session: Session, val data: Data)
    @Suppress("unused")
    val subject = {
        object {
            val graphQL = mock<GraphQL>()
            val subscriptions = mock<Subscriptions<Session>>()
            val transportCalls = mutableListOf<TransportCall>()
            val transport: Transport<Session> = { session, data -> transportCalls.add(TransportCall(session, data)) }
            val publisher = DefaultPublisher(graphQL, subscriptions, transport)
            init {
                whenever(graphQL.execute(any<ExecutionInput>())).thenReturn(MockGraphQLResult)
            }
            fun subscribeTheOnlyClient(id: String = "1"): Session {
                val client = mock<Session>()
                whenever(subscriptions.subscriptions)
                        .thenReturn(helloSubscriptionData(client, id))
                whenever(subscriptions.subscriptionsByClient)
                        .thenReturn(helloSubscriptionByClientData(client, id))
                return client
            }
        }
    }

    describe("publish") {

        it("does nothing when no one is subscribed to the subscription name") { with(subject()) {
            val client = mock<Session>()
            whenever(subscriptions.subscriptions)
                    .thenReturn(mapOf("somethingElse" to listOf(Identifier(client, "1"))))

            publisher.publish("hello")

            verifyZeroInteractions(graphQL)
            assertEquals(0, transportCalls.size)
        }}

        it("runs the query with the data as context") { with(subject()) {
            subscribeTheOnlyClient()

            publisher.publish("hello", "data")

            val input = ArgumentCaptor.forClass(ExecutionInput::class.java)
            verify(graphQL).execute(input.capture())

            assertEquals("subscription { hello { text } }", input.value.query)
            assertEquals("S", input.value.operationName)
            assertEquals("data", input.value.context)
            assertEquals(null, input.value.root)
            assertEquals(mapOf("arg" to 1), input.value.variables)
        }}

        it("sends a GQL_DATA response") { with(subject()) {
            val client = subscribeTheOnlyClient("3")

            publisher.publish("hello")
            assertEquals(1, transportCalls.size)
            assertSame(client, transportCalls[0].session)
            assertEquals(Type.GQL_DATA, transportCalls[0].data.type)
            assertEquals("3", transportCalls[0].data.id)
        }}

        it("applies the filter") { with(subject()) {
            subscribeTheOnlyClient()

            publisher.publish("hello") {
                false
            }
            assertEquals(0, transportCalls.size)
            publisher.publish("hello") {
                true
            }
            assertEquals(1, transportCalls.size)
        }}
    }

    describe("inline publish version") {
        it("infers the subscription name from the class name") {
            data class HelloSubscription(val world: String)
            val mockPublisher = mock<Publisher>()
            mockPublisher.publish(HelloSubscription("world"))
            verify(mockPublisher).publish("helloSubscription", HelloSubscription("world"))
        }
    }
})

fun helloSubscriptionData(client: Session, id: String) = mapOf(
        "hello" to listOf(
                Identifier(client, id)
        )
)

fun helloSubscriptionByClientData(client: Session, id: String) = mapOf(
        client to mapOf(id to Subscription(
                client = client,
                start = Start(id = id, payload = Start.Payload(
                        query = "subscription { hello { text } }",
                        operationName = "S",
                        variables = mapOf("arg" to 1)
                )),
                subscriptionName = "hello"
        ))
)

object MockGraphQLResult : ExecutionResult {
    override fun toSpecification() = mapOf("data" to null, "errors" to null, "extensions" to null)
    override fun getErrors() = null
    override fun getExtensions() = null
    override fun <T : Any?> getData(): T? = null
}
