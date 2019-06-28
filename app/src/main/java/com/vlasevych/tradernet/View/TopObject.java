package com.vlasevych.tradernet.View;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class TopObject {
    @SerializedName("tickers")
    @Expose
    private String[] tickers;

    public TopObject(String[] tickers) {
        this.tickers = tickers;
    }

    public String[] getTickers() {
        return tickers;
    }
}
