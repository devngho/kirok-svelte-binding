package io.github.devngho.kiroksvelte

import io.github.devngho.kirok.binding.Binding
import io.github.devngho.kirok.binding.BindingModel
import java.io.File
import java.nio.file.Path


@Suppress("unused")
class KirokSvelteBinding: Binding {
    override suspend fun create(buildDir: Path, models: List<BindingModel>) {
        models.forEach { createModelFile(buildDir, it) }
    }

    private fun createModelFile(buildDir: Path, model: BindingModel) {
        val modelSimpleName = model.name.split(".").last()
        val modelTemplate = javaClass.getClassLoader().getResourceAsStream("model.ts")!!.bufferedReader().readText()
        val modelFile = File(buildDir.toFile(), "${modelSimpleName}.ts")

        try {
            modelFile.createNewFile()
        } catch (_: Exception) { }

        val intents = buildString {
            model.intents.forEach { (t, u) ->
                val paramsWithType = u.toList().subList(1, u.count()).joinToString(", ") {
                    "${it.first}: ${TypeGenerator.convertType(it.second)}"
                }
                val params = (u.toList().subList(1, u.count())).joinToString(", ") {
                    it.first
                }

                val paramsWithValue = if (u.count() == 1) "serialize(n)" else "serialize(n), serializeArgs([$params])"
                if (t.startsWith("SUSPEND_")) {
                    this.append(
                        """${t.removePrefix("SUSPEND_")}: ($paramsWithType) => 
                        |new Promise<void>(async (resolve, reject) => { 
                        |  try { 
                        |    const n = get(_${modelSimpleName}instance)
                        |    //@ts-expect-error
                        |    _${modelSimpleName}instance.set(deserialize(await instance.${t.removePrefix("SUSPEND_")}${modelSimpleName}(${paramsWithValue}))); 
                        |    resolve() 
                        |  } catch (e) { reject(e) }
                        |}),""".trimMargin()
                    )
                } else {
                    // is function not suspend
                    this.append(
                        """${t}: ($paramsWithType) => 
                        |new Promise<void>((resolve, reject) => { 
                        |  _${modelSimpleName}instance.update(n => {
                        |  //@ts-expect-error
                        |    try { return deserialize(instance.$t${modelSimpleName}(${paramsWithValue})) } 
                        |    catch (e) { reject(e) }
                        |  }); 
                        |  resolve() 
                        |}),""".trimMargin()
                    )
                }
            }
        }

        val modelValueType =
            TypeGenerator.createValueType(model).map { (k, v) -> "$k: $v" }.joinToString(", ").run { "{$this}" }

        val initFunctionParamsWithType =
            model.init.map { (k, v) -> "$k: ${TypeGenerator.convertType(v)}" }.joinToString(", ")
        val initFunctionParams = model.init.map { (k, _) -> k }.joinToString(", ")

        modelFile.bufferedWriter().use {
            it.write(
                modelTemplate
                    .replace("%modelname%", modelSimpleName)
                    .replace("%modelinittype%", initFunctionParamsWithType)
                    .replace("%modelinitargs%", initFunctionParams)
                    .replace("%modelawait%", if (model.isInitSuspend) "await " else "")
                    .replace("%modelasync%", if (model.isInitSuspend) "async " else "")
                    .replace(
                        "%modellazytype%",
                        "[Writable<Partial<$modelValueType>>, Partial<{${
                            model.intents.toList().joinToString(", ")
                            { j ->
                                "${j.first.removePrefix("SUSPEND_")}: (${
                                    j.second.toList().subList(1, j.second.count()).joinToString(", ")
                                    { type ->
                                        "${type.first}: ${TypeGenerator.convertType(type.second)}"
                                    }
                                }) => Promise<void>"
                            }
                        }}>]"
                    )
                    .replace(
                        "%modelpromisetype%",
                        "[Writable<$modelValueType>, {${
                            model.intents.toList().joinToString(", ")
                            { j ->
                                "${j.first.removePrefix("SUSPEND_")}: (${
                                    j.second.toList().subList(1, j.second.count()).joinToString(", ")
                                    { type ->
                                        "${type.first}: ${TypeGenerator.convertType(type.second)}"
                                    }
                                }) => Promise<void>"
                            }
                        }}]".let { type ->
                            if (model.isInitSuspend) "Promise<${type}>" else type
                        }
                    )
                    .replace("%modelintent%", intents)
            )
        }
    }
}