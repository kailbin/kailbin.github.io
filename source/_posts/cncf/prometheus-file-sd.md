---
title: Prometheus 基于文件的服务发现 集成 Eureka
date: 2018-10-08
tags: [CNCF, Prometheus]
categories: [CNCF]
id: cncf/prometheus-file-sd
---



Prometheus 是 CNCF 的一员，内置了一些服务发现机制，但是只对 CNCF 的一些项目 、 国外大厂、云服务商 支持比较完善 ，包括 Kubernetes、Consul、DNS 等。

虽然针对 Prometheus  服务发现的各种 Pull Request 比较多，但是 任性的 Prometheus 并不打算接受，理由是 开发维护能力不足。但是官方提供了基于文件的服务发现，是一个通用的服务发现解决方案。

如果您需要使用当前不支持的服务发现系统（如：Eureka），Prometheus基于文件的服务发现机制可以最好地满足您的使用场景，您可以在JSON文件中定义被发现的目标。

<!-- more -->

> There is currently a moratorium on new service discovery mechanisms being added to Prometheus due to a lack of developer capacity. In the meantime `file_sd` remains available.
>
> https://github.com/prometheus/prometheus/tree/master/discovery



# 如何启用文件服务发现

## 新增 file_sd 类型的 job

在 scrape_configs 下新增 file_sd 类型的配置，如下（如果 targets.json 和 prometheus.yml 在同一个目录）

```yaml
scrape_configs:
 
  - job_name: 'service_file_sd'
    metrics_path: '/prometheus'
    file_sd_configs:
      - files:
        - 'targets.json'
```



## targets.json 的文件格式

``` json
[
  {
    "labels": {
      "job": "service_name"
    },
    "targets": [
      "service_id:service_port"
    ]
  }
]
```

然后重启 Prometheus 即可，这时候 修改 targets.json 文件内容，最多5分钟，就可生效，可访问 ${prometheus_host}/targets 查看效果

# 实施方案

## curl + jq + crontab

### 从 Eureka 获取 json 格式的服务列表

``` bash
curl -s -H 'Accept:application/json' http://localhost:8080/eureka/apps
```

- `-s` 静默模式
- `-H 'Accept:application/json'` 增加请求头，说明客户端仅支持 json 格式，否则默认返回的是 xml 格式

Eureka 的数据结构大致如下

``` json
{
    "applications":{
        "versions__delta":"1",
        "apps__hashcode":"UP_4_",
        "application":[
            {
                "name":"MS-REGISTRY",
                "instance":[
                    {
                        "...":"...",
                        "app":"MS-REGISTRY",
                        "ipAddr":"172.16.2.110",
                        "status":"UP",
                        "port":{
                            "$":8110,
                            "@enabled":"true"
                        },
                        "..":".."
                    },
                    {
                        "...":"...",
                        "app":"MS-REGISTRY",
                        "ipAddr":"172.16.2.120",
                        "status":"UP",
                        "port":{
                            "$":8120,
                            "@enabled":"true"
                        },
                        "..":".."
                    }
                ]
            }
        ]
    }
}
```



### 通过 ` jq`  把 Eureka 数据格式解析成 Prometheus 需要的格式

``` bash
curl eureka_url | jq '.applications.application[] | { labels:{job:.name},targets:[ .instance[] | .ipAddr + ":" + (.port["$"] | tostring) ] }' | jq '[.]'
```

- `.applications.application[]` 获取所有的应用列表
- `|` 为 jq 的管道操作符（Pipe），管道后面的指令可以直接使用 管道前面的查询结果
- `{ labels:{job:.name}, targets:[ .instance[] | .ipAddr + ":" + (.port["$"] | tostring) ] }` 按照逗号分隔拆分成以下两个部分
    - `labels:{job:.name}` 
        - 设置一个 Prometheus labels，键为 'job'，值`.name`是 Eureka 注册的应用名
    - `targets:[ .instance[] | .ipAddr + ":" + (.port["$"] | tostring) ]`
        - targets 定义一个列表，内容是所有的 Eureka 实例（`.instance[]`），并把实例的 IP 地址和端口进行字符串相加
        - `(.port["$"] | tostring)` 因为端口不是字符串类型，需要转成string再进行字符串相加
- `jq '[.]'` 把上一步jq 命令得到的一个一个结果，格式化成 json 数组

最后的结果如下
``` json
[
  {
    "labels": {
      "job": "MS-REGISTRY"
    },
    "targets": [
      "172.16.2.110:8110",
      "172.16.2.120:8120"
    ]
  }
]
```

### crontab

通过 crontab 定时执行，从 Eureka 拉取数据，解析成符合 Prometheus 需要的格式，写入文件，Prometheus 也会定时扫描该文件，从而实现自动发现。

## 代理

- 在 Prometheus 和 Eureka 直接加个代理 Proxy，通过 Proxy 生成符合Prometheus文件格式的数据；
- 如果想让一些服务不被 Prometheus 发现，可以通过 Proxy 进行管理；
- 如果有些系统并没有注册到 Eureka，但是也想被Prometheus发现，可以在 Proxy 系统进行页面手动管理；
- 相对于前一种方式会稍微复杂一点，但是会更加灵活

 

# Read More

- [Use File-Based Service Discovery to Discover Scrape Targets](https://prometheus.io/docs/guides/file-sd/)
- [Design of a Prometheus Service Discovery](https://github.com/prometheus/prometheus/tree/master/discovery) 
- https://prometheus.io/docs/prometheus/latest/configuration/configuration/#%3Cfile_sd_config%3E
- 
- [jq 官方快速指南](https://stedolan.github.io/jq/tutorial/) 
- [jq 官方手册](https://stedolan.github.io/jq/manual/) 

