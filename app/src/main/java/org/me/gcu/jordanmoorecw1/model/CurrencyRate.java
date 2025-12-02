package org.me.gcu.jordanmoorecw1.model;

// model class for a single currency rate item
public class CurrencyRate {

    // full currency code text for example british pound gbp or us dollar usd
    private String code;

    // readable title for this currency rate
    private String title;

    // numeric rate value for example 1 point 2564
    private double rate;

    // empty constructor used when the parser builds objects
    public CurrencyRate() {
    }


    // gets the full currency code text
    public String getCode() {
        return code;
    }

    // gets the readable title for this rate
    public String getTitle() {
        return title;
    }

    // gets the numeric rate value
    public double getRate() {
        return rate;
    }

    // sets the full currency code text
    public void setCode(String code) {
        this.code = code;
    }

    // sets the readable title for this rate
    public void setTitle(String title) {
        this.title = title;
    }

    // sets the numeric rate value
    public void setRate(double rate) {
        this.rate = rate;
    }
}
