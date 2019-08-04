---
title: HttpClient 4.4 无法携带 Cookies 问题
date: 2019-08-04
tags: [Bug]
categories:
  - Other
id: httpclient-4.4-cookie
---

最近在使用 HttpClient 升级到 4.4+ 之后，发现原来代码无法发送 Cookies 信息了，示例代码如下：

<!-- more -->

# 示例程序

## Maven 依赖

```xml
<dependencies>
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>1.7.26</version>
    </dependency>

    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>jcl-over-slf4j</artifactId>
        <version>1.7.26</version>
    </dependency>

    <dependency>
        <groupId>org.apache.httpcomponents</groupId>
        <artifactId>httpmime</artifactId>
        <version>4.3.6</version>
        <!--<version>4.4.1</version>-->
        <exclusions>
            <exclusion>
                <groupId>*</groupId>
                <artifactId>commons-logging</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

</dependencies>
```

## 示例代码

```java
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.slf4j.impl.SimpleLogger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpClientMain {

  static {
        // slf4j-simple , 日志级别设置为 Debug
        System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
    }

    public static void main(String[] args) throws IOException {
        // 设置 Cookies 信息
        CookieStore cookieStore = new BasicCookieStore();
        BasicClientCookie clientCookie = new BasicClientCookie("asd", "asd");
        clientCookie.setDomain(".baidu.cn");
        clientCookie.setPath("/");
        cookieStore.addCookie(clientCookie);

        // 发起请求
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build()) {
            HttpGet get = new HttpGet("http://www.baidu.cn");
            try (CloseableHttpResponse response = httpClient.execute(get)) {
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            }
        }
    }
}

```

## 日式输出

```bash
...
[main] DEBUG org.apache.http.wire - http-outgoing-0 >> "GET / HTTP/1.1[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 >> "Host: www.baidu.cn[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 >> "Connection: Keep-Alive[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 >> "User-Agent: Apache-HttpClient/4.3.6 (java 1.5)[\r][\n]"
# 请求头关注这一行， 升级 4.4 以上代码则不会发送 Cookie 信息
[main] DEBUG org.apache.http.wire - http-outgoing-0 >> "Cookie: asd=asd[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 >> "Cookie2: $Version=1[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 >> "Accept-Encoding: gzip,deflate[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 >> "[\r][\n]"

# 响应头
[main] DEBUG org.apache.http.wire - http-outgoing-0 << "HTTP/1.1 302 Found[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 << "Location: http://www.baidu.com/[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 << "Date: Sun, 04 Aug 2019 09:07:09 GMT[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 << "Content-Length: 44[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 << "Content-Type: text/html; charset=utf-8[\r][\n]"
[main] DEBUG org.apache.http.wire - http-outgoing-0 << "[\r][\n]"
...
```

# 调试调用链

- execute:82, CloseableHttpClient
- doExecute:184, InternalHttpClient
- execute:110, RedirectExec
- execute:182, ProtocolExec
- process:132, ImmutableHttpProcessor
- process:165, **RequestAddCookies**

# 默认的 Cookie 规范

```java
public class DefaultCookieSpec implements CookieSpec {

...
      public DefaultCookieSpec(
            final String[] datepatterns,
            final boolean oneHeader) {
        this.strict = new RFC2965Spec(oneHeader,
                new RFC2965VersionAttributeHandler(),
                new BasicPathHandler(),
                new RFC2965DomainAttributeHandler(),
                new RFC2965PortAttributeHandler(),
                new BasicMaxAgeHandler(),
                new BasicSecureHandler(),
                new BasicCommentHandler(),
                new RFC2965CommentUrlAttributeHandler(),
                new RFC2965DiscardAttributeHandler());
        this.obsoleteStrict = new RFC2109Spec(oneHeader,
                new RFC2109VersionHandler(),
                new BasicPathHandler(),
                new RFC2109DomainHandler(),
                new BasicMaxAgeHandler(),
                new BasicSecureHandler(),
                new BasicCommentHandler());
  
        // 网景规范草案  
        this.netscapeDraft = new NetscapeDraftSpec(
                new BasicDomainHandler(), // 域名处理
                new BasicPathHandler(), // Path 处理
                new BasicSecureHandler(), 
                new BasicCommentHandler(),
                new BasicExpiresHandler( // 超时时间处理
                        datepatterns != null ? datepatterns.clone() : new String[]{NetscapeDraftSpec.EXPIRES_PATTERN}));
    }

    @Override
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        if (cookie.getVersion() > 0) {
            if (cookie instanceof SetCookie2) {
                return strict.match(cookie, origin);
            } else {
                return obsoleteStrict.match(cookie, origin);
            }
        } else {
            // 默认 Cookie 版本是 0 走 网景规范草案
            return netscapeDraft.match(cookie, origin);
        }
    }
}
```

## NetscapeDraftSpec 

### BasicDomainHandler

```java
    @Override
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        Args.notNull(cookie, "Cookie");
        Args.notNull(origin, "Cookie origin");
        final String host = origin.getHost();
        String domain = cookie.getDomain();
        if (domain == null) {
            return false;
        }
        if (domain.startsWith(".")) {
            domain = domain.substring(1);
        }
        domain = domain.toLowerCase(Locale.ROOT);
        // Host 必须与 Cookie 中的设置的 domain 完全一致
        if (host.equals(domain)) {
            return true;
        }
        if (cookie instanceof ClientCookie) {
            // 如果 ClientCookie 包含属性 ClientCookie.DOMAIN_ATTR，
            if (((ClientCookie) cookie).containsAttribute(ClientCookie.DOMAIN_ATTR)) {
                // 后缀能匹配上即可 .baidu.com 匹配 xxx.baidu.com
                return domainMatch(domain, host);
            }
        }
        // 如果上面两个条件都没满足，设置的 Cookie 会被过滤掉
        return false;
    }

```

#### domainMatch

```java
    static boolean domainMatch(final String domain, final String host) {
        if (InetAddressUtils.isIPv4Address(host) || InetAddressUtils.isIPv6Address(host)) {
            return false;
        }
        final String normalizedDomain = domain.startsWith(".") ? domain.substring(1) : domain;
        if (host.endsWith(normalizedDomain)) {
            final int prefix = host.length() - normalizedDomain.length();
            // Either a full match or a prefix endidng with a '.'
            if (prefix == 0) {
                return true;
            }
            if (prefix > 1 && host.charAt(prefix - 1) == '.') {
                return true;
            }
        }
        return false;
    }
```



# 解决方案

## Cookies 增加 额外的属性

```java
CookieStore cookieStore = new BasicCookieStore();
BasicClientCookie clientCookie = new BasicClientCookie("asd", "asd");
clientCookie.setDomain(".baidu.cn");
clientCookie.setPath("/");
// ❤
clientCookie.setAttribute(ClientCookie.DOMAIN_ATTR, "true");
cookieStore.addCookie(clientCookie);
```

## Domain 设置成跟主机名一样


```java
CookieStore cookieStore = new BasicCookieStore();
BasicClientCookie clientCookie = new BasicClientCookie("asd", "asd");
// ❤
clientCookie.setDomain("www.baidu.cn");
clientCookie.setPath("/");
cookieStore.addCookie(clientCookie);
```




# Read More

- [The Cookie Spec](http://web.archive.org/web/20020803110822/http://wp.netscape.com/newsref/std/cookie_spec.html) （NetscapeDraftSpec 类注释）
- [Cookies getting ignored in Apache httpclient 4.4](https://stackoverflow.com/questions/29970409/cookies-getting-ignored-in-apache-httpclient-4-4)
- [public_suffix_list](https://publicsuffix.org/list/public_suffix_list.dat)
- [70489c4bb03491b6ea0bec60904fc78782963a3a 提交： RFC 6265 compliant cookie spec](https://github.com/apache/httpcomponents-client/commit/70489c4bb03491b6ea0bec60904fc78782963a3a#diff-54e2f21af41829eebd8e809f58bd490a)
- [http 协议之 cookie 标准 RFC6265 介绍](https://www.cnblogs.com/sunzhenchao/p/3897890.html)