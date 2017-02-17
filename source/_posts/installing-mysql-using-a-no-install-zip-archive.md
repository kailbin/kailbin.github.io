---

title: 解压版MySQL 安装
date: 2016-07-09 21:59:59
desc: 解压版MySQL 安装

tags: [软件安装,MySQL]

---

### 下载

下载页面: http://dev.mysql.com/downloads/mysql/  

下载版本: Windows (x86, 64-bit), ZIP Archive, 5.7.13, 310.0M	

具体下载链接： http://dev.mysql.com/get/Downloads/MySQL-5.7/mysql-5.7.13-winx64.zip

<!--more-->

    ˇ ˇ
    以下所有命令，建议以管理员身份运行
     ˇ ˇ


### 安装

下载后解压到任意目录，例如 D:\DataBase\MySQL5.7\mysql-5.7.13-winx64 ( = `MySQL_HOME`)

##### 修改配置文件

`MySQL_HOME` 下有名为`my-default.ini` 的配置文件，需要改名为 `my.ini`。  

5.7版本的配置文件相比其他版本的，给的参考配置很少, 5.5 的时候会提供 `my-huge.ini.properties`、`my-innodb-heavy-4G.ini.properties`、`my-large.ini.properties`、`my-medium.ini.properties`、`my-small.ini.properties`等参考配置可以选择。5.7 只有一个`my-default.ini`。  

里面主要有一下四项,默认是注释掉的，需要打开。
```
# basedir = .....
# datadir = .....
# port = .....
# server_id = .....
```

我的配置如下
```
basedir = D:/DataBase/MySQL5.7/mysql-5.7.13-winx64
datadir = D:/DataBase/MySQL5.7/data
port = 3307
server_id = 1
```
因为机器上已经有一个MySQL实例了，所以这里的端口号设置成了3307。


##### 初始化数据

初始化数据库的时候可以直接生成密码，或者使用空密码。直接生成的是一个随机数密码，相对安全，但是比较难记，这里只是本机安装测试，所以使用无密码的方式初始化。

```
> bin\mysqld --initialize-insecure
```
生成之后，启动MySQL，使用root账号登陆，再改密码即可。


>[Initializing the Data Directory Manually Using mysqld](http://dev.mysql.com/doc/refman/5.7/en/data-directory-initialization-mysqld.html)

##### 控制台启动

```
> D:\DataBase\MySQL5.7\mysql-5.7.13-winx64\bin\mysqld --console
```
如果已经把`mysqld`路径配置到环境变量，直接写mysqld即可，不用写那么长的路径，这里为了避免出错还是写的全路径。
  
如果出现以下情况，说明已经启动成功了。
```
mysqld: ready for connections
Version: '5.7.13'  socket: ''  port: 3307
```

如果以控制台方式启动，控制台一关，MySQL服务就停了，所以建议把MySQL安装到系统服务。

##### 以服务方式启动

```
> D:\DataBase\MySQL5.7\mysql-5.7.13-winx64\bin\mysqld –-install MySQL57 –-defaults-file="D:\DataBase\MySQL5.7\mysql-5.7.13-winx64\my.ini"
```
这里服务起名为 `MySQL57` , 如果不写，默认为`MySQL` 。

启动服务
```
> net start MySQL57
```

停止服务
```
> net stop MySQL57
```
或者直接在系统服务里面找到 `MySQL57`，右键停止或者启动。


##### 删除服务
```
> mysqld –-remove MySQL57
```



> 详请查看官网文档：  
>
> [Installing MySQL on Microsoft Windows Using a noinstall Zip Archive](http://dev.mysql.com/doc/refman/5.7/en/windows-install-archive.html)
