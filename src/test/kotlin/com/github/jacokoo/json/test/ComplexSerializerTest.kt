/*
 * The MIT License
 * Copyright (c) 2018 Jaco Koo <jaco.koo@guyong.in>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.jacokoo.json.test

import com.github.jacokoo.json.*
import io.kotlintest.be
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId

private val DEFAULT = SerializeContext(SerializeContext.defaults + SerializeContext.globals)


class Menu(var name: String, var parent: Menu? = null, var children: List<Menu> = listOf()) {

    fun getFoo(str: String): String = "foo"

    companion object {
        fun create(): Menu {
            var m1 = Menu("m1")
            var m2 = Menu("m2", m1)
            var m3 = Menu("m3", m1)
            var m4 = Menu("m4", m2)

            m1.children = listOf(m2, m3)
            m2.children = listOf(m4)

            return m1
        }

        @JvmStatic
        fun getDemo(value: String): String? = null
    }
}

class ComplexSerializerTest: FreeSpec({
    fun write(block: (Output) -> Unit): String =
        DefaultOutput().also(block).toString()

    "ArraySerializer" - {
        "first level is included by default" {
            val date = LocalDate.now()
            write {
                ArraySerializer(
                    DEFAULT,
                    PathMatcher.create(),
                    Path()
                ).write(it, arrayOf(1,2.toBigInteger(),3.toBigDecimal(), date))
            } should be("[1,2,3,${date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()}]")
        }

        "serializer should be cached" {
            DEFAULT[BigDecimal::class.java] shouldBe null
            val output = DefaultOutput()
            var ser = ArraySerializer(DEFAULT, PathMatcher.create(), Path())
            ser.write(output, arrayOf(1.toBigDecimal()))

            val method = ComplexSerializer::class.java.getDeclaredMethod("get", Class::class.java, String::class.java)
            method.isAccessible = true
            val s1 = method.invoke(ser, BigDecimal::class.java, null)

            ser.write(output, arrayOf(100.toBigDecimal()))
            val s2 = method.invoke(ser, BigDecimal::class.java, null)
            s1 shouldBe s2
        }

        "empty object like item should be ignored" {
            write {
                ArraySerializer(DEFAULT, PathMatcher.create(), Path())
                    .write(it, arrayOf(1, "a", Menu("a")))
            } shouldBe """[1,"a"]"""

            write {
                ArraySerializer(DEFAULT, PathMatcher.create(), Path())
                    .write(it, arrayOf(1, "a", emptyMap<Int, Int>()))
            } shouldBe """[1,"a"]"""
        }

        "nested array" {
            write {
                ArraySerializer(DEFAULT, PathMatcher.create(), Path())
                    .write(it, arrayOf(1, "a", Menu("a"), arrayOf(2, 3, 4)))
            } shouldBe """[1,"a",[2,3,4]]"""
        }

        "primitive array" {
            val arr = IntArray(3, {it})
            write {
                ArraySerializer(DEFAULT, PathMatcher.create(), Path())
                    .write(it, arr)
            } shouldBe "[0,1,2]"

        }
    }

    "CollectionSerializer" - {
        "similar to array" {
            val arr = IntArray(3, {it})
            write {
                CollectionSerializer(DEFAULT, PathMatcher.create(), Path())
                    .write(it, listOf(arr, 3, listOf(4, 5, 6)))
            } shouldBe "[[0,1,2],3,[4,5,6]]"
        }
    }

    "MapSerializer" - {
        "only included keys should be serialized" {
            write {
                MapSerializer(DEFAULT, PathMatcher.create("a.1"), Path())
                    .write(it, mapOf("a" to mapOf(1 to 2), "b" to 2))
            } shouldBe """{"a":{"1":2}}"""
        }

        "null should be ignored" {
            write {
                MapSerializer(DEFAULT, PathMatcher.create("a"), Path())
                    .write(it, mapOf("a" to null, null to 2, null to null))
            } shouldBe "{}"
        }

        "object in map should be ok" {
            write {
                MapSerializer(DEFAULT, PathMatcher.create("(a, menu).(name, children).name"), Path())
                    .write(it, mapOf("a" to 1, "menu" to Menu.create()))
            } shouldBe """{"a":1,"menu":{"name":"m1","children":[{"name":"m2"},{"name":"m3"}]}}"""
        }
    }

    "ObjectSerializer" - {
        "only included keys should be serialized" {
            write {
                ObjectSerializer(Menu::class.java, DEFAULT, PathMatcher.create("name"), Path())
                    .write(it, Menu.create())
            } shouldBe """{"name":"m1"}"""

            write {
                ObjectSerializer(Menu::class.java, DEFAULT, PathMatcher.create("(^children)"), Path())
                    .write(it, Menu.create())
            } shouldBe """{"name":"m1"}"""

            write {
                ObjectSerializer(Menu::class.java, DEFAULT, PathMatcher.create("children.name"), Path())
                    .write(it, Menu.create())
            } shouldBe """{"children":[{"name":"m2"},{"name":"m3"}]}"""

            write {
                ObjectSerializer(Menu::class.java, DEFAULT, PathMatcher.create("(name, parent).name"), Path())
                    .write(it, Menu.create())
            } shouldBe """{"name":"m1"}"""
        }
    }
})
