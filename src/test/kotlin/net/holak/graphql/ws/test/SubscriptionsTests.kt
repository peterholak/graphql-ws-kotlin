package net.holak.graphql.ws.test

import net.holak.graphql.ws.DefaultSubscriptions
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object DefaultSubscriptionsSpec : Spek({

    val subscriptions = DefaultSubscriptions<String>(testGraphQL().schema)

    describe("subscribe") {

        it("returns null when there are no errors in the subscriptions") {

        }

        it("returns an error on a parse exception") {

        }

        it("returns a list of errors if the subscription fails validation") {

        }

        it("returns an error if the operation cannot be uniquely determined") {

        }

        it("returns an error if the operation is not a subscription") {

        }

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
        it("cleans up all subscriptions associated with the client") {
            subscriptions.disconnected("Anne")
        }

        it("removes the client reference from the map") {

        }
    }

})
