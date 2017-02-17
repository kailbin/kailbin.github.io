---
title: 使用Nginx反向代理协助本地开发
date: 2016-07-24
desc: 使用nginx反向代理协助本地开发

tags: [Nginx]

---

有时候本地需要启动多个项目，模拟线上的访问环境，用不同的域名定到不同的项目，又不能加端口，这时候Nginx的反向代理即可帮助到我们。

<!--more-->

##### 打开 `conf/nginx.conf` ，找到 `http` 节点( 指令 directives)。
类似于这样
```
http {
    server {
    }
}
```
> 文档结构：http://nginx.org/en/docs/beginners_guide.html#conf_structure



### 配置
如果希望输入 `http://www.kail.com` 访问 `http://localhost:8080`，输入`http://api.kail.com` 访问 `http://localhost:8888`。

进行如下配置即可：  
**http {**
```
	server {
        server_name  www.kail.com;
        location / {
		  proxy_pass  http://localhost:8080;
        }
    }

    server {
        server_name  api.kail.com;
        location / {
		  proxy_pass  http://localhost:8888;
        }
    }
```
**}**

> 详见： http://nginx.org/en/docs/http/ngx_http_proxy_module.html#example


##### 配置生效

windows 下直接双击`nginx.exe`启动即可，如果是在启动后修改的，通过 `nginx.exe -s reload` 可以重新加载配置文件。

输入 `nginx.exe -?` 查看帮助 
```
nginx version: nginx/1.11.1
Usage: nginx [-?hvVtTq] [-s signal] [-c filename] [-p prefix] [-g directives]

Options:
  -?,-h         : this help
  -v            : show version and exit
  -V            : show version and configure options then exit
  -t            : test configuration and exit
  -T            : test configuration, dump it and exit
  -q            : suppress non-error messages during configuration testing
  -s signal     : send signal to a master process: stop, quit, reopen, reload
  -p prefix     : set prefix path (default: NONE)
  -c filename   : set configuration file (default: conf/nginx.conf)
  -g directives : set global directives out of configuration file
```

<br/>
以上只是简单的配置了域名和代理地址的映射关系，其实这样在本地开发环境中，已经基本上达到了开始说的目的了，快速方便。**最后别忘了配置hosts**。

##### 参考
> ngx_http_proxy_module 其他详细配置 : http://nginx.org/en/docs/http/ngx_http_proxy_module.html
>
> Serving Static Content： http://nginx.org/en/docs/beginners_guide.html#static
> 负载均衡：http://nginx.org/en/docs/http/load_balancing.html
 


