package net.holak.graphql.example

import com.coxautodev.graphql.tools.SchemaParser
import graphql.GraphQL
import net.holak.graphql.ws.LatePublisher
import net.holak.graphql.ws.Publisher
import net.holak.graphql.ws.SimplePublisher
import net.holak.graphql.ws.SimpleSubscriptions
import org.eclipse.jetty.websocket.api.Session

fun main(args: Array<String>) {
    val latePublisher = LatePublisher()
    val subscriptions = SimpleSubscriptions<Session>()
    val store = Store()
    val graphQL = loadSchema(store, latePublisher)
    val server = ExampleServer(subscriptions, graphQL)

    latePublisher.publisher = SimplePublisher(graphQL, subscriptions, server.transport())
}

fun loadSchema(store: Store, publisher: Publisher): GraphQL {
    val schema = SchemaParser.newParser()
            .file("graphql/example.graphql")
            .resolvers(QueryResolver(store), MutationResolver(store, publisher), SubscriptionResolver(store))
            .build()
            .makeExecutableSchema()

    return GraphQL.newGraphQL(schema).build()!!
}