package net.holak.graphql.ws.test

import graphql.GraphQLError
import net.holak.graphql.ws.DefaultSubscriptions
import net.holak.graphql.ws.Identifier
import net.holak.graphql.ws.Start
import net.holak.graphql.ws.Subscription
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.Assert.*
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object DefaultSubscriptionsSpec : Spek({

    val schema = testGraphQL().schema
    @Suppress("unused")
    val subject = {
        object {
            val subscriptions = DefaultSubscriptions<String>(schema)
        }
    }

    describe("subscribe") {

        it("returns null when there are no errors in the subscriptions") { with(subject()) {
            val result = subscriptions.subscribe("Anne", Start("1", "subscription { helloChanged }"))
            assertNull(result)
        }}

        it("returns an error on a parse exception") { with(subject()) {
            val result = subscriptions.subscribe("Anne", Start("1", "this is not a valid GraphQL request"))
            assertErrorsReturned(result)
        }}

        it("returns a list of errors if the subscription fails validation") { with(subject()) {
            val result = subscriptions.subscribe("Anne", Start("1", "subscription { nonExistingSubscription }"))
            assertErrorsReturned(result)
        }}

        it("returns an error if the operation cannot be uniquely determined") { with(subject()) {
            val result = subscriptions.subscribe("Anne", Start("1", Start.Payload(
                    query = "subscription S1 { helloChanged } subscription S2 { helloChanged }",
                    operationName = null
            )))
            assertErrorsReturned(result)
        }}

        it("recognizes the correct subscription when operationName is provided") { with(subject()) {
            val result = subscriptions.subscribe("Anne", Start("1", Start.Payload(
                    query = "subscription S1 { helloChanged } subscription S2 { helloChanged }",
                    operationName = "S1"
            )))
            assertNull(result)
        }}

        it("returns an error if the operation is not a subscription") { with(subject()) {
            val result = subscriptions.subscribe("Anne", Start("1", Start.Payload(
                    query = "subscription S1 { helloChanged } query Q2 { currentHello }",
                    operationName = "Q2"
            )))
            assertErrorsReturned(result)
        }}

        it("saves the subscription by subscription name and by client") { with(subject()) {
            val start = Start(id = "1", payload = Start.Payload(
                    query = "subscription S { helloChanged }",
                    variables = null,
                    operationName = "S"
            ))
            val result = subscriptions.subscribe("Anne", start)
            assertNull(result)

            assertEquals(1, subscriptions.subscriptions.size)
            assertEquals("helloChanged", subscriptions.subscriptions.keys().toList().firstOrNull())
            assertEquals(Identifier("Anne", "1"), subscriptions.subscriptions["helloChanged"]!!.firstOrNull())
            assertEquals(1, subscriptions.subscriptionsByClient.size)
            assertEquals("Anne", subscriptions.subscriptionsByClient.keys().toList().firstOrNull())

            val subscription = subscriptions.subscriptionsByClient["Anne"]!!["1"]!!
            val expectedSubscription = Subscription(
                    client = "Anne",
                    start = start,
                    subscriptionName = "helloChanged",
                    input = null
            )
            assertEquals(expectedSubscription, subscription)
        }}

        it("replaces an existing subscription if the id is already in use") {

        }

        it("removes the old subscription if the id is already in use and the subscription name differs") {

        }
    }

    describe("unsubscribe") {
        it("remove the subscription from both maps") {

        }

        it("returns null when the subscription did exist") {

        }

        it("returns an error if the subscription doesn't exist") {

        }
    }

    describe("disconnected") {
        it("cleans up all subscriptions associated with the client") { with(subject()) {
            subscriptions.subscribe("Anne", Start("1", "subscription { helloChanged }"))
            subscriptions.subscribe("Anne", Start("2", "subscription { helloChanged }"))
            subscriptions.subscribe("Jim", Start("1", "subscription { helloChanged }"))

            subscriptions.disconnected("Anne")

            assertNull(subscriptions.subscriptionsByClient["Anne"])
            assertNotNull(subscriptions.subscriptionsByClient["Jim"])
            subscriptions.subscriptions["helloChanged"]!!.forEach {
                assertEquals("Jim", it.client)
            }
        }}
    }

})

fun assertErrorsReturned(result: List<GraphQLError>?) {
    assertNotNull(result)
    assertTrue("at least one error must be returned", result!!.isNotEmpty())
    result.forEach {
        assertTrue("result must be a GraphQLError instance", it is GraphQLError)
    }
}
