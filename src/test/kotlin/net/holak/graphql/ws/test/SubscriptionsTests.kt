package net.holak.graphql.ws.test

import graphql.Assert.assertNotEmpty
import graphql.GraphQLError
import net.holak.graphql.ws.DefaultSubscriptions
import net.holak.graphql.ws.Start
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

        it("saves the subscription by subscription name and by client") {

        }

        it("replaces an existing subscription if the id is already in use") {
            it("removes the old subscription if the subscription name differs") {

            }
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
            subscriptions.disconnected("Anne")
        }}

        it("removes the client reference from the map") {

        }
    }

})

fun assertErrorsReturned(result: List<GraphQLError>?) {
    assertNotNull(result)
    assertTrue("at least one error must be returned", result!!.isNotEmpty())
    result.forEach {
        assertTrue("result must be a GraphQLError instance", it is GraphQLError)
    }
}
