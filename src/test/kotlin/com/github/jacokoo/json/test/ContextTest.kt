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
import io.kotlintest.matchers.beOfType
import io.kotlintest.should
import io.kotlintest.shouldThrow
import io.kotlintest.specs.FreeSpec

class DemoSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        TODO("not implemented")
    }
}

class ContextTest: FreeSpec({
    "SerializeContext" - {
        "register" {
            var ctx = SerializeContext.register(ContextTest::class.java to DemoSerializer())
            ctx[ContextTest::class.java]!! should beOfType<DemoSerializer>()

            ctx = ctx.register(DemoSerializer::class.java, DemoSerializer())
            ctx[DemoSerializer::class.java]!! should beOfType<DemoSerializer>()

            shouldThrow<NotImplementedError> { DemoSerializer().write(DefaultOutput(), 1) }
        }

        "default" {
            SerializeContext(SerializeContext.defaults)[Int::class.java]!! should beOfType<IntSerializer>()
        }
    }
})
