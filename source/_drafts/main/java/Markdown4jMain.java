import org.markdown4j.Markdown4jProcessor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Kail on 2017/2/7.
 * Copyright(c) ttpai All Rights Reserved.
 */
public class Markdown4jMain {

    public static void main(String[] args) throws URISyntaxException, IOException {
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

        String html = new Markdown4jProcessor().process(sb.toString());
        System.out.println(html);

        Files.write(outPath, html.getBytes(Charset.forName("UTF-8")), StandardOpenOption.TRUNCATE_EXISTING);
    }

}
