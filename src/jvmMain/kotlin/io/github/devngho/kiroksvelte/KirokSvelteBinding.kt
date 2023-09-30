package io.github.devngho.kiroksvelte

import io.github.devngho.kirok.binding.Binding
import java.io.File
import java.nio.file.Path


@Suppress("unused")
class KirokSvelteBinding: Binding {
    override suspend fun create(buildDir: Path, models: List<Binding.BindingModel>) {
        models.forEach { createModelFile(buildDir, it) }
    }

    private fun createModelFile(buildDir: Path, model: Binding.BindingModel) {
//        val protoPath = Path.of(model.protoFilePath).absolute().normalize().pathString.replace("\\", "/")
//        val protoFolder = Path.of(model.protoFilePath).parent.absolute().normalize().pathString.replace("\\", "/")
//        val command = "protoc --js_out=import_style=commonjs,binary:${buildDir.absolute().normalize().pathString.replace("\\", "/")} $protoPath -I $protoFolder"

        val modelSimpleName = model.name.split(".").last()

//        Runtime.getRuntime().exec(command).apply {
//            inputStream.copyTo(System.out)
//            errorStream.copyTo(System.err)
//
//            waitFor()
//
//            buildDir.toFile().listFiles()!!
//                .filter { it.name.contains("pb") }
//                .forEach {
//                    // cjs -> esm
//
//                    val file = it.readText()
//                        .replace("var jspb = require('google-protobuf');", "import * as jspb from 'google-protobuf';")
//                        .replace("goog.object.extend(exports, proto);",  "")
//                        .replace("goog.exportSymbol('proto.${modelSimpleName}', null, global);", "export let $modelSimpleName = null;")
//                        .replace("proto.${modelSimpleName}", modelSimpleName)
//                        .replace("${modelSimpleName}.serializeBinaryToWriter", "${modelSimpleName}.prototype.serializeBinaryToWriter")
//                        .replace("${modelSimpleName}.deserializeBinaryToWriter", "${modelSimpleName}.deprototype.serializeBinaryToWriter")
//                        .replace("// GENERATED CODE -- DO NOT EDIT!", "// GENERATED CODE -- DO NOT EDIT!\n// CODE WAS EDITED BY KIROK-SVELTE-BINDING FOR SUPPORTING ESM")
//                    it.writeText(file)
//                }
//        }

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
                    return@joinToString "arg${j - 1}: any"
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

        modelFile.bufferedWriter().use {
            it.write(
                modelTemplate
                    .replace("%modelname%", modelSimpleName)
                    .replace("%modeltype%", "[Writable<any>, any]")
                    .replace("%modelintent%", intents)
//                    .replace("%imports%", "import {${modelSimpleName}} from './${modelSimpleName}_pb';")
//                    .replace("%protosetfields%", model.values.map { (t, _) ->
//                        "  if (v.set${t.lowercase().replaceFirstChar { c -> c.uppercase() }}List) v.set${t.lowercase().replaceFirstChar { c -> c.uppercase() }}List(value.${t}List);\n else v.set${t.lowercase().replaceFirstChar { c -> c.uppercase() }}(value.$t);\n"
//                    }.joinToString(""))
//                    .replace("%modelremap%", "{${
//                        model.values.map { (t, _) ->
//                            "${t}: obj.${t.lowercase()} ?? obj.${t.lowercase()}List"
//                        }.joinToString(", ")
//                    }}")
            )
        }
    }
}