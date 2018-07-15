---
title: 使用 SIGAR 获取操作系统信息
tags:
  - Java
categories:
  - Java
date: 2018-07-15
---

Sigar（System Information Gatherer And Reporter）是一个开源的工具，提供了跨平台的系统信息收集的API，核心由C语言实现的。

可以收集的信息包括：
1. CPU信息： 包括基本信息（vendor、model、mhz、cacheSize）和统计信息（user、sys、idle、nice、wait）
2. 文件系统信息： 包括Filesystem、Size、Used、Avail、Use%、Type
3. 事件信息： 类似Service Control Manager
4. 内存信息： 物理内存和交换内存的总数、使用数、剩余数；RAM的大小
5. 网络信息： 包括 网络接口信息 和 网络路由 信息
6. 进程信息： 包括每个进程的内存、CPU占用数、状态、参数、句柄
7. IO信息： 包括IO的状态，读写大小等
8. 服务状态信息
9. 系统信息： 包括操作系统版本，系统资源限制情况，系统运行时间以及负载，JAVA的版本信息等.

> [系统信息收集API Sigar](https://www.oschina.net/p/sigar)

<!-- more -->

# 添加 Maven 依赖

``` xml
<dependency>
    <groupId>org.fusesource</groupId>
    <artifactId>sigar</artifactId>
    <version>1.6.4</version>
</dependency>
```

# 下载 本地库 依赖

- 下载地址：https://sourceforge.net/projects/sigar/files/sigar/1.6/hyperic-sigar-1.6.4.zip
- 解压后，拷贝 `hyperic-sigar-1.6.4/sigar-bin/lib` 文件夹 到 maven 工程的 `resources` 文件夹下
- 删除无用的文件 `.sigar_shellrc`、`log4j.jar`、`sigar.jar`，
- 重命名 `lib` 文件夹为 `sigar`

```bash
resources/
└── sigar
    ├── libsigar-amd64-freebsd-6.so
    ├── libsigar-amd64-linux.so
    ├── libsigar-amd64-solaris.so
    ├── libsigar-ia64-hpux-11.sl
    ├── libsigar-ia64-linux.so
    ├── libsigar-pa-hpux-11.sl
    ├── libsigar-ppc-aix-5.so
    ├── libsigar-ppc-linux.so
    ├── libsigar-ppc64-aix-5.so
    ├── libsigar-ppc64-linux.so
    ├── libsigar-s390x-linux.so
    ├── libsigar-sparc-solaris.so
    ├── libsigar-sparc64-solaris.so
    ├── libsigar-universal-macosx.dylib
    ├── libsigar-universal64-macosx.dylib
    ├── libsigar-x86-freebsd-5.so
    ├── libsigar-x86-freebsd-6.so
    ├── libsigar-x86-linux.so
    ├── libsigar-x86-solaris.so
    ├── sigar-amd64-winnt.dll
    ├── sigar-x86-winnt.dll
    └── sigar-x86-winnt.lib

1 directory, 23 files

```

以上做法是直接拷贝到了运行程序 classpath，最好是打成 jar 包，通过 Maven 依赖进来。


# Siger 工具类

``` java

import org.hyperic.sigar.Sigar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;

public class SigarUtil {

    private static Logger logger = LoggerFactory.getLogger(SigarUtil.class);

    static {

        // Linux MacOS 分隔符 : Windows 是;
        String osName = System.getProperty("os.name", "generic").toLowerCase();
        String splitSymbol = osName.contains("win") ? ";" : ":";

        // 寻找 classpath 根目录下的 sigar 文件夹
        URL sigarURL = SigarUtil.class.getResource("/sigar");
        if (null == sigarURL) {
            // 找不到抛异常
            throw new MissingResourceException("miss classpath:/sigar folder", SigarUtil.class.getName(), "classpath:/sigar");
        }
        
        File classPath = new File(sigarURL.getFile());
        String oldLibPath = System.getProperty("java.library.path");

        try {
            // 追加库路径
            String newLibPath = oldLibPath + splitSymbol + classPath.getCanonicalPath();
            System.setProperty("java.library.path", newLibPath);

            logger.info("set sigar java.library.path={}", newLibPath);

        } catch (IOException e) {
            logger.error("append sigar to java.library.path error", e);
        }
    }

    private static class SingleSigar {
        private static final Sigar SIGAR = new Sigar();
    }

    public static Sigar getInstance() {
        return SingleSigar.SIGAR;
    }

}
```

# Demo

``` java
Sigar sigar = SigarUtil.getInstance();

// 打印系统负载
double[] loadAverage = sigar.getLoadAverage();
for (double l : loadAverage) {
    System.out.println(l);
}
```

API 文档可以从 `hyperic-sigar-1.6.4/docs/javadoc` 找到



# Cli 功能

启动方式: `java -jar hyperic-sigar-1.6.4/sigar-bin/lib/sigar.jar`

支持的交互命令：
``` bash
sigar> help
Available commands:
	alias          - Create alias command
	cpuinfo        - Display cpu information
	df             - Report filesystem disk space usage
	du             - Display usage for a directory recursively
	free           - Display information about free and used memory
	get            - Get system properties
	help           - Gives help on shell commands
	ifconfig       - Network interface information
	iostat         - Report filesystem disk i/o
	kill           - Send signal to a process
	ls             - simple FileInfo test at the moment (like ls -l)
	mps            - Show multi process status
	netinfo        - Display network info
	netstat        - Display network connections
	nfsstat        - Display nfs stats
	pargs          - Show process command line arguments
	penv           - Show process environment
	pfile          - Display process file info
	pidof          - Find the process ID of a running program
	pinfo          - Display all process info
	pmodules       - Display process module info
	ps             - Show process status
	quit           - Terminate the shell
	route          - Kernel IP routing table
	set            - Set system properties
	sleep          - Delay execution for the a number of seconds
	source         - Read a file, executing the contents
	sysinfo        - Display system information
	time           - Time command
	ulimit         - Display system resource limits
	uptime         - Display how long the system has been running
	version        - Display sigar and system version info
	who            - Show who is logged on
```

也可以通过参数直接调用指定的命令
``` bash
# 显示系统运行的时间
$ java -jar hyperic-sigar-1.6.4/sigar-bin/lib/sigar.jar uptime

  1:30 下午  up 12 days, 2:15, load average: 5.73, 3.38, 3.13
```




# Read More

- [Sigar使用](http://jiangpz.github.io/articles/2015-11/sigar)
- [Sigar 获取CPU和Memory内存等信息使用详解](https://blog.csdn.net/a123demi/article/details/50689265)
- [借助Sigar API获取网络信息](https://blog.csdn.net/gaohuanjie/article/details/43984463)





