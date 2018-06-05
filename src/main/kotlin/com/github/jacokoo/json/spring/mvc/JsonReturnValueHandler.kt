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

package com.github.jacokoo.json.spring.mvc

import com.github.jacokoo.json.JSONSerializer
import org.springframework.core.MethodParameter
import org.springframework.http.MediaType
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodReturnValueHandler
import org.springframework.web.method.support.ModelAndViewContainer
import javax.servlet.http.HttpServletResponse

class JsonReturnValueHandler: HandlerMethodReturnValueHandler {
    private val map = mutableMapOf<String, JSONSerializer>()

    override fun supportsReturnType(returnType: MethodParameter) =
        returnType.hasMethodAnnotation(JSON::class.java)

    override fun handleReturnValue(returnValue: Any?, returnType: MethodParameter, mavContainer: ModelAndViewContainer, webRequest: NativeWebRequest) {
        val name = returnType.method!!.toString()
        var json = map[name];
        if (json == null) {
            synchronized(map) {
                if (!map.containsKey(name)) {
                    map[name] = JSONSerializer(*returnType.getMethodAnnotation(JSON::class.java)!!.value)
                }
            }
            json = map[name]!!
        }
        mavContainer.isRequestHandled = true
        val response = webRequest.getNativeResponse(HttpServletResponse::class.java)!!
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        json.write(response.outputStream, returnValue)
    }
}
