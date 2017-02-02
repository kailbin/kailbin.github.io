import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by kail on 2017/2/2.
 * http://cucaracha.iteye.com/blog/2055653
 * http://cucaracha.iteye.com/blog/2051279
 * http://cucaracha.iteye.com/blog/2057050
 * http://blog.csdn.net/lirx_tech/article/details/51425364
 * http://www.infoq.com/cn/articles/java7-nio2/
 */
public class Main {
    public static void main(String[] args) {
//        System.out.println(Paths.get("test/src").toAbsolutePath());
//        System.exit(1);
        final Path path = Paths.get("test/src");
        WatchRafaelNadal watch = new WatchRafaelNadal();
        try {
            watch.watchRNDir(path);
        } catch (IOException | InterruptedException ex) {
            System.err.println(ex);
        }
    }
}
