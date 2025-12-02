package ru.tcsbank.logging.mdcparams.mdcprocessing.beans

import ru.tcsbank.logging.mdcparams.annotations.MDCWrapper
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.BaseEvent

@MDCWrapper
open class TestBeanWithGenericType<T> {
    open fun foo(event: BaseEvent<T>) {
    }
}

class TestBeanWithLongEvent : TestBeanWithGenericType<Long>() {
    override fun foo(event: BaseEvent<Long>) {
        println(event.id)
    }
}
