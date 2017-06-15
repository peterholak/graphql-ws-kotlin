package net.holak.graphql.example

import com.coxautodev.graphql.tools.SchemaParser
import graphql.GraphQL
import graphql.schema.GraphQLSchema
import net.holak.graphql.ws.*
import org.eclipse.jetty.websocket.api.Session

fun main(args: Array<String>) {
    val latePublisher = LatePublisher()
    val lateSubscriptions = LateSubscriptions<Session>()
    val store = Store()
    val (graphQL, schema) = loadSchema(store, latePublisher)
    val server = ExampleServer(lateSubscriptions, graphQL, latePublisher)

    lateSubscriptions.handler = SimpleSubscriptions(schema)
    latePublisher.publisher = SimplePublisher(graphQL, lateSubscriptions.handler, server.transport())
}

fun loadSchema(store: Store, publisher: Publisher): Pair<GraphQL, GraphQLSchema> {
    val schema = SchemaParser.newParser()
            .file("graphql/example.graphql")
            .resolvers(QueryResolver(store), MutationResolver(store, publisher), SubscriptionResolver(store))
            .build()
            .makeExecutableSchema()

    return Pair(GraphQL.newGraphQL(schema).build()!!, schema)
}