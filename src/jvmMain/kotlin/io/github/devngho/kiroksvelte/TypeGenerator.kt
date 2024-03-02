package io.github.devngho.kiroksvelte

import io.github.devngho.kirok.binding.BindingModel
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.jvmErasure

object TypeGenerator {
    fun convertType(clazz: KType): String = when (clazz.jvmErasure.qualifiedName) {
        // kotlin primitive types
        "kotlin.Any" -> "any"
        "kotlin.Int", "kotlin.Double", "kotlin.Float", "kotlin.Long", "kotlin.Short", "kotlin.Byte" -> "number"
        "kotlin.Boolean" -> "boolean"
        "kotlin.String" -> "string"
        "kotlin.Char" -> "string"
        "kotlin.collections.List", "kotlin.collections.MutableList", "kotlin.collections.Collection", "kotlin.collections.MutableCollection" -> "${
            convertType(
                clazz.arguments.firstOrNull()?.type ?: Any::class.starProjectedType
            )
        }[]"

        "kotlin.collections.Map", "kotlin.collections.MutableMap" -> "Map<${convertType(clazz.arguments.firstOrNull()?.type ?: Any::class.starProjectedType)}, ${
            convertType(
                clazz.arguments.lastOrNull()?.type ?: Any::class.starProjectedType
            )
        }>"

        "kotlin.collections.Set", "kotlin.collections.MutableSet" -> "Set<${convertType(clazz.arguments.firstOrNull()?.type ?: Any::class.starProjectedType)}>"
        else -> {
            if (clazz.jvmErasure.isData) {
                // data class
                "{" + clazz.jvmErasure.memberProperties.joinToString(separator = ", ") { "${it.name}: ${convertType(it.returnType)}" } + "}"
            } else if (clazz.jvmErasure.isSubclassOf(Enum::class)) {
                val enumValues = clazz.jvmErasure.java.enumConstants
                enumValues?.joinToString(separator = " | ") { "\"$it\"" } ?: "any"
            } else "any"
        }
    }

    fun createValueType(model: BindingModel): Map<String, String> {
        return model.values.mapValues {
            convertType(it.value)
        }
    }
}