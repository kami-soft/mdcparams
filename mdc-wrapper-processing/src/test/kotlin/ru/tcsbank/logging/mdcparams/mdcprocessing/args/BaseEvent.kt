package ru.tcsbank.logging.mdcparams.mdcprocessing.args

import ru.tcsbank.logging.mdcparams.annotations.MDCEntityId

open class BaseEvent<T> (
    @get:MDCEntityId
    val id: T
)

class LongEvent(id: Long) : BaseEvent<Long>(id)
