package ru.tcsbank.logging.mdcparams.mdcprocessing.args

import ru.tcsbank.logging.mdcparams.annotations.MDCField

class TestClass : TestBaseClass() {
    @get:MDCField("field3")
    var field3: String? = null
}
