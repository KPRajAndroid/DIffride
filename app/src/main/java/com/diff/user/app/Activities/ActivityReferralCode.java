package com.diff.user.app.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.diff.user.app.DiffApplication;
import com.diff.user.app.Helper.ConnectionHelper;
import com.diff.user.app.Helper.CustomDialog;
import com.diff.user.app.Helper.SharedHelper;
import com.diff.user.app.Helper.URLHelper;
import com.diff.user.app.Models.AccessDetails;
import com.diff.app.R;
import com.diff.user.app.Utils.Utilities;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.diff.user.app.DiffApplication.trimMessage;

public class ActivityReferralCode extends AppCompatActivity {

    String TAG = "ActivityReferralCode";
    ImageView select_contact, backArrow,select_share;
    Button invite_friends;
    Context context = ActivityReferralCode.this;
    TextView referal_code_id, referal_amount_txt,content;
    String device_token, device_UDID;
    Utilities utils = new Utilities();
    Boolean isInternet;
    ConnectionHelper helper;
    CustomDialog customDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_referral_code);
        select_contact = findViewById(R.id.select_contact);
        backArrow = findViewById(R.id.backArrow);
        invite_friends = findViewById(R.id.invite_friends);
        referal_amount_txt = findViewById(R.id.referal_amount_txt);
        content = findViewById(R.id.content);
        referal_code_id = findViewById(R.id.referal_code_id);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        GetToken();
        getProfile();



//        select_contact.setOnClickListener(this);
//        invite_friends.setOnClickListener(this);

        invite_friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToShareScreen(referal_code_id.getText().toString());
            }
        });


        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    public void navigateToShareScreen(String shareUrl) {
        try {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl + " -via " + AccessDetails.siteTitle);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Share applications not found!", Toast.LENGTH_SHORT).show();
        }

    }

    public void GetToken() {
        try {
            if(!SharedHelper.getKey(context,"device_token").equals("") && SharedHelper.getKey(context,"device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                Log.i(TAG, "GCM Registration Token: " + device_token);
            }else{
                device_token = ""+ FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token",""+FirebaseInstanceId.getInstance().getToken());
                Log.i(TAG, "Failed to complete token refresh: " + device_token);
            }
        }catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            Log.i(TAG, "Device UDID:" + device_UDID);
        }catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            Log.d(TAG, "Failed to complete device UDID");
        }
    }

    public void getProfile() {
        if (isInternet) {
            customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, AccessDetails.serviceurl + URLHelper.UserProfile + "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if ((customDialog != null) && customDialog.isShowing())
                        customDialog.dismiss();
                    utils.print("GetProfile", response.toString());
                    SharedHelper.putKey(context, "id", response.optString("id"));
                    SharedHelper.putKey(context, "first_name", response.optString("first_name"));
                    SharedHelper.putKey(context, "last_name", response.optString("last_name"));
                    SharedHelper.putKey(context, "email", response.optString("email"));
                    if (response.optString("picture").startsWith("http"))
                        SharedHelper.putKey(context, "picture", response.optString("picture"));
                    else
                        SharedHelper.putKey(context, "picture", AccessDetails.serviceurl + "/storage/" + response.optString("picture"));
                    SharedHelper.putKey(context, "gender", response.optString("gender"));
                    SharedHelper.putKey(context, "mobile", response.optString("mobile"));
                    SharedHelper.putKey(context, "wallet_balance", response.optString("wallet_balance"));
                    SharedHelper.putKey(context, "payment_mode", response.optString("payment_mode"));
                    SharedHelper.putKey(context, "referral_earning", response.optString("referral_earning"));
                    SharedHelper.putKey(context, "referral_code", response.optString("referral_code"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    SharedHelper.putKey(context, "sos", response.optString("sos"));
                    SharedHelper.putKey(context, "loggedIn", getString(R.string.True));
//                    GoToMainActivity();

                    content.setText("Give the Referral bonus by reffering the invite code worth "+ SharedHelper.getKey(context, "currency")+"10.");

                    if (SharedHelper.getKey(context, "referral_code") != null && !SharedHelper.getKey(context, "referral_code").equalsIgnoreCase("null") && !SharedHelper.getKey(context, "referral_code").equalsIgnoreCase("") ) {
                        referal_code_id.setText(SharedHelper.getKey(context, "referral_code"));
                    } else {
                        referal_code_id.setText("Not Available");
                    }

                    if (SharedHelper.getKey(context, "referral_earning") != null && !SharedHelper.getKey(context, "referral_earning").equalsIgnoreCase("null")) {
                        referal_amount_txt.setText(SharedHelper.getKey(context, "currency") + SharedHelper.getKey(context, "referral_earning"));
                    } else {
                        referal_amount_txt.setText(SharedHelper.getKey(context, "currency") + "0");
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if ((customDialog != null) && customDialog.isShowing())
                        customDialog.dismiss();
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
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                            } else if (response.statusCode == 401) {
                                refreshAccessToken();
                            } else if (response.statusCode == 422) {

                                json = trimMessage(new String(response.data));
                                if (json != "" && json != null) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(getString(R.string.please_try_again));
                                }

                            } else if (response.statusCode == 503) {
                                displayMessage(getString(R.string.server_down));
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

                        } catch (Exception e) {
                            displayMessage(getString(R.string.something_went_wrong));
                        }

                    } else {
                        if (error instanceof NoConnectionError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof NetworkError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof TimeoutError) {
                            getProfile();
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " "
                            + SharedHelper.getKey(context, "access_token"));
                    utils.print("authoization", "" + SharedHelper.getKey(context, "token_type") + " "
                            + SharedHelper.getKey(context, "access_token"));
                    return headers;
                }
            };

            DiffApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }

    }

    private void refreshAccessToken() {
        if (isInternet) {
            customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
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
                    if ((customDialog != null) && customDialog.isShowing())
                        customDialog.dismiss();
                    utils.print("SignUpResponse", response.toString());
                    SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                    SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
                    SharedHelper.putKey(context, "token_type", response.optString("token_type"));
                    getProfile();


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if ((customDialog != null) && customDialog.isShowing())
                        customDialog.dismiss();
                    String json = null;
                    String Message;
                    NetworkResponse response = error.networkResponse;
                    utils.print("MyTest", "" + error);
                    utils.print("MyTestError", "" + error.networkResponse);
                    utils.print("MyTestError1", "" + response.statusCode);

                    if (response != null && response.data != null) {
                        SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                        GoToBeginActivity();
                    } else {
                        if (error instanceof NoConnectionError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof NetworkError) {
                            displayMessage(getString(R.string.oops_connect_your_internet));
                        } else if (error instanceof TimeoutError) {
                            refreshAccessToken();
                        }
                    }
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Requested-With", "XMLHttpRequest");
                    return headers;
                }
            };

            DiffApplication.getInstance().addToRequestQueue(jsonObjectRequest);

        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }

    }

    public void displayMessage(String toastString) {
        utils.print("displayMessage", "" + toastString);
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }
    }

    public void GoToBeginActivity() {
        Intent mainIntent;
        if (AccessDetails.demo_build) {
            mainIntent = new Intent(context, AccessKeyActivity.class);
        } else {
            mainIntent = new Intent(context, WelcomeScreenActivity.class);
        }
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
        finish();
    }

}
