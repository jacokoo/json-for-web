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

package com.github.jacokoo.json

import java.io.Writer

interface Output {
    fun key(key: String) {
        writeQuoted(key)
        write(COLON)
    }
    fun write(item: Any) = write(item.toString())
    fun writeQuoted(item: String, escape: Boolean = false) {
        write(QUOTE)
        if (escape) {
            item.forEach {
                val int = it.toInt()
                if (int >= ESCAPE.size) write(it)
                else ESCAPE[it.toInt()]?.let { write(it) } ?: write(it)
            }
        } else write(item)
        write(QUOTE)
    }
    fun array(block: Output.() -> Unit) {
        write(ARRAY_BEGIN)
        this.block()
        write(ARRAY_END)
    }
    fun `object`(block: Output.() -> Unit) {
        write(OBJECT_BEGIN)
        this.block()
        write(OBJECT_END)
    }
    fun itemEnd() = write(COMMA)

    fun write(item: Int)
    fun write(item: Long)
    fun write(item: Double)
    fun write(item: Float)
    fun write(item: Boolean)
    fun write(item: Char)
    fun write(item: String)

    companion object {
        val QUOTE = "\""
        val ARRAY_BEGIN = "["
        val ARRAY_END = "]"
        val OBJECT_BEGIN = "{"
        val OBJECT_END = "}"
        val COMMA = ","
        val COLON = ":"
        val ESCAPE = arrayOfNulls<String>(128).apply {
            repeat(10) { this[it] = "\\u000$it" }
            repeat(6) { this[it + 10] = "\\u000${'a' + it}"}
            repeat(10) {this[it + 16] = "\\u001$it"}
            repeat(6) {this[it + 26] = "\\u001${'a' + it}"}

            this['"'.toInt()] = "\\\""
            this['\\'.toInt()] = "\\\\"
            this['\t'.toInt()] = "\\t"
            this['\b'.toInt()] = "\\b"
            this['\n'.toInt()] = "\\n"
            this['\r'.toInt()] = "\\r"
            this[0x0c] = "\\f"
        }
    }
}

class DefaultOutput(private val sb: StringBuilder = StringBuilder()): Output {
    override fun write(item: String) {
        sb.append(item)
    }

    override fun write(item: Boolean) {
        sb.append(item)
    }

    override fun write(item: Char) {
        sb.append(item)
    }

    override fun write(item: Int) {
        sb.append(item)
    }

    override fun write(item: Long) {
        sb.append(item)
    }

    override fun write(item: Float) {
        sb.append(item)
    }

    override fun write(item: Double) {
        sb.append(item)
    }

    override fun toString() = sb.toString()
}

class StreamOutput(val writer: Writer): Output {
    override fun write(item: Int) {
        writer.write(Integer.toString(item))
    }

    override fun write(item: Long) {
        writer.write(item.toString())
    }

    override fun write(item: Double) {
        writer.write(item.toString())
    }

    override fun write(item: Float) {
        writer.write(item.toString())
    }

    override fun write(item: Boolean) {
        writer.write(item.toString())
    }

    override fun write(item: Char) {
        writer.write(item.toString())
    }

    override fun write(item: String) {
        writer.write(item)
    }

}
