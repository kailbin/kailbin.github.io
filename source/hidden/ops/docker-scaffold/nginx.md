---
title: Docker 脚手架 - Nginx
date: 2017-07-08
desc: Docker 脚手架 - Nginx

tags: [Docker]
categories: ops
---

# 目录结构

``` bash
.
├── Dockerfile
├── conf.d
│   └── default.conf
└── docker-compose.yml
```

# 文件内容

## Dockerfile

``` Dockerfile
FROM nginx
MAINTAINER Mr.Kail

# WORKDIR /
EXPOSE 80

# 删除容器内 Nginx 配置文件
RUN rm /etc/nginx/conf.d/default.conf
# 把配置文件拷贝到容器内
ADD ./conf.d/default.conf /etc/nginx/conf.d/default.conf
# 启动 nginx
RUN nginx

# docker build -t kail/nginx .
```

## docker-compose.yml

``` yml
version: '2'
services:
  kail:
    build: .
    restart: always

    # 端口是 80
    ports:
     - "80:80"
    
    # 
    volumes:
     - "/opt/data/docker-volume/nginx/html:/tmp/html"
  
# docker-compose up -d
# docker run -d -p 80:80 -v /opt/data/docker-volume/nginx/html:/tmp/html --name nginx_kail kail/nginx
# docker run -d -p 80:80 -v /opt/data/docker-volume/nginx/html:/tmp/html --name nginx_kail nginx_kail
```

## conf.d/default.conf

``` nginx
server {
    listen       80;
    server_name  localhost;

    location / {
        root   /tmp/html;
        index  index.html index.htm;
    }
}
```
`docker pull nginx` 拉下来的镜像，启动之后配置文件在 `/etc/nginx/nginx.conf`，其会 `incloud` `/etc/nginx/conf.d/` 下的 `*.conf` 配置，Dockerfile 中拷贝了自定义的 `default.conf` 到该目录下。

 `/etc/nginx/nginx.conf` 内容摘录如下：

``` nginx

...

http {
    include       /etc/nginx/mime.types;
    
    ...

    include /etc/nginx/conf.d/*.conf;
}

```

# 运行
``` bash
docker-compose up -d
```

# FAQ

### Dockerfile `RUN`、`CMD`、`ENTRYPOINT` 有什么区别 ？

`RUN`是在Build时运行的，在构建镜像时对镜像内容进行修改可以用`RUN`，先于`CMD`和`ENTRYPOINT`。

`ENTRYPOINT` 和 `CMD` 的不同点在于执行docker run时参数传递方式， `CMD` 指定的命令可以被docker run传递的命令覆盖，而 `ENTRYPOINT` 会把容器名后面的所有内容都当成参数传递给其指定的命令，不会被覆盖。

`ENTRYPOINT` 和 `CMD` 一个Dockerfile 只能出现一个，如果有多个，最后一个生效。

以下是官网列举的 `CMD`和`ENTRYPOINT`同时出现的时候的情况

| |	No ENTRYPOINT |	ENTRYPOINT exec_entry p1_entry |	ENTRYPOINT [“exec_entry”, “p1_entry”] |
|----|----|----|----|
|No CMD |	error, not allowed |	/bin/sh -c exec_entry p1_entry |	exec_entry p1_entry |
|CMD [“exec_cmd”, “p1_cmd”] |	exec_cmd p1_cmd |	/bin/sh -c exec_entry p1_entry |	exec_entry p1_entry exec_cmd p1_cmd |
|CMD [“p1_cmd”, “p2_cmd”] |	p1_cmd p2_cmd |	/bin/sh -c exec_entry p1_entry |	exec_entry p1_entry p1_cmd p2_cmd |
|CMD exec_cmd p1_cmd |	/bin/sh -c exec_cmd p1_cmd |	/bin/sh -c exec_entry p1_entry |	exec_entry p1_entry /bin/sh -c exec_cmd p1_cmd |

> [Dockerfile里指定执行命令用ENTRYPOING和用CMD有何不同？](https://segmentfault.com/q/1010000000417103)


### Dockerfile `ADD`、`COPY` 有什么区别 ？

`ADD` 和 `COPY` 的作用都是把文件复制到镜像中。
`COPY` 的功能比较单纯，仅仅就是字面意思的复制；
`ADD` 相对来说功能多一点，如果复制的是压缩包，会自动解压，如果是一个URL，会自动下载文件到镜像中。
选择的话建议使用 `COPY`，因为`ADD`相对来说有一定的歧义，例如你想复制一个压缩包到Docker容器，但是压缩格式繁多，结果 有可能解压有可能不解压，
`ADD` 的下载功能完全可以通过 `curl` 或者 `wget` 来搞定。

> [Dockerfile: ADD vs COPY](http://blog.csdn.net/liukuan73/article/details/52936045)
> [https://docs.docker.com/engine/reference/builder/#add](https://docs.docker.com/engine/reference/builder/#add)
> [https://docs.docker.com/engine/reference/builder/#copy](https://docs.docker.com/engine/reference/builder/#copy)



# 拓展阅读

> [Docker 脚手架](/post/2017-07-08/ops/docker-scaffold.html)

