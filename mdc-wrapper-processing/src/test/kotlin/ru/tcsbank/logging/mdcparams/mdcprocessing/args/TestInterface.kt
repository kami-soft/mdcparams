package ru.tcsbank.logging.mdcparams.mdcprocessing.args

import ru.tcsbank.logging.mdcparams.annotations.MDCField

interface TestInterface {
    @MDCField("field")
    fun getValue(): String
}
