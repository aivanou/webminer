package org.crawler.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.crawler.PatternStorage;

/**
 *
 */
public class TemplateParser {

    private final Pattern p;

    public TemplateParser() {
        this.p = Pattern.compile("[ ]*\\w+[ ]*=[ ]*\"[^\"]+\"[ ]*");
    }

    public PatternStorage parse(Collection<String> htmlPatterns) throws IOException {
        StringBuilder sbuffer = new StringBuilder();
        for (String pattern : htmlPatterns) {
            sbuffer.append(pattern).append("\n");
        }
        BufferedReader reader = new BufferedReader(new StringReader(sbuffer.toString()));
        return parse(reader);
    }

    public PatternStorage parse(BufferedReader reader) throws IOException {
        PatternStorage templates = new PatternStorage();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                continue;
            }
            String[] data = line.split(" ", 2);
            String name = data[0];
            String template = data[1];
            templates.putPatterns(name, templateToCssQuery(template));
        }
        return templates;
    }

    private Collection<String> templateToCssQuery(String template) {
        template = strip(template.trim()).trim();
        String[] data = template.split(" ", 2);
        if (data.length == 1) {
            return new ArrayList<>(Arrays.asList(data[0]));
        }
        String tagName = data[0];
        String attrs = data[1].trim();
        Collection<String> output = new ArrayList<>();
        Matcher m = p.matcher(attrs);
        while (m.find()) {
            String[] res = attrs.substring(m.start(), m.end()).split("=", 2);
            String attrName = res[0].trim();
            String attrValue = res[1].trim();
            attrValue = attrValue.substring(1, attrValue.length() - 1);
            attrValue = trimString(attrValue);
            switch (attrName) {
                case "id":
                    output.add(tagName + "[id=" + attrValue + "]");
                    break;
                case "class":
                    output.add(tagName + "[class=" + attrValue + "]");
                    break;
            }
        }
        return output;
    }

    private String trimString(String line) {
        line = line.trim();
        StringBuilder out = new StringBuilder();
        String[] data = line.split(" ");
        for (String d : data) {
            d = d.trim();
            if (!d.isEmpty()) {
                out.append(d).append(" ");
            }
        }
        return out.toString().trim();
    }

    private String strip(String template) {
        return template.substring(1, template.length() - 1);
    }

    public static void main(String[] args) {
        String l = "class=\"buying_price_lira fl green3\"";
        Pattern p = Pattern.compile("[ ]*\\w+[ ]*=[ ]*\"[^\"]+\"[ ]*");
        Matcher m = p.matcher(l);
        while (m.find()) {
            System.out.println(l.substring(m.start(), m.end()));
        }
    }

}
