package org.me.gcu.jordanmoorecw1.ui.theme;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.me.gcu.jordanmoorecw1.data.Repository;
import org.me.gcu.jordanmoorecw1.model.CurrencyRate;

import java.util.List;

// view model that holds the currency data for the ui
public class RatesViewModel extends ViewModel {

    // live data for the list of currency rates
    private final MutableLiveData<List<CurrencyRate>> rates = new MutableLiveData<>();

    // live data for the last updated text from the feed
    private final MutableLiveData<String> lastUpdated = new MutableLiveData<>();

    // live data for any error message
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // repository that loads data from the network or cache
    private final Repository repository = new Repository();

    // returns the live list of currency rates
    public LiveData<List<CurrencyRate>> getRates() {
        return rates;
    }

    // returns the live last updated text
    public LiveData<String> getLastUpdated() {
        return lastUpdated;
    }


    // fetches data using the repository
    // when force refresh is true it skips the cache and goes straight to the network
    public void fetchRates(boolean forceRefresh) {
        repository.fetchRates(forceRefresh, new Repository.DataCallback() {
            @Override
            public void onSuccess(List<CurrencyRate> newRates, String updatedTime) {
                // update live data with the new values
                rates.postValue(newRates);
                lastUpdated.postValue(updatedTime);
                error.postValue(null);
            }

            @Override
            public void onError(String errorMsg) {
                // send error message to the ui
                error.postValue(errorMsg);
            }
        });
    }

    // helper used by pull to refresh and first app load
    // always tries to fetch new data when possible
    public void refreshRates() {
        fetchRates(true);
    }
}
