---

title: 模板方法模式
date: 2016-08-30

---

一个抽象类中，有一个主方法，再定义1...n个方法，可以是抽象的，也可以是实际的方法，`定义一个类，继承该抽象类，重写抽象方法，通过调用抽象类，实现对子类的调用`。

<!--more-->

```
public abstract class AbstractCalculator {  
      
    /*主方法，实现对本类其它方法的调用*/  
    public final int calculate(String exp,String opt){  
        int array[] = split(exp,opt);  
        return calculate(array[0],array[1]);  
    }  
      
    /*被子类重写的方法*/  
    public abstract int calculate(int num1,int num2);  
      
    public int[] split(String exp,String opt){  
        String array[] = exp.split(opt);  
        int arrayInt[] = new int[2];  
        arrayInt[0] = Integer.parseInt(array[0]);  
        arrayInt[1] = Integer.parseInt(array[1]);  
        return arrayInt;  
    }  
}  
```

```
public class Plus extends AbstractCalculator {  
  
    @Override  
    public int calculate(int num1,int num2) {  
        return num1 + num2;  
    }  
}  
```

测试类：

```
public class StrategyTest {  
  
    public static void main(String[] args) {  
        String exp = "8+8";  
        AbstractCalculator cal = new Plus();  
        int result = cal.calculate(exp, "\\+");  
        System.out.println(result);  
    }  
}  
```

首先将`exp`和`"\\+"`做参数，调用`AbstractCalculator`类里的`calculate(String,String)`方法，在`calculate(String,String)`里调用同类的`split()`，
之后再调用`calculate(int ,int)`方法，从这个方法进入到子类中，执行完`return num1 + num2`后，将值返回到`AbstractCalculator`类，赋给`result`，打印出来。


<br>
<br>
<br>




> [设计模式 - 目录](/post/2016-08-24-design-pattern.html)

<br>

> 转自： [Java之美[从菜鸟到高手演变]之设计模式](http://blog.csdn.net/zhangerqing/article/details/8243942) 