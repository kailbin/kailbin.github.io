---

title: 抽象工厂模式
date: 2016-8-26

---


工厂方法模式有一个问题就是，类的创建依赖工厂类，也就是说，如果想要拓展程序，必须对工厂类进行修改，这违背了闭包原则，
所以，从设计角度考虑，有一定的问题，如何解决？
就用到抽象工厂模式，**创建多个工厂类，这样一旦需要增加新的功能，直接增加新的工厂类就可以了，不需要修改之前的代码**。

<!--more-->

请看例子：
```
public interface Sender {  
    public void Send();  
}  
```

两个实现类：
```
public class MailSender implements Sender {  

    @Override  
    public void Send() {  
        System.out.println("this is mailsender!");  
    }  
}  
```

```
public class SmsSender implements Sender {  
  
    @Override  
    public void Send() {  
        System.out.println("this is sms sender!");  
    }  
}  
```

再提供一个工厂接口：

```
public interface Provider {  
    public Sender produce();  
}  
```

两个工厂类：

```
public class SendMailFactory implements Provider {  
      
    @Override  
    public Sender produce(){  
        return new MailSender();  
    }  
}  
```

```
public class SendSmsFactory implements Provider{  
  
    @Override  
    public Sender produce() {  
        return new SmsSender();  
    }  
}  
```



测试类：

```
public class Test {  
  
    public static void main(String[] args) {  
        Provider provider = new SendMailFactory();  
        Sender sender = provider.produce();  
        sender.Send();  
    }  
}  
```

其实这个模式的好处就是，如果你现在想增加一个功能：
发及时信息，则只需做一个实现类，实现Sender接口，同时做一个工厂类，实现Provider接口，就OK了，无需去改动现成的代码。这样做，拓展性较好！


> [FactoryBean](http://www.cnblogs.com/chenying99/archive/2012/09/23/2698878.html)


> [设计模式 - 目录](../2016-08-24-design-pattern/index.html)

↓ 

> 转自： [Java之美[从菜鸟到高手演变]之设计模式](http://blog.csdn.net/zhangerqing/article/details/8194653) 