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
import io.kotlintest.specs.FreeSpec
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class SimpleSerializerTest: FreeSpec({
    fun write(block: (Output) -> Unit): String =
        DefaultOutput().also(block).toString()

    "Simple Serializer" - {
        "int" {
            write { IntSerializer().write(it, 1) } should be("1")
        }

        "long" {
            write { LongSerializer().write(it, 1L) } should be("1")
        }

        "float" {
            write { FloatSerializer().write(it, 1.0f) } should be("1.0")
        }

        "double" {
            write { DoubleSerializer().write(it, 1.12) } should be("1.12")
        }

        "char" {
            write { CharSerializer().write(it, 'c') } should be("c")
        }

        "boolean" {
            write { BooleanSerializer().write(it, true) } should be("true")
        }

        "string" {
            write { StringSerializer().write(it, "abc\"") } should be("\"abc\\\"\"")
        }

        "to string" {
            write { ToStringSerializer().write(it, this) } should be(this.toString())
        }

        "to quoted string" {
            write { ToQuotedStringSerializer().write(it, this) } should be("\"${this.toString()}\"")
        }

        "date" {
            val d = Date()
            write { DateSerializer().write(it, d) } should be("${d.time}")
        }

        "local date time" {
            val d = LocalDateTime.now()
            write { LocalDateTimeSerializer().write(it, d) } should be("${d.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()}")
        }
    }

})