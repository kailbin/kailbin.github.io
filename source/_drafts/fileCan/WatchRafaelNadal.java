package fileCan; /**
 * Created by kail on 2017/2/2.
 */

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;

public class WatchRafaelNadal {

    public void watchRNDir(Path path) throws IOException, InterruptedException {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {


            //start an infinite loop
            while (true) {

                //retrieve and remove the next watch key
                final WatchKey key = watchService.take();

                //get list of pending events for the watch key
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    //get the kind of event (create, modify, delete)
                    final Kind<?> kind = watchEvent.kind();
                    //handle OVERFLOW event
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }
                    //get the filename for the event
                    final WatchEvent<Path> watchEventPath = (WatchEvent<Path>) watchEvent;
                    final Path filename = watchEventPath.context();
                    //print it out
                    System.out.println(path + "----" + kind + " -> " + filename);
                }
                //reset the key
                boolean valid = key.reset();
                //exit loop if the key is not valid (if the directory was deleted, for example)
                if (!valid) {
                    break;
                }
            }
        }
    }
}
