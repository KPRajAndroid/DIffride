package com.diff.user.app.FCM;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */


import android.util.Log;
import android.widget.Toast;

import com.diff.user.app.Helper.SharedHelper;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        SharedHelper.putKey(getApplicationContext(), "device_token", "" + refreshedToken);
        SharedHelper.putKey(getApplicationContext(), "device_token_new", "" + refreshedToken);
        Log.e(TAG, "" + refreshedToken);

    }
}