package ru.kami.mdcparams.mdcprocessing.invocation

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation

class MdcMethodInvocationInterceptor(
    private val proxiedMethodsData: Map<String, MethodParamsToMDCDataMapper>,
) : MethodInterceptor {

    override fun invoke(invocation: MethodInvocation): Any? {
        val paramsMapper = proxiedMethodsData[invocation.method.name]
        return if (paramsMapper != null) {
            val mdcParameterValues = paramsMapper.argsToMdcDataCollection(invocation.arguments)
            println(mdcParameterValues)
            invocation.proceed()
        } else {
            invocation.proceed()
        }
    }

}

