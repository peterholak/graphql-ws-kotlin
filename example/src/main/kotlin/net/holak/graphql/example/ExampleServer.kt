package net.holak.graphql.example

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import graphql.GraphQL
import net.holak.graphql.ws.SubscriptionHandler
import net.holak.graphql.ws.SubscriptionWebSocketHandler
import org.eclipse.jetty.websocket.api.Session
import spark.Response
import spark.Service
import java.io.PrintWriter
import java.io.StringWriter

class ExampleServer(subscriptions: SubscriptionHandler<Session>, val graphQL: GraphQL) {
    val http = Service.ignite()!!
    val gson = Gson()
    val socketHandler: SubscriptionWebSocketHandler
    val indexHtml = ClassLoader.getSystemResourceAsStream("frontend/dist/index.html").reader().readText()
    fun transport() = socketHandler.transport

    init {
        http.port(4567)
        socketHandler = SubscriptionWebSocketHandler(subscriptions)
        http.webSocket("/subscriptions", socketHandler)

        http.staticFiles.location("frontend/dist")

        http.after { req, res ->
            if (req.headers("Sec-WebSocket-Protocol") == "graphql-ws") {
                res.header("Sec-WebSocket-Protocol", "graphql-ws")
            }
        }

        addGraphQLPostHandlers()
        defaultToIndexHtml()

        println("Server running at http://localhost:4567/")
    }

    private fun defaultToIndexHtml() {
        http.get("*") { req, res ->
            // https//github.com/perwendel/spark/issues/502#issuecomment-219324858
            if (req.raw().pathInfo == "/subscriptions") return@get null

            res.type("text/html")
            indexHtml
        }
    }

    private fun addGraphQLPostHandlers() {
        http.options("/graphql") { _, res ->
            corsAllowEveryone(res)
            ""
        }
        http.post("/graphql") { req, res ->
            try {
                res.type("application/json")
                val body = gson.fromJson(req.body(), LinkedTreeMap::class.java)
                corsAllowEveryone(res)

                @Suppress("UNCHECKED_CAST")
                val variables = body["variables"] as Map<String, Any?>? ?: emptyMap()
                val result = graphQL.execute(body["query"] as String, body["operationName"] as String?, null, variables)
                return@post gson.toJson(result)
            }catch(e: Exception) {
                return@post exceptionToJson(e)
            }
        }
    }

    fun exceptionToJson(e: Exception): String {
        val writer = StringWriter()
        e.printStackTrace(PrintWriter(writer))
        return gson.toJson(mapOf(
                "errors" to listOf(mapOf(
                        "message" to ("Server exception: " + e.message),
                        "stackTrace" to writer.toString().split('\n')
                ))
        ))
    }

    // Obviously this would be a bad idea for production
    private fun corsAllowEveryone(response: Response) {
        response.header("access-control-allow-origin", "*")
        response.header("access-control-allow-methods", "options, post")
        response.header("access-control-allow-headers", "content-type")
    }
}