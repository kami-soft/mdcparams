package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.methodwrapper.impl

import ru.tcsbank.logging.mdcparams.data.CombinedAnnotationAttributes
import ru.tcsbank.logging.mdcparams.methodwrapper.MdcMethodInvocationWrapper
import ru.tcsbank.logging.mdcparams.methodwrapper.MdcMethodInvocationWrapperFactory
import java.lang.reflect.Method
import java.util.Collections

class DefaultMdcMethodInvocationWrapperFactory : MdcMethodInvocationWrapperFactory {
    private val wrapperInstances = Collections.synchronizedMap(mutableMapOf<Boolean, MdcMethodInvocationWrapper>())

    override fun obtain(annotationAttributes: CombinedAnnotationAttributes, method: Method): MdcMethodInvocationWrapper {
        return wrapperInstances.computeIfAbsent(annotationAttributes.forceLogExceptions) {
            if (annotationAttributes.forceLogExceptions) {
                DefaultLoggedMdcMethodInvocationWrapper()
            } else {
                DefaultMdcMethodInvocationWrapper()
            }
        }
    }
}
