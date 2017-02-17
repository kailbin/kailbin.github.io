---
title: apache httpd ab
date: 2017-01-15 00:00:00
desc: apache httpd ab,压力测试

tags: [apache,常用命令,测试,ab]

---

ab 是 **Apache HTTP 服务器性能基准工具(Apache HTTP server benchmarking tool)**，可以进行简单的压力测试。 

<!--more-->

### 下载安装

#### Linux

``` bash
apt-get install apache2-utils 
```
默认如果安装 Apache httpd 的话，自带的有。

#### Windows

[http://httpd.apache.org/](http://httpd.apache.org/) > [Download](http://httpd.apache.org/download.cgi) > [Files for Microsoft Windows](http://httpd.apache.org/docs/current/platform/windows.html#down) > [ApacheHaus](http://www.apachehaus.com/cgi-bin/download.plx) > [**Apache 2.4.25 x86**](http://www.apachehaus.com/downloads/httpd-2.4.25-x86.zip)

下载之后，找到 httpd-2.4.25-x86.zip/**Apache24/bin/**`ab.exe`。


### 基本使用

使用 ab 主要关心两个参数：`-n`和`-c`。

`-n` 指：发送多少次请求。 建议最好超过 50000.
`-c` 指：请求时的并发数。最大不要超过1024，操作系统每个进程下打开的文件数是有限制的，Linux 下可以通过 `ulimit -a`  查看。

### 报告

``` bash
$ ab -n 100 -c 10 http://blog.kail.xyz/  
  
This is ApacheBench, Version 2.3 <$Revision: 1528965 $>
Copyright 1996 Adam Twiss, Zeus Technology Ltd, http://www.zeustech.net/
Licensed to The Apache Software Foundation, http://www.apache.org/
  
Benchmarking blog.kail.xyz (be patient).....done  
  
Server Software:
Server Hostname:        blog.kail.xyz
Server Port:            80
  
Document Path:          /
Document Length:        15644 bytes                                             # 请求返回的正文长度，不包括相应头  
  
Concurrency Level:      10                                                      # 并发个数
Time taken for tests:   4.206 seconds                                           # 总请求时间 
Complete requests:      100                                                     # 总请求数
Failed requests:        0                                                       # 失败的请求数
Total transferred:      1586900 bytes                                           # 请求返回的正文长度 + 响应头
HTML transferred:       1564400 bytes
Requests per second:    23.78 [#/sec] (mean)                                    #! 平均每秒的请求数 = 吞吐率
Time per request:       420.599 [ms] (mean)                                     # 用户平均请求等待时间 
Time per request:       42.060 [ms] (mean, across all concurrent requests)      # 用户平均请求等待时间/并发数 = 1/吞吐率
Transfer rate:          368.45 [Kbytes/sec] received                            # 传输速率  
  
Connection Times (ms)
              min  mean[+/-sd] median   max 
Connect:       34   41   7.6     38      75
Processing:    43  357 141.4    314     736
Waiting:       36  212 103.9    201     429
Total:        103  397 140.8    356     775
  
Percentage of the requests served within a certain time (ms)                    # 直译：在一定时间内送达请求的百分比     
  50%    356
  66%    383
  75%    388
  80%    405
  90%    653
  95%    729
  98%    775
  99%    775
 100%    775 (longest request)
```

### 详细参数

``` bash
ab 
    [ -c concurrency ]                              # 并发请求数
    [ -n requests ]                                 # 指定发起的请求数
    [ -t timelimit ]                                # 测试持续多长时间（Seconds to max. to spend on benchmarking，This implies -n 50000）  
  
  
    [ -A auth-username:password ]                   # 指定连接服务器的基本的认证凭据
    [ -b windowsize ]                               # Size of TCP send/receive buffer, in bytes
    [ -B local-address ]                            # Address to bind to when making outgoing connections
    [ -C cookie-name=value ]                        # 指定cookie
    [ -d ]                                          # Do not show percentiles served table.
    [ -e csv-file ]                                 # Output CSV file with percentages served
    [ -f protocol ]                                 # Specify SSL/TLS protocol (SSL3, TLS1, TLS1.1, TLS1.2 or ALL)
    [ -g gnuplot-file ]                             # 将测试结果输出为“gnuolot”文件，其数据可以产生一个统计图 > http://gnuplot.info/
    [ -h ]                                          # 查看帮助
    [ -H custom-header ]                            # 自定义请求头，例如：'Accept-Encoding: gzip'
    [ -i ]                                          # Use HEAD instead of GET
    [ -k ]                                          # 使用 HTTP keepAlive 特性
    [ -l ]                                          # 接受可变的文本长度 (use this for dynamic pages)
    [ -m HTTP-method ]                              # 设置请求方式
    [ -p POST-file ]                                # POST 的时候发送的数据，可配合 -T 使用
    [ -P proxy-auth-username:password ]             # Add Basic Proxy Authentication, the attributes are a colon separated username and password.
    [ -q ]                                          # 不显示进度百分比
    [ -r ]                                          # Don't exit on socket receive errors
    [ -s timeout ]                                  # 设置超时时间
    [ -S ]                                          # Do not show confidence estimators and warnings.
    [ -T content-type ]                             # 当使用 POST/PUT 发送数据的时候，可设置Content-type 头, 例如：'application/x-www-form-urlencoded'，默认是：'text/plain'
    [ -u PUT-file ]                                 # PUT 的时候发送的数据，可配合 -T 使用
    [ -v verbosity]                                 # 设置详细模式等级
    [ -V ]                                          # 查看版本信息
    [ -w ]                                          # 以HTML table 方式打印结果
    [ -x <table>-attributes ]                       # 以TML table 方式输出时，设置 table 的属性
    [ -X proxy[:port] ]                             # 使用指定的代理服务器发送请求
    [ -y <tr>-attributes ]                          # 以TML table 方式输出时，设置 tr 的属性
    [ -z <td>-attributes ]                          # 以TML table 方式输出时，设置 td 的属性
    [ -Z ciphersuite ]                              # Specify SSL/TLS cipher suite (See openssl ciphers)
    
    [http[s]://]hostname[:port]/path
```

### 其他工具

#### [WebBench](https://github.com/EZLippi/WebBench)

Webbench是一个在linux下使用的非常简单的网站压测工具。它使用fork()模拟多个客户端同时访问我们设定的URL，测试网站在压力下工作的性能，最多可以模拟3万个并发连接去测试网站的负载能力。

> https://github.com/EZLippi/WebBench

#### [siege](https://www.joedog.org/siege-manual/)

siege 一款开源的压力测试工具，可以根据配置对一个WEB站点进行多用户的并发访问，记录每个用户所有请求过程的相应时间，并在一定数量的并发访问下重复进行。

> https://www.joedog.org/siege-manual/

#### ulimit命令
> 来自Linux命令大全： [http://man.linuxde.net/ulimit](http://man.linuxde.net/ulimit)
    

``` bash
[root@localhost ~]# ulimit -a  
  
core file size (blocks, -c) 0                                      # core文件的最大值为100 blocks。 
data seg size (kbytes, -d) unlimited                               # 进程的数据段可以任意大。 
scheduling priority (-e) 0 file size (blocks, -f) unlimited        # 文件可以任意大。 
pending signals (-i) 98304                                         # 最多有98304个待处理的信号。 
max locked memory (kbytes, -l) 32                                  # 一个任务锁住的物理内存的最大值为32KB。 
max memory size (kbytes, -m) unlimited                             # 一个任务的常驻物理内存的最大值。 
open files (-n) 1024                                               # 一个任务最多可以同时打开1024的文件。 
pipe size (512 bytes, -p) 8                                        # 管道的最大空间为4096字节。 
POSIX message queues (bytes, -q) 819200                            # POSIX的消息队列的最大值为819200字节。 
real-time priority (-r) 0 stack size (kbytes, -s) 10240            # 进程的栈的最大值为10240字节。 
cpu time (seconds, -t) unlimited                                   # 进程使用的CPU时间。 
max user processes (-u) 98304                                      # 当前用户同时打开的进程（包括线程）的最大个数为98304。 
virtual memory (kbytes, -v) unlimited                              # 没有限制进程的最大地址空间。 
file locks (-x) unlimited                                          # 所能锁住的文件的最大个数没有限制。
```

选项

``` bash  
-a  显示目前资源限制的设定； 
-c  设定core文件的最大值，单位为区块； 
-d  <数据节区大小>：程序数据节区的最大值，单位为KB； 
-f  <文件大小>：shell所能建立的最大文件，单位为区块； 
-H  设定资源的硬性限制，也就是管理员所设下的限制； 
-m  <内存大小>：指定可使用内存的上限，单位为KB； 
-n  <文件数目>：指定同一时间最多可开启的文件数； 
-p  <缓冲区大小>：指定管道缓冲区的大小，单位512字节； 
-s  <堆叠大小>：指定堆叠的上限，单位为KB； 
-S  设定资源的弹性限制； 
-t  指定CPU使用时间的上限，单位为秒； 
-u  <程序数目>：用户最多可开启的程序数目； 
-v  <虚拟内存大小>：指定可使用的虚拟内存上限，单位为KB。

```
> 

### 参考
> [官方文档（ab - Apache HTTP server benchmarking tool）](http://httpd.apache.org/docs/2.4/programs/ab.html)
>
> [ab参数详解 - 压力测试](http://www.linuxeye.com/Linux/488.html)
>
> [apache的ab命令做压力测试](http://johnnyhg.iteye.com/blog/523818)

