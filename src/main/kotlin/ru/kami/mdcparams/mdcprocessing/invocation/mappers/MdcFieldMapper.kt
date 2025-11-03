package ru.kami.mdcparams.mdcprocessing.invocation.mappers

interface MdcFieldMapper {
    fun getFieldName(): String
    fun getValue(args: Array<out Any?>?): Any?
}