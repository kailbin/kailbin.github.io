---
title: MySQL PROFILE
date: 2018-08-14
tags: [MySQL,Tuning]
categories:
  - MySQL
id: mysql-profile
---

MySQL profile 可用来查询 SQL 执行状态，对定位一条语句的 **I/O消耗** 和**CPU消耗** 非常重要，SQL 语句执行所消耗的最大两部分资源就是 `IO` 和 `CPU` 。

<!-- more -->

在 MySQL 5.7 之后，profile 信息将逐渐被废弃，推荐使用 `performance_schema`。

> [Chapter 25 MySQL Performance Schema](https://dev.mysql.com/doc/refman/5.7/en/performance-schema.html)



# 查看 profile 功能是否可用

如果 `have_profiling` 为 `NO` 说明不可用

```sql
mysql> show variables like '%profiling%';
+----------------+-------+
| Variable_name  | Value |
+----------------+-------+
| have_profiling | NO    |
+----------------+-------+
1 row in set
```

以下输出说明可用：

```sql
mysql> show variables like '%profiling%';
+------------------------+-------+
| Variable_name          | Value |
+------------------------+-------+
| have_profiling         | YES   |
| profiling              | OFF   |
| profiling_history_size | 15    |
+------------------------+-------+
3 rows in set
```

# 使用步骤

```sql
-- 打开 profile
set profiling = 1;
 
-- 执行查询语句
SELECT NOW();
SELECT CURDATE();

-- 查看执行的 SQL 编号
show profiles;
+----------+------------+------------------+
| Query_ID | Duration   | Query            |
+----------+------------+------------------+
|        1 |   0.000933 | SELECT NOW()     |
|        2 | 0.00063325 | SELECT CURDATE() |
+----------+------------+------------------+

-- 查看每个阶段的耗时
show profile for query 1;
-- 查看每个阶段 各个资源的使用情况
show profile ALL for query 1;
-- 查看 CPU 使用情况
show profile CPU for query 1;

-- 关闭 profile 
set profiling = 0;

-- 查看帮助
help 'profile';
help 'profiles';
```

# 指标

| 指标名           | 指标描述                              | 英文描述                                                     |
| ---------------- | ------------------------------------- | ------------------------------------------------------------ |
| ALL              | 显示所有性能信息                      | displays all information                                     |
| `BLOCK IO`       | 显示 IO 的次数                        | displays counts for block input and output operations        |
| CONTEXT SWITCHES | 上下文切换次数                        | displays counts for voluntary and involuntary context switches |
| `CPU`            | 用户和系统 CPU 使用时间               | displays user and system CPU usage times                     |
| IPC              | 显示发送和接收的消息的计数            | displays counts for messages sent and received               |
| MEMORY           | 暂未实现                              | is not currently implemented                                 |
| PAGE FAULTS      | 主/次 缺页数                          | displays counts for major and minor page faults              |
| SOURCE           | 使用的 函数、源码文件名、在文件的行数 | displays the names of functions from the source code, together with the name and line number of the file in which the function occurs |
| SWAPS            | swap 次数                             | displays swap counts                                         |

## 使用方式

```sql
-- 显示单个指标
show profile CPU for query 1; 
-- 显示多个指标
show profile CPU,BLOCK IO for query 2; 
-- 显示第一行
show profile CPU for query 2 limit 1;
-- 显示第2、3行
show profile CPU for query 2 limit 1,2;
```

## ALL 的列

| 名称                | 描述                  | 示例数据                       |
| ------------------- | --------------------- | ------------------------------ |
| Status              | 状态                  | init 、executing、Sending data |
| Duration            | 耗时                  | 0.000006                       |
| `CPU_user`          | 用户态占用的 CPU 时长 | 0.008999                       |
| `CPU_system`        | 内核占用的 CPU 时长   | 0.003999                       |
| Context_voluntary   | 上下文主动切换 次数   | 0                              |
| Context_involuntary | 上下文被动切换 次数   | 1                              |
| `Block_ops_in`       | 阻塞输入操作          | 8                              |
| `Block_ops_out`       | 阻塞输出操作          | 32                             |
| Messages_sent       | 发送数                | 0                              |
| Messages_received   | 接收数                | 0                              |
| Page_faults_major   | 主 缺页数             | 0                              |
| Page_faults_minor   | 次 缺页数             | 0                              |
| Swaps               | 交换次数              | 0                              |
| Source_function     | 函数                  | mysql_execute_command          |
| Source_file         | 源文件                | sql_parse.cc                   |
| Source_line         | 源代码行              | 4465                           |

# Status

| 名称                 | 描述                |
| -------------------- | ------------------- |
| starting             | 开始                |
| checking permissions | 检查权限            |
| Opening tables       | 打开表              |
| init                 | 初始化              |
| `System lock`        | 系统锁              |
| optimizing           | 优化                |
| `statistics`         | 统计                |
| preparing            | 准备                |
| `executing`          | 执行                |
| `Sending data`       | 发送数据            |
| `Sorting result`     | 排序                |
| end                  | 结束                |
| query end            | 查询 结束           |
| closing tables       | 关闭表 ／去除TMP 表 |
| freeing items        | 释放物品            |
| cleaning up          | 清理                |

`show processlist;` 的 `State` 列，可以看出当前 SQL 的执行状态

# 开启 profiling

``` sql
UPDATE performance_schema.setup_instruments SET ENABLED = 'YES', TIMED = 'YES' WHERE NAME LIKE 'statement/%' OR NAME LIKE 'stage/%';

UPDATE performance_schema.setup_consumers SET ENABLED = 'YES' WHERE NAME LIKE 'statement/%' OR NAME LIKE 'stage/%';
```
> —— 来自 [Profiling MySQL queries from Performance Schema](https://www.percona.com/blog/2015/04/16/profiling-mysql-queries-from-performance-schema/) 的留言部分，未验证



# Read More

- 本文主要来自 —— [Mysql分析-profile详解](https://blog.csdn.net/ty_hf/article/details/54895026)
- [13.7.5.30 SHOW PROFILE Syntax](https://dev.mysql.com/doc/refman/5.7/en/show-profile.html)
- [Profiling MySQL queries from Performance Schema](https://www.percona.com/blog/2015/04/16/profiling-mysql-queries-from-performance-schema/)
- [page fault带来的性能问题](https://yq.aliyun.com/articles/55820)
  - PAGE FAULTS
  - Swap
- [进程间通信](https://zh.wikipedia.org/wiki/%E8%A1%8C%E7%A8%8B%E9%96%93%E9%80%9A%E8%A8%8A)
  - IPC: Inter-Process Communication
