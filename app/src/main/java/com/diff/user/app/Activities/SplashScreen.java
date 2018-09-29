package com.diff.user.app.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.diff.app.BuildConfig;
import com.diff.user.app.DiffApplication;
import com.diff.user.app.Helper.ConnectionHelper;
import com.diff.user.app.Helper.SharedHelper;
import com.diff.user.app.Helper.URLHelper;
import com.diff.user.app.Models.AccessDetails;
import com.diff.app.R;
import com.diff.user.app.Utils.Utilities;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.diff.user.app.DiffApplication.trimMessage;


/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class SplashScreen extends AppCompatActivity {

    public Activity activity = SplashScreen.this;
    public Context context = SplashScreen.this;
    String TAG = "SplashActivity";
    ConnectionHelper helper;
    Boolean isInternet;
    String device_token, device_UDID;
    Handler handleCheckStatus;
    int retryCount = 0;
    AlertDialog alert;
    boolean isOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();

        //        getAccess();
        handleCheckStatus = new Handler();
        // check status every 3 sec

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        handleCheckStatus.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.w("Handler", "Called");
                if (helper.isConnectingToInternet()) {
                    if (SharedHelper.getKey(context, "loggedIn").equalsIgnoreCase(context.getResources().getString(R.string.True))) {
                        GetToken();
                        accessKeyAPI();
                    } else {
                        if (AccessDetails.demo_build) {
                            if (SharedHelper.getKey(SplashScreen.this, "access_username").equalsIgnoreCase("")
                                    && SharedHelper.getKey(SplashScreen.this, "access_password").equalsIgnoreCase(""))
                                GoToAccessActivity();
                            else accessKeyAPI();
                        } else accessKeyAPI();
                        handleCheckStatus.removeCallbacksAndMessages(null);
                    }
                    if (alert != null && alert.isShowing()) alert.dismiss();
                } else {
                    showDialog();
                    handleCheckStatus.postDelayed(this, 3000);
                }
            }
        }, 3000);
    }

    public void accessKeyAPI() {

        JSONObject object = new JSONObject();
        try {
            if (AccessDetails.demo_build) {
                object.put("username", SharedHelper.getKey(SplashScreen.this, "access_username"));
                object.put("accesskey", SharedHelper.getKey(SplashScreen.this, "access_password"));
            } else {
                object.put("username", AccessDetails.username);
                object.put("accesskey", AccessDetails.password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                AccessDetails.access_login, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                processResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String json;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) try {
                    JSONObject errorObj = new JSONObject(new String(response.data));

                    switch (response.statusCode) {
                        case 400:
                        case 405:
                        case 500:
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                            break;
                        case 401:
                            displayMessage(errorObj.optString("message"));
                            break;
                        case 422:

                            json = trimMessage(new String(response.data));
                            if (!json.equals("") && json != null) displayMessage(json);
                            else displayMessage(getString(R.string.please_try_again));
                            break;
                        case 503:
                            displayMessage(getString(R.string.server_down));
                            break;
                        default:
                            displayMessage(getString(R.string.please_try_again));
                            break;
                    }

                } catch (Exception e) {
                    displayMessage(getString(R.string.something_went_wrong));
                }
                else {
                    if (error instanceof NoConnectionError)
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    else if (error instanceof NetworkError)
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    else if (error instanceof TimeoutError)
                        displayMessage(getString(R.string.timed_out));
                }
                if (AccessDetails.demo_build) GoToAccessActivity();
                else GoToBeginActivity();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };

        DiffApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void processResponse(final JSONObject response) {
        try {
            AccessDetails accessDetails = new AccessDetails();
            AccessDetails.status = response.optBoolean("status");

            if (AccessDetails.status) {
                JSONArray jsonArrayData = response.optJSONArray("data");
                JSONObject jsonObjectData = jsonArrayData.optJSONObject(0);
                AccessDetails.id = jsonObjectData.optInt("id");
                AccessDetails.clientName = jsonObjectData.optString("client_name");
                AccessDetails.email = jsonObjectData.optString("email");
                AccessDetails.product = jsonObjectData.optString("product");
                AccessDetails.username = jsonObjectData.optString("username");
                SharedHelper.putKey(SplashScreen.this, "access_username", AccessDetails.username);
                AccessDetails.password = jsonObjectData.optString("password");
                SharedHelper.putKey(SplashScreen.this, "access_password", AccessDetails.password);
                AccessDetails.passport = jsonObjectData.optString("passport");
                SharedHelper.putKey(SplashScreen.this, "passport", AccessDetails.passport);
                AccessDetails.clientid = jsonObjectData.optInt("clientid");
                SharedHelper.putKey(SplashScreen.this, "clientid", "" + AccessDetails.clientid);
                AccessDetails.serviceurl = jsonObjectData.optString("serviceurl");
                SharedHelper.putKey(SplashScreen.this, "serviceurl", AccessDetails.serviceurl);
                AccessDetails.isActive = jsonObjectData.optInt("is_active");
                AccessDetails.createdAt = jsonObjectData.optString("created_at");
                AccessDetails.updatedAt = jsonObjectData.optString("updated_at");
                AccessDetails.isPaid = jsonObjectData.optInt("is_paid");
                AccessDetails.isValid = jsonObjectData.optInt("is_valid");

                JSONObject jsonObjectSettings = response.optJSONObject("setting");

                AccessDetails.siteTitle = jsonObjectSettings.optString("site_title");
                SharedHelper.putKey(SplashScreen.this, "app_name", AccessDetails.siteTitle);
                AccessDetails.siteLogo = jsonObjectSettings.optString("site_logo");
                AccessDetails.siteEmailLogo = jsonObjectSettings.optString("site_email_logo");
                AccessDetails.siteIcon = jsonObjectSettings.optString("site_icon");
                AccessDetails.site_icon = Utilities.drawableFromUrl(SplashScreen.this, AccessDetails.siteIcon);
                AccessDetails.siteCopyright = jsonObjectSettings.optString("site_copyright");
                AccessDetails.providerSelectTimeout = jsonObjectSettings.optString("provider_select_timeout");
                AccessDetails.providerSearchRadius = jsonObjectSettings.optString("provider_search_radius");
                AccessDetails.basePrice = jsonObjectSettings.optString("base_price");
                AccessDetails.pricePerMinute = jsonObjectSettings.optString("price_per_minute");
                AccessDetails.taxPercentage = jsonObjectSettings.optString("tax_percentage");
                AccessDetails.stripeSecretKey = jsonObjectSettings.optString("stripe_secret_key");
                AccessDetails.stripePublishableKey = jsonObjectSettings.optString("stripe_publishable_key");
                SharedHelper.putKey(SplashScreen.this, "stripe_publishable_key", AccessDetails.stripePublishableKey);
                AccessDetails.cash = jsonObjectSettings.optString("CASH");
                AccessDetails.card = jsonObjectSettings.optString("CARD");
                AccessDetails.manualRequest = jsonObjectSettings.optString("manual_request");
                AccessDetails.defaultLang = jsonObjectSettings.optString("default_lang");
                AccessDetails.currency = jsonObjectSettings.optString("currency");
                AccessDetails.distance = jsonObjectSettings.optString("distance");
                AccessDetails.scheduledCancelTimeExceed = jsonObjectSettings.optString("scheduled_cancel_time_exceed");
                AccessDetails.pricePerKilometer = jsonObjectSettings.optString("price_per_kilometer");
                AccessDetails.commissionPercentage = jsonObjectSettings.optString("commission_percentage");
                AccessDetails.storeLinkAndroid = jsonObjectSettings.optString("store_link_android");
                AccessDetails.storeLinkIos = jsonObjectSettings.optString("store_link_ios");
                AccessDetails.dailyTarget = jsonObjectSettings.optString("daily_target");
                AccessDetails.surgePercentage = jsonObjectSettings.optString("surge_percentage");
                AccessDetails.surgeTrigger = jsonObjectSettings.optString("surge_trigger");
                AccessDetails.demoMode = jsonObjectSettings.optString("demo_mode");
                AccessDetails.bookingPrefix = jsonObjectSettings.optString("booking_prefix");
                AccessDetails.sosNumber = jsonObjectSettings.optString("sos_number");
                AccessDetails.contactNumber = jsonObjectSettings.optString("contact_number");
                AccessDetails.contactEmail = jsonObjectSettings.optString("contact_email");
                AccessDetails.socialLogin = jsonObjectSettings.optString("social_login");

                if (AccessDetails.isValid == 1) {
                    if (SharedHelper.getKey(context, "loggedIn").equalsIgnoreCase(context.getResources().getString(R.string.True))) {
                        getProfile();
                    } else {

                        if (isOpened==false){
                            GoToBeginActivity();
                            isOpened = true;
                        }

                    }
                } else {
                    displayMessage(getResources().getString(R.string.demo_expired));
                }
            } else {
                displayMessage(response.optString("message"));

                if (AccessDetails.demo_build) {
                    GoToAccessActivity();
                } else {
                    GoToBeginActivity();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Toast.makeText(activity, "Notification clicked", Toast.LENGTH_SHORT).show();
    }

    public void getProfile() {
        retryCount++;
        Log.e("GetPostAPI", "" + AccessDetails.serviceurl + URLHelper.UserProfile + "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token);
        JSONObject object = new JSONObject();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, AccessDetails.serviceurl + URLHelper.UserProfile + "" +
                "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                SharedHelper.putKey(context, "id", response.optString("id"));
                SharedHelper.putKey(context, "first_name", response.optString("first_name"));
                SharedHelper.putKey(context, "last_name", response.optString("last_name"));
                SharedHelper.putKey(context, "email", response.optString("email"));
                SharedHelper.putKey(context, "mobile", response.optString("mobile"));

                if (response.optString("picture").startsWith("http"))
                    SharedHelper.putKey(context, "picture", response.optString("picture"));
                else
                    SharedHelper.putKey(context, "picture", AccessDetails.serviceurl + "/storage/" + response.optString("picture"));
                SharedHelper.putKey(context, "gender", response.optString("gender"));
                SharedHelper.putKey(context, "wallet_balance", response.optString("wallet_balance"));
                SharedHelper.putKey(context, "payment_mode", response.optString("payment_mode"));
                if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                    SharedHelper.putKey(context, "currency", response.optString("currency"));
                else
                    SharedHelper.putKey(context, "currency", "$");
                SharedHelper.putKey(context, "sos", response.optString("sos"));
                Log.e(TAG, "onResponse: Sos Call" + response.optString("sos"));
                SharedHelper.putKey(context, "loggedIn", context.getResources().getString(R.string.True));
                GoToMainActivity();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (retryCount < 5) {
                    getProfile();
                } else if (retryCount >= 5) {
//                        if (AccessDetails.username.equalsIgnoreCase("") && AccessDetails.passport.equalsIgnoreCase("")){
//                            GoToAccessActivity();
//                        }else{
                    GoToBeginActivity();
//                        }
                }
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {

                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(context.getResources().getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            refreshAccessToken();
                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(context.getResources().getString(R.string.please_try_again));
                            }

                        } else if (response.statusCode == 503) {
                            displayMessage(context.getResources().getString(R.string.server_down));
                        }
                    } catch (Exception e) {
                        displayMessage(context.getResources().getString(R.string.something_went_wrong));
                    }

                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        getProfile();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                return headers;
            }
        };

        DiffApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }


    @Override
    protected void onDestroy() {
        handleCheckStatus.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void refreshAccessToken() {
        JSONObject object = new JSONObject();
        try {
            object.put("grant_type", "refresh_token");
            object.put("client_id", AccessDetails.clientid);
            object.put("client_secret", AccessDetails.passport);
            object.put("refresh_token", SharedHelper.getKey(context, "refresh_token"));
            object.put("scope", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AccessDetails.serviceurl + URLHelper.login, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v("SignUpResponse", response.toString());
                SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
                SharedHelper.putKey(context, "token_type", response.optString("token_type"));
                getProfile();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {
                    SharedHelper.putKey(context, "loggedIn", context.getResources().getString(R.string.False));
//                        if (AccessDetails.username.equalsIgnoreCase("") && AccessDetails.passport.equalsIgnoreCase("")){
//                            GoToAccessActivity();
//                        }else{
                    GoToBeginActivity();
//                        }
                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(context.getResources().getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        refreshAccessToken();
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Requested-With", "XMLHttpRequest");
                return headers;
            }
        };

        DiffApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(activity, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        activity.finish();
    }

    public void GoToBeginActivity() {
        Intent mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void GoToAccessActivity() {
        Intent mainIntent = new Intent(activity, AccessKeyActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void displayMessage(String toastString) {
        Log.e("displayMessage", "" + toastString);
        Toast.makeText(activity, toastString, Toast.LENGTH_SHORT).show();
    }

    public void GetToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                Log.i(TAG, "GCM Registration Token: " + device_token);
            } else {
                device_token = "" + FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
                Log.i(TAG, "Failed to complete token refresh: " + device_token);
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            Log.i(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            Log.d(TAG, "Failed to complete device UDID");
        }
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(context.getResources().getString(R.string.connect_to_network))
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.connect_to_wifi), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.quit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        if (alert == null) {
            alert = builder.create();
            alert.show();
        }
    }

//
//    @Override
//    public void onUpdateNeeded(final String updateUrl) {
//        AlertDialog dialog = new AlertDialog.Builder(this)
//                .setTitle(getResources().getString(R.string.new_version_available))
//                .setMessage(getResources().getString(R.string.update_to_continue))
//                .setPositiveButton(getResources().getString(R.string.update),
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                redirectStore(updateUrl);
//                            }
//                        }).setNegativeButton(getResources().getString(R.string.no_thanks),
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                finish();
//                            }
//                        }).create();
//        dialog.show();
//    }
//
//    private void redirectStore(String updateUrl) {
//        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//    }

}
