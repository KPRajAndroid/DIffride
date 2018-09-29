package com.diff.user.app.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.diff.user.app.DiffApplication;
import com.diff.user.app.Helper.AppHelper;
import com.diff.user.app.Helper.ConnectionHelper;
import com.diff.user.app.Helper.CustomDialog;
import com.diff.user.app.Helper.SharedHelper;
import com.diff.user.app.Helper.URLHelper;
import com.diff.user.app.Helper.VolleyMultipartRequest;
import com.diff.user.app.Models.AccessDetails;
import com.diff.app.R;
import com.diff.user.app.Utils.Utilities;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;
import com.google.android.gms.auth.api.signin.SignInAccount;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.diff.user.app.DiffApplication.trimMessage;


/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class RegisterActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    public static int APP_REQUEST_CODE = 99;
    public Context context = RegisterActivity.this;
    public Activity activity = RegisterActivity.this;
    String TAG = "RegisterActivity";
    String strViewPager = "";
    String device_token, device_UDID;
    ImageView backArrow;
    FloatingActionButton nextICON;
    EditText email, first_name, last_name, mobile_no, password, confirm_password, passport_number, referral_code;
    CustomDialog customDialog;
    ConnectionHelper helper;
    Boolean isInternet;
    Utilities utils = new Utilities();
    Boolean fromActivity = false;
    RadioGroup genderGrp;
    ImageView maleImg, femaleImg;
    String gender = "male";
    AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder;
    UIManager uiManager;

    byte[] Picture = null;
    byte[] PassportFront = null;
    byte[] PassportBack = null;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        pd = new ProgressDialog(this);
        pd.setMessage("please wait...");
        try {
            Intent intent = getIntent();
            if (intent != null) {
                if (getIntent().getExtras().containsKey("viewpager")) {
                    strViewPager = getIntent().getExtras().getString("viewpager");
                }
                if (getIntent().getExtras().getBoolean("isFromMailActivity")) {
                    fromActivity = true;
                } else if (!getIntent().getExtras().getBoolean("isFromMailActivity")) {
                    fromActivity = false;
                } else {
                    fromActivity = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fromActivity = false;
        }

        findViewById();
        GetToken();

        if (Build.VERSION.SDK_INT > 15) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        nextICON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Pattern ps = Pattern.compile(".*[0-9].*");
                Matcher firstName = ps.matcher(first_name.getText().toString());
                Matcher lastName = ps.matcher(last_name.getText().toString());

                Utilities.hideKeyboard(RegisterActivity.this);

                if (email.getText().toString().equals("") || email.getText().toString().equalsIgnoreCase(getString(R.string.sample_mail_id))) {
                    displayMessage(getString(R.string.email_validation));
                } else if (!Utilities.isValidEmail(email.getText().toString())) {
                    displayMessage(getString(R.string.not_valid_email));
                } else if (first_name.getText().toString().equals("") || first_name.getText().toString().equalsIgnoreCase(getString(R.string.first_name))) {
                    displayMessage(getString(R.string.first_name_empty));
                } else if (firstName.matches()) {
                    displayMessage(getString(R.string.first_name_no_number));
                } else if (password.getText().toString().equals("") || password.getText().toString().equalsIgnoreCase(getString(R.string.password_txt))) {
                    displayMessage(getString(R.string.password_validation));
                } else if (password.length() < 6 || password.length() > 16) {
                    displayMessage(getString(R.string.password_validation2));
                } else if (!Utilities.isValidPassword(password.getText().toString().trim())) {
                    displayMessage(getString(R.string.password_validation2));
                } else if (confirm_password.getText().toString().isEmpty()) {
                    displayMessage("Retype password could not be left empty");
                } else if (!password.getText().toString().equals(confirm_password.getText().toString())) {
                    displayMessage(getString(R.string.confirm_password_mismatched));
                } else if (passport_number.getText().toString().equals("") || passport_number.getText().toString().equalsIgnoreCase(getString(R.string.passport_number))) {
                    displayMessage(getString(R.string.passport_number_required));
                } else {

                    checkMailAlreadyExit();

                }
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
//                Intent mainIntent = new Intent(RegisterActivity.this, ActivityPassword.class);
//                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(mainIntent);
//                RegisterActivity.this.finish();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (customDialog != null && customDialog.isShowing())
            customDialog.dismiss();
    }

    public void findViewById() {
        email = (EditText) findViewById(R.id.email);
        first_name = (EditText) findViewById(R.id.first_name);
        last_name = (EditText) findViewById(R.id.last_name);
        //mobile_no = (EditText) findViewById(R.id.mobile_no);
        password = (EditText) findViewById(R.id.password);
        confirm_password = (EditText) findViewById(R.id.confirm_password);
        referral_code = (EditText) findViewById(R.id.referral_code);
        passport_number = (EditText) findViewById(R.id.passport_number);
        nextICON = (FloatingActionButton) findViewById(R.id.nextIcon);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        helper = new ConnectionHelper(context);

        genderGrp = (RadioGroup) findViewById(R.id.gender_group);
        genderGrp.setOnCheckedChangeListener(this);

        maleImg = (ImageView) findViewById(R.id.male_img);
        femaleImg = (ImageView) findViewById(R.id.female_img);

        maleImg.setColorFilter(ContextCompat.getColor(context, R.color.theme));
        femaleImg.setColorFilter(ContextCompat.getColor(context, R.color.calendar_selected_date_text));

        isInternet = helper.isConnectingToInternet();
        if (!fromActivity) {
            email.setText(SharedHelper.getKey(context, "email"));
        }

    }

    public void checkMailAlreadyExit() {
        customDialog = new CustomDialog(RegisterActivity.this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("email", email.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AccessDetails.serviceurl + URLHelper.CHECK_MAIL_ALREADY_REGISTERED,
                object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                phoneLogin();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                String json = null;
                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {
                    utils.print("MyTest", "" + error);
                    utils.print("MyTestError", "" + error.networkResponse);
                    utils.print("MyTestError1", "" + response.statusCode);
                    try {
                        if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                if (json.startsWith(getString(R.string.email_exist))) {
                                    displayMessage(getString(R.string.email_exist));
                                } else {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

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
                        //checkMailAlreadyExit();
                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show();
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

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000 * 10,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        DiffApplication.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    public void phoneLogin() {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        uiManager = new SkinManager(SkinManager.Skin.TRANSLUCENT,
                ContextCompat.getColor(this, R.color.grey), R.drawable.banner_fb, SkinManager.Tint.WHITE, 85);
        configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        configurationBuilder.setUIManager(uiManager);
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
                if (resultCode == RESULT_OK) {
                    AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            Log.e(TAG, "onSuccess: Account Kit" + account.getId());
                            Log.e(TAG, "onSuccess: Account Kit" + AccountKit.getCurrentAccessToken().getToken());
                            if (AccountKit.getCurrentAccessToken().getToken() != null) {
                                SharedHelper.putKey(RegisterActivity.this, "account_kit_token", AccountKit.getCurrentAccessToken().getToken());
                                //SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.True));
                                // Get phone number
                                PhoneNumber phoneNumber = account.getPhoneNumber();
                                String phoneNumberString = phoneNumber.toString();
                                SharedHelper.putKey(RegisterActivity.this, "mobile", phoneNumberString);

                                Intent i = new Intent(RegisterActivity.this, DocumentUploadActivity.class);
                                startActivityForResult(i, 1);

                                //registerAPI();
                            } else {
                                SharedHelper.putKey(RegisterActivity.this, "account_kit_token", "");
                                SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.False));
                                SharedHelper.putKey(context, "email", "");
                                SharedHelper.putKey(context, "login_by", "");
                                SharedHelper.putKey(RegisterActivity.this, "account_kit_token", "");
                                Intent goToLogin = new Intent(RegisterActivity.this, WelcomeScreenActivity.class);
                                goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(goToLogin);
                                finish();
                            }
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Log.e(TAG, "onError: Account Kit" + accountKitError);
//                        displayMessage(getResources().getString(R.string.social_cancel));
                        }
                    });
                    if (loginResult != null) {
                        SharedHelper.putKey(this, "account_kit", getString(R.string.True));
                    } else {
                        SharedHelper.putKey(this, "account_kit", getString(R.string.False));
                    }
                    if (loginResult.getError() != null) {
//                    displayMessage(getResources().getString(R.string.social_failed));
                    } else if (loginResult.wasCancelled()) {
//                    displayMessage(getResources().getString(R.string.social_cancel));
                    } else {
                        if (loginResult.getAccessToken() != null) {
                            Log.e(TAG, "onActivityResult: Account Kit" + loginResult.getAccessToken().toString());
                            SharedHelper.putKey(this, "account_kit", loginResult.getAccessToken().toString());
                        } else {
                            SharedHelper.putKey(this, "account_kit", "");
                        }
                    }
                } else {
                    AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
                    if (loginResult != null) {
                        SharedHelper.putKey(this, "account_kit", getString(R.string.True));
                    } else {
                        SharedHelper.putKey(this, "account_kit", getString(R.string.False));
                    }
                    if (loginResult.getError() != null) {
                    } else if (loginResult.wasCancelled()) {
                    } else {
                        if (loginResult.getAccessToken() != null) {
                            Log.e(TAG, "onActivityResult: Account Kit" + loginResult.getAccessToken().toString());
                            SharedHelper.putKey(this, "account_kit", loginResult.getAccessToken().toString());
                        } else {
                            SharedHelper.putKey(this, "account_kit", "");
                        }
                    }
                }
            }


            if (requestCode == 1) {
                if (resultCode == Activity.RESULT_OK) {
                    Picture = data.getByteArrayExtra("picture");
                    PassportFront = data.getByteArrayExtra("passport_front");
                    PassportBack = data.getByteArrayExtra("passport_back");
                    registerAPI();
                }
            }
        }
    }


    private void registerAPI() {

        if (!pd.isShowing()) {
            pd.show();
        }
        /*JSONObject object = new JSONObject();
        try {

            object.put("device_type", "android");
            object.put("device_id", device_UDID);
            object.put("device_token", "" + device_token);
            object.put("login_by", "manual");
            object.put("first_name", first_name.getText().toString());
            object.put("last_name", last_name.getText().toString());
            object.put("email", email.getText().toString());
            object.put("password", password.getText().toString());
            object.put("confirm_password", confirm_password.getText().toString());
            object.put("mobile", SharedHelper.getKey(RegisterActivity.this, "mobile"));
            object.put("referral_code", referral_code.getText().toString());
            object.put("picture", "");
            object.put("social_unique_id", "");
            object.put("gender", gender);
            object.put("pass_num", passport_number.getText().toString());

            utils.print("InputToRegisterAPI", "" + object);

        } catch (JSONException e) {
            e.printStackTrace();
        }*/

        VolleyMultipartRequest jsonObjectRequest = new VolleyMultipartRequest(Request.Method.POST, AccessDetails.serviceurl + URLHelper.register, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                pd.dismiss();
                utils.print("SignInResponse", response.toString());
                SharedHelper.putKey(RegisterActivity.this, "email", email.getText().toString());
                SharedHelper.putKey(RegisterActivity.this, "password", password.getText().toString());
                signIn();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                String json = null;
                String Message;
                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {
                    utils.print("MyTest", "" + error);
                    utils.print("MyTestError", "" + error.networkResponse);
                    utils.print("MyTestError1", "" + response.statusCode);
                    try {
                        JSONObject errorObj = new JSONObject(new String(response.data));

                        if (response.statusCode == 400 || response.statusCode == 405 || response.statusCode == 500) {
                            try {
                                displayMessage(errorObj.optString("message"));
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }
                        } else if (response.statusCode == 401) {
                            try {
                                if (errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                    //   Refresh token
                                } else {
                                    displayMessage(errorObj.optString("message"));
                                }
                            } catch (Exception e) {
                                displayMessage(getString(R.string.something_went_wrong));
                            }

                        } else if (response.statusCode == 422) {

                            json = trimMessage(new String(response.data));
                            if (json != "" && json != null) {
                                if (json.startsWith("The email has already been taken")) {
                                    displayMessage(getString(R.string.email_exist));
                                } else {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }
                                //displayMessage(json);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }

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
                        //registerAPI();
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

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();

                params.put("device_type", "android");
                params.put("device_id", device_UDID);
                params.put("device_token", "" + device_token);
                params.put("login_by", "manual");
                params.put("first_name", first_name.getText().toString());
                params.put("last_name", last_name.getText().toString());
                params.put("email", email.getText().toString());
                params.put("password", password.getText().toString());
                params.put("confirm_password", confirm_password.getText().toString());
                params.put("mobile", SharedHelper.getKey(RegisterActivity.this, "mobile"));
                params.put("referral_code", referral_code.getText().toString());
                //params.put("picture", "");
                params.put("social_unique_id", "");
                params.put("gender", gender);
                params.put("pass_num", passport_number.getText().toString());

                utils.print("InputToRegisterAPI", "" + params);


                return params;
            }

            @Override
            protected Map<String, VolleyMultipartRequest.DataPart> getByteData() throws AuthFailureError {
                Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();
                params.put("picture", new VolleyMultipartRequest.DataPart("Picture.jpg", Picture, "image/jpeg"));
                if (PassportFront != null) {
                    params.put("passport_front", new VolleyMultipartRequest.DataPart("PassportFront.jpg", PassportFront, "image/jpeg"));
                }
                if (PassportBack != null) {
                    params.put("passport_back", new VolleyMultipartRequest.DataPart("PassportBack.jpg", PassportBack, "image/jpeg"));
                }

                return params;
            }
        };
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                6000 * 20,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        DiffApplication.getInstance().addToRequestQueue(jsonObjectRequest);

    }

    private void GoToBeginActivity() {
        if (customDialog != null && customDialog.isShowing())
            customDialog.dismiss();
        Intent mainIntent = new Intent(RegisterActivity.this, ActivityEmail.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        RegisterActivity.this.finish();
    }

    public void signIn() {
        if (isInternet) {
            customDialog = new CustomDialog(RegisterActivity.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            try {
                object.put("grant_type", "password");
                object.put("client_id", AccessDetails.clientid);
                object.put("client_secret", AccessDetails.passport);
                object.put("username", SharedHelper.getKey(RegisterActivity.this, "email"));
                object.put("password", SharedHelper.getKey(RegisterActivity.this, "password"));
                object.put("scope", "");
                object.put("device_type", "android");
                object.put("device_id", device_UDID);
                object.put("device_token", device_token);
                utils.print("InputToLoginAPI", "" + object);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AccessDetails.serviceurl + URLHelper.login, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if ((customDialog != null) && (customDialog.isShowing()))
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
                    if ((customDialog != null) && (customDialog.isShowing()))
                        customDialog.dismiss();
                    String json = null;
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
                                try {
                                    if (errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                        //Call Refresh token
                                    } else if (errorObj.optString("message").equalsIgnoreCase("Waiting for approval")) {
                                        waitingDialog(errorObj.optString("message"));
                                    } else {
                                        displayMessage(errorObj.optString("message"));
                                    }
                                } catch (Exception e) {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }

                            } else if (response.statusCode == 422) {

                                json = trimMessage(new String(response.data));
                                if (json != "" && json != null) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(getString(R.string.please_try_again));
                                }

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
                            signIn();
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

    public void getProfile() {
        if (isInternet) {
            customDialog = new CustomDialog(RegisterActivity.this);
            customDialog.setCancelable(false);
            if (customDialog != null)
                customDialog.show();
            JSONObject object = new JSONObject();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, AccessDetails.serviceurl + URLHelper.UserProfile + "?device_type=android&device_id=" + device_UDID + "&device_token=" + device_token, object, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if ((customDialog != null) && (customDialog.isShowing()))
                        //customDialog.dismiss();
                        utils.print("GetProfile", response.toString());
                    SharedHelper.putKey(RegisterActivity.this, "id", response.optString("id"));
                    SharedHelper.putKey(RegisterActivity.this, "first_name", response.optString("first_name"));
                    SharedHelper.putKey(RegisterActivity.this, "last_name", response.optString("last_name"));
                    SharedHelper.putKey(RegisterActivity.this, "email", response.optString("email"));
                    SharedHelper.putKey(RegisterActivity.this, "picture", response.optString("picture"));
                    SharedHelper.putKey(RegisterActivity.this, "gender", response.optString("gender"));
                    SharedHelper.putKey(RegisterActivity.this, "mobile", response.optString("mobile"));
                    SharedHelper.putKey(RegisterActivity.this, "wallet_balance", response.optString("wallet_balance"));
                    SharedHelper.putKey(RegisterActivity.this, "payment_mode", response.optString("payment_mode"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    SharedHelper.putKey(context, "sos", response.optString("sos"));
                    SharedHelper.putKey(RegisterActivity.this, "loggedIn", getString(R.string.True));

                    GoToMainActivity();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if ((customDialog != null) && (customDialog.isShowing()))
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
                                try {
                                    if (errorObj.optString("message").equalsIgnoreCase("invalid_token")) {
                                        refreshAccessToken();
                                    } else {
                                        displayMessage(errorObj.optString("message"));
                                    }
                                } catch (Exception e) {
                                    displayMessage(getString(R.string.something_went_wrong));
                                }

                            } else if (response.statusCode == 422) {

                                json = trimMessage(new String(response.data));
                                if (json != "" && json != null) {
                                    displayMessage(json);
                                } else {
                                    displayMessage(getString(R.string.please_try_again));
                                }

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
                    headers.put("Authorization", "" + SharedHelper.getKey(RegisterActivity.this, "token_type") + " " + SharedHelper.getKey(RegisterActivity.this, "access_token"));
                    return headers;
                }
            };

            DiffApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }
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

    }

    public void GetToken() {
        try {
            if (!SharedHelper.getKey(context, "device_token").equals("") && SharedHelper.getKey(context, "device_token") != null) {
                device_token = SharedHelper.getKey(context, "device_token");
                utils.print(TAG, "GCM Registration Token: " + device_token);
            } else {
                device_token = "" + FirebaseInstanceId.getInstance().getToken();
                SharedHelper.putKey(context, "device_token", "" + FirebaseInstanceId.getInstance().getToken());
                utils.print(TAG, "Failed to complete token refresh: " + device_token);
            }
        } catch (Exception e) {
            device_token = "COULD NOT GET FCM TOKEN";
            utils.print(TAG, "Failed to complete token refresh");
        }

        try {
            device_UDID = android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            utils.print(TAG, "Device UDID:" + device_UDID);
        } catch (Exception e) {
            device_UDID = "COULD NOT GET UDID";
            e.printStackTrace();
            utils.print(TAG, "Failed to complete device UDID");
        }
    }


    public void GoToMainActivity() {
        if (customDialog != null && customDialog.isShowing())
            customDialog.dismiss();
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        RegisterActivity.this.finish();
    }

    public void displayMessage(String toastString) {
        utils.print("displayMessage", "" + toastString);
        try {

            Snackbar snackbar = Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_LONG).setDuration(Snackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            TextView tv = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            tv.setMaxLines(3);
            snackbar.show();

//            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
//                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onBackPressed() {
        if (strViewPager.equalsIgnoreCase("yes")) {
            super.onBackPressed();
        } else {
            if (fromActivity) {
                Intent mainIntent = new Intent(RegisterActivity.this, ActivityEmail.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                RegisterActivity.this.finish();
            } else if (!fromActivity) {
                Intent mainIntent = new Intent(RegisterActivity.this, ActivityPassword.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                RegisterActivity.this.finish();
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.male_btn:
                gender = "male";
                maleImg.setColorFilter(ContextCompat.getColor(context, R.color.theme));
                femaleImg.setColorFilter(ContextCompat.getColor(context, R.color.calendar_selected_date_text));
                break;
            case R.id.female_btn:
                gender = "female";
                femaleImg.setColorFilter(ContextCompat.getColor(context, R.color.theme));
                maleImg.setColorFilter(ContextCompat.getColor(context, R.color.calendar_selected_date_text));
                break;
        }
    }

    void waitingDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        GoToBeginActivity();
                    }
                }).show();
    }
}
