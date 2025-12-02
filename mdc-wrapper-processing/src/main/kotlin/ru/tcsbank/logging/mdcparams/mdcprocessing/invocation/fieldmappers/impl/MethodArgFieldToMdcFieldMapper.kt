package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl

import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.MdcFieldMapper
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
