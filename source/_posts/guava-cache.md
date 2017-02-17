---
title: Guava Cache
date: 2017-01-02 00:00:00
desc: Guava Cache,Guava 缓存

tags: [Guava,缓存,Cache]

---
Guava Cache 可以是实现以下功能

- LRU 回收
- 缓存超时
- 引用回收
- 缓存失效监听
- 缓存统计
- 权重
- 失效重新缓存
- 其他


<!--more-->

### 参数配置

``` java
Cache<String, String> cache = CacheBuilder.newBuilder()
    
    // ❤ 缓存的最大容量，如果超过的话回使用 LRU(最近最少使用) 算法进行淘汰  
    .maximumSize(100)
    .expireAfterAccess(10, TimeUnit.SECONDS) // 缓存项在给定时间内没有被读/写访问，则回收。请注意这种缓存的回收顺序和基于大小回收一样。  
  
  
    // ❤
    .expireAfterWrite(10, TimeUnit.SECONDS)  // 缓存项在给定时间内没有被写访问（创建或覆盖），则回收。
                                             // 如果认为缓存数据总是在固定时候后变得陈旧不可用，这种回收方式是可取的。
    .refreshAfterWrite(10, TimeUnit.SECONDS) // 可以为缓存增加自动定时刷新功能。
                                             // 和expireAfterWrite相反，refreshAfterWrite通过定时刷新可以让缓存项保持可用，
                                             // 但请注意：缓存项只有在被检索时才会真正刷新
                                             // （如果CacheLoader.refresh实现为异步，那么检索不会被刷新拖慢）。
                                             // 因此，如果你在缓存上同时声明expireAfterWrite和refreshAfterWrite，缓存并不会因为刷新盲目地定时重置，
                                             // 如果缓存项没有被检索，那刷新就不会真的发生，缓存项在过期时间后也变得可以回收。
  
  
    // ❤
    .weakKeys()   // 使用弱引用存储键。当键没有其它（强或软）引用时，缓存项可以被垃圾回收。
                  // 因为垃圾回收仅依赖恒等式（==），使用弱引用键的缓存用==而不是equals比较键。
    .weakValues() // 使用弱引用存储值。当值没有其它（强或软）引用时，缓存项可以被垃圾回收。
                  // 因为垃圾回收仅依赖恒等式（==），使用弱引用值的缓存用==而不是equals比较值。
    .softValues() // 使用软引用存储值。软引用只有在响应内存需要时，才按照全局最近最少使用的顺序回收。
                  // 考虑到使用软引用的性能影响，我们通常建议使用更有性能预测性的缓存大小限定（见上文，基于容量回收）。使用软引用值的缓存同样用==而不是
  
  
  
    // 声明一个监听器，在缓存项被移除时做一些额外操作[RemovalListeners.asynchronous(RemovalListener,Executor); // 异步监听]
    .removalListener(new RemovalListener<String, String>() {
        @Override
        public void onRemoval(RemovalNotification<String, String> notification) {
            System.out.println(notification.getKey() + ":" + notification.getValue());
        }
    })
  
  
    .recordStats() // 开启缓存统计功能
    .initialCapacity(10) // 缓存初始容量
    .concurrencyLevel(10) // 允许的并发操作数，默认是4
    .ticker(Ticker.systemTicker()) // 执行时间源，默认是当前系统时间
  
  
    // 设置键的权重信息
    .maximumWeight(100)
    .weigher(new Weigher<String, String>() {
        @Override
        public int weigh(String key, String value) {
            return 0;
        }
    })
  
  
    // ❤ 如果没有缓存，从以下操作中获得
    .build(new CacheLoader<String, String>() {
        public String load(String key) {
            return "123";
        }
    });
```

### 通过字符串配置

``` java
//通过CacheBuilderSpec实例构造CacheBuilder实例
Cache<String, String> cache = CacheBuilder.from("maximumSize=10,expireAfterAccess=5m,softValues").build();
```

#### 可配置项

`CacheBuilderSpec` 代码片段

``` java
private static final ImmutableMap<String, ValueParser> VALUE_PARSERS =
  ImmutableMap.<String, ValueParser>builder()
      .put("initialCapacity", new InitialCapacityParser())
      .put("maximumSize", new MaximumSizeParser())
      .put("maximumWeight", new MaximumWeightParser())
      .put("concurrencyLevel", new ConcurrencyLevelParser())
      .put("weakKeys", new KeyStrengthParser(Strength.WEAK))
      .put("softValues", new ValueStrengthParser(Strength.SOFT))
      .put("weakValues", new ValueStrengthParser(Strength.WEAK))
      .put("recordStats", new RecordStatsParser())
      .put("expireAfterAccess", new AccessDurationParser())
      .put("expireAfterWrite", new WriteDurationParser())
      .put("refreshAfterWrite", new RefreshDurationParser())
      .put("refreshInterval", new RefreshDurationParser())
      .build();
```
#### 时间单位

`CacheBuilderSpec` 代码片段

``` java
switch (lastChar) {
  case 'd':
    timeUnit = TimeUnit.DAYS;
    break;
  case 'h':
    timeUnit = TimeUnit.HOURS;
    break;
  case 'm':
    timeUnit = TimeUnit.MINUTES;
    break;
  case 's':
    timeUnit = TimeUnit.SECONDS;
    break;
  default:
    throw new IllegalArgumentException(
        format(
            "key %s invalid format.  was %s, must end with one of [dDhHmMsS]", key, value));
}
```

### 操作

#### 获取缓存

``` java
cache.get("key"); // 该方法会有一个显式的 `ExecutionException` 异常
  
cache.getUnchecked("key"); //该方法不需要捕获异常， 如果创建 `LoadingCache` 的时候，`CacheLoader.load` 没有抛出一个显式的异常，可以用该方法
  
// 类似于
// String value = map.get("key");
// if (null == value) {
//     value = selectFromDb(); // 从其他地方查询数据
// }
cache.get("key", new Callable<String>() {
    @Override
    public String call() throws Exception {
        return selectFromDb(); // 从其他地方查询数据
    }
});
  
// 以上方法如果不存在会再从其他地方查询放入缓存， 一下方法如果不存直接返回null
cache.getIfPresent("key");
```

#### 其他操作

``` java
cache.put("key", "value"); // 设置缓存
  
ConcurrentMap<String, String> stringStringConcurrentMap = cache.asMap(); // 缓存的 Map 形式
  
CacheStats stats = cache.stats(); // 缓存的统计信息
  
cache.invalidate("key"); // 移除指定的转存
cache.invalidateAll(Arrays.asList("key1", "key2")); // 移除多个
cache.invalidateAll(); // 移除所有
```



### 参考
>[[Google Guava] 3-缓存](http://ifeve.com/google-guava-cachesexplained/)
>
>[CachesExplained](https://github.com/google/guava/wiki/CachesExplained)  
