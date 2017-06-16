package net.holak.graphql.example

import com.coxautodev.graphql.tools.SchemaParser
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import net.holak.graphql.ws.*
import org.eclipse.jetty.websocket.api.Session

fun main(args: Array<String>) {
    val latePublisher = LatePublisher()
    val (graphQL, schema) = loadSchema(latePublisher)
    val subscriptions = DefaultSubscriptions<Session>(schema)
    val server = ExampleServer(subscriptions, graphQL, latePublisher)

    latePublisher.publisher = DefaultPublisher(graphQL, subscriptions, server.socketHandler.transport)
}

fun loadSchema(publisher: Publisher): Pair<GraphQL, GraphQLSchema> {
    val store = Store()
    val schema = SchemaParser.newParser()
            .file("graphql/example.graphql")
            .resolvers(QueryResolver(store), MutationResolver(store, publisher), SubscriptionResolver(store))
            .build()
            .makeExecutableSchema()

    return Pair(GraphQL.newGraphQL(schema).build()!!, schema)
}
