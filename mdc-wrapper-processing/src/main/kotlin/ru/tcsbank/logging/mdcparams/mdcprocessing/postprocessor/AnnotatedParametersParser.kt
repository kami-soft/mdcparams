package ru.tcsbank.logging.mdcparams.mdcprocessing.postprocessor

import org.springframework.core.annotation.AnnotatedElementUtils
import ru.tcsbank.logging.mdcparams.annotations.MDCField
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.MdcFieldMapper
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl.MethodArgFieldToMdcFieldMapper
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl.MethodArgGetterToMdcFieldMapper
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl.MethodArgToMdcFieldMapper
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Parameter

class AnnotatedParametersParser {
    fun parseMethodParameters(method: Method): List<MdcFieldMapper> {
        return parseArgumentsDirectAnnotations(method).plus(parseMethodComplexParameters(method))
    }

    private fun parseArgumentsDirectAnnotations(method: Method): List<MdcFieldMapper> {
        val allMethodDeclarations = getAllMethodAncestors(method)
        val argumentMappers = allMethodDeclarations
            .flatMap {
                it.parameters
                    .flatMapIndexed { index, parameter -> parseAnnotatedParameter(index, parameter) }
            }
        return argumentMappers
    }

    private fun parseMethodComplexParameters(method: Method): List<MdcFieldMapper>{
        return method.parameters
            .flatMapIndexed { index, parameter -> parseComplexParameter(index, parameter) }

    }

    internal fun parseComplexParameter(parameterIndex: Int, parameter: Parameter): Collection<MdcFieldMapper> {
        // Внутри объекта может быть несколько полей, помеченных разными MDC-аннотациями, надо смаппить все
        val parameterType = parameter.type

        val fieldsInParameterMappers = parameterType
            .fields
            .flatMap { field -> mapAnnotatedParameterField(parameterIndex, field) }

        val gettersInParameterMappers = parameterType
            .methods
            .filter { method -> method.parameterCount == 0 && method.returnType != Void.TYPE }
            .flatMap { method -> mapAnnotatedGetter(parameterIndex, method) }

        return fieldsInParameterMappers.plus(gettersInParameterMappers)
    }

    internal fun parseAnnotatedParameter(parameterIndex: Int, parameter: Parameter): Collection<MdcFieldMapper> {
        return constructElementMapper(parameterIndex, parameter) { annotation, argumentIndex ->
            if (annotation.spEl.isNotEmpty()) {
                ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl.MethodArgSpElToMdcFieldMapper(
                    annotation.mdcFieldName,
                    argumentIndex,
                    annotation.spEl
                )
            } else {
                MethodArgToMdcFieldMapper(annotation.mdcFieldName, argumentIndex)
            }
        }
    }

    private fun mapAnnotatedGetter(parameterIndex: Int, getter: Method): Collection<MdcFieldMapper> {
        return constructElementMapper(parameterIndex, getter) { annotation, argumentIndex ->
            MethodArgGetterToMdcFieldMapper(annotation.mdcFieldName, argumentIndex, getter)
        }
    }

    private fun mapAnnotatedParameterField(parameterIndex: Int, field: Field): Collection<MdcFieldMapper> {
        return constructElementMapper(parameterIndex, field) { annotation, argumentIndex ->
            MethodArgFieldToMdcFieldMapper(annotation.mdcFieldName, argumentIndex, field)
        }
    }

    private fun getAllMethodAncestors(method: Method): List<Method> {
        val classMethods = getAllRelatedMethodsFromClassAncestors(method)
        val interfaceMethods = getAllRelatedMethodsFromInterfaces(method)

        return classMethods + interfaceMethods
    }

    private fun getAllRelatedMethodsFromClassAncestors(sourceMethod: Method): List<Method> {
        val methods = mutableListOf<Method>()
        var currentClazz: Class<*>? = sourceMethod.declaringClass
        while (currentClazz != null) {
            val methodInClazz = findMethodInClass(currentClazz, sourceMethod)
            if (methodInClazz != null) {
                methods.add(methodInClazz)
                currentClazz = currentClazz.superclass
            } else {
                currentClazz = null
            }
        }

        return methods
    }

    private fun getAllRelatedMethodsFromInterfaces(sourceMethod: Method): List<Method> {
        return getAllClassInterfacesWithMethod(sourceMethod.declaringClass, sourceMethod)
            .mapNotNull { findMethodInClass(it, sourceMethod) }
    }

    private fun getAllClassInterfacesWithMethod(clazz: Class<*>, sourceMethod: Method): Set<Class<*>> {
        val interfaces = mutableSetOf<Class<*>>()
        if (clazz.isInterface) {
            if (findMethodInClass(clazz, sourceMethod) != null) {
                interfaces.add(clazz)
                interfaces.addAll(clazz.interfaces.flatMap { getAllClassInterfacesWithMethod(it, sourceMethod) })
            }
        } else {
            interfaces.addAll(clazz.interfaces.flatMap { getAllClassInterfacesWithMethod(it, sourceMethod) })
        }

        return interfaces
    }

    private fun findMethodInClass(clazz: Class<*>, sourceMethod: Method): Method? {
        return runCatching {
            // пробуем найти метод с такими же параметрами
            clazz.getDeclaredMethod(sourceMethod.name, *sourceMethod.parameterTypes)
        }.getOrElse {
            // не удалось, поищем дженериковский
            clazz.declaredMethods
                .firstOrNull { method -> isMethodOverridden(method, sourceMethod) }
        }
    }

    private fun constructElementMapper(
        parameterIndex: Int,
        element: AnnotatedElement,
        mapperConstructor: (annotation: MDCField, argumentIndex: Int) -> MdcFieldMapper?
    ): Collection<MdcFieldMapper> {
        val annotations = AnnotatedElementUtils.findMergedRepeatableAnnotations(element, MDCField::class.java)
        return annotations.mapNotNull { mapperConstructor(it, parameterIndex) }
    }

    private fun isMethodOverridden(method: Method, sourceMethod: Method): Boolean {
        if (method.name != sourceMethod.name || method.parameterCount != sourceMethod.parameterCount) {
            return false
        }

        val clazz = method.declaringClass
        val sourceClazz = sourceMethod.declaringClass
        if (!clazz.isAssignableFrom(sourceClazz) || !canMethodBeOverridden(method)) {
            return false
        }

        val methodParameters = method.parameters
        val sourceMethodParameters = sourceMethod.parameters
        return methodParameters.indices.all {
            methodParameters[it].type.isAssignableFrom(sourceMethodParameters[it].type)
        }
    }

    private fun canMethodBeOverridden(method: Method): Boolean {
        return !(Modifier.isFinal(method.modifiers) ||
                Modifier.isPrivate(method.modifiers) ||
                Modifier.isStatic(method.modifiers)
                )
    }
}