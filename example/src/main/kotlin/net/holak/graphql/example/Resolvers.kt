@file:Suppress("unused")

package net.holak.graphql.example

import com.coxautodev.graphql.tools.GraphQLMutationResolver
import com.coxautodev.graphql.tools.GraphQLQueryResolver
import com.coxautodev.graphql.tools.GraphQLSubscriptionResolver
import graphql.schema.DataFetchingEnvironment
import net.holak.graphql.ws.Publisher

class Store(var currentMessage: Message = Message("Hello world"))

class Message(val text: String) {
    fun randomNumber() = Math.random().times(100).toInt()
}

class QueryResolver(val store: Store) : GraphQLQueryResolver {
    fun currentMessage() = store.currentMessage
}

class MutationResolver(val store: Store, val publisher: Publisher) : GraphQLMutationResolver {
    fun setMessage(text: String): Message {
        store.currentMessage = Message(text)
        publisher.publish("messageChanged", null)
        return store.currentMessage
    }
}

class SubscriptionResolver(val store: Store) : GraphQLSubscriptionResolver {
    fun messageChanged(): Message {
        return store.currentMessage
    }

    fun unrelatedMessage(env: DataFetchingEnvironment): Message {
        return Message(env.getContext<String>())
    }
}
