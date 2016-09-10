---

title: Git 恢复和还原
date: 2016-09-10
desc: git 恢复和还原

---

### 删除未被跟踪的文件
`$ git clean [param]`
  
    `-n` 仅显示需要删除的文件，并不进行删除   
    `-d` 删除文件和目录  
    `-f` 强制删除  
    `-x` 删除忽略和为忽略的文件  
    `-X` 仅删除忽略的文件  

显示哪些文件可以删除 ： `$ git clean -n`   
强制删除文件夹和文件 ： `$ git clean -dfx`






### 检出一个分支上的某一次提交

`$ git cherry-pick [commit_hash]`

cherry-pick比较好的使用场景是在`一个分支上想要另一个分支的单独的某个特性`。这个命令经常会引起冲突，所以需要小心使用。






### 撤销修改

`$ git checkout -- <file>` 撤销对工作区修改  
`$ git reset HEAD -- <file>` 这个命令仅改变暂存区，并不改变工作区，这意味着`在无任何其他操作的情况下，工作区中的实际文件同该命令运行之前无任何变化`   





### 暂存

`$ git stash`  备份当前的工作区的内容，将当前的工作区内容保存到Git栈中  
`$ git stash pop`  从Git栈中读取最近一次保存的内容，恢复工作区的相关内容  
`$ git stash list`  显示Git栈内的所有备份  
`$ git stash clear`  清空Git栈  




### 不保留提交记录，还原到某次提交

- `$ git reset –-hard [commit_hash]`   
工作区和暂存区都会恢复到指定的版本  


- `$ git reset -–mixed [commit_hash]`  
此为默认方式，只保留源码，回退暂存区信息  
此时再执行 `$ git checkout -- .`，就相当于执行了`–-hard`模式。
相当于`-–mixed` 之后那些修改文件还没有纳入版本管理，不想恢复了可以再 `add` 、`commit`


- `$ git reset -–soft [commit_hash]`  
回退到某个版本，只回退了本地仓库的信息，不会恢复暂存区信息。  
此时再执行`$ git reset HEAD` 和 `git checkout -- .`，就相当于执行了`–-hard`模式  
相当于`-–soft` 之后那些修改文件已经纳入版本管理，但是还没有提交到本次仓库，不想恢复了可以直接`commit`

还原之后，由于本地版本落后于远端分支版本，push 的时候需要加上 -f 参数强制push，强制push有可能会把别的提交也覆盖掉，造成代码丢失，所有不建议用强制push。
可以在本地当前的版本上再建一个分支，推送到远端分支。





### git revert

### git rebese















### 查看丢失的提交记录并还原

`$ git reflog` 查看你所有的提交记录，包含`reset`后丢失的提交

```                                                                                                                                                                                                                          
6517ed3 HEAD@{1}: commit message                                                                                                                                                                                                                                      
e836a60 HEAD@{2}: commit message   
7827000 HEAD@{3}: commit message   
……
8aff630 HEAD@{n}: commit message   
```
`$ git show [commit_hash]` 查看提交的内容    

`$ git merge [commit_hash]` 合并其中一次提交   







==============================================================







<!--
9. 检查丢失的提交

尽管reflog是一种检查丢失提交的方法，大型的库里却不太实用。这个时候，应该用fsck（文件系统检查）命令。

git fsck --lost-found



这里你可以看到一个丢失的提交。你可以通过git show [commit_hash] 查看提交的更改或者通过运行git merge [commit_hash]命令进行恢复。

git fsck跟reflog命令相比有一个优点。假设你删除了一个远程分支，然后clone了这个库。用fsck命令你可以找到并且恢复这个删除的远程分支。

-->