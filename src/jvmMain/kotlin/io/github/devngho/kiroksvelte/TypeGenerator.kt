package io.github.devngho.kiroksvelte

import io.github.devngho.kirok.binding.Binding
import kotlin.reflect.KType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

object TypeGenerator {
    fun convertType(clazz: KType): String = when (clazz.jvmErasure.qualifiedName) {
        // kotlin primitive types
        "kotlin.Int" -> "number"
        "kotlin.String" -> "string"
        "kotlin.Boolean" -> "boolean"
        "kotlin.Double" -> "number"
        "kotlin.Float" -> "number"
        "kotlin.Long" -> "number"
        "kotlin.Short" -> "number"
        "kotlin.Byte" -> "number"
        "kotlin.Char" -> "string"
        "kotlin.collections.List" -> "any[]"
        "kotlin.collections.Map" -> "Map<any, any>"
        "kotlin.collections.Set" -> "Set<any>"
        "kotlin.collections.Collection" -> "any[]"
        "kotlin.collections.MutableList" -> "any[]"
        "kotlin.collections.MutableMap" -> "Map<any, any>"
        "kotlin.collections.MutableSet" -> "Set<any>"
        "kotlin.collections.MutableCollection" -> "any[]"
        else -> {
            if (clazz.jvmErasure.isData) {
                "{" + clazz.jvmErasure.memberProperties.joinToString(separator = ", ") { "${it.name}: ${convertType(it.returnType)}" } + "}"
            } else "any"
        }
    }

    fun createValueType(model: Binding.BindingModel): Map<String, String> {
        return model.values.mapValues {
            convertType(it.value.starProjectedType)
        }
    }
}