---
title: dpkg 和 apt-get
date: 2016-12-04 00:00:00
desc: dpkg,apt-get

tags: [Linux,常用命令]

---

dpkg 是一种的软件包管理工具，apt-get 是 dpkg 的一个前端程序，apt-get 最主要的功能是解决软件包的依赖性问题。

<!--more-->

### dpkg



##### 安装
``` Shell
dpkg -i package-name.deb                # 安装软件包 
```

##### 删除软件包
``` Shell
dpkg -r package-name                    # 删除软件包（保留配置信息）
dpkg -P package-name                    # 删除软件包（包括配置信息）
```


##### 包信息查询
```
dpkg -c package-name.deb                # 列出软件包内容
dpkg -I package-name.deb                # 查看软件包信息
```

    
##### 搜索软件包
``` Shell
dpkg -s filename                        # 查看package-name对应的软件包信息
dpkg -I filename                        # 查看软件说明(直接使用dpkg -l 查询所有安装的软件包，filename可以使用正则，我通常用```dpkg -l | grep "filename"因为会存在软件名记不住的情况)
dpkg -L filename                        # 查看package-name对应的软件包安装的文件及目录
dpkg -S filename-pattern                # 从已经安装的软件包中查找包含filename的软件包名称
```



##### dpkg软件包相关文件介绍
```
/etc/dpkg/dpkg.cfg                       # dpkg包管理软件的配置文件
/var/log/dpkg.log                        # dpkg包管理软件的日志文件
/var/lib/dpkg/available                  # 存放系统所有安装过的软件包信息
/var/lib/dpkg/status                     # 存放系统现在所有安装软件的状态信息
/var/lib/dpkg/info                       # 记安装软件包控制目录的控制信息文件
```

##### 其他

```

```

### apt-get

```
apt-get update                            # 更新源
apt-get dist-upgrade                      # 升级系统
apt-get upgrade                           # 更新所有已经安装的软件包  
  
apt-get install package_name              # 安装软件包(加上 --reinstall重新安装)
apt-get remove                            # 移除软件包（保留配置信息）
apt-get purge package_name                # 移除软件包（删除配置信息）
  
apt-get check                             # 检查是否有损坏的依赖
```



### apt

```Shell
Usage: apt [options] command

CLI for apt.
Basic commands:
 list                                      # list packages based on package names
 search                                    # 搜索包的相关信息  
 show                                      # 获取包的相关信息

 update                                    # update list of available packages

 install                                   # install packages
 remove                                    # remove packages

 upgrade                                   # upgrade the system by installing/upgrading packages
 full-upgrade                              # upgrade the system by removing/installing/upgrading packages

 edit-sources                              # edit the source information file(/etc/apt/sources.list)

```

##### apt 软件包相关文件介绍
```
/etc/apt/sources.list                     # 记录软件源的地址
/var/cache/apt/archives                   # 已经下载到的软件包都放在这里
```

> Ubuntu镜像使用帮助 [http://mirrors.163.com/.help/ubuntu.html](http://mirrors.163.com/.help/ubuntu.html)


### aptitude 

与 apt-get 不同的是，aptitude在处理依赖问题上更佳一些。举例来说，aptitude在删除一个包时，会同时删除本身所依赖的包。这样，系统中不会残留无用的包，整个系统更为干净。
```
aptitude update                           # 更新可用的包列表
aptitude safe-upgrade                     # 执行一次安全的升级
aptitude full-upgrade                     # 将系统升级到新的发行版
aptitude install pkgname                  # 安装包
aptitude remove pkgname                   # 删除包
aptitude purge pkgname                    # 删除包及其配置文件
aptitude search string                    # 搜索包
aptitude show pkgname                     # 显示包的详细信息
aptitude clean                            # 删除下载的包文件
aptitude autoclean                        # 仅删除过期的包文件
```

> [aptitude （Debian系统的包管理工具）](http://baike.baidu.com/link?url=b7OUyTP7eNemuKiuyTJkj_DjBwwJK2pFKi4XKqnxlE5yVktFM37QWtKIL_vRJwzqB_7iLSk-2dSOG0nT-v4_YvvyiakVm8tSbt3pCqPxdxe)




### apt-cache
```
Usage: apt-cache [options] command
       apt-cache [options] showpkg pkg1 [pkg2 ...]
       apt-cache [options] showsrc pkg1 [pkg2 ...]

apt-cache is a low-level tool used to query information
from APT's binary cache files

Commands:
   gencaches                            # Build both the package and source cache
   showpkg                              # Show some general information for a single package (显示软件包的一些常规信息 )
   showsrc                              # Show source records
   stats                                # Show some basic statistics   
   dump                                 # Show the entire file in a terse form
   dumpavail                            # Print an available file to stdout
   unmet                                # Show unmet dependencies
   search                               # Search the package list for a regex pattern (查找软件包)
   show                                 # Show a readable record for the package
   depends                              # 了解使用依赖
   rdepends                             # Show reverse dependency information for a package
   pkgnames                             # List the names of all packages in the system(列出所有的软件包)
   dotty                                # Generate package graphs for GraphViz
   xvcg                                 # Generate package graphs for xvcg
   policy                               # Show policy settings

Options:
  -h   This help text.
  -p=? The package cache.
  -s=? The source cache.
  -q   Disable progress indicator.
  -i   Show only important deps for the unmet command.
  -c=? Read this configuration file
  -o=? Set an arbitrary configuration option, eg -o dir::cache=/tmp
See the apt-cache(8) and apt.conf(5) manual pages for more information.
```




> [Apt和dpkg快速参考](http://wiki.ubuntu.org.cn/Apt%E5%92%8Cdpkg%E5%BF%AB%E9%80%9F%E5%8F%82%E8%80%83)    
> [ubuntu 源码编译,dpkg,apt 安装原理 及简单使用](http://www.jianshu.com/p/45fa3d6b2e8d )  
> [关于Ubuntu下apt的一些用法及和yum的比较](http://blog.csdn.net/mbxc816/article/details/7473906)