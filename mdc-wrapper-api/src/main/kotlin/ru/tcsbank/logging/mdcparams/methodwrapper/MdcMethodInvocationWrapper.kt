package ru.tcsbank.logging.mdcparams.methodwrapper

import ru.tcsbank.logging.mdcparams.data.MdcData

interface MdcMethodInvocationWrapper {
    fun wrap(mdcFieldsData: Collection<MdcData>, block: () -> Any?): Any?
}
