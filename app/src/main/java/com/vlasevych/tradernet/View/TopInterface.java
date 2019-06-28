package com.vlasevych.tradernet.View;

import com.vlasevych.tradernet.Utils.Constants;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import com.vlasevych.tradernet.View.TopObject;

public interface TopInterface {

    @GET(Constants.BASE_URL)
    Call<TopObject> loadTickers(@Query("q") JSONObject q);
}
