---
layout: post
paginate_path: blog/page:num

title: 把sublime添加到右键菜单
contentimg: 2014-12-13-contentImages.jpg

---

突然对sublime很感兴趣，原因很简单：看起来很高大上，启动速度很快，在Linux上也可以运行。  

[sublime官方](http://www.sublimetext.com/2)在 windows 上提供两种版本的安装文件，
一种是安装版，一种是解压版，安装版会自动在右键菜单添加类似与“用Sublime打开”的选项，而解压版（便携版）则不会。  

<!--break-->

1. 打开注册表， Windows键 + R → 运行 → regedit
2. 在 HKEY_CLASSSES_ROOT→ * → Shell 下面新建项命名为 sublime3
3. 在新建字符串值 命名为Icon，值为 “sublime text 所在路径*,0*″，例如 *sublime_home*\sublime_text.exe*,0*
4. 在 sublime3 ，下面新建项command ,在默认中输入值为 *sublime_home*\sublime_text.exe* %1*  

OK! 此时右键一个文件的时候就会在右键菜单中显示 sublime3，而且是带图标的。

右键菜单默认显示的名字是注册表键值的名字，这里是“sublime3”，如果想修改显示的名字，右键默认->修改->数值数据，即可。

相关截图：

<img src="{{ site.contentImgPath }}2014-12-13-sublime-regedit1.jpg">

<img src="{{ site.contentImgPath }}2014-12-13-sublime-regedit2.jpg">



