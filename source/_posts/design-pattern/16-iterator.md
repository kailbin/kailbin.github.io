---

title: 迭代器模式
date: 2016-08-30

---


顾名思义，迭代器模式就是顺序访问聚集中的对象，集合非常常见，如果对集合类比较熟悉的话，理解本模式会十分轻松。

<!--more--> 

MyCollection中定义了集合的一些操作，MyIterator中定义了一系列迭代操作，且持有Collection实例，我们来看看实现代码。

两个接口：

```
public interface Collection {  
      
    public Iterator iterator();  
      
    /*取得集合元素*/  
    public Object get(int i);  
      
    /*取得集合大小*/  
    public int size();  
}  
```

```
public interface Iterator {  
    //前移  
    public Object previous();  
      
    //后移  
    public Object next();  

    // 是否有下一个元素
    public boolean hasNext();  
      
    //取得第一个元素  
    public Object first();  

}  
```

两个实现：

```
public class MyIterator implements Iterator {  
  
    private Collection collection;  
    private int pos = -1;  
      
    public MyIterator(Collection collection){  
        this.collection = collection;  
    }  
      
    @Override  
    public Object previous() {  
        if(pos > 0){  
            pos--;  
        }  
        return collection.get(pos);  
    }  
  
    @Override  
    public Object next() {  
        if(pos<collection.size()-1){  
            pos++;  
        }  
        return collection.get(pos);  
    }  
  
    @Override  
    public boolean hasNext() {  
        if(pos<collection.size()-1){  
            return true;  
        }else{  
            return false;  
        }  
    }  
  
    @Override  
    public Object first() {  
        pos = 0;  
        return collection.get(pos);  
    }  
  
}  
```

```
public class MyCollection implements Collection {  
  
    public String string[] = {"A","B","C","D","E"};  
    
    @Override  
    public Iterator iterator() {  
        return new MyIterator(this);  
    }  
  
    @Override  
    public Object get(int i) {  
        return string[i];  
    }  
  
    @Override  
    public int size() {  
        return string.length;  
    }  
}  
```

测试类：

```
public class Test {  
  
    public static void main(String[] args) {  
        Collection collection = new MyCollection();  
        Iterator it = collection.iterator();  
          
        while(it.hasNext()){  
            System.out.println(it.next());  
        }  
    }  
}  
```

输出：`A B C D E`



<br>
<br>


> [设计模式 - 目录](../2016-08-24-design-pattern/index.html)

<br>

> 转自： [Java之美[从菜鸟到高手演变]之设计模式](http://blog.csdn.net/zhangerqing/article/details/8243942) 