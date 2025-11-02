package ru.kami.mdcparams.mdcprocessing.invocation

data class MethodParamsToMDCDataMapper(
    private val paramMappers: Collection<ArgumentsToMdcParamMapper>
) {
    fun argsToMdcDataCollection(args: Array<out Any?>?): Collection<MdcData> {
        return paramMappers
            .mapNotNull { mapper -> mapper.getValue(args)}
    }
}