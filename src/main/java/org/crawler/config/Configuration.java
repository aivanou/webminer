package org.crawler.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.crawler.ClientDomain;
import org.crawler.Domain;
import org.crawler.PatternStorage;
import org.crawler.parse.TemplateParser;

/**
 *
 */
public class Configuration {

    private final static TemplateParser tparser = new TemplateParser();

    private static boolean setLogger = false;

    public static Logger getLogger(String name) {
        Logger l = Logger.getLogger(name);
        return l;
    }

    public static List<ClientDomain> readDomains(File file) throws FileNotFoundException, IOException {
        Map<String, ClientDomain> cdomains = new HashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().startsWith("#")) {
                continue;
            }
            try {
                String[] data = line.split(" +");
                final String url = data[0];
                final String uniqueId = data[1];
                String name = data[2];
                String type = data[3];
                int maxDepth = Integer.parseInt(data[4]);
                int patternsSize = Integer.parseInt(data[5]);
                int crawlPeriod = Integer.parseInt(data[6]);
                Collection<String> htmlTemplateColl = new ArrayList<>();
                int index = 0;
                while (index < patternsSize) {
                    String pLine = reader.readLine();
                    System.out.println(pLine);
                    if (pLine.trim().startsWith("#")) {
                        continue;
                    }
                    htmlTemplateColl.add(pLine);
                    index += 1;
                }
                final PatternStorage patterns = tparser.parse(htmlTemplateColl);
                if (!cdomains.containsKey(url)) {
                    Domain d = new Domain.DomainBuilder().setCrawlPeriod(crawlPeriod).setMaxDepth(maxDepth).build(url);
                    ClientDomain cdomain = new ClientDomain(d, uniqueId, name, type, true, new HashMap<String, PatternStorage>() {
                        {
                            put(uniqueId, patterns);
                        }

                    });
                    cdomains.put(url, cdomain);
                } else {
                    ClientDomain cdomain = cdomains.get(url);
                    cdomain.getAllPatterns().put(uniqueId, patterns);
                    if (cdomain.getDomain().getMaxDepth() < maxDepth) {
                        cdomain.getDomain().setMaxDepth(maxDepth);
                    }
                }
            } catch (IOException | NumberFormatException ignore) {
            }
        }
        reader.close();
        return new LinkedList<>(cdomains.values());
    }

    public static String getPropertyIfNotNull(String prop, String defaultValue) {
        if (System.getProperty(prop) == null) {
            return defaultValue;
        }
        return System.getProperty(prop);
    }

}
