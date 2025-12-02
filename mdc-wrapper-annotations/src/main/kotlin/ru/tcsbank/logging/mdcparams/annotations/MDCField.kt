package ru.tcsbank.logging.mdcparams.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.VALUE_PARAMETER
)
@Repeatable
annotation class MDCField(
    /**
     * Имя поля в MDC, например - entityId, subEntityId...
     */
    val mdcFieldName: String,
    /**
     * SpEL выражение, которое будет использоваться для получения значения поля из аргумента метода
     * Если не указано, то будет использоваться значение поля из аргумента метода
     *
     * Может использоваться для получения значений из методов класса-параметра, если эти методы не являются геттерами
     * Например, если параметром метода выступает `Map<String, String>`, то можно получить значение по ключу `"methodParam['key']"`
     */
    val spEl: String = "",
)
