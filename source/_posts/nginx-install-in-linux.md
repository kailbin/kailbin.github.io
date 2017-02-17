---

title: Nginx 编译 安装笔记
date: 2016-06-25
desc: Nginx 安装

tags: [Nginx,软件安装]

---

#### 准备

##### Ubuntu 
``` shell
apt-get install build-essential 
apt-get install libtool 
apt-get install openssl 
```

##### CentOS 
``` shell
yum -y install gcc automake autoconf libtool make
yum install gcc gcc-c++
```


<!--more-->

### 下载PCRE库
官网： [http://www.pcre.org/](http://www.pcre.org/)  
下载地址： [ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/](ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/)

``` shell
$ wget wget ftp://ftp.csx.cam.ac.uk/pub/software/programming/pcre/pcre-8.38.tar.gz
$ tar zxvf pcre-8.38.tar.gz
```

### 下载 zlib库

``` shell
$ wget http://zlib.net/zlib-1.2.8.tar.gz
$ tar -zxvf zlib-1.2.8.tar.gz
```

### 下载 openssl
官网： [https://www.openssl.org](https://www.openssl.org)  
下载地址： [https://www.openssl.org/source/](https://www.openssl.org/source/)

``` shell
$ wget https://www.openssl.org/source/openssl-1.0.1t.tar.gz
$ tar zxvf openssl-1.0.1t.tar.gz
```

# 安装nginx

``` shell
$ wget http://nginx.org/download/nginx-1.11.1.tar.gz
$ tar zxvf nginx-1.11.1.tar.gz
$ cd nginx-1.11.1
$ ./configure --prefix=/usr/local/studio/nginx --with-http_ssl_module --with-pcre=/usr/local/studio/pcre-8.38 --with-zlib=/usr/local/studio/zlib-1.2.8 --with-openssl=/usr/local/studio/openssl-1.0.1t

$ make
$ make install 

```
# 卸载 nginx
由于是编译安装，直接删除 /usr/local/studio/nginx (--prefix=*path*) 即可

# 编译参数
http://nginx.org/en/docs/configure.html

>--prefix=path    定义一个目录，存放服务器上的文件 ，也就是nginx的安装目录。默认使用 /usr/local/nginx。

>--sbin-path=path 设置nginx的可执行文件的路径，默认为  prefix/sbin/nginx.

>--conf-path=path  设置在nginx.conf配置文件的路径。nginx允许使用不同的配置文件启动，通过命令行中的-c选项。默认为prefix/conf/nginx.conf.

>--pid-path=path  设置nginx.pid文件，将存储的主进程的进程号。安装完成后，可以随时改变的文件名 ， 在nginx.conf配置文件中使用 PID指令。默认情况下，文件名 为prefix/logs/nginx.pid.

>--error-log-path=path 设置主错误，警告，和诊断文件的名称。安装完成后，可以随时改变的文件名 ，在nginx.conf配置文件中 使用 的error_log指令。默认情况下，文件名 为prefix/logs/error.log.

>--http-log-path=path  设置主请求的HTTP服务器的日志文件的名称。安装完成后，可以随时改变的文件名 ，在nginx.conf配置文件中 使用 的access_log指令。默认情况下，文件名 为prefix/logs/access.log.

>--user=name  设置nginx工作进程的用户。安装完成后，可以随时更改的名称在nginx.conf配置文件中 使用的 user指令。默认的用户名是nobody。

>--group=name  设置nginx工作进程的用户组。安装完成后，可以随时更改的名称在nginx.conf配置文件中 使用的 user指令。默认的为非特权用户。

>--with-select_module --without-select_module 启用或禁用构建一个模块来允许服务器使用select()方法。该模块将自动建立，如果平台不支持的kqueue，epoll，rtsig或/dev/poll。

>--with-poll_module --without-poll_module 启用或禁用构建一个模块来允许服务器使用poll()方法。该模块将自动建立，如果平台不支持的kqueue，epoll，rtsig或/dev/poll。

>--without-http_gzip_module — 不编译压缩的HTTP服务器的响应模块。编译并运行此模块需要zlib库。

>--without-http_rewrite_module  不编译重写模块。编译并运行此模块需要PCRE库支持。

>--without-http_proxy_module — 不编译http_proxy模块。

>--with-http_ssl_module — 使用https协议模块。默认情况下，该模块没有被构建。建立并运行此模块的OpenSSL库是必需的。

>--with-pcre=path — 设置PCRE库的源码路径。PCRE库的源码（版本4.4 - 8.30）需要从PCRE网站下载并解压。其余的工作是Nginx的./ configure和make来完成。正则表达式使用在location指令和 ngx_http_rewrite_module 模块中。

>--with-pcre-jit —编译PCRE包含“just-in-time compilation”（1.1.12中， pcre_jit指令）。

>--with-zlib=path —设置的zlib库的源码路径。要下载从 zlib（版本1.1.3 - 1.2.5）的并解压。其余的工作是Nginx的./ configure和make完成。ngx_http_gzip_module模块需要使用zlib 。

>--with-cc-opt=parameters — 设置额外的参数将被添加到CFLAGS变量。例如,当你在FreeBSD上使用PCRE库时需要使用:--with-cc-opt="-I /usr/local/include。.如需要需要增加 select()支持的文件数量:--with-cc-opt="-D FD_SETSIZE=2048".

>--with-ld-opt=parameters —设置附加的参数，将用于在链接期间。例如，当在FreeBSD下使用该系统的PCRE库,应指定:--with-ld-opt="-L /usr/local/lib".


#### 参考
> http://www.nginx.cn/install
