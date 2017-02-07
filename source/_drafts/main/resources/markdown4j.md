# 概要

### 2016年6月13日 修改记录

1. 数据库以 172.16.2.50 上的 crawler_138 为准

2. 线下推送和抓取程序都在 172.16.2.138 机器的 henry 用户下面。  
    2.1 推送程序详见 `crontab -l` , 程序目录 `/home/henry/ttpdata-test`  
    2.2 爬虫程序目录 `/home/henry/ttp_spider`

3. 58的电话号码抓取详见 `com.ttpai.spider.crawler.quartz.Spder58PhoneJob` 类

# 数据抓取和推送

## 程序发布
>git clone http://172.16.2.2/ttp/ttp_spider.git
>cd ttp_spider  
>mvn compile  
>cd target/ttp_spider  
>cp ../ttp_spider-1.0.0.jar ./  

这时候 target/ttp_spider 目录下的文件为程序所有文件，拷贝到任意目录即可

## 文件介绍
conf ：　配置文件  
lib ：　第三方jar包  
webapp ：　web文件目录  
start.sh ：　启动文件（仅供参考）  
ttp_spider-1.0.0.jar ：　启动入口   

## 配置文件
### 系统参数
conf/config.properties
#### 数据库链接配置
default.driverClassName=com.mysql.jdbc.Driver  
**default.url**=jdbc:mysql://localhost/crawler?useUnicode=true&characterEncoding=utf-8  
**default.username**=username  
**default.password**=password  
#### 参数配置
**devMode** = false  
**showSql** = false  
asyn = true  
regexFilter=[\uD83C\uDC00-\uD83C\uDFFF]|[\uD83D\uDC00-\uD83D\uDFFF]|[\u2600-\u27FF]  

### 日志配置
conf/log4j.properties

## 启动程序
>java -jar ./ttp_spider-1.0.0.jar -webroot webapp -ctx / -port 80 -scan -1  

## 启动参数说明
>-webroot web文件目录  
>-ctx 上下文路径  
>-port 端口  
>-scan -1 代表不扫描变化  



## 抓取的程序
### 抓取的地区与代码 `tag`
| 编号 | 城市 | tag | code | reg |
| ---- | ------ | ---- | --- |--- |
|1 | 北京 | bj|1|010|
|2 | 上海 | sh|2|021|
|3 | 深圳 |sz|1457|0755|
|4 | 成都| cd|1760|028|
|5 | 南京| nj|571|025|
|6 | 广州| gz|1456|020|
|7 | 武汉| wh|1231|027|
|8 | 天津| tj |3|022|
|9 | 东莞| dg |1465|0769|
|10 | 苏州| suz |581|0512|
|11 | 杭州 |hz |865|0571|
|12 | 重庆 |cq |4|023|
|13 | 佛山 |fs |1468|0757|
|14 | 宁波 | nb |866|0574|
|15 | 合肥 | hf |652|0551|
|16 | 青岛 | qd |738|0532|
|17 | 长沙 | cs |1121|0731|
|18 | 西安 | xa |1974|029|
|19 | 郑州 | zz |1315|0371|
|20 | 南宁 | nn |1582|0771|
|   |             |     |    |    |
|   |             |     |    |    |
|21 | 惠州(深圳) | huiz |  |   |
|22 | 滁州(南京) | cz |  |   |
|23 | 丽水(南京) | ls |  |   |
|24 | 珠海(广州) | zh |  |   |
|25 | 中山(广州) | zs |  |   |



### 抓取的网站 `site`

| site | source | 网站 | url | 说明 |
| ---- | ------ | ---- | --- | ---- |
| 58   | 1-22-444  | 58同城       | http://sh.58.com/ershouche/ |  | 
| gj   | 1-22-447  | 赶集       | http://sh.ganji.com/ershouche/ |  | 
| tc   | 1-22-443  | 淘车       | http://chengdu.m.taoche.com/ | 97%的数据是商家 |
| bx   | 1-22-442  | 百姓       | http://shanghai.baixing.com/ershouqiche/ |  |
| c168 | 1-22-439  | 二手车之家 | http://www.che168.com |  |
| dyc  | 1-22-448  | 第一车     | http://so.iautos.cn/shanghai/ |  |
| cn2c | 1-22-499  | 中国二手车 | http://sh.cn2che.com/ | 已暂停 |
| sohu | 1-22-502  | 搜狐       | http://2sc.sohu.com/sh/  | 已暂停 |


# 新旧source
| s1 | s2| 旧source | 新source|
| ---- | ------ | ---- | --- |
|线上抓取|	二手车之家|	4-22-394|	3-76|
|线上抓取|	百姓网|	4-22-397|	3-79|
|线上抓取|	淘车网|	4-22-393|	3-80|
|线上抓取|	58同城|	4-22-391|	3-81|
|线上抓取|	第一车市|	4-22-448|	3-77|
|线上抓取|	二手车之家|	4-22-439|	3-76|
|线上抓取|	百姓网|	4-22-442|	3-79|
|线上抓取|	淘车网|	4-22-443|	3-80|
|线上抓取|	58同城|	4-22-444|	3-81|
|线上抓取|	赶集网|	4-22-447|	3-78|


## 数据推送

### 黄牛号查找

### 数据推送

``` java
aasdads
    asd
```