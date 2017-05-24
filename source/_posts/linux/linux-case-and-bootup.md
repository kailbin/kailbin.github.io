---------------
title: case..esac 与 开机启动
date: 2017-05-24
desc:  Linux系统启动, Shell 条件语句
tags: [shell]
categories: Linux
---------------

标题是*‘case..esac 与 开机启动’*，但是两者并没有关系。
本文主要记录了 使用**case..esac**条件语句写一个Shell脚本 管理一个Java程序，并使其能**跟随系统启动**。

<!--more-->

# case..esac

新建Shell文件 `/etc/init.d/rc.java_program`，（`rc`是 `runlevel control` 的意思）。文件内容如下

``` bash
#!/bin/bash

JAVA_HOME="/opt/websuite/jdk"                       # 你的JDK目录
PROGRAM_HOME="/home/kail/java_program"              # 你的应用程序目录
PROGRAM_NAME="java_program-1.0.0.jar"               # 你的jar包名称
PROGRAM_LOG="$PROGRAM_HOME/java_program.log"        # 日志路径

case $1 in                                          # $1 获得第一个命令行参数
    start)                                          # 当参数是 start 的时候，执行以下脚本( java -jar ***.jar >> file.log )
       nohup "$JAVA_HOME/bin/java" -jar "$PROGRAM_HOME/$PROGRAM_NAME" >> $PROGRAM_LOG 2>&1 &   
       # nohup 和 & 一前一后，使进程后台运行
    ;;

    stop)                                           # 当参数是 stop 的时候，执行以下脚本
        "$JAVA_HOME/bin/jcmd" | grep "$PROGRAM_NAME" | awk '{print $1}'| xargs kill -9         # 获取进程ID，kill掉
    ;;

    status)
       "$JAVA_HOME/bin/jcmd" | grep "$PROGRAM_NAME"                                            # 获取进程信息
    ;;

    restart)
        $0 stop        # $0 指shell本身文件名，后面加上stop参数，相当于执行 上面 stop 里面的逻辑
        sleep 10s      # 等待10秒，
        $0 start       # 启动
    ;;

    log)         
        $0 status      
        sleep 2s                    # 输出进程信息后，等待两秒
        tail -fn 200 $PROGRAM_LOG   # 滚动查看日志文件
    ;;

    *)                              # 如果以上 {start|stop|status|restart|log} 都没有匹配到，则执行该逻辑 
        echo "Usage: $0 {start|stop|status|restart|log}"
	exit 1    # 非正常退出码
esac          # case 倒过来写 case结束


exit 0	      # 正常退出状态码
```

接下来可以使用 `/etc/init.d/rc.java_program start` 启动程序，`/etc/init.d/rc.java_program stop` 关闭程序....

# 跟随系统启动

编辑 `/etc/rc.local` 文件，在最后一行新增 `/etc/init.d/rc.java_program restart`。
该 `/etc/rc.local` Shell文件会在系统启动完成之后执行。详请查看拓展阅读。

`$ reboot` 重启，使用 `/etc/init.d/rc.java_program status` 看一下进程启动了没有吧。 


### 拓展阅读

>[利用 case ..... esac 判断](http://cn.linux.vbird.org/linux_basic/0340bashshell-scripts_4.php#case)
>
>[使用者自订启动启动程序 (/etc/rc.d/rc.local)](http://cn.linux.vbird.org/linux_basic/0510osloader.php#startup_local)  
>
>[daemon 的启动脚本与启动方式](http://cn.linux.vbird.org/linux_basic/0560daemons.php#whereisdaemon)  
>
>[Systemd 入门教程：命令篇](http://www.ruanyifeng.com/blog/2016/03/systemd-tutorial-commands.html)
>[Systemd 入门教程：实战篇](http://www.ruanyifeng.com/blog/2016/03/systemd-tutorial-part-two.html)