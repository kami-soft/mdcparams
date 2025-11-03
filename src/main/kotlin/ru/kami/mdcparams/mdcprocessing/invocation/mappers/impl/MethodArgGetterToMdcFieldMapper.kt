package ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl

import ru.kami.mdcparams.mdcprocessing.invocation.mappers.MdcFieldMapper
import java.lang.reflect.Method

class MethodArgGetterToMdcFieldMapper(
    private val paramName: String,
    argIndex: Int,
    private val argGetter: Method,
): MdcFieldMapper {
    private val argumentExtractor = MethodArgToMdcFieldMapper(paramName, argIndex)

    override fun getFieldName(): String {
        return paramName
    }

    override fun getValue(args: Array<out Any?>?): Any? {
        val arg = argumentExtractor.getValue(args) ?: return null
        return argGetter.invoke(arg)
    }
}