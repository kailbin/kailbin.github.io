---

title: git 常用命令
date: 2016-6-10
desc: git 常用命令

tags: [git,常用命令]

---

### 全局设置

- 设置用户、邮箱  
`$ git config --global user.name "<用户名>"  `  
`$ git config --global user.email "<电子邮件>"  `

- 以下命令能让Git以彩色显示。  
`$ git config --global color.ui auto`

<!--more-->

- 设置别名  
`把「checkout」缩略为「co」，然后就使用「co」来执行命令。`  
`$ git config --global alias.co checkout`  

- 显示设定清单	
`$ git config --global --list`  

- 免密码登陆  
`$ git remote set-url  <branch-name> <https://username:password@url>` 
 

 ### 本地操作

- 初始化本地仓库   
`$ git init`  

- status命令确认工作树和索引的状态 
`$ git status`    

- 将文件加入到索引    
`$ git add <file>`      
用空格分割可以指定多个文件   

- 把所有的文件加入到索引 
`$ git add . `  

- 提交文件到本地仓库   
`$ git commit -m "message" `  

- 修改提交的注释   
`$ git commit --amend`    

- 查看本地仓库提交历史  
`$ git log`   
--graph:能以文本形式显示更新记录的流程图    
--oneline:能在一行中显示提交的信息  


``` Git
$ git log --graph --oneline
*   d845b81 合并
|\
| * 4c01823 添加pull的说明
* | 95f15c9 添加commit的说明
|/
* 3da09c1 添加add的说明
* ac56e47 first commit
```

- 暂时保存现状的操作  
`$ git stash save`

- 显示暂存列表  
`$ git stash list`  

- 恢复暂存的操作  
`$ git stash pop`  

- 删除暂存的操作  
`$ git stash drop`  

- 删除所有暂存的操作  
`$ git stash clear`  

- 查看文件每一行的作者  
`$ git blame <filename>`

- 查看文件每几行的作者  
    - 某一行  
    `$ git blame -L <n1,n1> <filename>`
 
    - 某一行到最后一行  
    `$ git blame -L <n> <filename>`

    - 某几行  
    `$ git blame -L <n3,n5> <filename>`

### 远程操作

- 克隆远程仓库    
`$ git clone <repository-url> [directory本地路径名]`

- 为远程仓库URL命名    
`$ git remote add <name> <url>`

- 推送到远程仓库   
`$ git push [origin] [master]`

- 拉取远程仓库    
`$ git pull [origin] [master]`

- 查看远程分支名   
`$ git remote`

- 查看远程分支名和对应的URL    
`$ git remote -v`

- 删除    
`$ git remote remove <name>`


- 修改URL   
`$ git remote set-url  <name> <newurl>`

- 添加    
`$ git remote add  <name> <url>`

- 重命名   
`$ git remote rename <old> <new>`

- 把本地分支推送到其他项目分支  
    1. 新增一个远程项目地址 `$ git remote add  <other-origin> <url>`
    2. 把本地分支推送到另一个项目的远程分支  
        `$ git push <other-origin> <local-branch-name>:<other-branch-name>`



### 分支

- 创建分支      
`$ git branch <branchname>`       

- 查看分支              
`$ git branch`

- 查看远程分支    
`$ git branch -r `

- 查看所有分支  
`$ git branch -a`

- 切换分支  
`$ git checkout <branch>`

- 创建并切换分支  
`$ git checkout -b <branch>`

- 合并分支  
`$ git merge <commit>`

- 删除本地分支  
`$ git branch -d <branchname>`

- 删除远程分支  
`$ git push origin :<branchname>`

- 修改分支  
`$ git branch -m <oldbranch> <newbranch>`

### Tag

- 新建标签   
`$ git tag <tagname>` 

- 显示包含标签资料的历史记录  
`$ git log --decorate`

- 新建标签的时候添加说明  
`$ git tag -a <tagname>`  
或  
`$ git tag -am "连猴子都懂的Git" banana`  

- 显示标签  
`$ git tag`

- 显示标签和注释  
 `$ git tag -n`

- 删除标签  
`$ git tag -d <tagname>`

- 查看远端分支信息  
`$ git ls-remote`

- 删除远端tag  
`$ git push origin :refs/tags/<tagname>`

### 恢复

- 恢复为提交的文件  
`$ git checkout -- <filename>`

- 恢复已提交的记录并创建新记录        
`$ git revert HEAD`

- 删除已提交的记录  
`$ git reset --hard HEAD`

- 拉取某个版本到当前分支  
`$ git cherry-pick HEAD`








> 猴子都能懂的GIT：[http://backlogtool.com/git-guide/cn/](http://backlogtool.com/git-guide/cn/)


