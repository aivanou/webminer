package org.crawler.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.crawler.PatternStorage;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TemplateParserTest {

    @Test
    public void testEmptyTemplate() throws IOException {
        TemplateParser parser = new TemplateParser();
        Collection<String> patterns = new ArrayList<>();
        patterns.add("");
        PatternStorage templates = parser.parse(patterns);
        Assert.assertTrue(templates.isEmpty());
    }

    @Test
    public void testPatterns() throws IOException {
        TemplateParser parser = new TemplateParser();
        Collection<String> patterns = new ArrayList<>();
        patterns.add("tag1 <div class=\"buying_price_lira fl green3\">");
        PatternStorage templates = parser.parse(patterns);
        Collection<String> out = templates.getPatterns("tag1");
        Assert.assertEquals("div[class=buying_price_lira fl green3]", out.iterator().next());
    }

    @Test
    public void testPatterns2() throws IOException {
        TemplateParser parser = new TemplateParser();
        Collection<String> patterns = new ArrayList<>();
        patterns.add("tag1 <div id=\"my_id.subid\" class=\"buying_price_lira fl green3\">");
        patterns.add("price <a    id=\"   my_id   \"    class  =   \"   cl1  cl2   cl3   cl4     \"   >");
        PatternStorage templates = parser.parse(patterns);
        Assert.assertTrue(templates.getPatterns("tag1").contains("div[class=buying_price_lira fl green3]"));
        Assert.assertTrue(templates.getPatterns("tag1").contains("div[id=my_id.subid]"));
        Assert.assertTrue(templates.getPatterns("price").contains("a[id=my_id]"));
        Assert.assertTrue(templates.getPatterns("price").contains("a[class=cl1 cl2 cl3 cl4]"));
    }

}
