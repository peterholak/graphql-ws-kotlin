@file:Suppress("unused")

package net.holak.graphql.ws

import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

typealias Filter = (args: JsonMap) -> Boolean

interface Publisher {
    fun publish(subscriptionName: String, data: Any? = null, filter: Filter? = null)
}

/**
 * A shorthand version that simply uses the context's
 * "class name with the first letter converted to lowercase"as the subscription name.
 */
inline fun <reified T> Publisher.publish(context: T?, noinline filter: Filter? = null) {
    // TODO: this obviously only supports that one special convention of class names right now, which is not enough.
    val className = T::class.java.simpleName
    val camelCased = className[0].toLowerCase() + className.substring(1)
    publish(camelCased, context, filter)
}

/** A hack to deal with the cyclical dependency. */
class LatePublisher : Publisher {
    override fun publish(subscriptionName: String, data: Any?, filter: Filter?) = publisher.publish(subscriptionName, data, filter)
    lateinit var publisher: Publisher
}

/**
 * Publisher that queues calls to `publish`, and calls them on another publisher in its own thread.
 */
class QueuedPublisher(val publisher: Publisher) : Publisher {

    private class PublishArguments(val subscriptionName: String?, val data: Any?, val filter: Filter?)
    private val queue = LinkedBlockingQueue<PublishArguments>()

    val publishThread = thread(name="QueuedPublisher") {
        while(true) {
            val arguments = queue.take()
            if (arguments.subscriptionName == null) break
            publisher.publish(arguments.subscriptionName, arguments.data, arguments.filter)
        }
    }

    override fun publish(subscriptionName: String, data: Any?, filter: Filter?) {
        queue.put(PublishArguments(subscriptionName, data, filter))
    }

    fun shutdown() {
        queue.add(PublishArguments(null, null, null))
        publishThread.join()
    }

}
