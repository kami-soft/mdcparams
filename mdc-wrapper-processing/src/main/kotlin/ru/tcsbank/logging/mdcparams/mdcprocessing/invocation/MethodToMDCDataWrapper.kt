package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation

import ru.tcsbank.logging.mdcparams.data.MdcData
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.MdcFieldMapper
import ru.tcsbank.logging.mdcparams.methodwrapper.MdcMethodInvocationWrapper

data class MethodToMDCDataWrapper(
    val argMappers: Collection<MdcFieldMapper>,
    val wrapperDelegate: MdcMethodInvocationWrapper,
) {
    fun wrap(args: Array<out Any?>?, block: () -> Any?): Any? {
        val mdcFieldsData = argsToMdcDataCollection(args)
        return wrapperDelegate.wrap(mdcFieldsData, block)
    }

    private fun argsToMdcDataCollection(args: Array<out Any?>?): Collection<MdcData> {
        return argMappers
            .mapNotNull { mapper ->
                val value = mapper.getValue(args)
                value?.let { MdcData(mapper.getFieldName(), it) }
            }
    }
}
