package org.me.gcu.jordanmoorecw1;

import org.me.gcu.jordanmoorecw1.model.CurrencyRate;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

// parser that reads the rss xml and builds currency rate objects
public class RssParser {

    // holds the last updated time from the feed
    private String lastUpdated = "";

    // parses the xml string and returns a list of currency rates
    public List<CurrencyRate> parse(String xmlData) {
        List<CurrencyRate> rates = new ArrayList<>();
        CurrencyRate currentRate = null;

        try {
            // set up xml pull parser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlData));

            int eventType = parser.getEventType();

            // loop through every xml event
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName;

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();

                        // start of a new item
                        if (tagName.equalsIgnoreCase("item")) {
                            currentRate = new CurrencyRate();
                        }
                        // read the feed published date
                        else if (tagName.equalsIgnoreCase("pubDate")) {
                            lastUpdated = parser.nextText().trim();
                        }
                        // inside an item read title and description
                        else if (currentRate != null) {
                            if (tagName.equalsIgnoreCase("title")) {
                                String title = parser.nextText();
                                currentRate.setTitle(title);

                                // try to pull a code from the title if it contains a slash
                                if (title != null && title.contains("/")) {
                                    try {
                                        String[] parts = title.split("/");
                                        if (parts.length >= 2) {
                                            String code = parts[1]
                                                    .replace(" ", "")
                                                    .trim()
                                                    .toUpperCase();
                                            currentRate.setCode(code);
                                        }
                                    } catch (Exception ignored) {
                                        // ignore any problems with bad title formats
                                    }
                                }
                            } else if (tagName.equalsIgnoreCase("description")) {
                                String description = parser.nextText();
                                double rateValue = extractRate(description);
                                currentRate.setRate(rateValue);
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        tagName = parser.getName();

                        // when we reach end of item add it to the list if it is valid
                        if (tagName.equalsIgnoreCase("item") && currentRate != null) {
                            if (currentRate.getCode() != null && currentRate.getRate() > 0) {
                                rates.add(currentRate);
                            }
                            currentRate = null;
                        }
                        break;
                }

                // move to the next event
                eventType = parser.next();
            }

        } catch (Exception e) {
            // print any errors while parsing
            e.printStackTrace();
        }

        return rates;
    }

    // returns the last updated text read from the feed
    public String getLastUpdated() {
        return lastUpdated;
    }

    // pull  rate value from a description string
    private double extractRate(String description) {
        try {
            String[] parts = description.split("=");
            if (parts.length >= 2) {
                String ratePart = parts[1].trim().split(" ")[0];
                return Double.parseDouble(ratePart);
            }
        } catch (Exception ignored) {
            // ignore any problems with unexpected formats
        }
        return 0.0;
    }
}
