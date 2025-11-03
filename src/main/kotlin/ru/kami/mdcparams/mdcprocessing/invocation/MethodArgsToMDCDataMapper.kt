package ru.kami.mdcparams.mdcprocessing.invocation

import ru.kami.mdcparams.mdcprocessing.invocation.mappers.MdcFieldMapper

data class MethodArgsToMDCDataMapper(
    private val argMappers: Collection<MdcFieldMapper>
) {
    fun argsToMdcDataCollection(args: Array<out Any?>?): Collection<MdcData> {
        return argMappers
            .mapNotNull { mapper ->
                val value = mapper.getValue(args)
                value?.let { MdcData(mapper.getFieldName(), it) }
            }
    }
}

data class MdcData(
    val fieldName: String,
    val value: Any
)