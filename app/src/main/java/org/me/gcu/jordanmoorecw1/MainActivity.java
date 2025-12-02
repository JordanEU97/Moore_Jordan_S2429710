package org.me.gcu.jordanmoorecw1;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.me.gcu.jordanmoorecw1.model.CurrencyRate;
import org.me.gcu.jordanmoorecw1.ui.theme.RatesViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// main activity used to show the currency list screen
public class MainActivity extends AppCompatActivity {

    // interval between automatic refreshes in milliseconds set to one hour
    private static final long AUTO_REFRESH_INTERVAL_MS = 60L * 60L * 1000L;

    // views on the screen
    private RecyclerView recyclerRates;
    private TextView tvUpdated;
    private TextView tvFeedUpdated;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnSearch;
    private LinearLayout searchPanel;
    private TextInputEditText etSearch;

    // view model and adapter
    private RatesViewModel viewModel;
    private RatesAdapter adapter;

    // full list of all currencies from the feed
    private final List<CurrencyRate> fullList = new ArrayList<>();
    // list that only holds the nine main currencies
    private final List<CurrencyRate> mainList = new ArrayList<>();

    // time of last successful update
    private long lastUpdateEpochMs = 0L;
    // flag that says if we are currently showing cached data
    private boolean usingCachedData = false;

    // handler and runnable for auto refresh and label update
    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateElapsedRunnable = new Runnable() {
        @Override
        public void run() {
            // check if it is time for an automatic refresh
            maybeAutoRefresh();
            // update the last refreshed label
            updateRelativeUpdatedLabel();
            // run again in one minute
            timerHandler.postDelayed(this, 60_000L);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get references to views
        recyclerRates = findViewById(R.id.recyclerRates);
        tvUpdated = findViewById(R.id.tvUpdated);
        tvFeedUpdated = findViewById(R.id.tvFeedUpdated);
        btnSearch = findViewById(R.id.btnSearch);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        searchPanel = findViewById(R.id.searchPanel);
        etSearch = findViewById(R.id.etSearch);

        // choose layout manager based on orientation
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // landscape uses a grid with two items per row
            recyclerRates.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            // portrait uses a simple vertical list
            recyclerRates.setLayoutManager(new LinearLayoutManager(this));
        }

        // set up view model
        viewModel = new ViewModelProvider(this).get(RatesViewModel.class);

        // observe currency rates list
        viewModel.getRates().observe(this, rates -> {
            swipeRefresh.setRefreshing(false);
            if (rates == null || rates.isEmpty()) {
                return;
            }

            // store full list and rebuild main currencies list
            fullList.clear();
            fullList.addAll(rates);
            buildMainCurrenciesList();

            // remember when we last updated
            lastUpdateEpochMs = System.currentTimeMillis();
            updateRelativeUpdatedLabel();

            // always show only the main nine currencies by default
            if (adapter == null) {
                adapter = new RatesAdapter(this, new ArrayList<>(mainList));
                recyclerRates.setAdapter(adapter);
            } else {
                adapter.updateList(mainList);
            }
        });

        // observe feed last updated text
        viewModel.getLastUpdated().observe(this, time -> {
            if (tvFeedUpdated != null) {
                if (time == null || time.isEmpty()) {
                    tvFeedUpdated.setText("RSS feed updated: --");
                } else {
                    tvFeedUpdated.setText("RSS feed updated: " + time);
                }
            }

            // check if this update came from cache by looking for the word cache
            usingCachedData = time != null
                    && time.toLowerCase(Locale.ROOT).contains("cache");

            updateRelativeUpdatedLabel();
        });

        // open and close the search panel when the button is pressed
        btnSearch.setOnClickListener(v -> toggleSearchPanel());

        // listen for text changes in the search box
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // nothing needed here
            }
        });

        // pull to refresh fetches new data
        swipeRefresh.setOnRefreshListener(() -> {
            swipeRefresh.setRefreshing(true);
            viewModel.refreshRates();
        });

        // first load of data when app opens
        viewModel.refreshRates();
    }

    // builds the list of the nine main currencies only
    private void buildMainCurrenciesList() {
        mainList.clear();
        if (fullList.isEmpty()) {
            return;
        }

        for (CurrencyRate rate : fullList) {
            if (rate == null || rate.getCode() == null) {
                continue;
            }

            String codeText = rate.getCode().toUpperCase(Locale.ROOT);

            // match the nine main currencies
            if (codeText.contains("(USD)")
                    || codeText.contains("(EUR)")
                    || codeText.contains("(JPY)")
                    || codeText.contains("(AUD)")
                    || codeText.contains("(CAD)")
                    || codeText.contains("(CHF)")
                    || codeText.contains("(CNY)")
                    || codeText.contains("(HKD)")
                    || codeText.contains("(NZD)")) {
                mainList.add(rate);
            }
        }
    }

    // filters the list based on the search text
    private void filterList(String query) {
        if (adapter == null) {
            return;
        }

        String text = query.toLowerCase(Locale.ROOT).trim();

        // empty search shows only the main currencies
        if (text.isEmpty()) {
            adapter.updateList(mainList);
            return;
        }

        List<CurrencyRate> filtered = new ArrayList<>();
        for (CurrencyRate rate : fullList) {
            if (rate == null) {
                continue;
            }

            String code = rate.getCode() != null
                    ? rate.getCode().toLowerCase(Locale.ROOT)
                    : "";
            String title = rate.getTitle() != null
                    ? rate.getTitle().toLowerCase(Locale.ROOT)
                    : "";

            if (code.contains(text) || title.contains(text)) {
                filtered.add(rate);
            }
        }

        adapter.updateList(filtered);
    }

    // open or close the search panel with a small fade
    private void toggleSearchPanel() {
        if (searchPanel.getVisibility() == View.GONE) {
            searchPanel.setVisibility(View.VISIBLE);
            searchPanel.setAlpha(0f);
            searchPanel.animate().alpha(1f).setDuration(200).start();
            etSearch.requestFocus();
        } else {
            searchPanel.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> searchPanel.setVisibility(View.GONE))
                    .start();
            etSearch.setText("");
            filterList("");
        }
    }

    // check if it is time to refresh data automatically
    private void maybeAutoRefresh() {
        if (lastUpdateEpochMs == 0L) {
            return;
        }

        long now = System.currentTimeMillis();
        long delta = now - lastUpdateEpochMs;
        if (delta >= AUTO_REFRESH_INTERVAL_MS && !swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
            viewModel.refreshRates();
        }
    }

    // update the label that shows when the app last refreshed
    private void updateRelativeUpdatedLabel() {
        if (usingCachedData) {
            tvUpdated.setText("App last refreshed: offline");
            return;
        }

        if (lastUpdateEpochMs == 0L) {
            tvUpdated.setText("App last refreshed: --");
            return;
        }

        String relative = computeRelativeString(lastUpdateEpochMs);
        tvUpdated.setText(String.format(
                Locale.US,
                "App last refreshed: %s",
                relative
        ));
    }

    // helper that builds a simple string like just now or 3 min ago
    private String computeRelativeString(long epochMs) {
        if (epochMs <= 0) {
            return "--";
        }

        long now = System.currentTimeMillis();
        long deltaMs = Math.max(0, now - epochMs);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(deltaMs);
        long hours = TimeUnit.MILLISECONDS.toHours(deltaMs);
        long days = TimeUnit.MILLISECONDS.toDays(deltaMs);

        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hr ago";
        return days + " day" + (days > 1 ? "s" : "") + " ago";
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerHandler.post(updateElapsedRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(updateElapsedRunnable);
    }
}
