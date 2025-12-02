package ru.tcsbank.logging.mdcparams.mdcprocessing.beans

import ru.tcsbank.logging.mdcparams.annotations.MDCEntityId
import ru.tcsbank.logging.mdcparams.annotations.MDCWrapper
import ru.tcsbank.logging.mdcparams.annotations.MdcAllowance

@MDCWrapper
@Suppress("UnusedParameter")
open class TestBeanWithoutAnnotations {
    fun methodWithoutAnnotatedParams(entityId: String?, someValue: Map<String, String>): String {
        return "methodWithoutAnnotatedParam $entityId"
    }

    @MDCWrapper(allow = MdcAllowance.DISALLOW)
    fun methodWithAnnotatedParams(@MDCEntityId applicationId: String) {
        println("methodWithAnnotatedParams $applicationId")
    }
}
