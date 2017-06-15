package net.holak.graphql.ws

interface Publisher {
    fun publish(subscriptionName: String, data: Any? = null)
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
    override fun publish(subscriptionName: String, data: Any?) = publisher.publish(subscriptionName, data)
    lateinit var publisher: Publisher
}
