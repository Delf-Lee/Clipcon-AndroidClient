package com.sprout.clipcon.transfer;

import android.util.Log;

import com.sprout.clipcon.model.Message;
import com.sprout.clipcon.server.BackgroundTaskHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by delf on 17-10-09.
 */

public class RetrofitCommonRequest {
    public Retrofit.Builder builder = new Retrofit.Builder().baseUrl(RetrofitInterface.BASE_URL).addConverterFactory(GsonConverterFactory.create());
    public Retrofit retrofit = builder.build();
    public RetrofitInterface retrofitInterface = retrofit.create(RetrofitInterface.class);
    public Call<ResponseBody> call = null;

    private String charset = "UTF-8";

    BackgroundTaskHandler.BackgroundCallback backgroundCallback;
    public RetrofitCommonRequest(BackgroundTaskHandler.BackgroundCallback backgroundCallback) {
        this.backgroundCallback = backgroundCallback;
    }

    public void commonRequest(String request) {
        Log.d("delf", "commonRequest test:" + request);
        RequestBody message = RequestBody.create(MediaType.parse("text/plain"), request);

        call = retrofitInterface.commonRequest(message);
        callResult(call);
    }

    /** logging method- check for a successful response */
    public void callResult(Call<ResponseBody> call) {
        Log.d("delf", "callResult");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Log.d("delf", "response.body(): " + getString(response.body().byteStream()));
                backgroundCallback.onSuccess(new Message().setJson(getString(response.body().byteStream())));
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable arg1) {
                System.out.println("Upload onFailure");
            }
        });
    }

    public String getString(InputStream is) {
        BufferedReader bufferedReader;
        StringBuilder stringBuilder = null;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(is, charset));

            stringBuilder = new StringBuilder();
            String line = null;

            try {
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        return stringBuilder.toString();
    }

}



