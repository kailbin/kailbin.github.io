import org.pegdown.PegDownProcessor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Created by Kail on 2017/2/7.
 * Copyright(c) ttpai All Rights Reserved.
 */
public class PegdownMain {

    public static void main(String[] args) throws IOException, URISyntaxException {
        Path inPath = Paths.get(ClassLoader.getSystemResource("markdown4j.md").toURI());
        Path outPath = Paths.get(ClassLoader.getSystemResource("markdown4j.html").toURI());


        List<String> allLines = Files.readAllLines(inPath, Charset.forName("UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (String line : allLines) {
            System.out.println(line);
            sb.append(line).append(System.getProperty("line.separator"));
        }

        System.out.println();
        System.out.println();

        String html = new PegDownProcessor().markdownToHtml(sb.toString());
        System.out.println(html);

        Files.write(outPath, html.getBytes(Charset.forName("UTF-8")), StandardOpenOption.TRUNCATE_EXISTING);
    }

}
