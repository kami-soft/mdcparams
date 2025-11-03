package ru.kami.mdcparams.mdcprocessing.invocation

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation

class MdcMethodInvocationInterceptor(
    private val proxiedMethodsData: Map<String, MethodArgsToMDCDataMapper>,
) : MethodInterceptor {

    override fun invoke(invocation: MethodInvocation): Any? {
        val argsMapper = proxiedMethodsData[invocation.method.name]
        return if (argsMapper != null) {
            val mdcFieldsData = argsMapper.argsToMdcDataCollection(invocation.arguments)
            println(mdcFieldsData)
            invocation.proceed()
        } else {
            invocation.proceed()
        }
    }

}

