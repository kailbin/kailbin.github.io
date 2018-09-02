---
title: Redis 对 lua 脚本的支持
date: 2018-09-03
tags: [Redis]
categories: [Redis]
id: redis-build-in-lua
---

Redis 从 2.6.0 版本开始增加了 lua 脚本的支持，通过内嵌对 Lua 环境的支持， Redis 解决了长久以来不能高效地处理 CAS （check-and-set）命令的缺点， 并且可以通过组合使用多个命令， 轻松实现以前很难实现或者不能高效实现的模式。

从 3.2.0 开始支持对 Lua 脚本的 Debug，但是需要注意的是，Debug 的时候会阻塞其它所有指令，导致无法对外提供服务，所以**千万不要在生产环境 Debug**。除此之外 不建议 Lua 脚本写的太过复杂，如果一个脚本复杂到需要 Debug 才能搞清楚，就建议简化脚本的逻辑了。

<!-- more -->

# 使用 Lua 脚本的好处
- **原子性的操作**：Redis会将整个脚本作为一个整体执行，中间不会被其他命令插入。因此在编写脚本的过程中无需担心会出现竞态条件，无需使用事务。
- **减少网络开销**：可以将多个请求通过脚本的形式一次发送，减少网络时延和请求次数
- **代码复用**：客户端发送的脚步会永久存在redis中，这样，其他客户端可以复用这一脚本来完成相同的逻辑

# 命令简介

- `EVAL` ：执行 lua 脚本
  - 语法： `EVAL script numkeys key [key ...] arg [arg ...]`
  - script 参数内置函数
    - redis.call()
    - redis.pcall() 
  - numkeys 是 key 的个数，如果没有 key，传 0
  - arg 的个数可以和 key 的个数不一样，索引从 1 开始
  - 例子
    - `eval "return redis.call('set','foo','bar')" 0`
    - `eval "return redis.call('set',KEYS[1], ARGV[1])" 1 foo bar`
    - 建议 脚本里使用的所有键都应该由 KEYS 数组来传递，便于 Redis 确定命令是对哪些键进行的操作
- `SCRIPT LOAD` :  加载脚本到内存，便于复用，返回 脚本的 SHA 值
  - `script load "return redis.call('set',KEYS[1], ARGV[1])"`
- `SCRIPT EXISTS` ：判断脚本是否已经加载到内存中，返回 1 存在，0 不存在
  - `script exists e00664dc91030ceafd9fbf1d1aad8b0767425e29`
- `EVALSHA` ：通过 脚本 SHA 值 执行，语法格式与 EVAL 一样
- `SCRIPT FLUSH` ：清空Lua脚本缓存
- `SCRIPT KILL` ：杀死当前正在运行的 Lua 脚本，主要用于终止运行时间过长的脚本
  - 当且仅当这个脚本没有执行过任何写操作时，这个命令才生效
  - 当前正在运行的脚本已经执行过写操作，那么即使执行 `SCRIPT KILL` ，也无法将它杀死，因为这是违反 Lua 脚本的原子性执行原则的，唯一可行的办法是使用 `SHUTDOWN NOSAVE` 命令，通过停止整个 Redis 进程来停止脚本的运行
  - **所以在 redis 上执行 lua 脚本要万分小心，性能有问题的脚本可能会使整个 redis 无法提供服务**



# 内置函数

- 打印日志 ： `redis.log(loglevel,message)`
  - `redis.LOG_DEBUG`
  - `redis.LOG_VERBOSE`
  - `redis.LOG_NOTICE`
  - `redis.LOG_WARNING`
- 返回错误信息 : `return redis.error_reply('error_string')`
- 返回状态信息 : `return redis.status_reply('status_string')`

# 注意事项

1. 不要执行随机性写入，例如写入 随机数字，这样会导致主从的数据不一致
2. 定义变量时 必须使用 `local` 关键字
3. Redis 配置文件中一定要设置lua超时时间 `lua-time-limit`

# Java 代码的操作方式

## Jedis

``` java
Jedis jedis = new Jedis();

String setFoo = jedis.set("foo", "bar");
// OK
System.out.println(setFoo);

String getFoo = jedis.get("foo");
// bar
System.out.println(getFoo);

Object evalGetFoo = jedis.eval("return redis.call('get',KEYS[1])", 1, "foo");
// bar
System.out.println(evalGetFoo);

Object evalStatusReply = jedis.eval("return redis.status_reply('ERROR')");
// ERROR
System.out.println(evalStatusReply);

// redis.clients.jedis.exceptions.JedisDataException: Exception Message
jedis.eval("return redis.error_reply('Exception Message')");
```



## Spring Data Redis

``` java
JedisConnectionFactory factory = new JedisConnectionFactory();
factory.afterPropertiesSet();

RedisTemplate<String, String> redisTemplate = new StringRedisTemplate(factory);
redisTemplate.afterPropertiesSet();

redisTemplate.opsForValue().set("foo", "bar");

// 定义脚本
DefaultRedisScript<String> script = new DefaultRedisScript<>();
script.setScriptText("return redis.call('get', KEYS[1])");
script.setResultType(String.class);
script.afterPropertiesSet();

String bar = redisTemplate.execute(script, Collections.singletonList("foo"));
System.out.println(bar);
```

# Read More

- [Redis系列二 - 通过redis命令和lua实现分布式锁](https://juejin.im/entry/5a5f3a496fb9a01cbe655abf)
- [Redis与Lua及Redis-py应用Lua](https://segmentfault.com/a/1190000007892988)
- []()
- [EVAL script numkeys key [key ...] arg [arg ...]](http://www.redis.cn/commands/eval.html)
- [Redis Lua 脚本](https://redisbook.readthedocs.io/en/latest/feature/scripting.html)
- []()
- [Redis Lua 脚本调试器用法说明](http://blog.huang.me/2017/redis-lua-debuger-introduction.html)
- [Redis Lua scripts debugger](https://redis.io/topics/ldb)