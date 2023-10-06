package io.github.devngho.kiroksvelte

import io.github.devngho.kirok.binding.Binding
import java.io.File
import java.nio.file.Path
import kotlin.reflect.full.starProjectedType


@Suppress("unused")
class KirokSvelteBinding: Binding {
    override suspend fun create(buildDir: Path, models: List<Binding.BindingModel>) {
        models.forEach { createModelFile(buildDir, it) }
    }

    private fun createModelFile(buildDir: Path, model: Binding.BindingModel) {
        val modelSimpleName = model.name.split(".").last()
        val modelTemplate = javaClass.getClassLoader().getResourceAsStream("model.ts")!!.bufferedReader().readText()
        val modelFile = File(buildDir.toFile(), "${modelSimpleName}.ts")

        try {
            modelFile.createNewFile()
        } catch (_: Exception) { }

        val intents = buildString {
            var i = 0
            model.intents.forEach { (t, u) ->
                var j = 0
                val paramsWithType = u.subList(1, u.count()).joinToString(", ") {
                    j += 1
                    return@joinToString "arg${j - 1}: ${TypeGenerator.convertType(it.starProjectedType)}"
                }
                j = 0
                val params = (u.subList(1, u.count())).joinToString(", ") {
                    j += 1
                    return@joinToString "arg${j - 1}"
                }
                val paramsWithValue = if (j == 0) "serialize(n)" else "serialize(n), $params"
                if (t.startsWith("SUSPEND_")) {
                    this.append(
                        """${t.removePrefix("SUSPEND_")}: ($paramsWithType) => 
                        |new Promise<void>(async (resolve, reject) => { 
                        |  try { 
                        |    const n = get(value)
                        |    value.set(deserialize(await instance.${t.removePrefix("SUSPEND_")}${modelSimpleName}(${paramsWithValue}))); 
                        |    resolve() 
                        |  } catch (e) { reject(e) }
                        |}),""".trimMargin()
                    )
                } else {
                    // is function not suspend
                    this.append(
                        """${t}: ($paramsWithType) => 
                        |new Promise<void>((resolve, reject) => { 
                        |  value.update(n => {
                        |    try { return deserialize(instance.$t${modelSimpleName}(${paramsWithValue})) } 
                        |    catch (e) { reject(e) }
                        |  }); 
                        |  resolve() 
                        |}),""".trimMargin()
                    )
                }
                 i += 1
            }
        }

        val modelValueType =
            TypeGenerator.createValueType(model).map { (k, v) -> "$k: $v" }.joinToString(", ").run { "{$this}" }

        modelFile.bufferedWriter().use {
            it.write(
                modelTemplate
                    .replace("%modelname%", modelSimpleName)
                    .replace(
                        "%modeltype%",
                        "[Writable<$modelValueType>, {${model.intents.keys.joinToString(", ") { j -> "${j.removePrefix("SUSPEND_")}: () => Promise<void>" }}}]"
                    )
                    .replace("%modelintent%", intents)
            )
        }
    }
}