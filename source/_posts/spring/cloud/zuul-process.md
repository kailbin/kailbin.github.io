---
title: Zuul 处理流程
date: 2018-09-26
tags: [Spring Cloud]
categories: [Spring Cloud]
id: spring/cloud/zuul-process
---

从 `ZuulServletFilter` 或 `ZuulServlet` 可以看出 Zuul 的整体执行流程（坐标：`com.netflix.zuul:zuul-core:1.3.1`）。

<!-- more -->


#  ZuulServlet

Zuul 官方示例 和 Spring Cloud Zuul 都是通过 `Servlet` 的形式而不是 `Servlet Filter` 的形式进行启动的，以下也以 Servlet 的流程举例：

``` java
@Override
public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
    try {
        init((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);

        RequestContext context = RequestContext.getCurrentContext();
        // 把这个请求标记为 Zuul 引擎处理
        context.setZuulEngineRan();

        try {
            // per
            preRoute();
        } catch (ZuulException e) {
            error(e); // errooooooooooooor
            postRoute(); // !!!
            return;
        }
        try {
            // route
            route();
        } catch (ZuulException e) {
            error(e); // errooooooooooooor
            postRoute(); // !!!
            return;
        }
        try {
            // post
            postRoute();
        } catch (ZuulException e) {
            error(e); // errooooooooooooor
            return;
        }

    } catch (Throwable e) {
        // errooooooooooooor
        error(new ZuulException(e, 500, "UNHANDLED_EXCEPTION_" + e.getClass().getName()));
    } finally {
        RequestContext.getCurrentContext().unset();
    }
}
```



# Zuul 过滤器的执行流程

## FilterProcessor # runFilters

``` java
public Object runFilters(String sType) throws Throwable {
    ...
    // 从 Zuul FilterRegistry 中获取所有指定类型的过滤器，按照 filterOrder 正序排序
    List<ZuulFilter> list = FilterLoader.getInstance().getFiltersByType(sType);
    if (list != null) {
        for (int i = 0; i < list.size(); i++) {
            ZuulFilter zuulFilter = list.get(i);
            // FilterProcessor # processZuulFilter
            // 遍历指定类型的过滤器(如：所有的 per) 一个一个执行
            Object result = processZuulFilter(zuulFilter);
            if (result != null && result instanceof Boolean) {
                bResult |= ((Boolean) result);
            }
        }
    }
    return bResult;
```

## FilterProcessor # processZuulFilter

``` java
public Object processZuulFilter(ZuulFilter filter) throws ZuulException {
	...
    try {
        Throwable t = null;
        ...
        // ZuulFilter # runFilter
        ZuulFilterResult result = filter.runFilter();
        ExecutionStatus s = result.getStatus();
        ...
        // 根据 过滤器的执行状态，记录概要到上下文
        switch (s) {
            case FAILED:
                // 如果执行失败，保存异常对象
                t = result.getException();
                ctx.addFilterExecutionSummary(filterName, ExecutionStatus.FAILED.name(), execTime);
                break;
            case SUCCESS:
                o = result.getResult();
                ctx.addFilterExecutionSummary(filterName, ExecutionStatus.SUCCESS.name(), execTime);
                ...
                break;
            default:
                break;
        }
	
        // 如果有异常 抛出异样
        if (t != null) throw t;

        usageNotifier.notify(filter, s);
        return o;

    } catch (Throwable e) {
        // 把异常包装成 ZuulException 抛出
        ...
    }
}
```

## ZuulFilter # runFilter

``` java
public ZuulFilterResult runFilter() {
    ZuulFilterResult zr = new ZuulFilterResult();
    // Filter 不可用，直接返回 zr
    if (!isFilterDisabled()) {
        // Filter 不应该执行，标记 状态为 SKIPPED 然后返回
        if (shouldFilter()) {
            Tracer t = TracerFactory.instance().startMicroTracer("ZUUL::" + this.getClass().getSimpleName());
            // 执行 Filter run() 方法，根据是否抛出异常，标记为成功或者失败状态
            try {
                Object res = run();
                // 标记成功状态
                zr = new ZuulFilterResult(res, ExecutionStatus.SUCCESS);
            } catch (Throwable e) {
                t.setName("ZUUL::" + this.getClass().getSimpleName() + " failed");
                // 标记失败状态
                zr = new ZuulFilterResult(ExecutionStatus.FAILED);
                zr.setException(e);
            } finally {
                t.stopAndLog();
            }
        } else {
            zr = new ZuulFilterResult(ExecutionStatus.SKIPPED);
        }
    }
    return zr;
}
```

# 如何中断 Zuul 过滤流程

## 抛异常

从上面 Zuul 的执行流程可以看出，**抛异常是可以终止 同一个类型 的过滤器执行的**，因为在 `FilterProcessor # runFilters` 循环执行某个指定类型的过滤器的时候，并没有捕捉异常，这时候如果有异常，循环就直接终止了。

## ctx.setSendZuulResponse(false);

但是从 Zuul Filter 的整个生命周期(`ZuulServlet`)来看，在 `pre` 和 `route` 这两个阶段 抛异常， `post` 类型的过滤器，仍然会执行。从 `ZuulFilter # runFilter` 中可以看出，除了抛异常外，`shouldFilter()` 返回 `false` 过滤器也是不执行的。

查看 Spring Cloud 内置 `RibbonRoutingFilter` 和 `SimpleHostRoutingFilter` 的 `shouldFilter()` 方法，里面都有一段这样的逻辑 `(...) && ctx.sendZuulResponse()` ， 所以把 sendZuulResponse 标记为 false，就不会转发到 Origin 了。



# 总结

自定义的过滤器的时候，最常实现的过滤器类型是 `pre` 和 `post`，`pre` 常用来用来进行权限校验等，如果校验失败，并且 `post` 写的不好的， `抛异常` 和 `设置ctx.setSendZuulResponse(false)` 都是无法阻止 `post` 型过滤器执行的。

建议：

- 一定要注意 过滤器的执行顺序，留意是在内置过滤器之前还是之后执行
- `shouldFilter()` 的实现，粒度一定经要细，建议 都加上 `(...) && ctx.sendZuulResponse()` 的逻辑，同时抛出异常，终止内置过滤器 和 自定义过滤器 的执行
- 如果要重定向 ，进行以下设置，这时候 内置过滤器 `LocationRewriteFilter` 会进行处理 
  - `ctx.setSendZuulResponse(false);`
  - `ctx.setResponseStatusCode(301);`
  - `ctx.addZuulResponseHeader("Location", "xxx");`

个人感觉 **Netflix Zuul 其实是只是一个 框架，还不算一个真正意义上的网关**，zuul-core 并没有实现具体的Filter逻辑；Spring Cloud Zuul 内置了一些的 Filter 实现，具有的基础的代理、转发、重定向 等简单的功能；但是如果要作为一个真正的网关，如统一权限校验、流量控制、缓存、监控等功能，都需要我们自己实现。 

# Read More

- [Spring Cloud Zuul 内置过滤器](http://blog.kail.xyz/docsify/docs/spring-cloud/#/zuul/inner-filter)
- [如何中断 Zuul 过滤流程](http://blog.kail.xyz/docsify/docs/spring-cloud/#/zuul/faq/break-filter)