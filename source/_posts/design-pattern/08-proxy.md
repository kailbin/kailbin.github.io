---

title: 代理模式
date: 2016-08-29

---


代理模式就是多一个代理类出来，替原对象进行一些操作，比如我们在租房子的时候回去找中介，为什么呢？
因为你对该地区房屋的信息掌握的不够全面，希望找一个更熟悉的人去帮你做，此处的代理就是这个意思。
再如我们有的时候打官司，我们需要请律师，因为律师在法律方面有专长，可以替我们进行操作，表达我们的想法。

<!--more-->

根据上文的阐述，代理模式就比较容易的理解了，我们看下代码：

```
public interface Sourceable {  
    public void method();  
}  
```

```
public class Source implements Sourceable {  
  
    @Override  
    public void method() {  
        System.out.println("the original method!");  
    }  
}  
```

```
public class Proxy implements Sourceable {  
  
    private Source source;  

    public Proxy(){  
        super();  
        this.source = new Source();  
    }  

    @Override  
    public void method() {  
        before();  
        source.method();  
        atfer();  
    }  

    private void atfer() {  
        System.out.println("after proxy!");  
    }  

    private void before() {  
        System.out.println("before proxy!");  
    }  
}  
```

```
public class ProxyTest {  
  
    public static void main(String[] args) {  
        Sourceable source = new Proxy();  
        source.method();  
    }  
  
}  
```

输出：

```
before proxy!
the original method!
after proxy!
```



如果在使用的时候需要对原有的方法进行改进，此时有两种办法：

1. 修改原有的方法来适应。这样违反了 `对扩展开放，对修改关闭` 的原则。
2. 就是采用一个代理类调用原有的方法，且对产生的结果进行控制。这种方法就是代理模式。

使用代理模式，可以将功能划分的更加清晰，有助于后期维护！


### 装饰模式与代理模式的区别

> 装饰器模式关注于在一个对象上动态的`添加`方法，然而代理模式关注于`控制`对对象的访问。
> 
> 用代理模式，代理类（proxy class）可以对它的客户隐藏一个对象的具体信息。因此，当使用代理模式的时候，我们常常在一个代理类中创建一个对象的实例。
> 当我们使用装饰器模式的时候，我们通常的做法是将原始对象作为一个参数传给装饰者的构造器。


> 二者的实现机制确实是一样的，可以看到他们的实例代码重复是很多的。但就语义上说，这两者的功能是相反的，模式的一个重要作用是`简化其他程序员对你程序的理解`，
> **你在一个地方写装饰，大家就知道这是在增加功能，你写代理，大家就知道是在限制**。




> [设计模式 - 目录](../2016-08-24-design-pattern/index.html)
> [装饰模式](07-decorator.html)

↓ 

> 转自： [Java之美[从菜鸟到高手演变]之设计模式](http://blog.csdn.net/zhangerqing/article/details/8239539) 