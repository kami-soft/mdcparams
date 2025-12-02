package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl

import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.MdcFieldMapper
import java.lang.reflect.Method

class MethodArgGetterToMdcFieldMapper(
    private val fieldName: String,
    argIndex: Int,
    private val argGetter: Method,
) : MdcFieldMapper {
    private val argumentExtractor = MethodArgToMdcFieldMapper(fieldName, argIndex)

    override fun getFieldName(): String {
        return fieldName
    }

    override fun getValue(args: Array<out Any?>?): Any? {
        val arg = argumentExtractor.getValue(args) ?: return null
        return argGetter.invoke(arg)
    }
}
