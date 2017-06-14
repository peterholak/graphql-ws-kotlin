package net.holak.graphql.ws

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type
import net.holak.graphql.ws.Type as GQL

class MessageDeserializer : JsonDeserializer<OperationMessage<*>> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): OperationMessage<*> {
        val type = json.asJsonObject?.get("type")?.asString
                ?: throw JsonSyntaxException("Cannot parse operation message: no 'type' field.")

        val messageClass = classForMessageType(type)
                ?: throw JsonSyntaxException("Unknown operation message type: $type")

        return context.deserialize(json, messageClass)
    }

}
