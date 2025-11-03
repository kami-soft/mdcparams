package ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl

import ru.kami.mdcparams.mdcprocessing.invocation.mappers.MdcFieldMapper
import java.lang.reflect.Field

class MethodArgFieldToMdcFieldMapper(
    private val fieldName: String,
    argumentIndex: Int,
    private val methodArgField: Field,
) : MdcFieldMapper {
    private val argumentExtractor = MethodArgToMdcFieldMapper(fieldName, argumentIndex)

    override fun getFieldName(): String {
        return fieldName
    }

    override fun getValue(args: Array<out Any?>?): Any? {
        val arg = argumentExtractor.getValue(args) ?: return null
        return methodArgField.get(arg)
    }
}