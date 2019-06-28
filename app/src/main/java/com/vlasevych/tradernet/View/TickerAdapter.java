package com.vlasevych.tradernet.View;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.vlasevych.tradernet.Model.Ticker;
import com.vlasevych.tradernet.R;

import java.util.List;

public class TickerAdapter extends RecyclerView.Adapter<TickerAdapter.TickerViewHolder> {

    private List<Ticker> tickerList;
    private Context contextAdapter;

    public class TickerViewHolder extends RecyclerView.ViewHolder {

        public TextView tickerNameTextView;
        public TextView priceChangePercentTextView;
        public TextView lastTimeExchangeTextView;
        public TextView lastPriceChangeTextView;

        public TickerViewHolder(View view) {
            super(view);
            // Элементы строки тикера
            tickerNameTextView = (TextView) view.findViewById(R.id.tickerNameTextView);
            priceChangePercentTextView = (TextView) view.findViewById(R.id.priceChangePercentTextView);
            lastTimeExchangeTextView = (TextView) view.findViewById(R.id.lastTimeExchangeTextView);
            lastPriceChangeTextView = (TextView) view.findViewById(R.id.lastPriceChangeTextView);

        }
    }

    public TickerAdapter(Context context, List<Ticker> tickerList) {
        contextAdapter = context;
        this.tickerList = tickerList;
    }

    @Override
    public int getItemCount() {
        return tickerList.size();
    }

    @Override
    public TickerAdapter.TickerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ticker_list_item, parent, false);
        return new TickerAdapter.TickerViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(final TickerAdapter.TickerViewHolder holder, final int position) {

        final Ticker t = tickerList.get(position);

        // Выводим значения в экранные элементы
        holder.tickerNameTextView.setText(t.getTickerName());
        holder.priceChangePercentTextView.setText(String.format("%.2f", t.getPriceChangePercent()) + "%");
        holder.lastTimeExchangeTextView.setText(t.getLastTime_DateOrTime() + " | " + t.getLastExchange());
        holder.lastPriceChangeTextView.setText(t.getLastPrice() + " ( " + t.getPriceChange() + " )");

        // Меняем цвет PriceChangePercent в зависимости от значения
        int colorRed = ContextCompat.getColor(contextAdapter, R.color.down);
        int colorGreen = ContextCompat.getColor(contextAdapter, R.color.up);
        int colorBlack = ContextCompat.getColor(contextAdapter, R.color.itemText);

        if (t.getPriceChangePercent() > 0) {holder.priceChangePercentTextView.setTextColor(colorGreen);}
        if (t.getPriceChangePercent() < 0) {holder.priceChangePercentTextView.setTextColor(colorRed);}
        if (t.getPriceChangePercent() == 0) {holder.priceChangePercentTextView.setTextColor(colorBlack);}

        if (t.getChanged()) {
            blink(holder.priceChangePercentTextView);
            t.setChanged(false);
        }

    }

    private void blink(TextView textView){
        Animation animationBlink = AnimationUtils.loadAnimation(contextAdapter, R.anim.blink);
        textView.startAnimation(animationBlink);
    }
}
