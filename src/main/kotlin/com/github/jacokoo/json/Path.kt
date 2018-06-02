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

enum class MatchResult {
    INCLUDED, NOT_FOUND, EXCLUDED
}

data class Path internal constructor(val items: List<String> = listOf()) {
    val depth: Int = items.size
    operator fun get(level: Int): String = items[level]
    fun push(name: String): Path = Path(items + name)

    override fun toString() = "Path: ${items.joinToString(".")}"
}

interface PathItem {
    fun match(name: String): MatchResult

    companion object {
        private fun fail(): Nothing = throw IllegalArgumentException("illegal path")

        fun create(string: String) =
            if (string == "*" || string == "(*)") IncludeAllPathItem()
            else if (string == "()" || string == "(^)" || string == "") fail()
            else if (string.startsWith("(^")) string.substring(2, string.length - 1).split(",").let {
                ExcludedPathItem(it.toSet())
            }
            else if (string.startsWith("(")) string.substring(1, string.length - 1).split(",").let {
                IncludedPathItem(it.toSet())
            }
            else if (!string.contains(",")) IncludedPathItem(setOf(string))
            else fail()
    }
}

class IncludeAllPathItem internal constructor(): PathItem {
    override fun match(name: String) = MatchResult.INCLUDED
    override fun toString() = "*"
}

class IncludedPathItem internal constructor(val items: Set<String>): PathItem {
    override fun match(name: String) = if (items.contains(name)) MatchResult.INCLUDED else MatchResult.NOT_FOUND
    override fun toString() = "(${items.joinToString()})"
}

class ExcludedPathItem internal constructor(val items: Set<String>): PathItem {
    override fun match(name: String) = if (items.contains(name)) MatchResult.EXCLUDED else MatchResult.INCLUDED
    override fun toString() = "(^${items.joinToString()})"
}

interface PathMatcher {
    val maxDepth: Int
    fun match(path: Path): MatchResult

    companion object {
        fun create(vararg items: String): PathMatcher = items
            .map { it.replace(Regex("\\s*"), "") }
            .filter { it != "" }.map { it.split(".") }
            .map { it.map { PathItem.create(it) }.let { DefaultPathMatcher(it) } }
            .let { CompositePathMatcher(it) }
    }
}

class DefaultPathMatcher internal constructor(val items: List<PathItem>): PathMatcher {
    override val maxDepth: Int = items.size
    override fun match(path: Path): MatchResult {
        if (path.depth > items.size) return MatchResult.NOT_FOUND
        repeat(path.depth) {
            val result = items[it].match(path[it])
            if (result != MatchResult.INCLUDED) return result
        }
        return MatchResult.INCLUDED
    }

    override fun toString() = items.joinToString(separator = ".")
}

class CompositePathMatcher internal constructor(val matchers: List<PathMatcher>): PathMatcher {
    override val maxDepth: Int = matchers.maxBy { it.maxDepth }?.let { it.maxDepth } ?: 0

    override fun match(path: Path): MatchResult {
        matchers.forEach {
            val result = it.match(path)
            if (result != MatchResult.NOT_FOUND) return result
        }
        return MatchResult.NOT_FOUND
    }

    override fun toString() = "Path Matcher: ${matchers.joinToString(" | ")}"
}
