---

title: base64 和 jq 命令
date: 2016-12-06
desc: base64 和 jq 命令

---

base64 一般会默认安装，可以进行 base64 编码和解码。  
jq 可以对json进行解析、选择、格式化、高亮等，需要通过 `apt-get install jq` 进行安装。


<!--more-->

### 测试数据

##### ① 原始数据

    eyJuYW1lIjoiNWJDUDVwaU8iLCJhZ2UiOjI0LCJDb250YWN0Ijp7InRlbCI6IjE4NzMzMzMzMzMzIiwicXEiOiI1NTU1NTU1NTUifSwiZnJpZW5kcyI6W3sibmFtZSI6IjViQ1A2SXF4IiwiYWdlIjoyMn0seyJuYW1lIjoiNWJDUDZJdTUiLCJhZ2UiOjI0fV19

**将原始数据保存到 `test.json` 文件中。**  


##### ② Bsee64 解码之后

```json
{
    "name":"5bCP5piO",
    "age":24,
    "Contact":{
        "tel":"18733333333",
        "qq":"555555555"
    },
    "friends":[
        {
            "name":"5bCP6Iqx",
            "age":22
        },
        {
            "name":"5bCP6Iu5",
            "age":24
        }
    ]
}
```

##### ③ json 字段解码之后
```json
{
    "name":"小明",
    "age":24,
    "Contact":{
        "tel":"18733333333",
        "qq":"555555555"
    },
    "friends":[
        {
            "name":"小花",
            "age":22
        },
        {
            "name":"小苹",
            "age":24
        }
    ]
}
```

### bass64

##### 简述

```bash
  
echo "str" | base64         # 将字符串 str + \n 编码为base64字符串输出 
echo -n "str" | base64      # 将字符串 str编码为base64字符串输出。注意与上面的差别
    
echo "str" | base64 -d      # 将base64编码的字符串 str + \n  解码输出  
echo -n "str" | base64 -d   # 将base64编码的字符串str解码输出
        
base64                      # 从标准输入中读取数据，按Ctrl+D结束输入。将输入的内容编码为base64字符串输出  
base64 file                 # 从指定的文件file中读取数据，编码为base64字符串输出 
    
base64 -d                   # 从标准输入中读取已经进行base64编码的内容，解码输出  
base64 -d file              # 从指定的文件file中读取base64编码的内容，解码输出  
    
base64 -d -i                # 从标准输入中读取已经进行base64编码的内容，解码输出。加上-i参数，忽略非字母表字符，比如换行符  

```


##### 测试 1
```bash
cat test.json | base64 -d
```
##### 结果 1 

    {"name":"5bCP5piO","age":24,"Contact":{"tel":"18733333333","qq":"555555555"},"friends":[{"name":"5bCP6Iqx","age":22},{"name":"5bCP6Iu5","age":24}]}




### jq

##### 格式化输出json
```bash
cat test.json | base64 -d | jq "."            
```
结果  

    {
        "name":"5bCP5piO",
        "age":24,
        "Contact":{
            "tel":"18733333333",
            "qq":"555555555"
        },
        "friends":[
            {
                "name":"5bCP6Iqx",
                "age":22
            },
            {
                "name":"5bCP6Iu5",
                "age":24
            }
        ]
    }



```bash
cat test.json | base64 -d | jq ".name"                                               # "5bCP5piO"
cat test.json | base64 -d | jq ".Contact.tel"                                        # "18733333333"
cat test.json | base64 -d | jq ".friends[0].name"                                    # "5bCP6Iqx"
cat test.json | base64 -d | jq ".friends[0].name"  | sed 's/"//g'                    # 5bCP6Iqx
cat test.json | base64 -d | jq ".friends[0].name" | sed 's/"//g' | base64 -d         # 小花
    
cat test.json | base64 -d | jq ".friends[].name"                                     # "5bCP6Iqx"
                                                                                     # "5bCP6Iu5"
```

```bash
cat test.json | base64 -d | jq ".friends[] | {friend_name: .name, friend_age: .age}"
```
    {
      "friend_age": 22,
      "friend_name": "5bCP6Iqx"
    }
    {
      "friend_age": 24,
      "friend_name": "5bCP6Iu5"
    }

```bash
cat test.json | base64 -d | jq "[ .friends[] | {friend_name: .name, friend_age: .age} ]"
```
    [
      {
        "friend_age": 22,
        "friend_name": "5bCP6Iqx"
      },
      {
        "friend_age": 24,
        "friend_name": "5bCP6Iu5"
      }
    ]






> [用base64编解码](http://codingstandards.iteye.com/blog/934928)  
> [jq 官方快速指南](https://stedolan.github.io/jq/tutorial/)  
> [jq 官方手册](https://stedolan.github.io/jq/manual/)  