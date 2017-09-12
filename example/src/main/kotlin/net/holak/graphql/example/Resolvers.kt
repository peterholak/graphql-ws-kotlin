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

@Suppress("UNUSED_PARAMETER")
class SubscriptionResolver(val store: Store) : GraphQLSubscriptionResolver {
    fun messageChanged(): Message {
        return store.currentMessage
    }

    fun textPublished(env: DataFetchingEnvironment): String {
        return env.getContext<String>()
    }

    fun multiplyPublishedText(by: Int, env: DataFetchingEnvironment): Int {
        val message = env.getContext<String>().toIntOrNull() ?: return 0
        return message * by
    }

    fun filteredPublishedText(lessThan: Int, env: DataFetchingEnvironment): Int {
        return env.getContext<String>().toInt()
    }

    fun complexInput(input: VariousTypes): Boolean {
        return false
    }
}

data class VariousTypes(
        val id: String,
        val star: Star,
        val listOfStrings: List<String>,
        val number: Int
)

enum class Star {
    Wars,
    Trek,
    Gate,
    Craft,
    Control,
    Citizen
}