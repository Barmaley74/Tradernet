package com.vlasevych.tradernet.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Ticker implements Parcelable {

    private String tickerName;
    private Double priceChangePercent;
    private String lastTime;
    private String lastExchange;
    private Double lastPrice;
    private Double priceChange;
    private Boolean changed;

    public Ticker(String tickerName){
        this.tickerName = tickerName;
        this.priceChangePercent = .0;
        this.lastTime = "";
        this.lastExchange = "";
        this.lastPrice = .0;
        this.priceChange = .0;
        this.changed = false;
    }

    public Ticker(String tickerName, Double priceChangePercent, String lastTime, String lastExchange, Double lastPrice, Double priceChange) {
        this.tickerName = tickerName;
        this.priceChangePercent = priceChangePercent;
        this.lastTime = lastTime;
        this.lastExchange = lastExchange;
        this.lastPrice = lastPrice;
        this.priceChange = priceChange;
        this.changed = false;
    }

    private Ticker(Parcel in) {
        tickerName = in.readString();
        priceChangePercent = in.readDouble();
        lastTime = in.readString();
        lastExchange = in.readString();
        lastPrice = in.readDouble();
        priceChange = in.readDouble();
        changed = in.readByte() != 0;
    }

    public int describeContents() {
        return 0;
    }

    public String getTickerName() {
        return tickerName;
    }

    public void setTickerName(String tickerName) {
        this.tickerName = tickerName;
    }

    public Double getPriceChangePercent() {
        return priceChangePercent;
    }

    public void setPriceChangePercent(Double priceChangePercent) {
        Double oldPCP = this.priceChangePercent;
        this.priceChangePercent = priceChangePercent;
        this.changed = (oldPCP != priceChangePercent);
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getLastTime_DateOrTime() {
        String result = "";
        if (!lastTime.isEmpty()) {
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String today = formatter.format(date);
            String last = lastTime.substring(0, 10);
            result = last; // Возвращаем дату
            if (last.equals(today)) {
                result = lastTime.substring(11); // Возвращаем время
            }
        }
        return result;
    }

    public String getLastExchange() {
        return lastExchange;
    }

    public void setLastExchange(String lastExchange) {
        this.lastExchange = lastExchange;
    }

    public Double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(Double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public Double getPriceChange() {
        return priceChange;
    }

    public void setPriceChange(Double priceChange) {
        this.priceChange = priceChange;
    }

    public Boolean getChanged() {
        return changed;
    }

    public void setChanged(Boolean changed) {
        this.changed = changed;
    }

    @Override
    public void writeToParcel(android.os.Parcel dest, int flags) {
        dest.writeString(tickerName);
        dest.writeDouble(priceChangePercent);
        dest.writeString(lastTime);
        dest.writeString(lastExchange);
        dest.writeDouble(lastPrice);
        dest.writeDouble(priceChange);
        dest.writeByte((byte) (changed ? 1 : 0));
    }

    public static final Parcelable.Creator<Ticker> CREATOR = new Parcelable.Creator<Ticker>() {
        public Ticker createFromParcel(Parcel in) {
            return new Ticker(in);
        }

        public Ticker[] newArray(int size) {
            return new Ticker[size];
        }
    };
}
