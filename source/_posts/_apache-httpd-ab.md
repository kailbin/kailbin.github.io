---
title: apache httpd ab
date: 2017-01-15 00:00:00
desc: apache httpd ab,压力测试
---

ab 是 **Apache HTTP 服务器性能基准工具(Apache HTTP server benchmarking tool)**，可以进行简单的压力测试。 Linux 下安装 `httpd` 的话默认自带的有。 
本文主要是在 Windows 下使用。

<!--more-->

### Windows 版下载地址

[http://httpd.apache.org/](http://httpd.apache.org/) > [Download](http://httpd.apache.org/download.cgi) > [Files for Microsoft Windows](http://httpd.apache.org/docs/current/platform/windows.html#down) > [ApacheHaus](http://www.apachehaus.com/cgi-bin/download.plx) > [**Apache 2.4.25 x86**](http://www.apachehaus.com/downloads/httpd-2.4.25-x86.zip)

下载之后，找到 httpd-2.4.25-x86.zip/**Apache24/bin/**`ab.exe`，拷贝到指定目录下。


### 基本使用

使用 ab 主要关心两个参数：`-n`和`-c`。

`-n` 指：发送多少次请求。 建议最好超过 50000.
`-c` 指：请求时的并发数。最大不要超过1024，操作系统每个进程下打开的文件数是有限制的，Linux 下可以通过 `ulimit -a`  查看。

### 报告



Request per second，即吞吐率
Time per request，即平均请求等待时间
Time per request（across all concurrent requests），即平均请求处理时间




### 详细参数

``` base
ab 
    [ -A auth-username:password ] 
    [ -b windowsize ] 
    [ -B local-address ] 
    [ -c concurrency ] 
    [ -C cookie-name=value ] 
    [ -d ] 
    [ -e csv-file ] 
    [ -f protocol ] 
    [ -g gnuplot-file ] 
    [ -h ] 
    [ -H custom-header ] 
    [ -i ] 
    [ -k ] 
    [ -l ] 
    [ -m HTTP-method ] 
    [ -n requests ] 
    [ -p POST-file ] 
    [ -P proxy-auth-username:password ] 
    [ -q ] 
    [ -r ] 
    [ -s timeout ] 
    [ -S ] 
    [ -t timelimit ] 
    [ -T content-type ] 
    [ -u PUT-file ] 
    [ -v verbosity] 
    [ -V ] 
    [ -w ] 
    [ -x <table>-attributes ] 
    [ -X proxy[:port] ] 
    [ -y <tr>-attributes ] 
    [ -z <td>-attributes ] 
    [ -Z ciphersuite ] 
    [http[s]://]hostname[:port]/path
```

### 其他

> http://www.jb51.net/article/72817.htm
> http://blog.csdn.net/sscsgss/article/details/47679691
> http://www.cnblogs.com/zengxiangzhan/archive/2012/12/07/2807141.html
> http://www.jb51.net/article/59469.htm
> http://onlyzq.blog.51cto.com/1228/516916/

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
> [apache的ab命令做压力测试](http://johnnyhg.iteye.com/blog/523818)
>

<script>document.getElementsByClassName("post-seo-from")[0].outerHTML=""</script>