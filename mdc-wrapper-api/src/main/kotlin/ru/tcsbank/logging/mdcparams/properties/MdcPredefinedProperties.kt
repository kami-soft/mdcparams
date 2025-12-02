package ru.tcsbank.logging.mdcparams.properties

interface MdcPredefinedProperties {
    /**
     * Список аннотаций на уровне класса, означающих, что методы бина
     * потенциально подлежат оборачиванию в LogContext
     *
     * Аналог простановки аннотации @MdcWrapper
     */
    val predefinedAnnotations: Collection<Class<out Annotation>>
        get() = emptySet()

    /**
     * Список классов/интерфейсов бинов, означающих, что бин - кандидат в
     * оборачивание в LogContext
     *
     * Аналог простановки аннотации @MdcWrapper
     */
    val predefinedClasses: Collection<Class<*>>
        get() = emptySet()
}
