package ru.kami.mdcparams.mdcprocessing.invocation

fun interface ArgumentsToMdcParamMapper {
    fun getValue(args: Array<out Any?>?): MdcData?
}

data class MdcData(
    val fieldName: String,
    val value: Any
)