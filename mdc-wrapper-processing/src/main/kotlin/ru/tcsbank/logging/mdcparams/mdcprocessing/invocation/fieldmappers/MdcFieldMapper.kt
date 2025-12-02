package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers

interface MdcFieldMapper {
    fun getFieldName(): String
    fun getValue(args: Array<out Any?>?): Any?
}
