package ru.kami.mdcparams.mdcprocessing.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class MDCWrapper(
    val allow: Boolean = true,
)
