package ru.tcsbank.logging.mdcparams.mdcprocessing.beans

import org.springframework.stereotype.Component
import ru.tcsbank.logging.mdcparams.annotations.MDCEntityId

interface TestGenericInterface<E> {
    fun foo(@MDCEntityId bar: E): String
}

@Component
class TestStringInterfaceImpl : TestGenericInterface<String> {
    override fun foo(bar: String): String {
        return bar
    }
}
