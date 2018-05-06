---
title: 国际电话号码格式
tags:
  - 随笔
categories:
  - Other
toc: false
date: 2018-05-06
---


`E.164` 是国际电话编号计划，它可以确保 `PSTN` 上的每个设备具有全球唯一的号码，使得 电话和短信 正确路由到不同国家的个人电话。 E.164号码格式化为 `[+] [国家代码] [用户号码，包括地区代码]`，最多可以有`15`位数字。

> [公共交换电话网](https://zh.wikipedia.org/wiki/%E5%85%AC%E5%85%B1%E4%BA%A4%E6%8D%A2%E7%94%B5%E8%AF%9D%E7%BD%91)（Public Switched Telephone Network 或 简称 `PSTN`）是一种用于全球语音通信的电路交换网络，是目前世界上最大的网络，拥有用户数量大约是8亿。


<!-- more -->

## E.164 号码示例

| E.164 Format  |  Country Code	 |  Country  |  Subscriber Number  |
|----|----|----|----|
|  +1  4155552671  |  1 |  US(美国) |  4155552671  |  
|  +44 2071838750 |  44 |  GB(英国) |  2071838750 |  
|  +55 1155256325 |  55 |  BR(巴西) |  1155256325 |  
|  +85 15300830723 |  86 |  CN(中国) |  15300830723 |  

> [ISO 3166](https://zh.wikipedia.org/wiki/ISO_3166)


## E.164 正则

有时您需要以编程方式验证字符串是否是效的`E.164`电话号码格式。
如：获取表单中用户的电话号码，发送SMS 或 语音电话 以及 验证数据库中的电话号码。
根据国际电联`E.164`的建议，电话号码格式必须是以**`+`开头的最多15位数字**，并排除0作为第一个字符的电话号码（因为没有以0开头的国家代码）。
一个正则表达式示例: `^\+?[1-9]\d{1,14}$`

这个正则也会匹配到无效的电话号码。
您需要指定一组更复杂的模式 来匹配全球所有国家的有效`E.164`号码。
更简单和更强大的选择是使用`Twilio Lookup API`来执行电话号码验证和格式化，而不是`RegEx`（这是原味的的一个广告 😆）。

> [国际电话区号列表](https://zh.wikipedia.org/wiki/%E5%9B%BD%E9%99%85%E7%94%B5%E8%AF%9D%E5%8C%BA%E5%8F%B7%E5%88%97%E8%A1%A8)
> [E.164 : The international public telecommunication numbering plan](https://www.itu.int/rec/T-REC-E.164/en)


## Read More

- [E.164](https://www.twilio.com/docs/glossary/what-e164)
- [Formatting International Phone Numbers](https://support.twilio.com/hc/en-us/articles/223183008-Formatting-International-Phone-Numbers)
