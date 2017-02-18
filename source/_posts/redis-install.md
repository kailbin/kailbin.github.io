---

title: Redis 安装部署
date: 2016-12-16
desc: Redis 安装部署

tags: [Redis,软件安装]

---



# 快速安装

下面是最基本的安装步骤，便于快速上手操作,操作环境在为 windows bash

```bash
wget http://download.redis.io/releases/redis-3.2.6.tar.gz
tar zxvf redis-3.2.6.tar.gz
cd redis-3.2.6
make
# cd src && make install
```

<!--more-->

如果 `cd src && make install` 的话，默认安装在 `/usr/local/bin` 下面(`find / -name redis`or`find / -path '/mnt/*' -prune -o -print | grep redis`)

配置文件在 `redis-3.2.6` 下面。

# 启动
```bash
# 启动服务
src/redis-server redis.conf

# 使用
src/redis-cli             # or `redis-cli -h 127.0.0.1 -p 6379`

# 退出
shutdown                  # or `Ctrl + C`
```



> # 参考
>
> [Redis官网](https://redis.io/download)
>
> [Redis中文网](http://www.redis.cn/download.html)
>

# 工具
>
> [jedis github wiki](https://github.com/xetorthio/jedis/wiki)
>
> [redisson github wiki](https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95)
>
> [Redis Desktop Manager](https://redisdesktop.com/download) (图形化工具)
>
>

