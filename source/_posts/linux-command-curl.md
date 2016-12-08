---

title: curl 命令常见用法
date: 2016-12-07
desc: curl 命令常见用法

---



> curl命令是一个利用URL规则在命令行下工作的文件传输工具。它支持文件的上传和下载，所以是综合传输工具，但按传统，习惯称curl为下载工具。作为一款强力工具，curl支持包括HTTP、HTTPS、ftp等众多协议，还支持POST、cookies、认证、从指定偏移处下载部分文件、用户代理字符串、限速、文件大小、进度条等特征。做网页处理流程和数据检索自动化，curl可以祝一臂之力。

> <div style="text-align: right;">来自: http://man.linuxde.net/curl</div>


<!--more-->

### 设置请求方式 （GET、POST、PUT、DELETE …）
```bash
curl -X POST http://baidu.com
curl -X PUT http://baidu.com
curl -X DELETE http://baidu.com

# GET 方式
curl -G http://baidu.com
# POST 提交数据
curl -d "param1=value1&param2=value2" http://baidu.com
# PUT 
curl -X PUT -d "param1=value1&param2=value2" http://baidu.com
```


### 模拟请求
```bash
# 设置 referer
curl http://baidu.com --referer http://yokoboy.oschina.io 
curl http://baidu.com -e http://yokoboy.oschina.io 
# 设置 cookie
curl http://baidu.com --cookie "user=root;pass=123456"
# 设置 user-agent
curl http://baidu.com --user-agent "Mozilla/5.0" 
curl http://baidu.com -A "Mozilla/5.0"
```

以上参数都可以使用 `-H` 代替

```bash
curl 'http://baidu.com' -H 'Referer: http://yokoboy.oschina.io' -H 'Cookie: user=root;pass=123456' -H 'User-Agent: Mozilla/5.0'
```


### 下载

```bash
curl http://baidu.com > baidu.html
curl -o baidu.html http://baidu.com
curl -O https://ss0.bdstatic.com/5aV1bjqh_Q23odCf/static/superman/img/logo/logo_white.png
```

`-o` 指定保存文件名  
`-O` 保留原文件名  



### 静默方式

```bash
# 默认会输出进度信息
curl  http://baidu.com >> /dev/null
```
      % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                     Dload  Upload   Total   Spent    Left  Speed
    100    81  100    81    0     0    307      0 --:--:-- --:--:-- --:--:--   306
    
加上 `-s` 或者 `--silent` 之后则什么都不输出，例如获得源码的时候并不希望输出进行信息
```bash
is_open=$(curl -s http://baidu.com)
```


### 查看请求详细信息
```bash
curl -v http://baidu.com
```

    * Rebuilt URL to: http://baidu.com/
    * Hostname was NOT found in DNS cache
    *   Trying 180.149.132.47...
    * Connected to baidu.com (180.149.132.47) port 80 (#0)
    > GET / HTTP/1.1
    > User-Agent: curl/7.35.0
    > Host: baidu.com
    > Accept: */*
    >
    < HTTP/1.1 200 OK
    < Date: Wed, 07 Dec 2016 14:55:59 GMT
    * Server Apache is not blacklisted
    < Server: Apache
    < Last-Modified: Tue, 12 Jan 2010 13:48:00 GMT
    < ETag: "51-47cf7e6ee8400"
    < Accept-Ranges: bytes
    < Content-Length: 81
    < Cache-Control: max-age=86400
    < Expires: Thu, 08 Dec 2016 14:55:59 GMT
    < Connection: Keep-Alive
    < Content-Type: text/html
    <
    <html>
    <meta http-equiv="refresh" content="0;url=http://www.baidu.com/">
    </html>
    * Connection #0 to host baidu.com left intact
    

### 查看响应头（Response）
```bash
curl -I http://baidu.com
```

    HTTP/1.1 200 OK
    Date: Wed, 07 Dec 2016 14:59:18 GMT
    Server: Apache
    Last-Modified: Tue, 12 Jan 2010 13:48:00 GMT
    ETag: "51-47cf7e6ee8400"
    Accept-Ranges: bytes
    Content-Length: 81
    Cache-Control: max-age=86400
    Expires: Thu, 08 Dec 2016 14:59:18 GMT
    Connection: Keep-Alive
    Content-Type: text/html
    
```bash
# 输出响应头和源码
curl -i http://baidu.com
```

### 其他
| 参数                   | 描述                          |
|:------------------------|:-------------------------------|
| -c / --cookie-jar &lt;file&gt; | 操作结束后把cookie写入到这个文件中|
| -T / --upload-file &lt;file&gt; | 上传文件                      |
| -x / --proxy &lt;host[:port]&gt; | 在给定的端口上使用HTTP代理     |


> 参考：   
> [Linux命令大全 » 网络管理 » curl ](http://man.linuxde.net/curl)  
> [Using curl to automate HTTP jobs](https://curl.haxx.se/docs/httpscripting.html)  

  
> [Linux命令大全 » 网络管理 » wget](http://man.linuxde.net/wget)