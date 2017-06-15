package net.holak.graphql.ws

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object SubscriptionWebSocketHandlerSpec : Spek({
    describe("when a client disconnects") {
        it("unsubscribes the client from everything") {

        }
    }

    describe("when a client subscribes with GQL_START") {
        it("sends a GQL_DATA with an error if there are errors during subcribing") {

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