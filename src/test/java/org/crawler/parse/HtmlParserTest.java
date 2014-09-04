package org.crawler.parse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.crawler.HttpUtil;
import org.crawler.PatternStorage;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class HtmlParserTest {

    @Test
    public void testEmpty() {
        HtmlParser parser = new HtmlParser();
        ParseOutput out = parser.parse("", "protocol://url", new HashMap<String, PatternStorage>());
        Assert.assertEquals(0, out.getLinks().size());
        Assert.assertEquals(0, out.getOutput().size());
    }

    @Test
    public void testLinksExtraction() throws Exception {
        HtmlParser parser = new HtmlParser();
        ParseOutput out = parser.parse(readFile("test1.html"), "http://en.wikipedia.org/", new HashMap<String, PatternStorage>());
        Assert.assertEquals(156, out.getLinks().size());
    }

    @Test
    public void testPatterns() throws Exception {
        HtmlParser parser = new HtmlParser();
        Map<String, PatternStorage> patterns = new HashMap<>();

        PatternStorage ps1 = new PatternStorage();
        PatternStorage ps2 = new PatternStorage();
        Collection<String> p1List = new LinkedList<>(Arrays.asList("li[id=n-currentevents]"));
        Collection<String> p2List = new LinkedList<>(Arrays.asList("span[id=Did_you_know...]"));

        ps1.putPatterns("li", p1List);
        ps2.putPatterns("span", p2List);
        patterns.put("client1", ps1);
        patterns.put("client2", ps2);

        ParseOutput out = parser.parse(readFile("test1.html"), "http://en.wikipedia.org/", patterns);
        Assert.assertEquals(2, out.getOutput().size());
        Assert.assertEquals("Current events", out.getOutput().get("client1").get("li"));
        Assert.assertEquals("Did you know...", out.getOutput().get("client2").get("span"));
    }

    @Test
    public void testIDandClass() {
        HtmlParser parser = new HtmlParser();
        String content = "<div id  =  \"Person.Name\" class=\"Webpage.Person\" >  Default Full Name  </div> "
                + "<div id=\"Person.Name\" class = \"Webpage.Name \" > Default Short Name</div>";

        Map<String, PatternStorage> patterns = new HashMap<>();
        PatternStorage ps = new PatternStorage();
        ps.putPatterns("name", new LinkedList<>(Arrays.asList("div[id=Person.Name]", "div[class=Webpage.Person]")));
        patterns.put("clientId", ps);
        ParseOutput out = parser.parse(content, "default://def", patterns);
        Assert.assertEquals(1, out.getOutput().size());
        Assert.assertEquals("Default Full Name", out.getOutput().get("clientId").get("name"));
    }

    public static String readFile(String filename) throws Exception {
        File testFile = new File(ClassLoader.getSystemResource(filename).toURI());
        BufferedInputStream str = new BufferedInputStream(new FileInputStream(testFile));
        StringBuilder sb = new StringBuilder();
        int a;
        while ((a = str.read()) > 0) {
            sb.append((char) a);
        }
        str.close();
        return sb.toString();
    }
}
