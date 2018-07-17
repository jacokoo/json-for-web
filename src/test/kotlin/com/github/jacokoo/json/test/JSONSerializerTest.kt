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
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import java.io.ByteArrayOutputStream

class MenuSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write("menu")
    }
}

class JSONSerializerTest: FreeSpec({
    "JSONSerializer" - {
        "stringify" {
            JSONSerializer("(^children)").stringify(Menu.create()) shouldBe "{\"name\":\"m1\"}"
            JSONSerializer(SerializeContext.DEFAULT, "*").stringify(null) shouldBe ""
        }

        "customize serializer" {
            JSONSerializer(PathMatcher.create("*"), Menu::class.java to MenuSerializer())
                .stringify(Menu.create()) shouldBe "menu"

            JSONSerializer("*").stringify(null) shouldBe ""
        }

        "stream" {
            ByteArrayOutputStream().also {
                JSONSerializer(PathMatcher.create("*"), Menu::class.java to MenuSerializer())
                    .write(it, Menu.create())
            }.toByteArray().toString(Charsets.UTF_8) shouldBe "menu"

            ByteArrayOutputStream().also {
                val w = it.writer()
                JSONSerializer(PathMatcher.create("*"), Menu::class.java to MenuSerializer())
                    .write(w, Menu.create())
                w.flush()
            }.toByteArray().toString(Charsets.UTF_8) shouldBe "menu"

            ByteArrayOutputStream().also {
                JSONSerializer("*").write(it, null)
            }.toByteArray().size shouldBe 0

            ByteArrayOutputStream().also {
                val w = it.writer()
                JSONSerializer("*").write(w, null)
                w.flush()
            }.toByteArray().size shouldBe 0
        }
    }
})
