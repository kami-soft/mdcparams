package ru.kami.mdcparams.mdcprocessing.invocation

import ru.kami.mdcparams.mdcprocessing.invocation.mappers.MdcFieldMapper

data class MethodArgsToMDCDataMapper(
    private val paramMappers: Collection<MdcFieldMapper>
) {
    fun argsToMdcDataCollection(args: Array<out Any?>?): Collection<MdcData> {
        return paramMappers
            .mapNotNull { mapper ->
                val value = mapper.getValue(args)
                if (value != null) {
                    MdcData(mapper.getFieldName(), value)
                } else {
                    null
                }
            }
    }
}

data class MdcData(
    val fieldName: String,
    val value: Any
)