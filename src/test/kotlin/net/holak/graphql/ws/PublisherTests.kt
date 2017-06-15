package net.holak.graphql.ws

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.junit.platform.runner.JUnitPlatform
import org.junit.runner.RunWith

@RunWith(JUnitPlatform::class)
object DefaultPublisherSpec : Spek({
    describe("publish") {
        it("does nothing when no one is subscribed to the subscription name") {

        }

        it("runs the query") {

        }

        it("sends a GQL_DATA response") {

        }
    }
})
