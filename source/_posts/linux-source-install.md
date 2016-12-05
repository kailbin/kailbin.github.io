---
title: 源码软件包编译安装方式
date: 2016-12-01 00:00:00
desc: 源码软件包编译安装方式
---

通常编译可以获得该软件的最新版本，但是源码安装通常安装比较麻烦，特别是解决依赖经常会出现问题。

<!--more-->

##### 安装

源码编译安装一般三个步骤

    1. ./configure           # 建立Makefile文件
     2. make                  # 编译
     3. make install          # 安装

##### 卸载

如果 MakeFile提供 uninstall 文件，可以通过 `make uninstall` 卸载软件


##### 配置

有些可以使用 `./configure --prefix='File Path'` 命令，将软件安装在File Path位置，这样可以方便删除。
关于configure的更多参数可以查看软件包中列如ReadMe文件或INSTALL文件，一般有参数设置说明。

    
    
> [Git 源码方式安装](/post/2016-09-15-git-source-install.html)
> [Nginx 编译 安装笔记](/post/2016-06-25-nginx-install-in-linux.html)


