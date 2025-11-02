package ru.kami.mdcparams.test

import org.springframework.stereotype.Component
import ru.kami.mdcparams.mdcprocessing.annotations.MDCWrapper
import ru.kami.mdcparams.mdcprocessing.annotations.MDCEntityId
import ru.kami.mdcparams.mdcprocessing.annotations.MDCField

@Component
@MDCWrapper
class MdcService {
    fun foo(@MDCEntityId entityId: String, sample: Sample): String{
        println(entityId)
        return entityId+"wer"
    }
}

data class Sample(
    val id: Int,
    @get:MDCField("someField")
    val data: String,
)