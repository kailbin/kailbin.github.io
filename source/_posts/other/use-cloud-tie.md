---------------
title: 使用网易云跟帖
date: 2017-05-14
desc:  使用网易云跟帖
tags: [hexo]
categories: Other
---------------

刚开始评论使用的是多说，不幸的是[撂挑子了](http://dev.duoshuo.com/threads/58d1169ae293b89a20c57241)；
后来用了一段时间[友言](http://www.uyan.cc/),发现是在是渣的不行，服务器不行，老是挂，连官方博客的评论都挂了也没人管；
这次吸取经验，使用黄易的“网易云跟帖”，毕竟是大厂，应该不会甩手不干吧。

这里基于 Hexo 的 next 主题进行修改，集成网易云跟帖 评论。

<!--more-->


# 修改next主题模板文件

文件路径 **themes/next/layout/_partials/comments.swig**

在**{<!---->% endif %}** 上新增一下代码

```
{% elseif theme.cloud_tie %}

    <div id="cloud-tie-wrapper" class="cloud-tie-wrapper"></div>
    <script src="https://img1.cache.netease.com/f2e/tie/yun/sdk/loader.js"></script>
    <script>
    var cloudTieConfig = {
        url: document.location.href, 
        sourceId: "",
        productKey: "{{ theme.cloud_tie }}",
        target: "cloud-tie-wrapper"
    };
    var yunManualLoad = true;
    Tie.loader("aHR0cHM6Ly9hcGkuZ2VudGllLjE2My5jb20vbW9iaWxlL2xpdmVzY3JpcHQuaHRtbA==", true);
    </script>
```

# 修改next主题配置文件

找到 **# Hypercomments**，在该配置下面新增一下配置

```
cloud_tie: 改为你的productKey
```
productKey 从 "[获取代码](https://manage.gentie.163.com/#/code) > Web 代码"里面获得


### 拓展阅读


>[网易云跟帖](https://gentie.163.com/)  
>
>[网易云跟帖帮助文档](https://gentie.163.com/help.html)  