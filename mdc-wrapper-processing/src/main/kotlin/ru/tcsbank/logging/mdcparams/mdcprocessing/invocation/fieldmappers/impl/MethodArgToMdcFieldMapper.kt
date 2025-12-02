package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl

import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.MdcFieldMapper

class MethodArgToMdcFieldMapper(
    private val fieldName: String,
    private val argIndex: Int,
) : MdcFieldMapper {
    override fun getFieldName(): String {
        return fieldName
    }

    override fun getValue(args: Array<out Any?>?): Any? {
        return if (!args.isNullOrEmpty() && args.size > argIndex) {
            args[argIndex]
        } else {
            null
        }
    }
}
