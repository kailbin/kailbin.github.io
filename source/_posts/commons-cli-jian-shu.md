---

title: commons-cli 简述
date: 2013-05-31 18:14
desc: commons-cli 简述

---

>官网：http://commons.apache.org/proper/commons-cli/

 commons-cli 用来处理程序启动时命令行传入的参数。如果你仅仅只有一到两个参数需要处理，那么使用它有点多余，但是，如果你需要从命令行中捕获大多数应用程序的设置参数，那么使用CLI是恰到好处的。

<!--more-->
 

``` java 
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class HelloWorld {

    /**
     * @author yokoboy
     * @throws ParseException
     * @date 2013-5-7
     */
    public static void main(String[] args) throws ParseException {
        // 在使用CLI之前需要创建一个Options对象，该对象相当于一个容器，另外还有Option对象，
        // 每个Option对象相对于命令行中的一个参数。
        Options opts = new Options();
        // 通过利用这个Options，你可以使用addOption()方法定义你的应用程序可接受的命令行参数，
        // 每次都为一个option调用一次这个方法，看下面例示：
        opts.addOption("h", false, "打印帮助。");
        opts.addOption("th", true, "线程个数，int值。");
        opts.addOption("tn", true, "数据库表名。");
        // 一旦你定义了类的参数，创建一个CommandLineParser，并分析已传送到主方法中的组。
        BasicParser parser = new BasicParser();
        CommandLine cl = parser.parse(opts, args);
        // 等到所有的参数都被解析以后，你可以开始检查返回的命令行，这些命令行中，提供用户的参数和值已被
        // 语法分析程序详细检查过了
        if (cl.hasOption('h')) {
            HelpFormatter hf = new HelpFormatter();
            hf.printHelp("OptionsTip", opts);
        } else {
            System.out.println(cl.getOptionValue("th"));
            System.out.println(cl.getOptionValue("tn"));
        }
    }
}
```