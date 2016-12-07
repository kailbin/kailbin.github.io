---

title: curl 命令常见用法
date: 2016-12-07
desc: curl 命令常见用法

---



> curl命令是一个利用URL规则在命令行下工作的文件传输工具。它支持文件的上传和下载，所以是综合传输工具，但按传统，习惯称curl为下载工具。作为一款强力工具，curl支持包括HTTP、HTTPS、ftp等众多协议，还支持POST、cookies、认证、从指定偏移处下载部分文件、用户代理字符串、限速、文件大小、进度条等特征。做网页处理流程和数据检索自动化，curl可以祝一臂之力。

> <div style="text-align: right;">来自: http://man.linuxde.net/curl</div>


<!--more-->

### 设置请求方式 （GET、POST、PUT、DELETE …）
```base
curl -X POST http://baidu.com
curl -X PUT http://baidu.com
curl -X DELETE http://baidu.com


```

### 查看请求详细信息
```base
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
```base
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
    
### 


> 参考： [Linux命令大全 » 网络管理 » curl ](http://man.linuxde.net/curl)
https://curl.haxx.se/docs/httpscripting.html
http://www.cnblogs.com/gbyukg/p/3326825.html
http://outofmemory.cn/code-snippet/3306/curl-head-request
https://linux.cn/article-4957-1.html
http://www.lenky.info/archives/2012/07/1841?utm_source=tuicool&utm_medium=referral