package fileCan;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;

/**
 * Created by kail on 2017/2/2.
 * http://cucaracha.iteye.com/blog/2055653
 * http://cucaracha.iteye.com/blog/2051279
 * http://cucaracha.iteye.com/blog/2057050
 * http://blog.csdn.net/lirx_tech/article/details/51425364
 * http://www.infoq.com/cn/articles/java7-nio2/
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Files.walkFileTree(Paths.get("D:/Java"), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                final Path dir = Paths.get("D:/");
                WatchRafaelNadal watch = new WatchRafaelNadal();
                try {
                    watch.watchRNDir(dir);
                } catch (IOException | InterruptedException ex) {
                    System.err.println(ex);
                }
                return super.preVisitDirectory(dir, attrs);
            }

        });
//        System.exit(1);

    }
}
