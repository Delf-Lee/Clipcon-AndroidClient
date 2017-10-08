package com.sprout.clipcon.transfer;

import android.util.Log;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
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

    public void commonRequest(String request) {
        Log.d("delf", "commonRequest test:" + request);
        RequestBody message = RequestBody.create(MediaType.parse("text/plain"), request);

        call = retrofitInterface.commonRequest(message);
        callResult(call);
    }

    /** logging method- check for a successful response */
    public void callResult(Call<ResponseBody> call) {
        Log.d("delf", "callResul");
       /* call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                System.out.println("Upload success");
                uploadCallback.onComplete();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable arg1) {
                System.out.println("Upload onFailure");
            }
        });*/
    }


}



