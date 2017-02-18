---

title: Redis Pub/Sub
date: 2016-12-18
desc: Redis Pub Sub,发布,订阅

tags: [Redis]

---

　Redis的 `发布/订阅` 功能可用于构建即时通信应用，比如网络聊天室、实时广播、实时提醒等。

<!--more-->

# 命令概述

## SUBSCRIBE & PSUBSCRIBE

订阅给定的一个或多个频道的信息，不同之处在于 `PSUBSCRIBE` 可以模式（模糊）订阅，比如 it* 匹配所有以 it 开头( it.news、it.blog、……)的频道。

SUBSCRIBE  channel_1 [channel_2 ...]
PSUBSCRIBE pattern_1 [pattern_2 ...]

## PUBLISH

将信息 message 发送到指定的频道(channel)

**时间复杂度：**
O(N+M)，其中 N 是频道 channel 的订阅者数量，而 M 则是使用模式订阅(subscribed patterns)的客户端的数量。


**返回值：**
接收到信息 message 的订阅者数量。


## UNSUBSCRIBE & PUNSUBSCRIBE

`UNSUBSCRIBE [channel [channel ...]]` or `PUNSUBSCRIBE [pattern [pattern ...]]`


指示客户端退订所有给定 模式/频道。

如果没有注定参数，那么客户端订阅的所有 模式/频道 都会被退订。在这种情况下，命令会返回一个信息，告知客户端所有被退订的 模式/频道。

## PUBSUB

`PUBSUB` 由三个子命令组成(`CHANNELS`/`NUMSUB`/`NUMPAT`)，需要配合使用。

**CHANNELS：** 查看被订阅的频道

`PUBSUB CHANNELS` or `PUBSUB CHANNELS it.*`

    1) "it.sport"
    2) "it.internet"


**NUMSUB：** 查看频道订阅者的数量

```bash
PUBSUB NUMSUB it.*
```
    1) "it.news"    # 频道
    2) "2"          # 订阅该频道的客户端数量
    3) "it.interne
    4) 1


**NUMPAT：** 查看有多少种订阅模式


# 发布/订阅模式的结构

结构用json表示

## SUBSCRIBE 

```json
{
    "news":["client1","client2"],
    "sport":["client2"]
}
```
其类似于一个Map的结构，键为`channel`，值为订阅的客户端。

`PUBLISH` 的时候，找到对应的键，遍历其值，发送消息即可。即是上面介绍 `PUBLISH` 时间复杂度中的`N`。


## PSUBSCRIBE

```json
[
    {
        "pattern":"news.*",
        "client":"client1"
    },
    {
        "pattern":"news.*",
        "client":"client2"
    },
    {
        "pattern":"it.*",
        "client":"client1"
    }
]
```
其类似于一个列表，列表中每个对象包含 模式和客户端 的对应信息。发布的时候需要与每一个对象的模式进行匹配，如果匹配上的话就推送信息给客户端。

感觉以上结构还可以调整为如下结构：
```json
[
    {
        "pattern":"news.*",
        "clients":["client1","client2"]
    },
    {
        "pattern":"it.*",
        "clients":["client1"]
    }
]
```
或者

```json
{
    "news.*":["client1","client2"],
    "it.*":["client1"]
}
```

这样模式匹配的时候可以少匹配几次，不知道作者为什么没有这样设计？

> [Redis 发布/订阅机制原理分析](http://blog.csdn.net/clh604/article/details/19754939)
> [huangz1990/reading_redis_source](https://github.com/huangz1990/reading_redis_source/blob/master/pubsub.c_redis_2.9.7_b62bdf1c/pubsub.c)
> [订阅发布机制](http://wiki.jikexueyuan.com/project/redis/subscribe-to-release-mechanism.html)
> [黄健宏 Github](https://github.com/huangz1990)

# 客户端的使用

## Jedis

```xml
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>2.9.0</version>
</dependency>
```

``` java
Jedis jedis = new Jedis();
  
JedisPubSub jedisPubSub = new JedisPubSub() {
  
    @Override
    public void onPMessage(String pattern, String channel, String message) {
        System.out.println(pattern + ":" + channel + ":" + message);
    }
  
    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
        System.out.println("onUnsubscribe::" + pattern + ": subscribedChannels:" + subscribedChannels);
    }
};
  
new Thread(() -> jedis.psubscribe(jedisPubSub, "hello", "sub.*")).start();
  
  
Thread.sleep(30000);
System.out.println("subscribe");
jedisPubSub.punsubscribe();
```

**输入**
```bash
PUBLISH sub.asd asd
PUBLISH hello asd
```

**输出**

    sub.*:sub.asd:asd                                  # `PUBLISH sub.asd asd` -> onPMessage
    hello:hello:asd                                     # `PUBLISH hello asd`   -> onPMessage
    
    subscribe                                           # System.out.println("subscribe");
    onUnsubscribe::hello: subscribedChannels:1          # onPUnsubscribe
    onUnsubscribe::sub.*: subscribedChannels:0          # onPUnsubscribe

# 参考
> [Pub/Sub（发布/订阅）](http://doc.redisfans.com/pub_sub/index.html)
>
> [Redis 设计与实现](http://redisbook.com/index.html)


