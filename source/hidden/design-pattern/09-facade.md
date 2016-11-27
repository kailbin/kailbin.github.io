---

title: 外观模式
date: 2016-08-29

---


外观模式是为了解决类与类之间的依赖关系的，像Spring一样，可以将类和类之间的关系配置到配置文件中，而外`观模式就是将类和类之间的关系放在一个 Facade 类中`，
降低了类类之间的耦合度，该模式中没有涉及到接口，我们以一个计算机的启动过程为例。

<!--more-->

我们先看下实现类：

```
public class CPU {  
      
    public void startup(){  
        System.out.println("cpu startup!");  
    }  
      
    public void shutdown(){  
        System.out.println("cpu shutdown!");  
    }  
}  
```

```
public class Memory {  
      
    public void startup(){  
        System.out.println("memory startup!");  
    }  
      
    public void shutdown(){  
        System.out.println("memory shutdown!");  
    }  
}  
```

```
public class Disk {  
      
    public void startup(){  
        System.out.println("disk startup!");  
    }  
      
    public void shutdown(){  
        System.out.println("disk shutdown!");  
    }  
}  
```

```
public class Computer {  
    private CPU cpu;  
    private Memory memory;  
    private Disk disk;  
      
    public Computer(){  
        cpu = new CPU();  
        memory = new Memory();  
        disk = new Disk();  
    }  
      
    public void startup(){  
        System.out.println("start the computer!");  
        cpu.startup();  
        memory.startup();  
        disk.startup();  
        System.out.println("start computer finished!");  
    }  
      
    public void shutdown(){  
        System.out.println("begin to close the computer!");  
        cpu.shutdown();  
        memory.shutdown();  
        disk.shutdown();  
        System.out.println("computer closed!");  
    }  
}  
```

User类如下：

```
public class User {  
  
    public static void main(String[] args) {  
        Computer computer = new Computer();  
        computer.startup();  
        computer.shutdown();  
    }  
}  
```

输出：

```
start the computer!
cpu startup!
memory startup!
disk startup!
start computer finished!

begin to close the computer!
cpu shutdown!
memory shutdown!
disk shutdown!
computer closed!
```

如果我们没有Computer类，那么，CPU、Memory、Disk他们之间将会相互持有实例，产生关系，这样会造成严重的依赖，
修改一个类，可能会带来其他类的修改，这不是我们想要看到的，有了Computer类，
他们之间的关系被放在了Computer类里，这样就起到了解耦的作用，这就是外观模式！







> [设计模式 - 目录](/post/2016-08-24-design-pattern.html)

↓ 

> 转自： [Java之美[从菜鸟到高手演变]之设计模式](http://blog.csdn.net/zhangerqing/article/details/8239539) 