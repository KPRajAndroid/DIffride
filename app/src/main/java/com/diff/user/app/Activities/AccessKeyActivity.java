package com.diff.user.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
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
import com.diff.user.app.Helper.SharedHelper;
import com.diff.user.app.Models.AccessDetails;
import com.diff.app.R;
import com.diff.user.app.Utils.Utilities;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.diff.user.app.DiffApplication.trimMessage;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class AccessKeyActivity extends AppCompatActivity {

    EditText txtAccessKey, txtUserName;

    FloatingActionButton btnAccessKey;

    LinearLayout lnrAccessLogin, lnrAccessLoading;

    TextView txtLoading;

    LottieAnimationView lnrAnimationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_begin);
        initView();
    }

    private void initView() {
        txtAccessKey = (EditText) findViewById(R.id.txtAccessKey);
        txtUserName = (EditText) findViewById(R.id.txtUserName);
        lnrAccessLogin = (LinearLayout) findViewById(R.id.lnrAccessLogin);
        lnrAccessLoading = (LinearLayout) findViewById(R.id.lnrAccessLoading);
        btnAccessKey = (FloatingActionButton) findViewById(R.id.btnAccessKey);
        txtLoading = (TextView) findViewById(R.id.txtLoading);
        lnrAnimationView = (LottieAnimationView) findViewById(R.id.lnrAnimationView);

        btnAccessKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtAccessKey.getText().toString().equalsIgnoreCase("")) {
                    displayMessage(getResources().getString(R.string.enter_access_key));
                } else {
                    accessKeyAPI();
                }
            }
        });
    }

    public void accessKeyAPI() {

        JSONObject object = new JSONObject();
        try {
            object.put("username", txtUserName.getText().toString());
            object.put("accesskey", txtAccessKey.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadingVisibility();

        txtLoading.setText(getResources().getString(R.string.fetching_server));

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AccessDetails.access_login, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                processResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;
                if (response != null && response.data != null) {

                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            txtLoading.setText(getResources().getString(R.string.server_error));
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            txtLoading.setText(getResources().getString(R.string.server_error));
                            displayMessage(errorObj.optString("message"));
                        } else if (response.statusCode == 422) {
                            txtLoading.setText(getResources().getString(R.string.server_error));
                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
                        } else if (response.statusCode == 503) {
                            txtLoading.setText(getResources().getString(R.string.server_error));
                            displayMessage(getString(R.string.server_down));
                        } else {
                            txtLoading.setText(getResources().getString(R.string.server_error));
                            displayMessage(getString(R.string.please_try_again));
                        }

                    } catch (Exception e) {
                        displayMessage(getString(R.string.something_went_wrong));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                lnrAccessLogin.setVisibility(View.VISIBLE);
                                lnrAccessLoading.setVisibility(View.GONE);
                            }
                        }, 2000);
                    }

                } else {
                    if (error instanceof NoConnectionError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof NetworkError) {
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    } else if (error instanceof TimeoutError) {
                        displayMessage(getString(R.string.timed_out));
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
    }

    private void processResponse(final JSONObject response) {
        try {
            AccessDetails accessDetails = new AccessDetails();
            accessDetails.status = response.optBoolean("status");

            if (accessDetails.status) {
                JSONArray jsonArrayData = response.optJSONArray("data");
                JSONObject jsonObjectData = jsonArrayData.optJSONObject(0);
                accessDetails.id = jsonObjectData.optInt("id");
                accessDetails.clientName = jsonObjectData.optString("client_name");
                accessDetails.email = jsonObjectData.optString("email");
                accessDetails.product = jsonObjectData.optString("product");
                accessDetails.username = jsonObjectData.optString("username");
                SharedHelper.putKey(AccessKeyActivity.this, "access_username", accessDetails.username);
                accessDetails.password = jsonObjectData.optString("password");
                SharedHelper.putKey(AccessKeyActivity.this, "access_password", accessDetails.password);
                accessDetails.passport = jsonObjectData.optString("passport");
                SharedHelper.putKey(AccessKeyActivity.this, "passport", accessDetails.passport);
                accessDetails.clientid = jsonObjectData.optInt("clientid");
                SharedHelper.putKey(AccessKeyActivity.this, "clientid", "" + accessDetails.clientid);
                accessDetails.serviceurl = jsonObjectData.optString("serviceurl");
                SharedHelper.putKey(AccessKeyActivity.this, "serviceurl", accessDetails.serviceurl);
                accessDetails.isActive = jsonObjectData.optInt("is_active");
                accessDetails.createdAt = jsonObjectData.optString("created_at");
                accessDetails.updatedAt = jsonObjectData.optString("updated_at");
                accessDetails.isPaid = jsonObjectData.optInt("is_paid");
                accessDetails.isValid = jsonObjectData.optInt("is_valid");

                JSONObject jsonObjectSettings = response.optJSONObject("setting");

                accessDetails.siteTitle = jsonObjectSettings.optString("site_title");
                SharedHelper.putKey(AccessKeyActivity.this, "app_name", accessDetails.siteTitle);
                accessDetails.siteLogo = jsonObjectSettings.optString("site_logo");
                accessDetails.siteEmailLogo = jsonObjectSettings.optString("site_email_logo");
                accessDetails.siteIcon = jsonObjectSettings.optString("site_icon");
                accessDetails.site_icon = Utilities.drawableFromUrl(AccessKeyActivity.this, accessDetails.siteIcon);
                accessDetails.siteCopyright = jsonObjectSettings.optString("site_copyright");
                accessDetails.providerSelectTimeout = jsonObjectSettings.optString("provider_select_timeout");
                accessDetails.providerSearchRadius = jsonObjectSettings.optString("provider_search_radius");
                accessDetails.basePrice = jsonObjectSettings.optString("base_price");
                accessDetails.pricePerMinute = jsonObjectSettings.optString("price_per_minute");
                accessDetails.taxPercentage = jsonObjectSettings.optString("tax_percentage");
                accessDetails.stripeSecretKey = jsonObjectSettings.optString("stripe_secret_key");
                accessDetails.stripePublishableKey = jsonObjectSettings.optString("stripe_publishable_key");
                SharedHelper.putKey(AccessKeyActivity.this, "stripe_publishable_key", accessDetails.stripePublishableKey);
                accessDetails.cash = jsonObjectSettings.optString("CASH");
                accessDetails.card = jsonObjectSettings.optString("CARD");
                accessDetails.manualRequest = jsonObjectSettings.optString("manual_request");
                accessDetails.defaultLang = jsonObjectSettings.optString("default_lang");
                accessDetails.currency = jsonObjectSettings.optString("currency");
                accessDetails.distance = jsonObjectSettings.optString("distance");
                accessDetails.scheduledCancelTimeExceed = jsonObjectSettings.optString("scheduled_cancel_time_exceed");
                accessDetails.pricePerKilometer = jsonObjectSettings.optString("price_per_kilometer");
                accessDetails.commissionPercentage = jsonObjectSettings.optString("commission_percentage");
                accessDetails.storeLinkAndroid = jsonObjectSettings.optString("store_link_android");
                accessDetails.storeLinkIos = jsonObjectSettings.optString("store_link_ios");
                accessDetails.dailyTarget = jsonObjectSettings.optString("daily_target");
                accessDetails.surgePercentage = jsonObjectSettings.optString("surge_percentage");
                accessDetails.surgeTrigger = jsonObjectSettings.optString("surge_trigger");
                accessDetails.demoMode = jsonObjectSettings.optString("demo_mode");
                accessDetails.bookingPrefix = jsonObjectSettings.optString("booking_prefix");
                accessDetails.sosNumber = jsonObjectSettings.optString("sos_number");
                accessDetails.contactNumber = jsonObjectSettings.optString("contact_number");
                accessDetails.contactEmail = jsonObjectSettings.optString("contact_email");
                accessDetails.socialLogin = jsonObjectSettings.optString("social_login");


                if (AccessDetails.isValid == 1) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            GoToBeginActivity();
                        }
                    }, 2000);
                } else {
                    displayMessage(getResources().getString(R.string.demo_expired));
                }
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lnrAccessLogin.setVisibility(View.VISIBLE);
                        lnrAccessLoading.setVisibility(View.GONE);
                        displayMessage(response.optString("message"));
                    }
                }, 2000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void GoToBeginActivity() {
        Intent mainIntent = new Intent(AccessKeyActivity.this, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void loadingVisibility() {
        lnrAccessLogin.setVisibility(View.GONE);
        lnrAccessLoading.setVisibility(View.VISIBLE);
    }


    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(AccessKeyActivity.this, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                e.printStackTrace();
            }
        }
    }
}
