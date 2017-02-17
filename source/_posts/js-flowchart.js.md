---

title: flowchart.js 绘制流程图
date: 2016-12-17
desc: flowchart.js 绘制流程图,Markdown 流程图

tags: [js]

---

通过文本绘制简单的SVG流程图
> Draws simple SVG flow chart diagrams from textual representation of the diagram



<!--more-->


### 表达式

<textarea id="code" style="box-shadow: 0px 0px 10px #42b983 inset;width: 100%;padding: 10px; " rows="4"></textarea>

<button id="run">生成</button>

<br/>

### 结果
<div id="diagram" style="width: 100% ;border: 1px solid #42b983 ;padding: 10px; margin: 20px 0px"></div>

### 拷贝一下代码到文本框中，点击运行查看效果

##### 官方示例1
```code
st=>start: Start:>http://www.google.com[blank]
e=>end:>http://www.google.com
op1=>operation: My Operation
sub1=>subroutine: My Subroutine
cond=>condition: Yes
or No?:>http://www.google.com
io=>inputoutput: catch something...

st->op1->cond
cond(yes)->io->e
cond(no)->sub1(right)->op1
```

##### 官方示例2
```code
st=>start: Start|past:>http://www.google.com[blank]
e=>end: End|future:>http://www.google.com
op1=>operation: My Operation|past
op2=>operation: Stuff|current
sub1=>subroutine: My Subroutine|invalid
cond=>condition: Yes
or No?|approved:>http://www.google.com
c2=>condition: Good idea|rejected
io=>inputoutput: catch something...|future

st->op1(right)->cond
cond(yes, right)->c2
cond(no)->sub1(left)->op1
c2(yes)->io->e
c2(no)->op2->e
```

##### 开源协议介绍
```code
begin=>start: 如何选择开源许可证？ :> http://www.ruanyifeng.com/blog/2011/05/how_to_choose_free_software_licenses.html[blank]

can_closed=>condition: 修改代码是否
可以闭源？

same_license=>condition: 新增代码是否需
采用同样的许可证

must_license=>condition: 修改过的文件是否
必须放置版权说明

must_doc=>condition: 修改之处是否需
提供说明文档

change_copy=>condition: 衍生的软件广告是否
可以用你的名字促销
  
  
lgpl=>operation: LGPL 许可证
mozilla=>operation: Mozilla 许可证
gpl=>operation: GPL 许可证
bsd=>operation: BSD 许可证
mit=>operation: MIT 许可证
apache=>operation: Apache 许可证
  
  
begin->can_closed(yes)->must_license(no)->change_copy(no)->bsd
can_closed(no)->same_license(no)->must_doc(no)

  
same_license(yes)->gpl
must_license(yes)->apache
must_doc(yes)->mozilla
must_doc(no)->lgpl
change_copy(yes)->mit
```

### 本文中的代码
```html

### 表达式

<textarea id="code" style="box-shadow: 0px 0px 10px #42b983 inset;width: 100%;padding: 10px; " rows="4"></textarea>

<button id="run">生成</button>

<br/>

### 结果

<div id="diagram" style="width: 100% ;border: 1px solid #42b983 ;padding: 10px; margin: 20px 0px"></div>

<script src="/js/raphael-2.2.0-min.js"></script>
<script src="/js/jquery.1.11.0.min.js"></script>
<script src="/js/flowchart-1.6.4.js"></script>

<script>

    $("#code").val("   st=>start: 开始  \n   e=>end: 结束           \n   st->e ".replace(/   /g,""));
    
    function run(){
        $("#diagram").html("");
        flowchart.parse($("#code").val()).drawSVG('diagram',{
            'line-width': 1,
            'font-size': 8,
            'line-length': 8,
            'line-color': '#42b983',
            'element-color': '#42b983',
            'font-color': '#42b983',
            'fill': 'white'
        });
    };
    
    $("#run").click(run);
    
    run();

</script>


```

### flowchart.js 参数配置

```js
diagram.drawSVG('diagram', 
{
    'x': 0,
    'y': 0,
    'line-width': 3,
    'line-length': 50,
    'text-margin': 10,
    'font-size': 14,
    'font-color': 'black',
    'line-color': 'black',
    'element-color': 'black',
    'fill': 'white',
    'yes-text': 'yes',
    'no-text': 'no',
    'arrow-end': 'block',
    'scale': 1,
    // style symbol types
    'symbols': {
        'start': {
          'font-color': 'red',
          'element-color': 'green',
          'fill': 'yellow'
        },
        'end':{
            'class': 'end-element'
        }
    },
    // even flowstate support ;-)
    'flowstate' : {
        // 'past' : { 'fill' : '#CCCCCC', 'font-size' : 12},
        // 'current' : {'fill' : 'yellow', 'font-color' : 'red', 'font-weight' : 'bold'},
        // 'future' : { 'fill' : '#FFFF99'},
        'request' : { 'fill' : 'blue'}//,
        // 'invalid': {'fill' : '#444444'},
        // 'approved' : { 'fill' : '#58C4A3', 'font-size' : 12, 'yes-text' : 'APPROVED', 'no-text' : 'n/a' },
        // 'rejected' : { 'fill' : '#C45879', 'font-size' : 12, 'yes-text' : 'n/a', 'no-text' : 'REJECTED' }
      }
}
);
```


> [flowchart.js](http://flowchart.js.org/)
>
> [Markdown笔记：如何画流程图](https://segmentfault.com/a/1190000006247465)
>
> [如何选择开源许可证？](http://www.ruanyifeng.com/blog/2011/05/how_to_choose_free_software_licenses.html)
>
> [开源许可证的一些介绍](http://git.oschina.net/oschina/git-osc/wikis/License)



<script src="/js/raphael-2.2.0-min.js"></script>
<script src="/js/jquery.1.11.0.min.js"></script>
<script src="/js/flowchart-1.6.4.js"></script>

<script>

    $("#code").val("   st=>start: 开始  \n   e=>end: 结束           \n   st->e ".replace(/   /g,""));
    
    function run(){
        $("#diagram").html("");
        flowchart.parse($("#code").val()).drawSVG('diagram',{
            'line-width': 1,
            'font-size': 8,
            'line-length': 8,
            'line-color': '#42b983',
            'element-color': '#42b983',
            'font-color': '#42b983',
            'fill': 'white'
        });
    };
    
    $("#run").click(run);
    
    run();

</script>

