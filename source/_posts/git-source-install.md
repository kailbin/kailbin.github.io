---

title: Git 源码方式安装
date: 2016-09-15
desc: Git 源码方式安装

---

默认使用 yum 或者 apt 安装Git 的时候，版本会比较老，所以这里使用源码方式进行安装。

<!--more-->

### 下载地址

- https://www.kernel.org/pub/software/scm/git/
- https://github.com/git/git/releases

以上两个地址都可以，感觉第一个比较快一点。

### 环境准备
``` bash
$ yum install curl-devel expat-devel gettext-devel openssl-devel perl-devel zlib-devel autoconf
```
or
```bash
$ apt-get install libcurl4-gnutls-dev libexpat1-dev gettext  libz-dev libssl-dev autoconf
```

### 安装

```bash
wget https://www.kernel.org/pub/software/scm/git/git-2.11.0.tar.gz
tar zxvf git-2.11.0.tar.gz
cd git-2.11.0/
make configure
        
./configure --prefix=/usr
make
make install
# or
# make all doc info
# make install install-doc install-html install-info
```

    
    

> [Installing from Source](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)