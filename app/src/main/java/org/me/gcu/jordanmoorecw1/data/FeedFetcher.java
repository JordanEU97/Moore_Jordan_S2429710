package org.me.gcu.jordanmoorecw1.data;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

// helper class that downloads the rss feed for gbp exchange rates
public class FeedFetcher {

    // url for the rss feed
    private static final String FEED_URL = "https://www.fx-exchange.com/gbp/rss.xml";

    // downloads the xml feed from the internet and returns it as a string
    public String fetchFeed() {
        StringBuilder result = new StringBuilder();
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // make a url object for the feed
            URL url = new URL(FEED_URL);
            connection = (HttpURLConnection) url.openConnection();

            // set method and basic timeouts
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10_000);
            connection.setReadTimeout(15_000);

            // connect to the server
            connection.connect();

            // check the response code is ok
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "";
            }

            // set up a reader to read the response
            reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream())
            );

            // read each line nd add it to the result string
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

        } catch (Exception e) {
            // if something goes wrong print the error and return an empty string
            e.printStackTrace();
            return "";
        } finally {
            // always close the reader and disconnect the connection
            try {
                if (reader != null) {
                    reader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception ignored) {
                // ignore errors while closing resources
            }
        }

        // send back the full xml text
        return result.toString();
    }
}
