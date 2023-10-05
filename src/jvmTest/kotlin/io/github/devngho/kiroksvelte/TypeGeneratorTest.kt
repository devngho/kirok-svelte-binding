package io.github.devngho.kiroksvelte

import io.github.devngho.kirok.binding.Binding
import io.kotest.common.ExperimentalKotest
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.reflect.full.starProjectedType

@OptIn(ExperimentalKotest::class)
class TypeGeneratorTest : BehaviorSpec({
    given("TypeGenerator") {
        `when`("convertType") {
            then("기본 타입을 올바르게 변환한다") {
                TypeGenerator.convertType(Int::class.starProjectedType) shouldBe "number"
                TypeGenerator.convertType(String::class.starProjectedType) shouldBe "string"
                TypeGenerator.convertType(Boolean::class.starProjectedType) shouldBe "boolean"
                TypeGenerator.convertType(Double::class.starProjectedType) shouldBe "number"
                TypeGenerator.convertType(Float::class.starProjectedType) shouldBe "number"
                TypeGenerator.convertType(Long::class.starProjectedType) shouldBe "number"
                TypeGenerator.convertType(Short::class.starProjectedType) shouldBe "number"
                TypeGenerator.convertType(Byte::class.starProjectedType) shouldBe "number"
                TypeGenerator.convertType(Char::class.starProjectedType) shouldBe "string"
                TypeGenerator.convertType(List::class.starProjectedType) shouldBe "any[]"
                TypeGenerator.convertType(Map::class.starProjectedType) shouldBe "Map<any, any>"
                TypeGenerator.convertType(Set::class.starProjectedType) shouldBe "Set<any>"
                TypeGenerator.convertType(Collection::class.starProjectedType) shouldBe "any[]"
                TypeGenerator.convertType(MutableList::class.starProjectedType) shouldBe "any[]"
                TypeGenerator.convertType(MutableMap::class.starProjectedType) shouldBe "Map<any, any>"
                TypeGenerator.convertType(MutableSet::class.starProjectedType) shouldBe "Set<any>"
                TypeGenerator.convertType(MutableCollection::class.starProjectedType) shouldBe "any[]"
            }
            then("데이터 클래스를 올바르게 변환한다") {
                data class TestClass(val a: Int, val b: String)
                TypeGenerator.convertType(TestClass::class.starProjectedType) shouldBe "{a: number, b: string}"
            }
            then("중첩된 데이터 클래스를 올바르게 변환한다") {
                data class TestClass(val a: Int, val b: String)
                data class TestClass2(val a: Int, val b: String, val c: TestClass)

                TypeGenerator.convertType(TestClass2::class.starProjectedType) shouldBe "{a: number, b: string, c: {a: number, b: string}}"
            }
        }

        given("Binding.BindingModel") {
            data class TestClass(val a: Int, val b: String)
            data class TestClass2(val a: Int, val b: String, val c: TestClass)

            val model = Binding.BindingModel(
                "io.github.devngho.kiroksvelte.TestModel",
                mapOf(
                    "testPrimitive" to String::class,
                    "testModels" to TestClass2::class
                ),
                mapOf(
                    "testPrimitive" to listOf(
                        String::class,
                        Boolean::class
                    ),
                    "testModels" to listOf(
                        TestClass2::class
                    )
                )
            )

            `when`("values") {
                then("값을 올바르게 변환한다") {
                    TypeGenerator.createValueType(model) shouldBe mapOf(
                        "testPrimitive" to "string",
                        "testModels" to "{a: number, b: string, c: {a: number, b: string}}"
                    )
                }
            }
        }
    }
})