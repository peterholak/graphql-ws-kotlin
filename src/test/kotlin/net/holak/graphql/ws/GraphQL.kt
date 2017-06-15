@file:Suppress("unused", "UNUSED_PARAMETER")

package net.holak.graphql.ws

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLSubscriptionResolver
import com.coxautodev.graphql.tools.SchemaParser
import graphql.GraphQL
import graphql.schema.GraphQLSchema

class GraphQLWithSchema(val graphQL: GraphQL, val schema: GraphQLSchema)

val schemaDefinition = """
    type Query {
        currentHello: String!
    }

    type Mutation {
        setHello(text: String!): String!
    }

    type Subscription {
        helloChanged: String!
    }
"""

fun testGraphQL(): GraphQLWithSchema {
    val schema = SchemaParser.newParser()
            .schemaString(schemaDefinition)
            .resolvers(Query(), Mutation(), Subscription())
            .build()
            .makeExecutableSchema()

    return GraphQLWithSchema(GraphQL.newGraphQL(schema).build()!!, schema)
}

class Query : GraphQLQueryResolver {
    val currentHello = "Query"
}

class Mutation : GraphQLMutationResolver {
    fun setHello(text: String) = "Mutation"
}

class Subscription : GraphQLSubscriptionResolver {
    val helloChanged = "Subscription"
}
