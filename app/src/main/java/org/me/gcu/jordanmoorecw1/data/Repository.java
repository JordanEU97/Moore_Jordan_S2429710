package org.me.gcu.jordanmoorecw1.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.me.gcu.jordanmoorecw1.App;
import org.me.gcu.jordanmoorecw1.RssParser;
import org.me.gcu.jordanmoorecw1.model.CurrencyRate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// class that loads and stores currency rates
public class Repository {

    // interface used to send data back to the view model
    public interface DataCallback {
        void onSuccess(List<CurrencyRate> rates, String updatedTime);
        void onError(String errorMsg);
    }

    // executor that runs work on a background thread
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // helper that downloads the rss feed xml text
    private final FeedFetcher feedFetcher = new FeedFetcher();

    // parser that turns xml text into currency rate objects
    private final RssParser parser = new RssParser();

    // name of the shared preferences file used for cache
    private static final String PREFS_NAME = "feed_cache";

    // key used to store the last xml feed string
    private static final String KEY_LAST_FEED = "last_feed";

    // tries to read cached data before using the network
    // returns true when cache was used
    private boolean tryUseCachedDataFirst(DataCallback callback) {
        SharedPreferences prefs = App.getInstance()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String cachedXml = prefs.getString(KEY_LAST_FEED, "");

        if (cachedXml == null || cachedXml.isEmpty()) {
            return false;
        }

        try {
            List<CurrencyRate> cachedRates = parser.parse(cachedXml);
            if (cachedRates != null && !cachedRates.isEmpty()) {
                String lastUpdatedFromCache = parser.getLastUpdated();
                String label = (lastUpdatedFromCache != null && !lastUpdatedFromCache.isEmpty())
                        ? lastUpdatedFromCache + " (from cache)"
                        : "(cached data)";

                callback.onSuccess(cachedRates, label);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // loads currency rates either from cache or from the network
    // when force refresh is true it skips the cache and goes to the network
    public void fetchRates(boolean forceRefresh, DataCallback callback) {
        executor.execute(() -> {
            try {
                if (!forceRefresh) {
                    // try cached data first when not forcing refresh
                    boolean usedCache = tryUseCachedDataFirst(callback);
                    if (usedCache) {
                        return;
                    }
                }

                // fetch data from the network
                String xmlData = feedFetcher.fetchFeed();

                if (xmlData == null || xmlData.isEmpty()) {
                    // if network fails then try cached data
                    useCachedData(callback);
                    return;
                }

                // parse the xml into a list of rates
                List<CurrencyRate> parsedRates = parser.parse(xmlData);
                String lastUpdated = parser.getLastUpdated();

                if (parsedRates != null && !parsedRates.isEmpty()) {
                    // save the raw xml text so we can use it offline later
                    SharedPreferences prefs = App.getInstance()
                            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit().putString(KEY_LAST_FEED, xmlData).apply();

                    // send parsed data back through the callback
                    callback.onSuccess(parsedRates, lastUpdated);
                } else {
                    callback.onError("Parsed feed returned no valid results.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                // if something goes wrong here then fall back to cached data
                useCachedData(callback);
            }
        });
    }
    // used when there is no internet connection
    private void useCachedData(DataCallback callback) {
        try {
            SharedPreferences prefs = App.getInstance()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String cachedXml = prefs.getString(KEY_LAST_FEED, "");

            if (!cachedXml.isEmpty()) {
                List<CurrencyRate> cachedRates = parser.parse(cachedXml);
                if (cachedRates != null && !cachedRates.isEmpty()) {
                    String lastUpdatedFromCache = parser.getLastUpdated();
                    String label = (lastUpdatedFromCache != null && !lastUpdatedFromCache.isEmpty())
                            ? lastUpdatedFromCache + " (from cache)"
                            : "(cached data)";

                    callback.onSuccess(cachedRates, label);
                    return;
                }
            }

            callback.onError("No internet connection and no cached data available.");

        } catch (Exception e) {
            e.printStackTrace();
            callback.onError("Offline fallback failed: " + e.getMessage());
        }
    }
}
