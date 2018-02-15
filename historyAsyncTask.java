package com.example.ollie.fitnessapp;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;

import java.util.concurrent.TimeUnit;

import static com.example.ollie.fitnessapp.MainActivity.TAG;

/**
 * Created by Ollie on 12/02/2018.
 */

public class historyAsyncTask extends AsyncTask<GoogleApiClient, Void, Void> {
    protected Void doInBackground(GoogleApiClient... params) {

        long total = 0;

        PendingResult<DailyTotalResult> result = Fitness.HistoryApi.readDailyTotal(params[0], DataType.TYPE_STEP_COUNT_CUMULATIVE);
        DailyTotalResult totalResult = result.await(30, TimeUnit.SECONDS);
        if (totalResult.getStatus().isSuccess()) {
            DataSet totalSet = totalResult.getTotal();
            total = totalSet.isEmpty()
                    ? 0
                    : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
        } else {
            Log.w(TAG, "There was a problem getting the step count.");
        }

        Log.i(TAG, "Total steps: " + total);

        return null;
    }
}
