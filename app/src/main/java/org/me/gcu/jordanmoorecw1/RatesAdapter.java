package org.me.gcu.jordanmoorecw1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.me.gcu.jordanmoorecw1.model.CurrencyRate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// adapter that shows the list of currency rates in the recycler view
public class RatesAdapter extends RecyclerView.Adapter<RatesAdapter.RateViewHolder> {

    // list of currency rate items currently shown
    private final List<CurrencyRate> rates;
    // reference to the activity for colours flags and bottom sheet
    private final FragmentActivity activity;

    public RatesAdapter(FragmentActivity activity, List<CurrencyRate> rates) {
        this.activity = activity;
        // make a copy so we do not change the original list passed in
        this.rates = new ArrayList<>(rates);
    }

    @NonNull
    @Override
    public RateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate one row view from xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rate, parent, false);
        return new RateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RateViewHolder holder, int position) {
        CurrencyRate rate = rates.get(position);

        // set basic text values
        holder.tvTitle.setText(rate.getTitle());
        holder.tvSubtitle.setText(rate.getCode());
        holder.tvRate.setText(String.format(Locale.US, "%.4f", rate.getRate()));

        // simple colour logic based on how large the rate is
        double value = rate.getRate();
        int textColor;
        int bgColor;

        if (value >= 1.5) {
            // strong currency compared to gbp
            textColor = ContextCompat.getColor(activity, R.color.rate_strong);
            bgColor = ContextCompat.getColor(activity, R.color.rate_bg_strong);
        } else if (value >= 1.1) {
            // a bit stronger than gbp
            textColor = ContextCompat.getColor(activity, R.color.rate_moderate);
            bgColor = ContextCompat.getColor(activity, R.color.rate_bg_moderate);
        } else if (value >= 0.9) {
            // close to one to one
            textColor = ContextCompat.getColor(activity, R.color.rate_neutral);
            bgColor = ContextCompat.getColor(activity, R.color.rate_bg_neutral);
        } else if (value >= 0.5) {
            // weaker than gbp
            textColor = ContextCompat.getColor(activity, R.color.rate_weak);
            bgColor = ContextCompat.getColor(activity, R.color.rate_bg_weak);
        } else {
            // much weaker than gbp
            textColor = ContextCompat.getColor(activity, R.color.rate_very_weak);
            bgColor = ContextCompat.getColor(activity, R.color.rate_bg_very_weak);
        }

        holder.tvRate.setTextColor(textColor);
        holder.itemView.setBackgroundColor(bgColor);

        // choose the right flag image for this currency
        int flagRes = getFlagResource(rate.getCode());
        if (flagRes != 0) {
            holder.imgFlag.setVisibility(View.VISIBLE);
            holder.imgFlag.setImageResource(flagRes);
        } else {
            holder.imgFlag.setVisibility(View.INVISIBLE);
        }

        // when user taps a row open the converter bottom sheet for that currency
        holder.itemView.setOnClickListener(vw -> {
            ConverterBottomSheet bottomSheet =
                    ConverterBottomSheet.newInstance(rate.getCode(), rate.getRate());
            bottomSheet.show(activity.getSupportFragmentManager(), "converter");
        });
    }

    @Override
    public int getItemCount() {
        return rates.size();
    }

    // replaces the current list with a new one and refreshes the recycler view
    public void updateList(List<CurrencyRate> newList) {
        rates.clear();
        rates.addAll(newList);
        notifyDataSetChanged();
    }

    // helper that works out the flag drawable name from the currency code text
    // i do not know a better way to do this
    private int getFlagResource(String currencyCode) {
        if (currencyCode == null || currencyCode.isEmpty()) {
            return 0;
        }

        String baseCode = getString(currencyCode);

        // map three letter code to a two letter flag name
        String drawableCode;
        switch (baseCode) {
            case "GBP": drawableCode = "gb"; break;
            case "USD": drawableCode = "us"; break;
            case "EUR": drawableCode = "eu"; break;
            case "JPY": drawableCode = "jp"; break;
            case "AUD": drawableCode = "au"; break;
            case "CAD": drawableCode = "ca"; break;
            case "CHF": drawableCode = "ch"; break;
            case "CNY": drawableCode = "cn"; break;
            case "NZD": drawableCode = "nz"; break;
            case "SEK": drawableCode = "se"; break;
            case "NOK": drawableCode = "no"; break;
            case "DKK": drawableCode = "dk"; break;
            case "CZK": drawableCode = "cz"; break;
            case "PLN": drawableCode = "pl"; break;
            case "HUF": drawableCode = "hu"; break;
            case "RON": drawableCode = "ro"; break;
            case "BGN": drawableCode = "bg"; break;
            case "MXN": drawableCode = "mx"; break;
            case "BRL": drawableCode = "br"; break;
            case "ARS": drawableCode = "ar"; break;
            case "CLP": drawableCode = "cl"; break;
            case "COP": drawableCode = "co"; break;
            case "AED": drawableCode = "ae"; break;
            case "SAR": drawableCode = "sa"; break;
            case "EGP": drawableCode = "eg"; break;
            case "ZAR": drawableCode = "za"; break;
            case "ILS": drawableCode = "il"; break;
            case "KWD": drawableCode = "kw"; break;
            case "QAR": drawableCode = "qa"; break;
            case "INR": drawableCode = "in"; break;
            case "SGD": drawableCode = "sg"; break;
            case "HKD": drawableCode = "hk"; break;
            case "KRW": drawableCode = "kr"; break;
            case "THB": drawableCode = "th"; break;
            case "MYR": drawableCode = "my"; break;
            case "TWD": drawableCode = "tw"; break;
            case "PHP": drawableCode = "ph"; break;
            case "IDR": drawableCode = "id"; break;
            case "VND": drawableCode = "vn"; break;
            case "PKR": drawableCode = "pk"; break;
            case "TRY": drawableCode = "tr"; break;
            case "RUB": drawableCode = "ru"; break;
            case "BDT": drawableCode = "bd"; break;
            case "XDR": drawableCode = "un"; break;
            default:
                // last fallback is first two letters of the base code
                if (baseCode.length() >= 2) {
                    drawableCode = baseCode.substring(0, 2).toLowerCase(Locale.ROOT);
                } else {
                    drawableCode = baseCode.toLowerCase(Locale.ROOT);
                }
                break;
        }

        return activity.getResources()
                .getIdentifier(drawableCode, "drawable", activity.getPackageName());
    }

    @NonNull
    private static String getString(String currencyCode) {
        String upper = currencyCode.toUpperCase(Locale.ROOT);

        // first try to get a three letter code from inside brackets
        String baseCode;
        int open = upper.lastIndexOf('(');
        int close = upper.lastIndexOf(')');
        if (open >= 0 && close > open + 1) {
            baseCode = upper.substring(open + 1, close).trim();
        } else if (upper.length() >= 3) {
            // fallback to first three letters
            baseCode = upper.substring(0, 3);
        } else {
            baseCode = upper;
        }
        return baseCode;
    }

    // holder for one row in the recycler view
    static class RateViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvSubtitle, tvRate;
        ImageView imgFlag;

        RateViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvRate = itemView.findViewById(R.id.tvRate);
            imgFlag = itemView.findViewById(R.id.imgFlag);
        }
    }
}
