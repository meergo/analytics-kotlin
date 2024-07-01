import com.segment.analytics.kotlin.core.BaseEvent
import kotlinx.serialization.json.buildJsonObject

fun withoutSessionInfo(event: BaseEvent): BaseEvent {
    event.context = buildJsonObject {
        event.context.forEach { (key, value) -> if (key !== "SessionId" && key !== "SessionStart") put(key, value) }
    }
    return event
}