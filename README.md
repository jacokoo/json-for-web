# JSON For Web 
[![Build Status](https://travis-ci.org/jacokoo/json-for-web.svg?branch=master)](https://travis-ci.org/jacokoo/json-for-web)
[![Coverage Status](https://img.shields.io/codecov/c/github/jacokoo/json-for-web/master.svg?style=flat)](https://codecov.io/github/jacokoo/json-for-web?branch=master)

这个库主要用于`Web`请求返回`JSON`, 只有从`Java`到`JSON`的序列化, 没有从`JSON`到`Java`的反序列化. 相应的, 它提供非常灵活的字段过滤功能.

# Web 开发痛点

由于`Java`开发大多有`ORM`框架, 那么都会有实体(Entity / DTO)到数据库的映射, 而Web请求返回的时候, 通常不会返回实体里所有的属性, 而且对于同一个实体, 在不同应对不同的Web请求的时候, 可能需要返回的字段都是不一样的, 从而导致需要创建很多的`VO`

# 解决的问题

通过针对每个`Web请求`提供不同的字段过滤方式来完全去掉`VO`的存在

# 示例

```java
    
    public class Foo {
        private Long id;
        private String name;
        private Integer foo;

        // getter and setter
    }

    public class Bar {
        private Long id;
        private String name;
        private Integer bar;
        private Foo foo;

        // getter and setter
    }

    // foo = new Foo(1, "foo", 1)
    // bar = new Bar(2, "bar", 2, foo)

    @RequestMapping("/{id}")
    @JSON("*.*") // 返回所有属性: {"id":2,"name":"bar","bar":2,"foo":{"id":1,"name":"foo","foo":1}}
    public Bar getBar() {
        // ...
    }

    @RequestMapping("/{id}")
    @JSON("*") // 返回bar的所有属性, 不包括内嵌实体: {"id":2,"name":"bar","bar":2}
    public Bar getBar() {
        // ...
    }

    @RequestMapping("/{id}")
    @JSON("(id, foo).(id)") // 返回bar的id, 与foo的id: {"id":2,"foo":{"id: 1}}
    public Bar getBar() {
        // ...
    }

    @RequestMapping("/{id}")
    @JSON("(^id).(id)") // 返回bar中除了id外的所有属性, 与foo的id: {"name":"bar","bar":2,"foo":{"id: 1}}
    public Bar getBar() {
        // ...
    }
```

# 过滤规则

## 层级

以示例中的`Foo`与`Bar`为例

* 因为返回值为`Bar`, 所以`Bar`是第一层
* `Bar`中包含实体`Foo`, 所以`Foo`是第二层
* 如果`Bar`中还包含其它实体, 那这些实体都同为第二层
* 如果`Foo`中还包含其它实体, 那这些实体则为第三层

## 单层规则

* 包含所有字段: `*` 或 `(*)` 括号可选
* 包含单个字段: `name` 或 `(name)` 括号可选
* 包含多个字段: `(name1, name2)` 必须加括号
* 排除单个字段: `(^name)` 必须加括号
* 排除多个字段: `(^name1, name2, name3)` 必须加括号

## 单条规则

用`.`把单层规则连接起来, 组成单条规则

如: `id.*.(name1, name2).(^id)` 表示:

* 第一层只包含`id`字段
* 第二层包含所有字段
* 第三层包含`name1`与`name2`两个字段
* 第四层包含除了`id`以外的所有字段

## 完整的规则

完整的无则由一个或多个单条规则组成完整的规则

```java
    @JSON("(foo, baz).(id, name)")
    // foo 与 baz 的 id 和 name 字段都包含了
    public Bar getBar() {}

    @JSON({"foo.id", "baz.name"})
    // 仅包含 foo 的 id 字段与 baz 的 name 字段
    public Bar getBar() {}
```

## 匹配方式

* 从第一条规则开始匹配
* 如果匹配到`被包含`或`被排除`则停止匹配
* 如果不是则匹配下一条, 直到最后
* 只有匹配到`被包含`的字段会被序列化

***`(^id)` 表示的是除了`id`其它字段都包含***

***字段只认`get`方法, 不认属性名***
