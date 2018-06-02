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

import java.lang.reflect.Array
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

interface Serializer {
    fun write(output: Output, obj: Any)
    fun isEmpty(): Boolean = false
}

class IntSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write(obj as Int)
    }
}
class LongSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write(obj as Long)
    }
}
class FloatSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write(obj as Float)
    }
}
class DoubleSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write(obj as Double)
    }
}
class CharSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write(obj as Char)
    }
}
class BooleanSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write(obj as Boolean)
    }
}
class StringSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.writeQuoted(obj as String, true)
    }
}
class ToStringSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write(obj)
    }
}
class ToQuotedStringSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.writeQuoted(obj.toString())
    }
}
class DateSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write((obj as Date).time)
    }
}
class LocalDateTimeSerializer: Serializer {
    override fun write(output: Output, obj: Any) {
        output.write((obj as LocalDateTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
    }
}
class EmptySerializer: Serializer {
    override fun write(output: Output, obj: Any) {
    }
    override fun isEmpty() = true

    companion object {
        val DEFAULT = EmptySerializer()
    }
}

abstract class ComplexSerializer(protected val context: SerializeContext, protected val matcher: PathMatcher, protected val path: Path): Serializer {
    protected val map = mutableMapOf<Class<*>, Serializer>()

    protected fun get(clazz: Class<*>, key: String? = null): Serializer {
        val ser = context[clazz] ?: map[clazz]
        if (ser != null) return ser
        val p = key?.let { path.push(key) } ?: path

        if (clazz.isArray) {
            if (p.depth > matcher.maxDepth) return cache(clazz, EmptySerializer.DEFAULT)
            return cache(clazz, ArraySerializer(context, matcher, p))
        }

        if (Collection::class.java.isAssignableFrom(clazz)) {
            if (p.depth > matcher.maxDepth) return cache(clazz, EmptySerializer.DEFAULT)
            return cache(clazz, CollectionSerializer(context, matcher, p))
        }

        if (Map::class.java.isAssignableFrom(clazz)) {
            if (p.depth >= matcher.maxDepth) return cache(clazz, EmptySerializer.DEFAULT)
            return cache(clazz, MapSerializer(context, matcher, p))
        }

        if (Number::class.java.isAssignableFrom(clazz)) {
            return cache(clazz, ToStringSerializer())
        }

        val pkg = clazz.`package`.name
        if (pkg.startsWith("java") || pkg.startsWith("kotlin")) {
            return cache(clazz, ToQuotedStringSerializer())
        }

        if (p.depth >= matcher.maxDepth) return cache(clazz, EmptySerializer.DEFAULT)
        return cache(clazz, ObjectSerializer(clazz, context, matcher, p))
    }

    private fun cache(clazz: Class<*>, ser: Serializer) = ser.also { map[clazz] = it }

    private fun <T> next(it: Iterator<T>, clazz: (T) -> Class<*>): Pair<T, Serializer>? {
        while (it.hasNext()) {
            val item = it.next()
            var ser = get(clazz(item))

            if (!ser.isEmpty()) return item to ser
        }
        return null
    }

    protected fun <T> writeIterator(output: Output, it: Iterator<T>, clazz: (T) -> Class<*>, block: (T, Serializer) -> Unit) {
        var t1 = next(it, clazz)
        var t2 = next(it, clazz)

        while (t1 != null) {
            block(t1.first, t1.second)
            if (t2 != null) output.itemEnd()

            t1 = t2
            t2 = next(it, clazz)
        }
    }
}

class ArraySerializer(context: SerializeContext, matcher: PathMatcher, path: Path): ComplexSerializer(context, matcher, path) {
    override fun write(output: Output, obj: Any) {
        val list = (0 until Array.getLength(obj)).map { Array.get(obj, it) }.filterNotNull().iterator()
        output.array {
            writeIterator(this, list, {it::class.java}, {t, s -> s.write(this, t)})
        }
    }
}

class CollectionSerializer(context: SerializeContext, matcher: PathMatcher, path: Path): ComplexSerializer(context, matcher, path) {
    override fun write(output: Output, obj: Any) {
        val list = (obj as Collection<*>).filterNotNull().iterator()
        output.array {
            writeIterator(this, list, {it::class.java}, {t, s -> s.write(this, t)})
        }
    }
}

class MapSerializer(context: SerializeContext, matcher: PathMatcher, path: Path): ComplexSerializer(context, matcher, path) {
    override fun write(output: Output, obj: Any) {
        val list = (obj as Map<*, *>).toList()
            .filter { it.first != null && it.second != null }
            .map { Pair(it.first!!.toString(), it.second!!) }
            .filter {
                matcher.match(path.push(it.first)) == MatchResult.INCLUDED
            }.iterator()

        output.`object` {
            writeIterator(this, list, {it.second::class.java}, {t, s ->
                this.key(t.first)
                s.write(this, t.second)
            })
        }
    }
}

internal data class ObjectItem(val name: String, val method: Method, val serializer: Serializer)

class ObjectSerializer(private val clazz: Class<*>, context: SerializeContext, matcher: PathMatcher, path: Path): ComplexSerializer(context, matcher, path) {
    private val items: List<ObjectItem>

    init {
        items = clazz.methods.filter {
            it.modifiers and Modifier.PUBLIC != 0
            && it.modifiers and Modifier.STATIC == 0
            && it.name != "getClass"
            && it.name.startsWith("get") && it.parameterCount == 0
        }.map { fieldName(it) to it }.filter {
            matcher.match(path.push(it.first)) == MatchResult.INCLUDED
        }.map {
            ObjectItem(it.first, it.second, get(it.second.returnType, it.first))
        }.filter { !it.serializer.isEmpty() }
    }

    override fun isEmpty() = items.isEmpty()

    private fun fieldName(method: Method) = method.name.let {
        it.substring(3).let { it[0].toLowerCase() + it.substring(1) }
    }

    override fun write(output: Output, obj: Any) {
        output.`object` {
            items.fold(false) { acc, item ->
                val o = item.method.invoke(obj)
                if (o == null) return@fold acc

                if (acc) this.itemEnd()
                output.key(item.name)
                item.serializer.write(this, o)
                return@fold true
            }
        }
    }
}


