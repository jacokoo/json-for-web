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

import com.github.jacokoo.json.spring.mvc.JSON
import com.github.jacokoo.json.spring.mvc.JsonReturnValueHandler
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import org.springframework.core.MethodParameter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.ModelAndViewContainer

class Demo {
    @JSON(["name"])
    fun foo() = Menu.create()

    fun bar() {}
}

class JsonReturnValueHandlerTest: FreeSpec({
    "JsonReturnValueHandler" - {
        val h = JsonReturnValueHandler()
        val m = MethodParameter(Demo::class.java.getMethod("foo"), -1)
        h.supportsReturnType(m) shouldBe true
        h.supportsReturnType(MethodParameter(Demo::class.java.getMethod("bar"), -1)) shouldBe false

        val ro = m.method!!.invoke(Demo())
        val mav = ModelAndViewContainer()
        val response = MockHttpServletResponse()
        h.handleReturnValue(ro, m, mav, ServletWebRequest(MockHttpServletRequest(), response))

        response.contentAsString shouldBe "{\"name\":\"m1\"}"
    }
})
