package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl

import org.springframework.expression.spel.standard.SpelExpressionParser
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.MdcFieldMapper

class MethodArgSpElToMdcFieldMapper(
    private val fieldName: String,
    argIndex: Int,
    spElExpression: String,
) : MdcFieldMapper {
    private val argumentExtractor = MethodArgToMdcFieldMapper(fieldName, argIndex)
    private val expression = SpelExpressionParser().parseExpression(spElExpression)

    override fun getFieldName(): String {
        return fieldName
    }

    override fun getValue(args: Array<out Any?>?): Any? {
        val argument = argumentExtractor.getValue(args) ?: return null
        return expression.getValue(argument)
    }
}
