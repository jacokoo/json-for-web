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
import io.kotlintest.*
import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.beOfType
import io.kotlintest.matchers.collections.contain
import io.kotlintest.matchers.containAll
import io.kotlintest.matchers.haveSize
import io.kotlintest.specs.FreeSpec

class PathTest: FreeSpec({
    "Path" - {
        "should have 0 depth by default" {
            Path().depth shouldBe 0
        }

        "can push item in" {
            Path().push("a").also {
                it.depth shouldBe 1
                it[0] shouldBe "a"
            }
        }

        "and can push more" {
            Path().push("a").push("b").also {
                it.depth shouldBe 2
                it[0] shouldBe "a"
                it[1] shouldBe "b"

                it.toString() shouldBe "Path: a.b"
            }
        }
    }

    "PathItem" - {
        "create include all item" {
            PathItem.create("*") should beOfType<IncludeAllPathItem>()
            PathItem.create("(*)") should beOfType<IncludeAllPathItem>()
        }

        "include item without bracket - single item" {
            PathItem.create("a").also {
                it should beOfType<IncludedPathItem>()
                forAll((it as IncludedPathItem).items) {
                    it shouldBe "a"
                }
            }
        }

        "include items with bracket - multiple items" {
            PathItem.create("(a,b)").also {
                it should beOfType<IncludedPathItem>()
                (it as IncludedPathItem).items.also {
                    it should haveSize(2)
                    it should containAll("a", "b")
                }
            }
        }

        "exclude items - single or multiple items" {
            PathItem.create("(^a)").also {
                it should beOfType<ExcludedPathItem>()
                (it as ExcludedPathItem).items.also {
                    it should haveSize(1)
                    it should contain("a")
                }
            }

            PathItem.create("(^a,b,c)").also {
                it should beOfType<ExcludedPathItem>()
                (it as ExcludedPathItem).items.also {
                    it should haveSize(3)
                    it should containAll("a", "b", "c")
                }
            }
        }

        "multiple included items must be bracketed" {
            shouldThrow<IllegalArgumentException> { PathItem.create("a,b") }
        }

        "exclude items must be bracketed" {
            PathItem.create("^a").also {
                it shouldNot beOfType<ExcludedPathItem>()
                it should beOfType<IncludedPathItem>()
                (it as IncludedPathItem).items should contain("^a")
            }
        }

        "at least one item be supplied" {
            shouldThrow<IllegalArgumentException> { PathItem.create("") }
            shouldThrow<IllegalArgumentException> { PathItem.create("()") }
            shouldThrow<IllegalArgumentException> { PathItem.create("(^)") }
        }

        "include all test" {
            PathItem.create("*").also {
                it.match("a") should be(MatchResult.INCLUDED)
                it.match("b") should be(MatchResult.INCLUDED)
            }
        }

        "include test" {
            PathItem.create("a").also {
                it.match("a") should be(MatchResult.INCLUDED)
                it.match("b") should be(MatchResult.NOT_FOUND)
            }

            PathItem.create("(a,b)").also {
                it.match("a") should be(MatchResult.INCLUDED)
                it.match("b") should be(MatchResult.INCLUDED)
                it.match("c") should be(MatchResult.NOT_FOUND)
            }
        }

        "exclude test" {
            PathItem.create("(^a,b)").also {
                it.match("a") should be(MatchResult.EXCLUDED)
                it.match("b") should be(MatchResult.EXCLUDED)
                it.match("c") should be(MatchResult.INCLUDED)
            }
        }
    }

    "PathMatcher" - {
        "it should be CompositePathMatcher" {
            PathMatcher.create("*") should beOfType<CompositePathMatcher>()
        }

        "each argument should be parsed to a PathMatcher" {
            PathMatcher.create("a", "b").let { it as CompositePathMatcher }.also {
                it.matchers.also {
                    it should haveSize(2)
                    forAll(it) {
                        it should beInstanceOf<PathMatcher>()
                    }
                }
            }
        }

        "all empty strings should be ignored" {
            PathMatcher.create("", "", "").let { it as CompositePathMatcher }.also {
                it.matchers should haveSize(0)
            }
        }

        "max depth" {
            PathMatcher.create().maxDepth shouldBe 0
            PathMatcher.create("a").maxDepth shouldBe 1
            PathMatcher.create("a", "a.b.c").maxDepth shouldBe 3
        }

        "path item should be separated by dot" {
            PathMatcher.create("*.(a,b).(^c)").let {it as CompositePathMatcher}.let {
                it.matchers[0] as DefaultPathMatcher
            }.also {
                it.items[0] should beOfType<IncludeAllPathItem>()
                it.items[1].also {
                    it should beOfType<IncludedPathItem>()
                    (it as IncludedPathItem).items.also {
                        it should haveSize(2)
                        it should containAll("a", "b")
                    }
                }
                it.items[2].also {
                    it should beOfType<ExcludedPathItem>()
                    (it as ExcludedPathItem).items.also {
                        it should haveSize(1)
                        it should contain("c")
                    }
                }
            }
        }

        "all spaces should be ignored" {
            PathMatcher.create("  a   .  b   .(    c d, e    f   )").toString() should
                be("Path Matcher: (a).(b).(cd, ef)")
        }

        "it should do match level by level" {
            PathMatcher.create("(a, b).(c, d)").also {
                val a = Path().push("a")
                it.match(a) should be(MatchResult.INCLUDED)

                val c = Path().push("c")
                it.match(c) should be(MatchResult.NOT_FOUND)

                it.match(a.push("c")) should be(MatchResult.INCLUDED)
                it.match(a.push("e")) should be(MatchResult.NOT_FOUND)

                it.match(c.push("c")) should be(MatchResult.NOT_FOUND)
                it.match(c.push("d")) should be(MatchResult.NOT_FOUND)
                it.match(a.push("c").push("d")) should be(MatchResult.NOT_FOUND)
            }
        }

        "it should do match matcher by matcher" {
            PathMatcher.create("a.b", "c.d").also {
                it.match(Path().push("c")) should be(MatchResult.INCLUDED)
            }
        }

        "it can not match cross matchers" {
            PathMatcher.create("a.b", "c.d").also {
                it.match(Path().push("a").push("d")) should be(MatchResult.NOT_FOUND)
            }
        }

        "all items not be excluded should be included" {
            PathMatcher.create("(^a, b)").also {
                it.match(Path().push("a")) should be(MatchResult.EXCLUDED)
                it.match(Path().push("c")) should be(MatchResult.INCLUDED)
                it.match(Path().push("e")) should be(MatchResult.INCLUDED)
            }
        }

        "match should stop at INCLUDED or EXCLUDED" {
            PathMatcher.create("(^a, b)", "(^c)").also {
                it.match(Path().push("a")) should be(MatchResult.EXCLUDED)
                it.match(Path().push("c")) should be(MatchResult.INCLUDED)
            }
        }
    }
})
