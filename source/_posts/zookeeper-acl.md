---
title: ZooKeeper ACL 权限控制
date: 2017-01-04 00:00:00
desc: ZooKeeper,ACL,权限控制
---

ZooKeeper ACL 的权限控制信息由三部分组成：`scheme:id:permission`， 分别是 **权限模式**、**权限对象** 和 **权限**。

<!-- more -->

### 权限模式（scheme）
**world**   ：[默认] 固定用户为anyone，为所有Client端开放权限
**digest**  ：Client端由用户名和密码验证，譬如`user:password`，digest的密码生成方式是`BASE64(SHA-1(user:password))`
**auth**    ：不使用任何id，代表任何已确认用户
**ip**      ：Client端由IP地址验证，譬如172.2.0.0/24
**super**   ：在这种scheme情况下，对应的id拥有超级权限，可以做任何事情(cdrwa）

> 注意：`exists`操作和`getAcl`操作并不受ACL许可控制，因此任何客户端可以查询节点的状态和节点的ACL。

### 权限模式（id）

#### digest

**生成密文**
``` bash
$ java -cp ./zookeeper-3.4.6.jar:./lib/slf4j-log4j12-1.6.1.jar:./lib/slf4j-api-1.6.1.jar:./lib/log4j-1.2.16.jar \
org.apache.zookeeper.server.auth.DigestAuthenticationProvider test:test
```
输出：

    test:test->test:V28q/NynI4JI3Rk54h0r8O5kMug=
    
**设置权限**
``` bash
[zk] setAcl /digest_test digest:test:V28q/NynI4JI3Rk54h0r8O5kMug=:cdrwa
```

#### ip

可以访问的ip地址（比如127.0.0.1）或ip地址段（比如192.168.1.0/16）  

**设置权限**
``` bash
[zk] setAcl /ip_test ip:127.0.0.1:crwda
```

**权限信息**  
``` bash
[zk] getAcl /ip_test
'ip,'127.0.0.1
: cdrwa
```

#### super

ZooKeeper 有个管理员权限，需要在启动参数里面配置。

**生成管理员账户密文**
``` bash
$ java -cp ./zookeeper-3.4.6.jar:./lib/slf4j-log4j12-1.6.1.jar:./lib/slf4j-api-1.6.1.jar:./lib/log4j-1.2.16.jar \
org.apache.zookeeper.server.auth.DigestAuthenticationProvider admin:admin
```
输出：

    admin:admin->admin:x1nq8J5GOJVPY6zgzhtTtA9izLc=

**编辑zkServer.sh**
``` bash
vim ./bin/zkServer.sh
```

**`:/nohup` 找到启动的地方，大概在109行(`:set nu`)左右，新增启动参数**
``` vim
nohup "$JAVA" "-Dzookeeper.log.dir=${ZOO_LOG_DIR}" "-Dzookeeper.root.logger=${ZOO_LOG4J_PROP}" \
"-Dzookeeper.DigestAuthenticationProvider.superDigest=admin:x1nq8J5GOJVPY6zgzhtTtA9izLc=" \
```

**重启**
``` bash
$ ./bin/zkServer.sh restart
```

**授权**
``` bash
[zk] addauth digest admin:admin
```

### 权限（permission）

Create（**c**）：允许对`子节点`Create操作
Delete（**d**）：允许对`子节点`Delete操作
Read（**r**）  ：允许对`本节点`GetChildren和GetData操作
Write（**w**） ：允许对`本节点`SetData操作
Admin（**a**） ：允许对`本节点`setAcl操作

### 权限相关操作

#### `getAcl` 获取权限信息

``` bash
[zk: localhost:2181(CONNECTED) 16] getAcl /taobao-pamirs-schedule
'digest,':BaefBs8/Z/cm2uaNGKIpD2yaUMk=
: cdrwa
'world,'anyone
: r
```
`/taobao-pamirs-schedule` 该节点对所有人只读（`r`），对`digest`验证通过的人拥有所有权限（`cdrwa`）

#### `addauth` 为当前 session 授权

``` bash
addauth digest username:password
```
为当前session授权之后，就可以对带权限的节点进行操作了

#### Java 相关操作

创建带权限的节点

``` java
List<ACL> acls = new ArrayList<ACL>(2);     
  
Id id1 = new Id("digest", DigestAuthenticationProvider.generateDigest("admin:admin123"));  
ACL acl1 = new ACL(ZooDefs.Perms.ALL, id1);  
  
Id id2 = new Id("digest", DigestAuthenticationProvider.generateDigest("guest:guest123"));  
ACL acl2 = new ACL(ZooDefs.Perms.READ, id2);  
  
acls.add(acl1);  
acls.add(acl2);  
  
ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 10000, new DefaultWatcher());  
zk.create("/test", new byte[0], acls, CreateMode.PERSISTENT);  
```

#### 登陆
``` java
ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 10000, new DefaultWatcher());  
zk.addAuthInfo("digest", "guest:guest123".getBytes());  
```



### 参考
[Zookeeper权限管理与Quota管理](http://www.cnblogs.com/linuxbug/p/5023677.html)  
[ZooKeeper access control using ACLs](http://zookeeper.apache.org/doc/trunk/zookeeperProgrammers.html#sc_ZooKeeperAccessControl)  
[ZooKeeper权限配置](http://nettm.iteye.com/blog/1721778)  