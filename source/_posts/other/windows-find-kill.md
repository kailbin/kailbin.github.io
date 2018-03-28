---
title: Windows 下查找和删除进程
tags:
  - Windows
  - shell
categories:
  - Other
toc: false
date: 2018-03-28
---

这里涉及到的命令有 `netstat`、`findstr`、`tasklist`、`taskkill`


<!-- more -->

## 查询端口号

``` bash
netstat -ano | findstr ":80"
```

本地端口号
``` bash
netstat -ano | findstr "0.0.0.0:80"
```
输出（最后一个是进程号 PID）

    TCP    0.0.0.0:80             0.0.0.0:0              LISTENING       19984

## 根据 PID 查找进程

``` bash
tasklist | findstr "19984"
```

## 根据 进程名 查找进程

``` bash
tasklist | findstr "nginx.exe"
```

## 根据进程名 杀掉进程

``` bash
taskkill /F /T /IM "nginx.exe"
```

## 根据进程ID 杀掉进程
``` bash
taskkill /F /PID 19984
```


## tasklist

``` bash
> tasklist /?

TASKLIST [/S system [/U username [/P [password]]]] [/M [module] | /SVC | /V] [/FI filter] [/FO format] [/NH]

Description:
    This tool displays a list of currently running processes on either a local or remote machine.
    此工具显示本地或远程计算机上当前正在运行的进程的列表。

Parameter List:
   /S     system           Specifies the remote system to connect to. 
                           指定要连接的远程系统。

   /U     [domain\]user    Specifies the user context under which the command should execute.
                           指定执行命令的用户上下文。

   /P     [password]       Specifies the password for the given user context. Prompts for input if omitted.
                           指定执行命令的用户上下文 的密码，如果不指定 会提示输入

   /M     [module]         Lists all tasks currently using the given exe/dll name. If the module name is not specified all loaded modules are displayed.
                           列出当前使用给定的 exe/dll 名称的所有任务。如果未指定模块名称，则显示所有已加载的模块。

   /SVC                    Displays services hosted in each process.
                           显示每个进程中托管的服务                    

   /APPS                   Displays Store Apps and their associated processes.
                           显示 应用商店程序 及其关联的进程

   /V                      Displays verbose task information.
                           显示详细的任务信息

   /FI    filter           Displays a set of tasks that match a given criteria specified by the filter.
                           显示一组符合筛选器指定条件的任务

   /FO    format           Specifies the output format. Valid values: "TABLE", "LIST", "CSV".
                           指定输出格式。有效值："TABLE", "LIST", "CSV"

   /NH                     Specifies that the "Column Header" should not be displayed in the output. Valid only for "TABLE" and "CSV" formats.
                           指定“列标题”不应显示在输出中。仅适用于 'TABLE' 和 'CSV' 格式

   /?                      Displays this help message.
                           显示帮助信息

Filters:
    Filter Name     Valid Operators           Valid Value(s)
    -----------     ---------------           --------------------------
    STATUS          eq, ne                    RUNNING | SUSPENDED | NOT RESPONDING | UNKNOWN
    IMAGENAME       eq, ne                    Image name
    PID             eq, ne, gt, lt, ge, le    PID value
    SESSION         eq, ne, gt, lt, ge, le    Session number
    SESSIONNAME     eq, ne                    Session name
    CPUTIME         eq, ne, gt, lt, ge, le    CPU time in the format of hh:mm:ss. hh - hours, mm - minutes, ss - seconds
    MEMUSAGE        eq, ne, gt, lt, ge, le    Memory usage in KB
    USERNAME        eq, ne                    User name in [domain\]user format
    SERVICES        eq, ne                    Service name
    WINDOWTITLE     eq, ne                    Window title
    MODULES         eq, ne                    DLL name

NOTE: "WINDOWTITLE" and "STATUS" filters are not supported when querying a remote machine.
注意: "WINDOWTITLE" 和 "STATUS" 过滤器在查询远程机器时不支持。

Examples:
    TASKLIST
    TASKLIST /M
    TASKLIST /V /FO CSV
    TASKLIST /SVC /FO LIST
    TASKLIST /APPS /FI "STATUS eq RUNNING"
    TASKLIST /M wbem*
    TASKLIST /S system /FO LIST
    TASKLIST /S system /U domain\username /FO CSV /NH
    TASKLIST /S system /U username /P password /FO TABLE /NH
    TASKLIST /FI "USERNAME ne NT AUTHORITY\SYSTEM" /FI "STATUS eq running"
```


## taskkill

``` bash
>taskkill /?

TASKKILL [/S system [/U username [/P [password]]]] { [/FI filter] [/PID processid | /IM imagename] } [/T] [/F]

Description:
    This tool is used to terminate tasks by process id (PID) or image name.
    该工具用于通过进程标识（PID）或映像名称终止任务。

Parameter List:
    /S    system           Specifies the remote system to connect to.
                           指定要连接的远程系统。

    /U    [domain\]user    Specifies the user context under which the command should execute.
                           指定执行命令的用户上下文。
                           
    /P    [password]       Specifies the password for the given user context. Prompts for input if omitted.
                           指定执行命令的用户上下文 的密码，如果不指定 会提示输入

    /FI   filter           Applies a filter to select a set of tasks. Allows "*" to be used. ex. imagename eq acme*
                           应用过滤器来选择一组任务

    /PID  processid        Specifies the PID of the process to be terminated. Use TaskList to get the PID.
                           指定要终止的进程的PID。使用taskList来获取PID

    /IM   imagename        Specifies the image name of the process to be terminated. Wildcard '*' can be used to specify all tasks or image names.
                           指定要终止的进程名称。通配符'*'可用于指定所有任务或名称

    /T                     Terminates the specified process and any child processes which were started by it.
                           终止指定的进程以及由它启动的任何子进程

    /F                     Specifies to forcefully terminate the process(es).
                           指定强制终止进程
                           
    /?                     Displays this help message.
                           显示帮助信息
                           
Filters:
    Filter Name   Valid Operators           Valid Value(s)
    -----------   ---------------           -------------------------
    STATUS        eq, ne                    RUNNING | NOT RESPONDING | UNKNOWN
    IMAGENAME     eq, ne                    Image name
    PID           eq, ne, gt, lt, ge, le    PID value
    SESSION       eq, ne, gt, lt, ge, le    Session number.
    CPUTIME       eq, ne, gt, lt, ge, le    CPU time in the format of hh:mm:ss. hh - hours, mm - minutes, ss - seconds
    MEMUSAGE      eq, ne, gt, lt, ge, le    Memory usage in KB
    USERNAME      eq, ne                    User name in [domain\]user format
    MODULES       eq, ne                    DLL name
    SERVICES      eq, ne                    Service name
    WINDOWTITLE   eq, ne                    Window title

    NOTE
    ----
    1) Wildcard '*' for /IM switch is accepted only when a filter is applied.
    2) Termination of remote processes will always be done forcefully (/F).
    3) "WINDOWTITLE" and "STATUS" filters are not considered when a remote machine is specified.

Examples:
    TASKKILL /IM notepad.exe
    TASKKILL /PID 1230 /PID 1241 /PID 1253 /T
    TASKKILL /F /IM cmd.exe /T
    TASKKILL /F /FI "PID ge 1000" /FI "WINDOWTITLE ne untitle*"
    TASKKILL /F /FI "USERNAME eq NT AUTHORITY\SYSTEM" /IM notepad.exe
    TASKKILL /S system /U domain\username /FI "USERNAME ne NT*" /IM *
```

## Read More

- [windows中，端口查看&关闭进程及Kill使用](https://www.cnblogs.com/lynn-li/p/6077993.html)
