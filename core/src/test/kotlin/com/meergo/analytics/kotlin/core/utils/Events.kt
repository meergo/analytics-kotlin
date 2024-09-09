import com.meergo.analytics.kotlin.core.BaseEvent
import kotlinx.serialization.json.buildJsonObject

fun withoutSessionInfo(event: BaseEvent): BaseEvent {
    event.context = buildJsonObject {
        event.context.forEach { (key, value) -> if (key !== "sessionId" && key !== "sessionStart") put(key, value) }
    }
    return event
}