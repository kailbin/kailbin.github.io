---
title: h2 几种连接模式
categories:
  - Tools
date: 2018-10-28
id: h2-connect-model
---

H2 Database 是一款轻量级的内嵌数据库，功能十分强大，纯Java编写，运行时只需要一个jar包即可。

<!-- more -->

这里记录了 H2 的几种使用方式，用于备忘。

# 嵌入模式

在嵌入模式下，应用程序使用JDBC在同一JVM中使用数据库。这是最快速，最简单的连接模式。缺点是只能在一个虚拟机（和类加载器）中打开。与所有模式一样，支持持久性和内存数据库。同时打开的数据库数或打开的连接数量没有限制。

### 内存模式

内存模式不进行数据持久化，是几种模式中最快的，常见的使用常见有：

- 作为单元测试的数据源，可以通过 `INIT` 参数创建初始化数据
  - `INIT=runscript from '~/create.sql'\\;runscript from '~/init.sql'"`
- 可以看做**支持 SQL 查询的本地缓存**
- ...

链接字符串 `jdbc:h2:mem` 或 `jdbc:h2:mem:<databaseName>`，如果 `<databaseName>` 不写，则每次创建链接都是新的数据库。

### 文件模式

文件模式会在本地磁盘创建数据文件 对数据进行持久化。

链接字符串：`jdbc:h2:[file:][<path>]<databaseName>`

# 服务器模式


使用服务器模式（有时称为 远程模式 或 C/S 模式）时，应用程序使用JDBC或ODBC API远程打开数据库。 需要在相同或另一个虚拟机或另一台计算机上启动服务器。应用程序可以通过连接到此服务器上数据库。 实际在服务器在内部，进程以嵌入模式打开数据库。

服务器模式比嵌入模式慢，因为所有数据都通过 TCP/IP 传输。与所有模式一样，支持持久性和内存数据库。 每台服务器并发打开的数据库数量或打开的连接数量没有限制。

### 启动服务
``` bash
$H2_HOME/bin/h2.sh
```
启动之后会自动打开浏览器，访问Web工具
- 切换语言
- 配置
    - 允许来自其他远程计算机的连接
    - 使用非加密的 HTTP 连接
    - 配置Web服务端口号
    - 查看链接信息
    - ...
- 工具
    - 备份
    - 还原
    - 创建集群
    - 执行脚本
    - ...
- 帮助

设置 JDBC URL `jdbc:h2:tcp://localhost/~/DB/data-h2/db_kail`，输入用户名密码（如果是首次则是创建用户名密码），点击 Connect ，会在 目录`~/DB/data-h2/`下创建数据库文件 `db_kail.mv.db`。

> [Spring 的方式启动 TCP Server](http://www.h2database.com/html/tutorial.html#spring)

# 混合模式

混合模式是嵌入式和服务器模式的组合。

连接到数据库的第一个应用程序在嵌入模式下执行此操作，但也启动服务器，以便其他应用程序（在不同进程或虚拟机中运行）可以同时访问相同的数据。本地连接的速度与仅在嵌入模式下使用数据库的速度一样快，而远程连接速度稍慢。

可以在应用程序内（使用服务器API）或自动（自动混合模式）启动和停止服务器。使用自动混合模式时，所有想要连接到数据库的客户端（无论是本地连接还是远程连接）都可以使用完全相同的数据库URL。

启动混合模式只需要在 链接字符串后添加  `AUTO_SERVER=TRUE` 参数即可，该参数不适合加在内存模式下，主要**针对文件模式**。通过`AUTO_SERVER_PORT=8083`指定端口。

> 混合模式在实际测试时候并没有成功，一直提示 “Wrong user name or password [28000-197] 28000/28000 ” 用户名或密码错误，暂未找到原因

# Read More
- 官方：http://www.h2database.com/
- [功能对比](http://www.h2database.com/html/features.html#comparison)
- [Database URL Overview](http://www.h2database.com/html/features.html#database_url)