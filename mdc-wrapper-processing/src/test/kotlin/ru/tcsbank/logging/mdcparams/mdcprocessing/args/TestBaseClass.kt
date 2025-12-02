package ru.tcsbank.logging.mdcparams.mdcprocessing.args

import ru.tcsbank.logging.mdcparams.annotations.MDCField

open class TestBaseClass {
    @get:MDCField("field1")
    var field1: Long? = null

    @get:MDCField("field2")
    var field2: String? = null
}
