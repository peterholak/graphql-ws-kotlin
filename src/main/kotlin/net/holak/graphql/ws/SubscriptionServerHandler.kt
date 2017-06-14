package net.holak.graphql.ws

interface SubscriptionServerHandler<in Client> {
    fun subscribe(client: Client, start: Start)
    fun unsubscribe(client: Client, subscriptionId: String)
    fun disconnected(client: Client)
}
