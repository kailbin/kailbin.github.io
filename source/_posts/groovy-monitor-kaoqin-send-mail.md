---------------
title: Groovy 监控考勤 发邮件提醒
date: 2017-05-13
desc: Groovy 监控考勤 发邮件提醒
tags: [Groovy,mail]
categories: Groovy
---------------

刚开始了解 Groovy，本文纯属练手，可以看出来，虽然是Groovy，但是基本上没有用到Groovy的任何语法特性，写的过程基本上是我先用Java 写好，然后全部拷贝到一个 `monitor.KaoQin.groovy` 文件中，o(╯□╰)o

为什么要监控考勤的打卡信息呢？ 原来公司打卡器就在门口，最近公司装修好之后换了位置，上下班的的时候看不到打卡器，老是忘打卡（实际上下班的时候定个闹钟就行了，该程序纯属练手o(╯□╰)o）

<!--more-->

# 获取考勤信息

公司的考勤系统是“引进”的，而且是 Asp.Net 写的，对Asp.Net不是特别的了解，其每次提交的表单都有很多乱七八糟的隐藏域，所以每次模拟提交都需要先 GET 一下当前页面才行，以下是基本逻辑。

1. 访问登录页，获取登陆表单
2. 登陆，同一个 HttpClient 会保存 Session 信息
3. 访问打卡记录页面，获取表单信息，构造表单参数
4. 获取当天打卡记录
5. 解析打卡信息，判断是否打卡
6. 如果没有打卡，发送邮件提醒

该程序并没有通用性，只针对特定的考勤系统

``` Groovy
package monitor

// 添加依赖
@groovy.lang.Grapes([
        @groovy.lang.Grab('org.jsoup:jsoup:1.10.2'),
        @groovy.lang.Grab('org.apache.httpcomponents:httpclient:4.5.3'),
        @groovy.lang.Grab('org.apache.httpcomponents:httpmime:4.5.3')
])

import mail.Mail

import org.apache.http.NameValuePair
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicNameValuePair
import org.apache.http.util.EntityUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.select.Elements

import java.nio.charset.Charset

/**
 * 考勤监控， 每周一到到周五，定时检查当天是否有打卡记录
 * 25 9 * * MON-FRI
 * 35 18 * * MON-FRI
 *
 * groovy -c utf-8 monitor/KaoQin.groovy
 */

String HOST = "http://考勤地址"
String USERNAME = "****"
String PASSWORD = "****"

String URL_LOGIN = "$HOST/Login.aspx"
String URL_ATTENDANCE_DATA = "$HOST/StaffSelf/AttendanceData.aspx"


CloseableHttpClient httpClient = HttpClients.createDefault()

/*******************************************************************************
 * 获取登陆表单
 */
HttpGet httpGet = new HttpGet(URL_LOGIN)
CloseableHttpResponse httpResponse = httpClient.execute(httpGet)
String initHtml = EntityUtils.toString(httpResponse.getEntity())

Elements inputs = Jsoup.parse(initHtml).select("#form1 input")
List<NameValuePair> loginNameValuePairs = new ArrayList<>()
for (Element input : inputs) {
    String name = input.attr("name")
    if ("txtName" == name) {
        loginNameValuePairs.add(new BasicNameValuePair("txtName", USERNAME))
    } else if ("txtPass" == name) {
        loginNameValuePairs.add(new BasicNameValuePair("txtPass", PASSWORD))
    } else {
        loginNameValuePairs.add(new BasicNameValuePair(name, input.attr("value")))
    }
}

/*******************************************************************************
 * 登陆
 */
HttpPost httpPost = new HttpPost(URL_LOGIN)
httpPost.setEntity(new UrlEncodedFormEntity(loginNameValuePairs, Charset.forName("UTF-8")))
httpClient.execute(httpPost)

/*******************************************************************************
 * 获取考勤表单
 */
httpGet = new HttpGet(URL_ATTENDANCE_DATA)
httpResponse = httpClient.execute(httpGet)
String attendanceDataHtml = EntityUtils.toString(httpResponse.getEntity())

inputs = Jsoup.parse(attendanceDataHtml).select("#form1 input")

// 查询日期
Calendar calendar = Calendar.getInstance()
calendar.set(Calendar.HOUR_OF_DAY, 8)
calendar.set(Calendar.MINUTE, 0)
calendar.set(Calendar.SECOND, 0)
calendar.set(Calendar.MILLISECOND, 0)

List<NameValuePair> attendanceDataNameValuePairs = new ArrayList<>()
for (Element input : inputs) {
    String name = input.attr("name")
    if ("ASPxRoundPanel1_deStartTime_Raw" == name || "ASPxRoundPanel1_deEndTime_Raw" == name) {
        attendanceDataNameValuePairs.add(new BasicNameValuePair(name, "" + calendar.getTimeInMillis()))
    } else {
        attendanceDataNameValuePairs.add(new BasicNameValuePair(name, input.attr("value")))
    }
}

/*******************************************************************************
 * 查询考勤数据
 */
httpPost = new HttpPost(URL_ATTENDANCE_DATA)
httpPost.setEntity(new UrlEncodedFormEntity(attendanceDataNameValuePairs, Charset.forName("UTF-8")))
httpResponse = httpClient.execute(httpPost)
String attendanceData = EntityUtils.toString(httpResponse.getEntity())

/*******************************************************************************
 * 解析考勤数据
 */
Elements trs = Jsoup.parse(attendanceData).select(".dxgvTable_Office2010Blue tr.dxgvDataRow_Office2010Blue")
int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

// 12点之前须有一次打卡记录， 12点之后需要有两次打卡记录
if (!((hour < 12 && trs.size() >= 1) || (hour >= 12 && trs.size() >= 2))) {
    Mail.send("记得打卡", "记得打卡: <a href='$HOST' target='_balnk'>$HOST</a>", "ykb553@163.com")
}
```

# 发送邮件

``` Groovy
/**
 * groovy -c utf-8 mail/Mail.groovy
 */
package mail

@groovy.lang.Grapes([
        @groovy.lang.Grab('org.apache.commons:commons-email:1.4')
])
import org.apache.commons.mail.HtmlEmail

class Mail {

    private static interface Const {
        String SMTP = "smtp.163.com"
        String USERNAME = "****" // 修改成你的邮箱
        String PASSWORD = "****" // 修改成你的邮箱密码
        String FROM = USERNAME
    }

    static def send(String subject, String contentHtml, String... to) {
        HtmlEmail email = new HtmlEmail()
        email.setHostName(Const.SMTP)
        email.setAuthentication(Const.USERNAME, Const.PASSWORD)
        email.setCharset("UTF-8")  // 指定编码方式，否则会乱码

        email.setFrom(Const.FROM)
        email.setSubject(subject)     // 主题
        email.setHtmlMsg(contentHtml) // 内容
        email.addTo(to)               // 发送给谁

        println email.send()
    }

}

//mail.Mail.send("测试主题", "测试内容", "blog@kail.xyz")

```

# 运行

执行 `groovy -c utf-8 monitor/KaoQin.groovy`, 定时执行的话使用 Linux 的 `crontab`。

这里使用的是 `Grape` 作为的依赖管理，第一次运行 Groovy自带了一个嵌入式的jar依赖管理器，会下载指定的依赖到`~/.groovy/grapes` 目录下。

### 拓展阅读


>[The Grape dependency manager](http://www.groovy-lang.org/grape.html)  
>[Grape 依赖管理器](http://blog.csdn.net/u011054333/article/details/60478730)  
>
>[Commons-Email 收发邮件](http://www.oschina.net/translate/commons-email-userguide)  
>
>[Groovy Download](http://www.groovy-lang.org/download.html)