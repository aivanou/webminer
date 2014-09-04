package org.crawler.parse;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.crawler.HttpUtil;
import org.crawler.PatternStorage;
import org.crawler.fetch.HttpFetcher;
import org.crawler.fetch.ProtocolFetcher;
import org.crawler.fetch.ProtocolOutput;
import org.crawler.fetch.ProtocolResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * This class is html code parser. Using open source project - Jsoup.
 */
public class HtmlParser {

    protected static final Logger logger = Logger.getLogger(HtmlParser.class.getName());

    public HtmlParser() {
    }

    public ParseOutput parse(ProtocolOutput entity, Map<String, PatternStorage> parseExpressions) {
        String htmlContent;
        if (entity.getEncoding() == null) {
            htmlContent = new String(entity.getContent().array());
        } else {
            htmlContent = new String(entity.getContent().array(), Charset.forName(entity.getEncoding()));
        }
        return parse(htmlContent, entity.getUrl(), parseExpressions);
    }

    public ParseOutput parse(String htmlContent, String urlFrom, Map<String, PatternStorage> parseExpressions) {
        assert parseExpressions != null;
        assert !urlFrom.isEmpty();
        assert htmlContent != null;

        Document doc = Jsoup.parse(htmlContent);
        String domainFrom = HttpUtil.getProtocol(urlFrom).toString() + "://" + HttpUtil.getDomain(urlFrom);
        Collection<URL> links = gatherInLinks(doc, domainFrom);
        Map<String, Map<String, String>> parsedParts = new HashMap<>();
        for (String clientId : parseExpressions.keySet()) {
            PatternStorage pstorage = parseExpressions.get(clientId);
            parsedParts.put(clientId, parseClientPatterns(doc, pstorage));
        }

        return new ParseOutput(parsedParts, links);
    }

    private Map<String, String> parseClientPatterns(Document doc, PatternStorage pstorage) {
        Map<String, String> output = new HashMap<>();
        for (String patternName : pstorage.getKeySet()) {
            output.put(patternName, parseExpression(doc, pstorage.getPatterns(patternName)));
        }
        return output;
    }

    private String parseExpression(Document doc, Collection<String> repexps) {
        Iterator<String> it = repexps.iterator();
        if (!it.hasNext()) {
            return "";
        }
        Elements els = doc.select(it.next());
        while (it.hasNext()) {
            els = els.select(it.next());
        }
        if (els.isEmpty()) {
            return "";
        }
        return els.get(0).text();
    }

    private Collection<URL> gatherInLinks(Document doc, String domainFrom) {
        Elements links = doc.select("a[href]");
        Set<URL> linkList = new HashSet<>();
        for (Element el : links) {
            String linkAddress = el.attr("href").trim();
            if (linkAddress != null && !linkAddress.isEmpty()) {
                String relevantAddres = null;
                try {
                    relevantAddres = HttpUtil.normalizeUrl(domainFrom, linkAddress);
                    if (urlBelongsToDomain(relevantAddres, domainFrom)) {
                        if (relevantAddres.endsWith("/")) {
                            relevantAddres = relevantAddres.substring(0, relevantAddres.length() - 1);
                        }
                        linkList.add(new URL(relevantAddres));
                    }
                } catch (MalformedURLException ignore) {
                    System.out.println("ERRROR:::  " + relevantAddres);
                }
            }
        }
        return new ArrayList<>(linkList);
    }

    private boolean urlBelongsToDomain(String url, String domain) {
        return url.startsWith(domain);
    }

    public static void main(String[] args) throws Exception {
//        URL url = ClassLoader.getSystemResource("templates");
//        FileReader freader = new FileReader(new File(url.toURI()));
//        TemplateParser tparser = new TemplateParser();
////        Map<String, String> pobjects = tparser.parse(new BufferedReader(freader));
//        Map<String, String> pobjects = new HashMap<>();
//        pobjects.put("test", "span[id=Did_you_know...class=mw-headline]");
//        ProtocolFetcher fetcher = HttpFetcher.init();
//        ProtocolResponse fout = fetcher.fetch("http://en.wikipedia.org/wiki/Main_Page");
//        String content = new String(fout.getProtocolOutput().getContent().array());
//        HtmlParser p = new HtmlParser();
//        ParseOutput out = p.parse(content,
//                "http://en.wikipedia.org/wiki/Main_Page",
//                pobjects);
//        System.out.println(out.getLinks().size());
//        for (URL u : out.getLinks()) {
//            System.out.println(u);
//        }
//        System.out.println(out.getOutput().get("test"));

        Document d = Jsoup.parse("<html> <div class =\"class1 class2\" id =\"id1.\"> bla bla bla</div><div class =\"class1\" id =\"id1\" > bla bla bla11</div> <div id=\"id1\">nenenen</div> </html>");
        Elements els = d.select("div[class=class1 class2]");
        for (Element el : els) {
            System.out.println(el.text());
        }
    }

    public static String readFile(URI uri) throws Exception {
        File testFile = new File(uri);
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
