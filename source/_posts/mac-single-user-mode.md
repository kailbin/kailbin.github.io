---
title: Mac 单用户模式(Single User Mode)获得root权限
date: 2017-02-16
desc: mac,单用户模式,Single User Mode

tags: [MAC]

---

最近在用 CleanMyMac 的时候把一些系统文件给删掉了，主要还是用 Windows 上的软件管理工具用习惯了，卸载软件有了洁癖。

CleanMyMac 是收费，我就只通过它找到软件关联的文件，不小心把 `/etc/pam.d/` 文件夹给删了（没真正删除，放进回收站了），结果系统登陆不进去，提示密码输入错误，这里记录了恢复的过程。


<!--more-->


系统启动的时候按住 `Command + S`，进入单用户模式。单用户模式是 Shell 操作界面，默认就是root权限，不需要输入密码。


1. `/sbin/fsck -y`，`fsck` 会检查并且试图修复文件系统中的错误。

2. `mount -uw /` 安装磁盘卷，否则无法写入文件。

3. `cp /Users/kail/.Trash/pam.d /etc/pam.d/`，把删除的文件恢复到指定位置。

4. `reboot` 重启。



以上仅供参考，如果对 Unix/Linux 熟悉的话，单用户模式可以让你充分的发挥想象力。


# 参考

> [Mac OS X:单用户模式(Single User Mode)的操作和安全漏洞](http://blog.csdn.net/cneducation/article/details/3857713)
>
> [fsck 命令](http://man.linuxde.net/fsck)
>
> [mount 命令](http://man.linuxde.net/mount)
