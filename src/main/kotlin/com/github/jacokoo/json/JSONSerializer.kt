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

import java.io.OutputStream
import java.io.Writer

class JSONSerializer private constructor(
    matcher: PathMatcher,
    context: SerializeContext = SerializeContext(SerializeContext.defaults + SerializeContext.globals),
    path: Path = Path()
): ComplexSerializer(context, matcher, path) {

    constructor(vararg paths: String): this(PathMatcher.create(*paths))
    constructor(context: SerializeContext, vararg paths: String): this(PathMatcher.create(*paths), context)
    constructor(matcher: PathMatcher, vararg serializers: Pair<Class<*>, Serializer>): this(matcher, SerializeContext.register(*serializers))

    fun stringify(obj: Any?) =
        obj?.let { o -> DefaultOutput().also { write(it, o) }.toString() } ?: ""

    fun write(stream: OutputStream, obj: Any?) =
        obj?.let { o -> stream.bufferedWriter().also { write(it, o) }.flush() }

    fun write(writer: Writer, obj: Any?) =
        obj?.let { o -> StreamOutput(writer).also { write(it, o) } }

    override fun write(output: Output, obj: Any) {
        get(obj::class.java).write(output, obj)
    }

}
