package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation

import mu.KLogging
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation

class MdcMethodInvocationInterceptor(
    private val proxiedMethodsData: Map<String, MethodToMDCDataWrapper>,
) : MethodInterceptor {
    override fun invoke(invocation: MethodInvocation): Any? {
        val methodDataWrapper = proxiedMethodsData[invocation.method.name]
        return if (methodDataWrapper != null) {
            methodDataWrapper.wrap(invocation.arguments) {
                invocation.proceed()
            }
        } else {
            invocation.proceed()
        }
    }

    companion object : KLogging()
}
