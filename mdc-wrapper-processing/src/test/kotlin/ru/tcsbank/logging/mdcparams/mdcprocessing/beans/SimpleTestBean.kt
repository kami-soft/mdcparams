package ru.tcsbank.logging.mdcparams.mdcprocessing.beans

import ru.tcsbank.logging.mdcparams.annotations.MDCEntityId
import ru.tcsbank.logging.mdcparams.annotations.MDCWrapper
import ru.tcsbank.logging.mdcparams.annotations.MdcAllowance
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestBaseClass
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestClass
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestDataClass
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestInterface

@MDCWrapper
@Suppress("UnusedParameter")
open class SimpleTestBean {
    fun methodWithoutAnnotatedParams(entityId: String?, someValue: Map<String, String>): String {
        return "methodWithoutAnnotatedParam $entityId"
    }

    @MDCWrapper(allow = MdcAllowance.DISALLOW)
    fun deniedMethod(@MDCEntityId applicationId: String) {
        println("deniedMethod")
    }

    open fun methodWithDirectParam(@MDCEntityId applicationId: String): String {
        return "methodWithDirectParam $applicationId"
    }

    fun methodWithParamFromDataClass(@MDCEntityId applicationId: String, dataClass: TestDataClass): String {
        return "methodWithParamFromDataClass ${dataClass.field1}"
    }

    fun methodWithParamFromInterface(@MDCEntityId applicationId: String, testInterface: TestInterface): String {
        return "methodWithParamFromInterface ${testInterface.getValue()}"
    }

    fun methodWithParamFromBaseClass(@MDCEntityId applicationId: String, testBaseClass: TestBaseClass): String {
        return "methodWithParamFromBaseClass ${testBaseClass.field1}"
    }

    fun methodWithParamFromSubClass(@MDCEntityId applicationId: String, testClass: TestClass): String {
        return "methodWithParamFromSubClass ${testClass.field3}"
    }
}

@Suppress("UnusedParameter")
class WrongSimpleTestBean : SimpleTestBean() {
    fun methodWithAmbiguousAnnotatedParams(
        @MDCEntityId applicationId: String,
        dataClass: TestDataClass,
        dataClass2: TestDataClass
    ): String {
        return "methodWithParamFromDataClass ${dataClass.field1}"
    }

    @MDCWrapper(ignoreDuplicates = MdcAllowance.ALLOW)
    fun methodWithAmbiguousAnnotatedParamsAllowed(
        @MDCEntityId applicationId: String,
        dataClass: TestDataClass,
        dataClass2: TestDataClass
    ): String {
        return "methodWithParamFromDataClass ${dataClass.field1}"
    }
}
