package ru.tcsbank.logging.mdcparams.data

data class CombinedAnnotationAttributes(
    val allow: Boolean,
    val ignoreDuplicates: Boolean,
    val forceLogExceptions: Boolean,
)
