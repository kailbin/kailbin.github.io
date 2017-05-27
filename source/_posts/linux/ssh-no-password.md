---

title: ssh 免密码登录
date: 2016-6-11 23:00:04
desc: git 常用命令

tags: [git,ssh]
categories: Linux
---

如果没有安装ssh，先进行安装
``` shell
sudo apt-get install ssh
```

<!--more-->

# 生成SSH公钥
``` shell
$ ssh-keygen [-C "注释"] [-t rsa] [-P '']  
```
中括号括起来的是可选项，可以都不写。   
**-C** 给公钥添加注释，如果不写的话默认的注释是`用户名@主机名`，注释在`.ssh/id_rsa.pub`文件的结尾，因为公钥是要发给其它主机的，到时候其它主机上可能会有很多份公钥信息，所以建议加上注释便于区分是谁的公钥

**-P** 设置密码，空字符串代表没有密码，加个这个参数，只用回车确认一次，否则会议一下三次确认
1. 确认秘钥的保存地址
2. 输入密码
3. 确认密码
一般情况下全部回车即可


# 把生成的公钥复制到其它机器的`.ssh/authorzied_keys`文件中

``` shell
# copy
$ scp .ssh/id_rsa.pub kail@192.168.84.133:/home/kail/id_rsa.pub 

# 登陆其他机器(本次是需要输入密码的)
$ ssh kail@192.168.84.133

# 把公钥追加到 .ssh/authorized_keys 文件里
$ cat id_rsa.pub >> .ssh/authorized_keys
```
接下来再 `ssh kail@192.168.84.133` 就需要登陆密码了

# git 免密码提交

``` shell
$ cat .ssh/id_rsa.pub
```
把公钥告诉git托管服务(`码云`、`github`)即可。如下图所示:

![github 免密码登陆](//pic01.kail.xyz/images/ssh-no-password/1.jpg)

不过目前好像不太建议这种方式，OSC的说明是：  

>部署公钥允许以只读的方式访问项目，主要用于项目在生产服务器的部署上，免去HTTP方式每次操作都要输入密码和普通SSH方式担心不小心修改项目代码的麻烦。 

而github多了一步勾选项'Allow write access'才允许写操作。说白了就是怕误操作。

# git 其他免密码方式

## 直接写到连接中

``` git
$ git remote set-url origin <https://username:password@url> 
```
可以直接把用户名密码写到url中。**如果用户名密码包含`@`，需要写成`%40`**

## git 全局配置

1. 在用户根目录下新建`.git-credentials`文件(`~/` OR `C:\Users\kail`)
```
$ vim .git-credentials
https://username:password@git.oschina.net
```

2. 添加Git Config 内容

    进入git bash终端， 输入如下命令：
    ```
    git config --global credential.helper store
    ```
    执行完后查看用户目录下的.gitconfig文件，会多了一项：
    ```
    [credential]
       helper = store
    ```

