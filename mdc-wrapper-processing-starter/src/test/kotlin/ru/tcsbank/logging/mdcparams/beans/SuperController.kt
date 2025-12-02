package ru.tcsbank.logging.mdcparams.beans

import org.springframework.stereotype.Controller
import ru.tcsbank.logging.mdcparams.annotations.MDCEntityId
import ru.tcsbank.logging.mdcparams.annotations.MDCField
import ru.tcsbank.logging.mdcparams.annotations.MDCWrapper
import java.util.UUID

@MDCWrapper
interface SuperApi {
    fun foo(@MDCEntityId entityId: String, sample: Sample): String {
        throw NotImplementedError()
    }

    fun throwException(message: String) {
        throw NotImplementedError(message)
    }
}

@Controller
class SuperController : SuperApi {
    override fun foo(entityId: String, sample: Sample): String {
        return "foo" + entityId + sample.data + UUID.randomUUID()
    }
}

data class Sample(
    val id: Int,
    @get:MDCField("someField")
    val data: String,
) {
    fun bar() {
        println("bar")
    }
}
