package ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl

import ru.kami.mdcparams.mdcprocessing.invocation.mappers.MdcFieldMapper

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