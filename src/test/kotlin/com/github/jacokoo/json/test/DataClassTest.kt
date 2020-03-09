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

import com.github.jacokoo.json.IntSerializer
import com.github.jacokoo.json.ObjectItem
import com.github.jacokoo.json.Path
import com.github.jacokoo.json.SerializeContext
import io.kotlintest.specs.FreeSpec

class DataClassTest: FreeSpec({
    fun item() = ObjectItem("", Object::class.java.methods[0]!!, IntSerializer())

    "let jacoco happy" - {
        Path(listOf("a")).also {
            it.hashCode()
            it.toString()
            it.equals(null)
            it.equals(it)
            it.equals(Path())
        }

        item().also {
            it.hashCode()
            it.toString()
            it.equals(it)
            it.equals(null)
            it.equals(item())
            it.component1()
            it.component2()
            it.component3()
            it.copy()
        }
    }
})
