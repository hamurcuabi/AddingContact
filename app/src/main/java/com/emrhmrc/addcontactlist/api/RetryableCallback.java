package com.emrhmrc.addcontactlist.api;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class RetryableCallback<T> implements Callback<T> {

    private static final String TAG = "RetryableCallback";
    private final Call<T> call;
    private int totalRetries = 3;
    private int retryCount = 0;

    public RetryableCallback(Call<T> call, int totalRetries) {
        this.call = call;
        this.totalRetries = totalRetries;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (!APIHelper.isCallSuccess(response))
            if (retryCount++ < totalRetries) {
                Log.d(TAG, "Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
                Log.d(TAG, "onResponse ErrorBody " + response.message());
                retry();
            } else
                onFinalResponse(call, response);
        else
            onFinalResponse(call, response);
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        Log.d(TAG, "Fail");
        if (retryCount++ < totalRetries) {
            Log.d(TAG, "Retrying API Call -  (" + retryCount + " / " + totalRetries + ")");
            retry();
        } else
            onFinalFailure(call, t);
    }

    public void onFinalResponse(Call<T> call, Response<T> response) {

    }

    public void onFinalFailure(Call<T> call, Throwable t) {
    }

    private void retry() {
        call.clone().enqueue(this);
    }
}
