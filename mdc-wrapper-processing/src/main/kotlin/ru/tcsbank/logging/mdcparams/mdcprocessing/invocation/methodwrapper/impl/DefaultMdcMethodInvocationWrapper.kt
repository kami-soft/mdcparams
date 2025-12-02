package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.methodwrapper.impl

import org.slf4j.MDC
import ru.tcsbank.logging.mdcparams.data.MdcData
import ru.tcsbank.logging.mdcparams.methodwrapper.MdcMethodInvocationWrapper

class DefaultMdcMethodInvocationWrapper : MdcMethodInvocationWrapper {
    override fun wrap(mdcFieldsData: Collection<MdcData>, block: () -> Any?): Any? {
        mdcFieldsData.forEach {
            MDC.put(it.fieldName, it.value.toString())
        }
        try {
            return block()
        } finally {
            mdcFieldsData.forEach {
                MDC.remove(it.fieldName)
            }
        }
    }
}
