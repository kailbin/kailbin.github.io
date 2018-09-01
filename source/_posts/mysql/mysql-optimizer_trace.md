---
title: MySQL optimizer_trace
date: 2018-08-18
tags: [MySQL,Tuning]
categories:
  - MySQL
id: mysql-optimizer_trace
---


optimizer trace 是 MySQL5.6 添加的新功能，可以看到大量的内部查询计划产生的信息。

<!-- more -->

# 查看 MySQL 跟踪开关

``` mysql
mysql> show variables like '%trace%';
+------------------------------+----------------------------------------------------------------------------+
| Variable_name                | Value                                                                      |
+------------------------------+----------------------------------------------------------------------------+
| optimizer_trace              | enabled=off,one_line=off                                                   |
| optimizer_trace_features     | greedy_search=on,range_optimizer=on,dynamic_range=on,repeated_subselect=on |
| optimizer_trace_limit        | 1                                                                          |
| optimizer_trace_max_mem_size | 16384                                                                      |
| optimizer_trace_offset       | -1                                                                         |
+------------------------------+----------------------------------------------------------------------------+
5 rows in set
```

# 使用步骤
只能跟踪自己session执行的，不能跟踪别人的
``` sql
-- 开启跟踪
mysql> set optimizer_trace = "enabled=on";
-- mysql> set optimizer_trace_max_mem_size=1000000;    --- 设置trace内存大小
-- mysql> set end_markers_in_json=on;    --- 增加trace中注释

-- 执行 SQL 语句
mysql> select onick,fanscount from t_sinaweibouser where sex = 'm' order by fanscount desc limit 10;
-- 查看跟踪信息
mysql>  select * from information_schema.optimizer_trace\G

-- 关闭跟踪
mysql> SET optimizer_trace="enabled=off";
```


# optimizer_trace 表结构

```sql
mysql> show create table information_schema.optimizer_trace;
...
CREATE TEMPORARY TABLE `OPTIMIZER_TRACE` ( -- 临时表
  `QUERY` longtext NOT NULL, -- 查询语句
  `TRACE` longtext NOT NULL, -- 跟踪信息
  `MISSING_BYTES_BEYOND_MAX_MEM_SIZE` int(20) NOT NULL DEFAULT '0', -- show variables like 'optimizer_trace_max_mem_size';
  `INSUFFICIENT_PRIVILEGES` tinyint(1) NOT NULL DEFAULT '0' -- 是否权限不足
) ENGINE=MyISAM DEFAULT CHARSET=utf8
...
```



# 跟踪信息

不同 SQL 的跟踪信息均不一样，以下内容仅供参考

```json
{
  "steps": [
    /* 优化准备工作 */
    {
      "join_preparation": {
        "select#": 1,
        "steps": [{
          "expanded_query": "/* select#1 */ select `t_sinaweibouser`.`onick` AS `onick`,`t_sinaweibouser`.`fanscount` AS `fanscount` from `t_sinaweibouser` where (`t_sinaweibouser`.`sex` = 'm') order by `t_sinaweibouser`.`fanscount` desc limit 10"
        }]
      }
    },
    /* 优化工作的主要阶段,包括逻辑优化和物理优化两个阶段 */
    {
      "join_optimization": {
        "select#": 1,
        "steps": [
          /* 逻辑优化,条件简化 */
          {
            "condition_processing": {
              "condition": "WHERE",
              "original_condition": "(`t_sinaweibouser`.`sex` = 'm')",
              "steps": [
                /* 等值处理 */
                {
                  "transformation": "equality_propagation",
                  "resulting_condition": "(`t_sinaweibouser`.`sex` = 'm')"
                },
                /* 常量处理 */
                {
                  "transformation": "constant_propagation",
                  "resulting_condition": "(`t_sinaweibouser`.`sex` = 'm')"
                },
                /* 条件去除 */
                {
                  "transformation": "trivial_condition_removal",
                  "resulting_condition": "(`t_sinaweibouser`.`sex` = 'm')"
                }
              ]
            }
          },
          /* 找出表之间的相互依赖关系 */
          {
            "table_dependencies": [{
              "table": "`t_sinaweibouser`",
              "row_may_be_null": false,
              "map_bit": 0,
              "depends_on_map_bits": []
            }]
          },
          /* 找出备选的索引 */
          {
            "ref_optimizer_key_uses": []
          },
          /* 估算 全表扫描 和 索引 扫描的代价 */
          {
            "rows_estimation": [{
              "table": "`t_sinaweibouser`",
              "table_scan": {
                "rows": 3203920,
                "cost": 32448
              }
            }]
          },
          /* 物理优化, 开始多表连接的物理优化计算 */
          {
            "considered_execution_plans": [{
              "plan_prefix": [],
              "table": "`t_sinaweibouser`",
              "best_access_path": {
                "considered_access_paths": [{
                  "access_type": "scan",
                  "rows": 3.2e6,
                  "cost": 673232,
                  "chosen": true
                }]
              },
              "cost_for_plan": 673232,
              "rows_for_plan": 3.2e6,
              "chosen": true
            }]
          },
          /* 尽量把条件绑定到对应的表上 */
          {
            "attaching_conditions_to_tables": {
              "original_condition": "(`t_sinaweibouser`.`sex` = 'm')",
              "attached_conditions_computation": [],
              "attached_conditions_summary": [{
                "table": "`t_sinaweibouser`",
                "attached": "(`t_sinaweibouser`.`sex` = 'm')"
              }]
            }
          },
          {
            "clause_processing": {
              "clause": "ORDER BY",
              "original_clause": "`t_sinaweibouser`.`fanscount` desc",
              "items": [{
                "item": "`t_sinaweibouser`.`fanscount`"
              }],
              "resulting_clause_is_simple": true,
              "resulting_clause": "`t_sinaweibouser`.`fanscount` desc"
            }
          },
          /* 提炼计划 */
          {
            "refine_plan": [{
              "table": "`t_sinaweibouser`",
              "access_type": "table_scan"
            }]
          },
          /* 送心考虑索引排序的访问路径 */
          {
            "reconsidering_access_paths_for_index_ordering": {
              "clause": "ORDER BY",
              "index_order_summary": {
                "table": "`t_sinaweibouser`",
                /* 索引排序 */
                "index_provides_order": true,
                "order_direction": "desc",
                /* 使用索引 */
                "index": "fanscount",
                /* 计划改变 */
                "plan_changed": true,
                /* 访问方式 */
                "access_type": "index_scan"
              }
            }
          }
        ]
      }
    },
    {
      "join_execution": {
        "select#": 1,
        "steps": []
      }
    }
  ]
}
```



# Read More



- [MySQL索引选择不正确并详细解析OPTIMIZER_TRACE格式](https://blog.csdn.net/melody_mr/article/details/48950601)
- [mysql 执行计划分析三看， explain,profiling,optimizer_trace](https://blog.csdn.net/xj626852095/article/details/52767963)
- [单表扫描，使用ref和range从索引获取数据一例](http://blog.163.com/li_hx/blog/static/183991413201461853637715/)
- [Chapter 8 Tracing the Optimizer](https://dev.mysql.com/doc/internals/en/optimizer-tracing.html)