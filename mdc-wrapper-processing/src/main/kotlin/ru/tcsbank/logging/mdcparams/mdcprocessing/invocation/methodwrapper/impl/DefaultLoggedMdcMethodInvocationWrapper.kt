package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.methodwrapper.impl

import mu.KLogging
import org.slf4j.MDC
import ru.tcsbank.logging.mdcparams.data.MdcData
import ru.tcsbank.logging.mdcparams.methodwrapper.MdcMethodInvocationWrapper

class DefaultLoggedMdcMethodInvocationWrapper : MdcMethodInvocationWrapper {
    @Suppress("TooGenericExceptionCaught")
    override fun wrap(mdcFieldsData: Collection<MdcData>, block: () -> Any?): Any? {
        mdcFieldsData.forEach {
            MDC.put(it.fieldName, it.value.toString())
        }
        try {
            return block()
        } catch (e: Throwable) {
            logger.error(e) {
                "method throw exception: $e"
            }
            throw e
        } finally {
            mdcFieldsData.forEach {
                MDC.remove(it.fieldName)
            }
        }
    }

    companion object : KLogging()
}
