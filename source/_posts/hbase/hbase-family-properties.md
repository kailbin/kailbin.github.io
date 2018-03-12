---
title: HBase 列族配置
tags:
  - HBase
categories:
  - HBase
date: 2018-03-12 21:12:09
---


创建一个测试表（`test`），列族名为 `f`
``` bash
hbase> create 'test', {NAME => 'f'}
```

其他默认属性如下：
``` bash
hbase(main):003:0> describe 'test'

{
 NAME => 'f', BLOOMFILTER => 'ROW', VERSIONS => '1', IN_MEMORY => 'false',
 KEEP_DELETED_CELLS => 'FALSE', DATA_BLOCK_ENCODING => 'NONE', TTL => 'FOREVER', 
 COMPRESSION => 'NONE', MIN_VERSIONS => '0', BLOCKCACHE => 'true', BLOCKSIZE => '65536', 
 REPLICATION_SCOPE => '0'
}
```

{
    BLOOMFILTER => 'ROW',                   默认是 NONE
    VERSIONS => '100000',                   设置数据版本
    MIN_VERSIONS => '0',                    最新版本数，如果所有的版本都超期了，要保留的最新的版本数
    TTL => 'FOREVER',                       超时的数据会在下一次大合并的时候被删除，单位是s
    COMPRESSION => 'NONE',                  是否压缩：SNAPPY、LZO、GZIP，数据只在写一硬盘的时候进行压缩，内存和网络硬盘中不进行压缩
    BLOCKSIZE => '65536',                   数据块大小：默认 65536
    BLOCKCACHE => 'true'                    数据块缓存：默认 false
    IN_MEMORY => 'false',                   ？？？？？
    REPLICATION_SCOPE => '0', 
    DATA_BLOCK_ENCODING => 'NONE',          
    KEEP_DELETED_CELLS => 'FALSE', 
}


# 基本属性

## BLOOMFILTER(布隆过滤器)

**布隆过滤器 可以判断数据不存在**，当判断结果存在时，数据可能真的存在，也可能不存在。可选的值有 三个 `NONE`, `ROW` (default), `ROWCOL`。

当开启 布隆过滤器 时，**可以判断要查询的数据不存在在表中**，从而避免查表。

行级（`ROW`）布隆过滤器在数据块里**检查特定行键是否不存在**，列标识符级（`ROWCOL`）布隆过滤器**检查行和列标识符联合体是否不存在**。`ROWCOL`布隆过滤器的开销高于`ROW`布隆过滤器。





## VERSIONS(Cell数据版本)

0.96版本默认是3个， 0.98版本之后是1， 要根据业务来划分，版本是历史记录，版本增多意味空间消耗。

插入数据的时候，版本默认是当前时间；查询的时候可以指定要获取的版本个数 `get 'test', 'rk1',  { COLUMN => 'f',  VERSIONS => 100}`；

获取多个版本的时候，多个数据是按照 时间戳倒序排序，也可以通过这个特性，来保存类似于事件发生的数据，查询时间历史的时候，拿出来的数据是按照时间排好序，如果要拿最新的事件，不指定版本即可。

版本的时间戳，也可以自定义，不使用默认生成的时间戳，可以自己指定业务相关的ID



## MIN_VERSIONS(TODO)




## IN_MEMORY(激进缓存的配置)

可以选择一个列族赋予更高的优先级缓存, 激进缓存，表示优先级更高，IN_MEMORY 默认是 false




## KEEP_DELETED_CELLS(`TODO`)

http://hbase.apache.org/book.html#cf.keep.deleted

hbase 的`delete`命令，并不是真的删除了文件，而是设置一个标记（`delete marker`）。用户在检索数据的时候，会过滤掉这些标示的数据。


``` bash
alter 'test', NAME => 'f', VERSIONS => 100

put 'test', 'k1' , 'f:c', 123
put 'test', 'k1' , 'f:c', 456
scan 'test',{VERSIONS => 100}
alter 'test' , NAME => 'f', KEEP_DELETED_CELLS => true



```







## TTL(存活时间)
当数据记录一段时间想删除掉，是以s为单位的，设置一个时间 超过后会被设置为删除标记
生存时间配置：(TTL)
超过这个时间设置的就会在下一次大合并中被删除
create "stu",{NAME => "cf",TTL =>"18000"}





## DATA_BLOCK_ENCODING

HBase最佳实践－列族设计优化: http://blog.csdn.net/javastart/article/details/51820212
HBase-0.96中新BlockEncoding算法-PREFIX_TREE压缩的初步探究及测试: http://zjushch.iteye.com/blog/1843793

## COMPRESSION(压缩)
压缩可以节省空间，读写数据会增加CPU的使用率 LZO，SNAPPY，GZIP
create "stu",{NAME => "cf",COMPRESSION =>"GZIP"}






## BLOCKCACHE(数据块缓存的配置)
数据块缓存的配置：
如果经常顺序访问或很少被访问，可以关闭列族的缓存，列族缓存默认打开
create "stu",{NAME => "cf",BLOCKCACHE =>"false"}




## BLOCKSIZE
数据块大小配置优化:   blocksize默认是64K  
  数据块越小，索引越大，占用内存也越大，   如果随机查询，比如某个表+某个ID方式，   如果是顺序scan扫描区间，那么设置大一点  否则保持默认值 
 
create "stu",{NAME => "cf",BLOCKSIZE =>"65536"} 设置stu cf列族块大小为64K，默认单位是字节，采用这种细粒度，目的是块操作时更加有效加载和缓存数据，
不依赖于hdfs块尺寸设计，仅仅是hbase内部的一个属性。

> 随着BlockSize的增大，系统随机读的吞吐量不断降低，延迟不断增大。64K大小比16K大小的吞吐量大约降低13%，延迟增大13%。
> 同样的，128K大小比64K大小的吞吐量降低约22%，延迟增大27%。
> **因此，对于以随机读为主的业务，可以适当调低BlockSize的大小，以获得更好的读性能；如果业务请求以Get请求为主，可以考虑将块大小设置较小；如果以Scan请求为主，可以将块大小调大**
> 
> [HBase最佳实践－列族设计优化](http://blog.csdn.net/javastart/article/details/51820212)


## REPLICATION_SCOPE
 

# 其他

## SPLIT


 
 
# Read More

- [113. Schema Design](http://hbase.apache.org/book.html#perf.schema)
    - [113.4. Bloom Filters](http://hbase.apache.org/book.html#schema.bloom)
    - [41. Keeping Deleted Cells](http://hbase.apache.org/book.html#cf.keep.deleted)
 

 

 
http://blog.csdn.net/qqpy789/article/details/52486964
https://yq.aliyun.com/articles/43538
https://issues.apache.org/jira/browse/HBASE-4536?spm=a2c4e.11153940.blogcont43538.7.51d469c2ez3Myi

 

 

 