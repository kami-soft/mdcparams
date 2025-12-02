package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl

import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.MdcFieldMapper
import java.lang.reflect.Method

class MethodNameToMdcFieldMapper(
    method: Method
) : MdcFieldMapper {
    private val methodName: String = "${method.declaringClass.simpleName}.${method.name}"

    override fun getFieldName(): String {
        return FIELD_NAME
    }

    override fun getValue(args: Array<out Any?>?): Any {
        return methodName
    }

    companion object {
        const val FIELD_NAME = "methodName"
    }
}
