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

import com.github.jacokoo.json.DefaultOutput
import io.kotlintest.be
import io.kotlintest.should
import io.kotlintest.specs.FreeSpec
import java.math.BigDecimal

class OutputTest: FreeSpec({
    fun write(block: (DefaultOutput) -> Unit): String =
        DefaultOutput().also { block(it) }.toString()

    "Output" - {
        "write numbers" {
            write { it.write(1) } should be("1")
            write { it.write(1L) } should be("1")
            write { it.write(1.0f) } should be("1.0")
            write { it.write(1.23) } should be("1.23")
            write { it.write(1.23e10) } should be("1.23E10")
            write { it.write(-1.23e-10) } should be("-1.23E-10")
            write { it.write(1.toByte()) } should be("1")
            write { it.write(1.toShort()) } should be("1")

            write { it.write(BigDecimal("1.23e10")) } should be("1.23E+10")
            write { it.write(BigDecimal("-1.23e-10")) } should be("-1.23E-10")
        }

        "write boolean" {
            write { it.write(true) } should be("true")
            write { it.write(false) } should be("false")
        }

        "write string" {
            write { it.write('c') } should be("c")
            write { it.write("abc") } should be("abc")

            write {it.writeQuoted("abc")} should be("\"abc\"")
            write { it.writeQuoted("abc\"") } should be("\"abc\"\"")
            write { it.writeQuoted("abc\"", true) } should be("\"abc\\\"\"")
        }

        "write others" {
            val list = listOf(1,2,3)
            write { it.write(list) } should be(list.toString())
        }

        "write key" {
            write { it.key("abc") } should be("\"abc\":")
        }

        "write item end" {
            write { it.itemEnd() } should be(",")
        }

        "write array" {
            write { it.array { this.write(1) } } should be("[1]")
        }

        "write object" {
            write { it.`object` { this.write(1) } } should be("{1}")
        }
    }
})