package org.me.gcu.jordanmoorecw1;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;

// bottom sheet used for quick currency conversion
public class ConverterBottomSheet extends BottomSheetDialogFragment {

    // argument key for the selected currency code
    private static final String ARG_CURRENCY_CODE = "currency_code";

    // argument key for the selected currency rate
    private static final String ARG_CURRENCY_RATE = "currency_rate";

    // selected currency code for example usd
    private String currencyCode;

    // rate of selected currency compared to gbp
    private double rate;

    // true when converting from currency back to gbp
    private boolean isReversed = false;

    // text view that shows the pair label
    private TextView tvPair;

    // text view that shows the result
    private TextView tvResult;

    // input field where the user types the amount
    private TextInputEditText etAmount;

    // button used to reverse the conversion direction
    private MaterialButton btnReverse;

    // creates a new bottom sheet with the given currency and rate
    public static ConverterBottomSheet newInstance(String code, double rate) {
        ConverterBottomSheet fragment = new ConverterBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_CURRENCY_CODE, code);
        args.putDouble(ARG_CURRENCY_RATE, rate);
        fragment.setArguments(args);
        return fragment;
    }

    // called when the view for this bottom sheet is created
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this sheet
        View view = inflater.inflate(R.layout.bottom_sheet_converter, container, false);

        // connect java fields to xml views
        tvPair = view.findViewById(R.id.tvPair);
        tvResult = view.findViewById(R.id.tvResult);
        etAmount = view.findViewById(R.id.etAmount);
        btnReverse = view.findViewById(R.id.btnReverse);

        // read code and rate from the arguments bundle
        if (getArguments() != null) {
            currencyCode = getArguments().getString(ARG_CURRENCY_CODE);
            rate = getArguments().getDouble(ARG_CURRENCY_RATE);
        }

        // set the initial label to show gbp to currency direction
        updatePairLabel();

        // set up button and input handlers

        // handle reverse button click and swap conversion direction
        btnReverse.setOnClickListener(v -> {
            isReversed = !isReversed;
            updatePairLabel();
            calculateAndDisplayResult();
        });

        // watch the amount input so conversion updates as the user types
        etAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // not used here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAndDisplayResult();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // not used here
            }
        });

        return view;
    }

    // updates the label text to match the current conversion direction
    private void updatePairLabel() {
        if (isReversed) {
            tvPair.setText(currencyCode + " → GBP");
        } else {
            tvPair.setText("GBP → " + currencyCode);
        }
    }

    // reads the amount and shows the converted result
    private void calculateAndDisplayResult() {
        String input = etAmount.getText() != null ? etAmount.getText().toString() : "";

        // clear result when there is no input
        if (input.trim().isEmpty()) {
            tvResult.setText("");
            return;
        }

        try {
            // parse the amount from the input text
            double amount = Double.parseDouble(input);

            // when reversed convert from currency back to gbp when normal convert from gbp to currency
            double result = isReversed
                    ? amount / rate
                    : amount * rate;

            // show the result formatted to four decimal places
            tvResult.setText(String.format("%.4f", result));

        } catch (NumberFormatException e) {
            tvResult.setText("Invalid amount");
        }
    }
}
