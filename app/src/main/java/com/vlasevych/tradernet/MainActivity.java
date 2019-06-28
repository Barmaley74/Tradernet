package com.vlasevych.tradernet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.vlasevych.tradernet.R;
import com.vlasevych.tradernet.Utils.RetrofitBuilder;
import com.vlasevych.tradernet.Model.Ticker;
import com.vlasevych.tradernet.Utils.Constants;
import com.vlasevych.tradernet.Utils.ProgressDialogLoader;
import com.vlasevych.tradernet.View.TickerAdapter;
import com.vlasevych.tradernet.View.TopInterface;
import com.vlasevych.tradernet.View.TopObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    String[] tickers;

    private Socket mSocket;

    public RecyclerView tickerRecylerView ;
    public ArrayList<Ticker> tickerList ;
    public ArrayList<Ticker> tickerListToShow;
    public TickerAdapter tickerAdapter;

    public EditText searchEditText;
    public ImageButton searchImageButton;

    public Spinner exchangeSpinner;
    public Spinner typeSpinner;
    public Spinner countTickersSpinner;

    public String exchange = Constants.TOP_EXCHANGE;
    public String type = Constants.TOP_TYPE;
    public Integer limit = Constants.TOP_LIMIT;

    public Integer exchangeSelected = 0;
    public Integer typeSelected = 0;
    public Integer countTickersSelected = 0;

    private ImageView flagImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // Screen elements
        searchEditText = (EditText) findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(searchTextWatcher);

        searchImageButton = (ImageButton) findViewById(R.id.searchImageButton);
        searchImageButton.setOnClickListener(searchListener);

        exchangeSpinner = (Spinner) findViewById(R.id.exchangeSpinner);
        exchangeSpinner.setOnItemSelectedListener(exchangeListener);
        typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        typeSpinner.setOnItemSelectedListener(typeListener);
        countTickersSpinner = (Spinner) findViewById(R.id.countTickersSpinner);
        countTickersSpinner.setOnItemSelectedListener(countTickersListener);

        flagImageView = (ImageView) findViewById(R.id.flagImageView);

        tickerRecylerView = (RecyclerView) findViewById(R.id.tickerList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        tickerRecylerView.setLayoutManager(mLayoutManager);
        tickerRecylerView.setItemAnimator(new DefaultItemAnimator());

        if (savedInstanceState != null) {
            tickers = savedInstanceState.getStringArray("tickers");
            exchangeSelected = savedInstanceState.getInt("exchangeSelected");
            typeSelected = savedInstanceState.getInt("typeSelected");
            countTickersSelected = savedInstanceState.getInt("countTickersSelected");
            tickerList = savedInstanceState.getParcelableArrayList("tickerList");
            tickerListToShow = savedInstanceState.getParcelableArrayList("tickerListToShow");
            tickerAdapter = new TickerAdapter(this, tickerListToShow);
            tickerRecylerView.setAdapter(tickerAdapter);
            showView();
        }

        buildListTopTickers();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray("tickers", tickers);
        outState.putInt("exchangeSelected", exchangeSpinner.getSelectedItemPosition());
        outState.putInt("typeSelected", typeSpinner.getSelectedItemPosition());
        outState.putInt("countTickersSelected", countTickersSpinner.getSelectedItemPosition());
        outState.putParcelableArrayList("tickerList", tickerList);
        outState.putParcelableArrayList("tickerListToShow", tickerListToShow);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_about:
                AlertDialog.Builder adb = new AlertDialog.Builder(this)
                        .setTitle(R.string.aboutTitle)
                        .setPositiveButton(R.string.okButton, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton(R.string.tradernetButton, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse( Constants.API_URL ));
                                startActivity(i);
                            }
                        })
                        .setNeutralButton(R.string.authorButton, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse( Constants.AUTHOR_URL ));
                                startActivity(i);
                            }
                        })
                        .setMessage(R.string.aboutMessage);
                adb.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // WebSocket event handling
    private void setListening() {

        // Respponse from server with tickers
        mSocket.on("q", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ProgressDialogLoader.progressdialog_dismiss();
                        // Обрабатываем результат
                        JSONObject data = (JSONObject) args[0];
                        try {
                            JSONArray q = data.getJSONArray("q");
                            for(int i=0; i<q.length(); i++) {
                                JSONObject row = q.getJSONObject(i);
                                // Get field values
                                if (row.has("c")) {
                                    String tickerName = row.getString("c");
                                    for(int y=0; y<tickerList.size(); y++) {
                                        if (tickerList.get(y).getTickerName().equals(tickerName)) {
                                            // Update Ticker
                                            Ticker t = tickerList.get(y);
                                            if (row.has("pcp")) {t.setPriceChangePercent(row.getDouble("pcp"));};
                                            if (row.has("ltt")) {t.setLastTime(row.getString("ltt"));};
                                            if (row.has("ltr")) {t.setLastExchange(row.getString("ltr"));};
                                            if (row.has("ltp")) {t.setLastPrice(row.getDouble("ltp"));};
                                            if (row.has("chg")) {t.setPriceChange(row.getDouble("chg"));};
                                            tickerList.set(y, t);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Display a list
                        showList();
                    }
                });
            }
        });

        // connection to server is established
        mSocket.on("connect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                // Convert the list to tickers in the format for the request
                ArrayList tickersForJson = new ArrayList();
                for(int i=0; i<tickers.length; i++) {
                    tickersForJson.add(tickers[i]);
                }

                JSONArray json = (new JSONArray(tickersForJson));
                mSocket.emit("sup_updateSecurities2", json);
            }
        });

    }

    // Start search
    private View.OnClickListener searchListener = new View.OnClickListener() {
        public void onClick(View v) {
            showList();
        }
    };

    // List output based on search string
    private void showList() {
        tickerListToShow.clear();
        for(int y=0; y<tickerList.size(); y++) {
            tickerListToShow.add(tickerList.get(y));
        }
        //Поиск
        String searchText = searchEditText.getText().toString();
        if (!searchText.isEmpty()) {
            for (int i = tickerListToShow.size() - 1; i > -1 ; i--) {
                String tickerName = tickerListToShow.get(i).getTickerName();
                if (!tickerName.toLowerCase().contains(searchText.toLowerCase())) {
                    tickerListToShow.remove(i);
                }
            }
        }
        // Update the list
        tickerAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Disconnect connection with WebSocket
        mSocket.disconnect();
    }

    TextWatcher searchTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(
                CharSequence c, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(
                CharSequence c, int start, int before, int count) {
            if (searchEditText.getText().toString().length() == 0) {
                hideKeyboard();
                showList(); // Display the entire list if the search field is empty
            }
        }
        @Override
        public void afterTextChanged(Editable c) {

        }
    };

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = this.getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void buildListTopTickers(){

        ProgressDialogLoader.progressdialog_creation(this, getResources().getString(R.string.loadingTickers));

        Retrofit mRetrofit = RetrofitBuilder.Build();

        TopInterface request = mRetrofit.create(TopInterface.class);

        try
        {
            JSONObject params = new JSONObject();
            params.put("type", type);
            params.put("exchange", exchange);
            params.put("gainers", Constants.TOP_GAINERS);
            params.put("limit", limit);

            JSONObject json = new JSONObject();
            json.put("cmd", "getTopSecurities");
            json.put("params", params);

            Call<TopObject> call = request.loadTickers(json);

            call.enqueue(new Callback<TopObject>() {
                @Override
                public void onResponse(Call<TopObject> call, Response<TopObject> response) {
                    ProgressDialogLoader.progressdialog_dismiss();
                    Log.d(Constants.TAG, response.body().toString());
                    TopObject top = response.body();
                    String[] oldTickers = tickers;
                    tickers = top.getTickers();
                    if ((tickers != null) && (tickers.length > 0)) {
                        exchangeSelected = exchangeSpinner.getSelectedItemPosition();
                        typeSelected = typeSpinner.getSelectedItemPosition();
                        countTickersSelected = countTickersSpinner.getSelectedItemPosition();
                        buildListTickers();
                    } else {
                        tickers = oldTickers;
                        Toast.makeText(getApplicationContext(),
                                R.string.emptyTickers, Toast.LENGTH_LONG).show();
                        showView();
                    }
                }

                @Override
                public void onFailure(Call<TopObject> call, Throwable t) {
                    ProgressDialogLoader.progressdialog_dismiss();
                    Log.d(Constants.TAG,getResources().getString(R.string.failureLoad) + t.getMessage());
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void  buildListTickers(){
        ProgressDialogLoader.progressdialog_creation(this, getResources().getString(R.string.loadingData));
        // Initial ticker array fill
        tickerList = new ArrayList<>();
        for(int i=0; i<tickers.length; i++) {
            Ticker t = new Ticker(tickers[i]);
            tickerList.add(t);
        }
        tickerListToShow = new ArrayList<>();

        tickerAdapter = new TickerAdapter(this, tickerListToShow);
        tickerRecylerView.setAdapter(tickerAdapter);

        // Start connection with the server
        try {
            mSocket = IO.socket(Constants.WS_SOCKETURL);
            setListening();
            mSocket.connect();
        } catch (URISyntaxException e) {
            Log.d(Constants.TAG, e.getMessage());
        }
    }

    private void showFlag(int selectedExchange) {
        switch (selectedExchange) {
            case 0: flagImageView.setImageResource(R.drawable.flag_rus); break;
            case 1: flagImageView.setImageResource(R.drawable.flag_kaz); break;
            case 2: flagImageView.setImageResource(R.drawable.flag_ukr); break;
            case 3: flagImageView.setImageResource(R.drawable.flag_usa); break;
            case 4: flagImageView.setImageResource(R.drawable.flag_eur); break;
            case 5: flagImageView.setImageResource(R.drawable.flag_cur); break;
        }
    }

    private void showView() {
        exchangeSpinner.setSelection(exchangeSelected);
        typeSpinner.setSelection(typeSelected);
        countTickersSpinner.setSelection(countTickersSelected);
        showFlag(exchangeSelected);
    }

    // Spinner's listener
    private AdapterView.OnItemSelectedListener exchangeListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent,
                                   View itemSelected, int selectedItemPosition, long selectedId) {
            showFlag(selectedItemPosition);

            String[] choose = getResources().getStringArray(R.array.exchangevalueslist);
            exchange = choose[selectedItemPosition];
            buildListTopTickers();
        }
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private AdapterView.OnItemSelectedListener typeListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent,
                                   View itemSelected, int selectedItemPosition, long selectedId) {
            String[] choose = getResources().getStringArray(R.array.typelist);
            type = choose[selectedItemPosition];
            buildListTopTickers();
        }
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private AdapterView.OnItemSelectedListener countTickersListener = new AdapterView.OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent,
                                   View itemSelected, int selectedItemPosition, long selectedId) {
            String[] choose = getResources().getStringArray(R.array.countTickers);
            String countString = choose[selectedItemPosition];
            limit = Integer.parseInt(countString);
            buildListTopTickers();
        }
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
}
