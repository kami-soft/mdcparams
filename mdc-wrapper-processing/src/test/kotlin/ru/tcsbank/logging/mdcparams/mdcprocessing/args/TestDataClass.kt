package ru.tcsbank.logging.mdcparams.mdcprocessing.args

import ru.tcsbank.logging.mdcparams.annotations.MDCField

data class TestDataClass(
    @get:MDCField("field1")
    val field1: String?,
    @get:MDCField("field2")
    val field2: Boolean,
)
