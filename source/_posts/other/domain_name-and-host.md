---
title: 访问域名与请求头Host
tags:
  - Java
  - Nginx
categories:
  - Other
toc: false
date: 2018-05-27
---

最近公司的APP一到星期天的中午就会出现网络异常，只有的移动的网络的有问题，电信和联通都访问正常，排查下来是运营商DNS劫持。
解决方案是使用 HTTP DNS： 绕过 DNS 解析，直接通过 IP 直接访问。
如通过 `http://119.29.29.29/d?dn=blog.kail.xyz`，获取 `blog.kail.xyz` 域名的IP地址，然后通过 IP 直接进行访问。
> [DNSPod HTTP DNS demo](https://www.dnspod.cn/httpdns/demo)

这里就产生的了一个疑问，一般多个服务会对应有多个域名，但是多个域名会解析到同一个IP入口，入口根据域名通过 Nginx 反向代理，转发到内网不同的服务。如果用IP直接访问，如何判断出来是哪个服务呢？

<!-- more -->

实际上 Nginx 反向代理不是通过 浏览器地址栏中的 域名部分来进行转发的，而是通过 HTTP 请求头中的 Host 值。
地址栏中 URL 的域名部分，只是主要目的是用来建立连接用的，是 TCP/IP 协议层的东西。
流程实际上应该是：
1. 浏览器获取到 URL 的域名部分，通过 应用层DNS协议 解析出IP
2. 然后通过 IP+默认80端口 与服务器建立 Socket 连接
3. 浏览器获取域名部分，构建 HTTP 请求头的 Host 值
4. 浏览器获取 URL 的 路径部分，构建 HTTP 请求头的 Request-Line

通过以下的例子，可能更容易理解

``` java
// 监听从 http://119.29.29.29/d?dn=blog.kail.xyz 获取到的 IP，如果返回多个，随机获取一个
Socket socket = new Socket("185.199.108.153", 80);
OutputStream outputStream = socket.getOutputStream();
PrintWriter pw = new PrintWriter(outputStream);

// 请求的第一行Request-Line，需要写请求的URL(/)
pw.println("GET / HTTP/1.1");
// 请求头，Host是必须的
pw.println("Host: blog.kail.xyz");
// 响应结束后关闭链接 https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Connection
pw.println("Connection: close");
// 一定要有个空行表示请求结束
pw.println();
// 刷新 HTTP 请求头数据
pw.flush();

// 获取响应结果
InputStream inputStream = socket.getInputStream();
InputStreamReader reader = new InputStreamReader(inputStream);
BufferedReader bufferedReader = new BufferedReader(reader);
// 输出响应内容（如果 Connection: close 不指定，这个读取完相应后会阻塞）
for (String readLine; null != (readLine = bufferedReader.readLine()); ) {
    System.out.println(readLine);
}

// 关闭资源
```


可以看出，这个 Socket 链接 发送的以下 4行(最后一个空行)才是 HTTP 协议的部分，

```
GET / HTTP/1.1
Host: blog.kail.xyz
Connection: close
空行
```
如果通过浏览器访问，完整的链接是这样 `http://blog.kail.xyz`。

# Read More

- [Mozilla Host](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Host)
- [Hypertext Transfer Protocol (HTTP/1.1)](https://tools.ietf.org/html/rfc7230#section-5.4)
- [HTTP请求方法](http://www.runoob.com/http/http-methods.html)
- [JAVA Socket 实现HTTP与HTTPS客户端发送POST与GET方式请求](https://blog.csdn.net/jia20003/article/details/17104791)

