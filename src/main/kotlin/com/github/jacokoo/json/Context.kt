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

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class SerializeContext(private val serializers: Map<out Class<*>, Serializer>, val enumSerializer: Serializer = IntEnumSerializer()) {

    operator fun get(clazz: Class<out Any>): Serializer? = serializers[clazz]

    fun register(clazz: Class<*>, serializer: Serializer) =
        this.copy(serializers + (clazz to serializer))

    companion object {
        private val defaults = mutableMapOf(
            Int::class.java to IntSerializer(),
            Integer::class.java to IntSerializer(),
            Long::class.java to LongSerializer(),
            java.lang.Long::class.java to LongSerializer(),
            Float::class.java to FloatSerializer(),
            java.lang.Float::class.java to FloatSerializer(),
            Double::class.java to DoubleSerializer(),
            java.lang.Double::class.java to DoubleSerializer(),
            Byte::class.java to ToStringSerializer(),
            java.lang.Byte::class.java to ToStringSerializer(),
            Short::class.java to ToStringSerializer(),
            java.lang.Short::class.java to ToStringSerializer(),
            Boolean::class.java to BooleanSerializer(),
            java.lang.Boolean::class.java to BooleanSerializer(),
            Char::class.java to CharSerializer(),
            java.lang.Character::class.java to CharSerializer(),
            String::class.java to StringSerializer(),

            Date::class.java to DateSerializer(),
            LocalDate::class.java to LocalDateSerializer(),
            LocalDateTime::class.java to LocalDateTimeSerializer()
        )

        val DEFAULT = SerializeContext(defaults)

        @JvmStatic
        fun register(vararg pairs: Pair<Class<*>, Serializer>) = SerializeContext(defaults + pairs)

    }
}
