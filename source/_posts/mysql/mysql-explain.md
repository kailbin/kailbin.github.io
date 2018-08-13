---

title: MySQL EXPLAIN
date: 2018-08-13
tags: [MySQL,Tuning]
categories:
  - MySQL
id: mysql-explain
---


`EXPLAIN` 用来获取查询的执行计划信息。使用方法是直接在 SQL 前加上 `EXPLAIN` 执行即可，效果如下：

<!-- more -->

```
mysql> EXPLAIN SELECT 1;
+----+-------------+-------+------+---------------+------+---------+------+------+----------------+
| id | select_type | table | type | possible_keys | key  | key_len | ref  | rows | Extra          |
+----+-------------+-------+------+---------------+------+---------+------+------+----------------+
|  1 | SIMPLE      | NULL  | NULL | NULL          | NULL | NULL    | NULL | NULL | No tables used |
+----+-------------+-------+------+---------------+------+---------+------+------+----------------+
1 row in set
```

如果查询是两个表链接，那么输出中将有两列。

# 扩展命令

## EXPLAIN EXTENDED

`EXPLAIN EXTENDED` 看起来和 `EXPLAIN` 是一样，但是紧接其后运行 `SHOW WARNINGS` 会看到 MySQL 优化 原SQL生成的新SQL，通过这个命令可以查看 MySQL 优化器是如何转换SQL 的。

```SQL
EXPLAIN EXTENDED some_sql;SHOW WARNINGS;
```

效果：

```SQL
mysql> EXPLAIN EXTENDED SELECT * FROM mysql.help_keyword;SHOW WARNINGS;
+----+-------------+--------------+------+---------------+------+---------+------+------+----------+-------+
| id | select_type | table        | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra |
+----+-------------+--------------+------+---------------+------+---------+------+------+----------+-------+
|  1 | SIMPLE      | help_keyword | ALL  | NULL          | NULL | NULL    | NULL |  467 |      100 | NULL  |
+----+-------------+--------------+------+---------------+------+---------+------+------+----------+-------+
1 row in set

+-------+------+---------------------------------------------------------------------------------------------------------------------------------------------------------+
| Level | Code | Message                                                                                                                                                 |
+-------+------+---------------------------------------------------------------------------------------------------------------------------------------------------------+
| Note  | 1003 | /* select#1 */ select `mysql`.`help_keyword`.`help_keyword_id` AS `help_keyword_id`,`mysql`.`help_keyword`.`name` AS `name` from `mysql`.`help_keyword` |
+-------+------+---------------------------------------------------------------------------------------------------------------------------------------------------------+
1 row in set
```



## EXPLAIN PARTITIONS

如果表被设置过数据分区，会显示查询访问的分区

## DESC / DESCRIBE

`DESC` 或者 `DESCRIBE` 放到 SQL 最前面执行与 `EXPLAIN ` 的效果是一样的

# EXPLAIN 的列

| Column          | Meaning                    |
| --------------- | -------------------------- |
| `id`            | `SELECT` ID                |
| `select_type`   | `SELECT` 类型              |
| `table`         | 使用的表                   |
| `partitions`    | 使用的分区                 |
| ❤ **`type`** ❤  | 访问类型                   |
| `possible_keys` | 可用的索引                 |
| `key`           | 实际使用的索引             |
| `key_len`       | 使用的索引长度             |
| `ref`           | 连表时使用的字段           |
| `rows`          | 估计要遍历的行数           |
| `filtered`      | 按查询条件过滤的行的百分比 |
| ❤ **`Extra`** ❤ | 附加信息                   |




## select_type 

1. `SIMPLE`: 简单SELECT,不使用UNION或子查询等
2. `PRIMARY` : 查询中若包含任何复杂的子部分,最外层的select被标记为`PRIMARY`
3. `UNION` : 中的第二个或后面的SELECT语句
4. `DEPENDENT UNION`  : UNION中的第二个或后面的SELECT语句，取决于外面的查询
5. `UNION RESULT`  : UNION的结果
6. `SUBQUERY` : 子查询中的第一个SELECT
7. `DEPENDENT SUBQUERY` : 子查询中的第一个SELECT，取决于外面的查询
8. `DERIVED` : 派生表的SELECT, FROM子句的子查询
9. `UNCACHEABLE SUBQUERY`  一个子查询的结果不能被缓存，必须重新评估外链接的第一行

## table        

显示查的是哪张表，如下 3个 SQL 进行 UNION ALL，前两个没有查表，第三个个查的 user 表，最后一个是 UNION 操作生成的临时表

```mysql
mysql> DESC SELECT 1 UNION ALL SELECT 1 UNION ALL SELECT 1 FROM mysql.`user`;
+------+--------------+--------------+-------+---------------+---------+---------+------+------+-----------------+
| id   | select_type  | table        | type  | possible_keys | key     | key_len | ref  | rows | Extra           |
+------+--------------+--------------+-------+---------------+---------+---------+------+------+-----------------+
|    1 | PRIMARY      | NULL         | NULL  | NULL          | NULL    | NULL    | NULL | NULL | No tables used  |
|    2 | UNION        | NULL         | NULL  | NULL          | NULL    | NULL    | NULL | NULL | No tables used  |
|    3 | UNION        | user         | index | NULL          | PRIMARY | 228     | NULL |   39 | Using index     |
| NULL | UNION RESULT | <union1,2,3> | ALL   | NULL          | NULL    | NULL    | NULL | NULL | Using temporary |
+------+--------------+--------------+-------+---------------+---------+---------+------+------+-----------------+
4 rows in set
```



## type 

表示 MySQL 在表中找到所需行的方式，又称 **访问类型** 。以下类型，**从上到下 性能越来越差**：

- `NULL` :  执行时甚至不用访问表或索引，例如： MAX(主键)
- `const` : 唯一索引 或者 主键的等值查询
  - `system` : 是 `const` 类型的特例，当查询的表只有一行的情况下，使用 `system`
- `eq_ref` : 类似 `ref`，区别就在**使用的索引是唯一索引**，对于每个索引键值，表中只有一条记录匹配，简单来说，就是多表连接中使用 `primary key` 或者 `unique key` 作为关联条件
- `ref` ： 索引上的等值查询
- `range` ： 索引上的范围查询，或者 IN 操作

  ----------------------------------------------------------------------------------------------------------------

- `index` ： 需要遍整个索引
- `ALL` ： 全表扫描



## possible_keys 

查询涉及到的字段上若存在索引，则该索引将被列出，但不一定被查询使用

 

## key

显示实际使用的索引

- 强制索引： `FORCE INDEX(idx)`
- 忽略索引： `IGNORE INDEX(idx)`
- 参考索引：`USE INDEX(idx1,idx2)`
  

## key_len 

索引中使用的字节数，可通过该列计算查询中使用的索引的长度
显示的值为索引字段的最大可能长度，并非实际使用长度，即 `key_len` 是根据表定义计算而得，不是通过表内检索出的
不损失精确性的情况下，长度越短越好 



## ref

表连接时与前表连接的字段，`const` 代表是常量值连接



## rows 

根据 **表统计信息** 及 **索引选用情况**，**估算**的 找到所需的记录需要读取的行数，该数值越小，性能越高



## Extra

- `Using index` : 使用覆盖索引，**常出现在 唯一索引 或者 主键索引 上的等值查询**
- `Using where; Using index` : 使用覆盖索引，常出现在 **范围查询** 的时候
- `Using index condition` : 查找使用了索引，但是需要回表查询数据
- `Using where` :  通过索引无法过滤出所需数据，还需要过滤数据
-  
- `Select tables optimized away` ： 这个值意味着仅通过使用索引，优化器就可以仅从聚合函数结果中返回一行
- 
- `Using temporary` ： 表示MySQL需要使用临时表来存储结果集，常见于排序和分组查询
- `Using filesort` ：无法利用索引完成的排序操作称为 **文件排序**， 往往会导致 **慢 SQL**
- `Using join buffer` ：在获取连接条件时没有使用索引，并且需要连接缓冲区来存储中间结果。**如果出现了这个值，就应该注意，根据查询的具体情况可能需要添加索引来改进能**
- 
- `Impossible where` ：这个值强调了where语句会导致没有符合条件的行，如： `SELECT * FROM mysql.user WHERE 1=0`


>
> - [Whats the difference between “Using index” and “Using where; Using index” in the EXPLAIN](https://stackoverflow.com/questions/25672552/whats-the-difference-between-using-index-and-using-where-using-index-in-the)
> - [MySQL - 'Using index condition' vs 'Using where; Using index'](https://stackoverflow.com/questions/28759576/mysql-using-index-condition-vs-using-where-using-index)
>



# SQL 性能优化的目标

至少要达到 `range` 级别， 要求是 `ref` 级别， 如果可以是 `consts` 最好。
- `consts` 单表中最多只有一个匹配行（主键或者唯一索引） ，在优化阶段即可读取到数据。
- `ref` 指的是使用普通的索引（normal index） 。
- `range` 对索引进行范围检索。

反例： explain 表的结果， type=index，索引物理文件全扫描，速度非常慢，这个 index 级别比较 range 还低，与全表扫描是小巫见大巫。
> 来自 [《阿里巴巴Java开发手册（详尽版）.pdf》](https://github.com/alibaba/p3c/blob/master/%E9%98%BF%E9%87%8C%E5%B7%B4%E5%B7%B4Java%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C%EF%BC%88%E8%AF%A6%E5%B0%BD%E7%89%88%EF%BC%89.pdf)

# Read More

- [高性能MySQL 第三版](https://book.douban.com/subject/23008813/)
- 本文大部分来自 —— [MySQL Explain详解](https://www.cnblogs.com/xuanzhi201111/p/4175635.html)
- [8.8.2 EXPLAIN Output Format](https://dev.mysql.com/doc/refman/5.7/en/explain-output.html)