---

title: Shell 获取命令行参数
date: 2017-2-20
desc: Shell 获取命令行参数,Shell特殊变量

tags: [shell,git]

---

前一段时间使用 Server 端 Git Hooks 的时候，需要获取 Git 提交的信息，这里对如何获取 Git 传递的参数 进行简单记录总结。

git 通过`传递参数` 或者 `标准输入流`的形式传递提交的信息。


<!--more-->

# 获取参数信息

git hooks 的 **`update` 可执行文件** 会在已经推送完成但是还没有更新到远程仓库的时候执行，这时候可以获得推送上来的数据内容，分析是否接受推送。

git 会传递一下三个参数，接收方法如下：

``` bash
#!/bin/sh

refname="$1"    # the name of the ref being updated,
oldrev="$2"     # the old object name stored in the ref,
newrev="$3"     # and the new object name to be stored in the ref.
```




# 获取标准输入流信息

git hooks 的 **`post-receive` 可执行文件** 会在整个推送周期完成后执行，一般用来进行事件通知。

与 **`update` 可执行文件** 不同的是，git会以标准输入流的形式传递参数给`post-receive`文件，数据格式如下：

``` bash
<old-value> SP <new-value> SP <ref-name> LF
```

接收方法如下：


``` bash
#!/bin/sh

read oldValue newValue refName
```
此处用三个变量接收标准输入流参数，如果参数超过三个，从第三个参数开始，后面所有的参数都会赋值给最后一个`refName`变量。
 
 
 
 

# 获取返回的状态码

可执行文件中如果遇到 `exit <num>` 会退出执行，git会获取退出的状态码，如果是`0`，则接受提交，否则拒绝提交。

获取退出状态的方式是 `$?`，Java 中的 `System.exit(0);` 即是退出状态码。





# Bash 特殊变量总结

|  变量  |  含义  |
|------:|:-------|
|    `$n` | 传递给脚本或函数的参数。n 是一个数字，表示第几个参数。<br> 例如，第一个参数是`$1`，第二个参数是`$2`，大于9的要写成 `${10}`|
|    `$?` | 上个命令的退出状态，或函数的返回值 |
|    `$#` | 传递给脚本或函数的参数个数|
|    $$ | 当前Shell进程ID。对于 Shell 脚本，就是这些脚本所在的进程ID|
|    $0 | 当前脚本的文件名|
|    $* | 传递给脚本或函数的所有参数，所有参数以一个双引号包裹 `"$1 $2 … $n"` |
|    `$@` | 传递给脚本或函数的所有参数，每个参数都以双引号包裹 `"$1" "$2" … "$n"`|
|    $_ | 上一个命令的最后一个参数|
|    $! | 后执行的后台命令的进程ID |
	
	

# 拓展阅读

> [Special Parameters](http://www.gnu.org/software/bash/manual/bash.html#Special-Parameters)
>
> [Shell特殊变量](http://c.biancheng.net/cpp/view/2739.html)
>
> [git hooks Manual Page](https://www.kernel.org/pub/software/scm/git/docs/githooks.html)
