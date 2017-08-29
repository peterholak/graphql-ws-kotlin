package net.holak.graphql.ws

typealias Filter = (args: JsonObject) -> Boolean

interface Publisher {
    fun publish(subscriptionName: String, data: Any? = null, filter: Filter? = null)
}

/**
 * A shorthand version that simply uses the context's
 * "class name with the first letter converted to lowercase"as the subscription name.
 */
inline fun <reified T> Publisher.publish(context: T?) {
    // TODO: this obviously only supports that one special convention of class names right now, which is not enough.
    val className = T::class.java.simpleName
    val camelCased = className[0].toLowerCase() + className.substring(1)
    publish(camelCased, context)
}

/** A hack to deal with the cyclical dependency. */
class LatePublisher : Publisher {
    override fun publish(subscriptionName: String, data: Any?, filter: Filter?) = publisher.publish(subscriptionName, data, filter)
    lateinit var publisher: Publisher
}
