package com.diff.user.app.Activities;

import android.Manifest;
import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.bumptech.glide.Glide;
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
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.splunk.mint.Mint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

import static com.diff.user.app.DiffApplication.trimMessage;


/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class ActivitySocialLogin extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int REQ_SIGN_IN_REQUIRED = 100;
    private static final int RC_SIGN_IN = 100;
    public static int APP_REQUEST_CODE = 99;
    public Context context = ActivitySocialLogin.this;
    /*----------Facebook Login---------------*/
    CallbackManager callbackManager;
    ImageView backArrow;
    AccessTokenTracker accessTokenTracker;
    String UserName, UserEmail, result, FBUserID, FBImageURLString;
    JSONObject json;
    ConnectionHelper helper;
    Boolean isInternet;
    LinearLayout facebook_layout;
    LinearLayout google_layout;
    CustomDialog customDialog;
    public static String TAG = "ActivitySocialLogin";
    String device_token, device_UDID;
    Utilities utils = new Utilities();
    /*----------Google Login---------------*/
    GoogleApiClient mGoogleApiClient;
    UIManager uiManager;
    String accessToken = "";
    String loginBy = "";
    String mobileNumber = "";
    EditText first_name, passport_number, referral_code;
    byte[] PassportFront = null;
    byte[] PassportBack = null;
    Boolean isPermissionGivenAlready = false;

    @BindView(R.id.passport_front)
    ImageView passportFront;
    @BindView(R.id.passport_back)
    ImageView passportBack;
    @BindView(R.id.social_layout)
    LinearLayout social_layout;
    @BindView(R.id.login_details)
    LinearLayout login_details;

    ImageView currentImageView;
    private static final int SELECT_PHOTO = 100;
    public static int deviceHeight;
    public static int deviceWidth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        Mint.setApplicationEnvironment(Mint.appEnvironmentStaging);
        Mint.initAndStartSession(this.getApplication(), "3c1d6462");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_social);
        helper = new ConnectionHelper(ActivitySocialLogin.this);
        isInternet = helper.isConnectingToInternet();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        deviceHeight = displayMetrics.heightPixels;
        deviceWidth = displayMetrics.widthPixels;
        ButterKnife.bind(this);
        GetToken();

        facebook_layout = (LinearLayout) findViewById(R.id.facebook_layout);
        google_layout = (LinearLayout) findViewById(R.id.google_layout);
        backArrow = (ImageView) findViewById(R.id.backArrow);
        first_name = (EditText) findViewById(R.id.first_name);
        referral_code = (EditText) findViewById(R.id.referral_code);
        passport_number = (EditText) findViewById(R.id.passport_number);

        /*----------Google Login---------------*/

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                //taken from google api console (Web api client id)
//                .requestIdToken("795253286119-p5b084skjnl7sll3s24ha310iotin5k4.apps.googleusercontent.com")
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        google_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



        /*----------Facebook Login---------------*/

        callbackManager = CallbackManager.Factory.create();
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {
            @SuppressLint("PackageManagerGetSignatures") PackageInfo info = getPackageManager().getPackageInfo(getPackageName(),
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

        }

//        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));

        facebook_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isInternet) {
                    LoginManager.getInstance().logInWithReadPermissions(ActivitySocialLogin.this,
                            Arrays.asList("public_profile", "email"));


                    LoginManager.getInstance().registerCallback(callbackManager,
                            new FacebookCallback<LoginResult>() {

                                public void onSuccess(LoginResult loginResult) {
                                    if (AccessToken.getCurrentAccessToken() != null) {
                                        Log.i("loginresult", "" + loginResult.getAccessToken().getToken());
                                        SharedHelper.putKey(ActivitySocialLogin.this, "accessToken", loginResult.getAccessToken().getToken());
                                        accessToken = loginResult.getAccessToken().getToken();
                                        loginBy = "facebook";
                                        phoneLogin();
                                    } else {
                                        displayMessage(getString(R.string.something_went_wrong));
                                    }
                                }

                                @Override
                                public void onCancel() {
                                    // App code
                                    displayMessage(getResources().getString(R.string.fb_cancel));
                                }

                                @Override
                                public void onError(FacebookException exception) {
                                    // App code
                                    displayMessage(getResources().getString(R.string.fb_error));
                                }
                            });
                } else {
                    //mProgressDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActivitySocialLogin.this);
                    builder.setMessage("Check your Internet").setCancelable(false);
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent NetworkAction = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(NetworkAction);

                        }
                    });
                    builder.show();
                }
            }
        });
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                // Set the access token using
                // currentAccessToken when it's loaded or set.
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (data != null) {
            // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
            }

            if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request

                if (resultCode == RESULT_OK) {

                    AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);

                    AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                        @Override
                        public void onSuccess(Account account) {
                            try {
                                Log.e(TAG, "onSuccess: Account Kit" + account.getId());
                                Log.e(TAG, "onSuccess: Account Kit" + AccountKit.getCurrentAccessToken().getToken());

                                if (AccountKit.getCurrentAccessToken().getToken() != null) {
                                    SharedHelper.putKey(ActivitySocialLogin.this, "account_kit_token", AccountKit.getCurrentAccessToken().getToken());
                                    mobileNumber = account.getPhoneNumber().toString();

                                    social_layout.setVisibility(View.GONE);
                                    login_details.setVisibility(View.VISIBLE);
                                    /*if (loginBy.equalsIgnoreCase("facebook")) {
                                        login(accessToken, AccessDetails.serviceurl + URLHelper.FACEBOOK_LOGIN, "facebook");
                                    } else {
                                        login(accessToken, AccessDetails.serviceurl + URLHelper.GOOGLE_LOGIN, "google");
                                    }*/
                                    //GoToMainActivity();
                                } else {
                                    SharedHelper.putKey(ActivitySocialLogin.this, "account_kit_token", "");
                                    SharedHelper.putKey(ActivitySocialLogin.this, "loggedIn", getString(R.string.False));
                                    SharedHelper.putKey(context, "email", "");
                                    SharedHelper.putKey(context, "login_by", "");
                                    SharedHelper.putKey(ActivitySocialLogin.this, "account_kit_token", "");
                                    Intent goToLogin;
                                    if (AccessDetails.demo_build) {
                                        goToLogin = new Intent(ActivitySocialLogin.this, AccessKeyActivity.class);
                                    } else {
                                        goToLogin = new Intent(ActivitySocialLogin.this, WelcomeScreenActivity.class);
                                    }
                                    goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(goToLogin);
                                    finish();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(AccountKitError accountKitError) {
                            Log.e(TAG, "onError: Account Kit" + accountKitError);
//                        displayMessage(getResources().getString(R.string.social_cancel));
                        }
                    });

                } else {
                    AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
                    if (loginResult != null) {
                        SharedHelper.putKey(this, "account_kit", getString(R.string.True));
                    } else {
                        SharedHelper.putKey(this, "account_kit", getString(R.string.False));
                    }
                    if (loginResult.getError() != null) {
//                        displayMessage(getResources().getString(R.string.social_failed));
                    } else if (loginResult.wasCancelled()) {
//                        displayMessage(getResources().getString(R.string.social_cancel));
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


            /*if (requestCode == SELECT_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {

                Uri uri = data.getData();
                Bitmap bitmap = null;

                try {
                    Bitmap resizeImg = getBitmapFromUri(this, uri);
                    if (resizeImg != null) {
                        Bitmap reRotateImg = AppHelper.modifyOrientation(resizeImg, AppHelper.getPath(this, uri));
                        currentImageView.setImageBitmap(reRotateImg);
                        if (currentImageView.getTag().equals("passport_front")) {
                            InputStream iStream = getContentResolver().openInputStream(uri);
                            PassportFront = getBytes(iStream);
                        }
                        if (currentImageView.getTag().equals("passport_back")) {
                            InputStream iStream = getContentResolver().openInputStream(uri);
                            PassportBack = getBytes(iStream);
                        }
                        //profile_Image.setImageBitmap(reRotateImg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/
        }

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
            }

            @Override
            public void onImagesPicked(@NonNull List<File> imageFiles, EasyImage.ImageSource source, int type) {
                if(imageFiles.size() > 0){
                    File imgFile = imageFiles.get(0);
                    if(imgFile.exists()) {
                        Glide.with(ActivitySocialLogin.this).load(Uri.fromFile(imgFile)).into(currentImageView);
                    }
                }
            }
        });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        try {
            Log.d("Beginscreen", "handleSignInResult:" + result.isSuccess());
            if (result.isSuccess()) {
                // Signed in successfully, show authenticated UI.
                GoogleSignInAccount acct = result.getSignInAccount();
                Log.d("Google", "display_name:" + acct.getDisplayName());
                Log.d("Google", "mail:" + acct.getEmail());
                Log.d("Google", "photo:" + acct.getPhotoUrl());

                new RetrieveTokenTask().execute(acct.getEmail());
            } else {
                displayMessage(getResources().getString(R.string.google_login));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ((customDialog != null) && customDialog.isShowing())
            customDialog.dismiss();
    }

    public void login(final String accesstoken, final String URL, final String Loginby) {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        final JsonObject json = new JsonObject();
        json.addProperty("device_type", "android");
        json.addProperty("device_token", device_token);
        json.addProperty("accessToken", accesstoken);
        json.addProperty("device_id", device_UDID);
        json.addProperty("login_by", Loginby);
        json.addProperty("mobile", mobileNumber);
        Log.e(TAG, "login: Facebook" + json);
        Ion.with(ActivitySocialLogin.this)
                .load(URL)
                .addHeader("X-Requested-With", "XMLHttpRequest")
//                .addHeader("Authorization",""+SharedHelper.getKey(context, "token_type")+" "+SharedHelper.getKey(context, "access_token"))
                .setJsonObjectBody(json)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if ((customDialog != null) && customDialog.isShowing())
                            customDialog.dismiss();
                        if (e != null) {
                            if (e instanceof NetworkErrorException) {
                                displayMessage(getString(R.string.oops_connect_your_internet));
                            } else if (e instanceof TimeoutException) {
                                login(accesstoken, URL, Loginby);
                            } else {
                                displayMessage(getString(R.string.please_try_again));
                            }
                            return;
                        }
                        if (result != null) {
                            Log.v(Loginby + "_Response", result.toString());
                            try {
                                JSONObject jsonObject = new JSONObject(result.toString());
                                String status = jsonObject.optString("status");
                                if (status.equalsIgnoreCase("true")) {
                                    SharedHelper.putKey(ActivitySocialLogin.this, "token_type", jsonObject.optString("token_type"));
                                    SharedHelper.putKey(ActivitySocialLogin.this, "access_token", jsonObject.optString("access_token"));
                                    if (Loginby.equalsIgnoreCase("facebook"))
                                        SharedHelper.putKey(ActivitySocialLogin.this, "login_by", "facebook");
                                    if (Loginby.equalsIgnoreCase("google"))
                                        SharedHelper.putKey(ActivitySocialLogin.this, "login_by", "google");

                                    if (!jsonObject.optString("currency").equalsIgnoreCase("") && jsonObject.optString("currency") != null)
                                        SharedHelper.putKey(context, "currency", jsonObject.optString("currency"));
                                    else
                                        SharedHelper.putKey(context, "currency", "$");
                                    //phoneLogin();
                                    getProfile();
                                } else {
                                    JSONObject errorObject = new JSONObject(result.toString());
                                    String strMessage = errorObject.optString("message");
                                    displayMessage(strMessage);
                                    GoToBeginActivity();
                                }

                            } catch (JSONException e1) {
                                e1.printStackTrace();
                                if (Loginby.equalsIgnoreCase("facebook")) {
                                    displayMessage(getResources().getString(R.string.fb_error));
                                } else {
                                    displayMessage(getResources().getString(R.string.google_login));
                                }
                            }
                        } else {
                            displayMessage(getString(R.string.please_try_again));
                        }
                        // onBackPressed();
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
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
                    Log.v("SignUpResponse", response.toString());
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
                    Log.e("MyTest", "" + error);
                    Log.e("MyTestError", "" + error.networkResponse);
                    Log.e("MyTestError1", "" + response.statusCode);

                    if (response != null && response.data != null) {
                        SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                        GoToBeginActivity();
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

    public void phoneLogin() {
        Log.e(TAG, "onActivityResult: phone Login Account Kit" + AccountKit.getCurrentAccessToken() + "");
        final Intent intent = new Intent(this, AccountKitActivity.class);

        uiManager = new SkinManager(SkinManager.Skin.TRANSLUCENT,
                ContextCompat.getColor(this, R.color.grey), R.drawable.banner_fb, SkinManager.Tint.WHITE, 85);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        configurationBuilder.setUIManager(uiManager);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    public void GoToBeginActivity() {
        Intent mainIntent;
        if (AccessDetails.demo_build) {
            mainIntent = new Intent(ActivitySocialLogin.this, AccessKeyActivity.class);
        } else {
            mainIntent = new Intent(ActivitySocialLogin.this, WelcomeScreenActivity.class);
        }
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
        finish();
    }

    public void displayMessage(String toastString) {

        Log.e("displayMessage", "" + toastString);
        try {
            Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
        }
    }

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        finish();
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
                    Log.v("GetProfile", response.toString());
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
                    SharedHelper.putKey(context, "refer_code", response.optString("refer_code"));
                    SharedHelper.putKey(context, "wallet_balance", response.optString("wallet_balance"));
                    SharedHelper.putKey(context, "payment_mode", response.optString("payment_mode"));
                    if (!response.optString("currency").equalsIgnoreCase("") && response.optString("currency") != null)
                        SharedHelper.putKey(context, "currency", response.optString("currency"));
                    else
                        SharedHelper.putKey(context, "currency", "$");
                    SharedHelper.putKey(context, "sos", response.optString("sos"));
                    SharedHelper.putKey(context, "loggedIn", getString(R.string.True));
                    GoToMainActivity();
                    //phoneLogin();
/*
                    if (SharedHelper.getKey(ActivitySocialLogin.this,"account_kit").equalsIgnoreCase(getString(R.string.True))) {
                    }else {
                        GoToMainActivity();
                    }*/
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
                    headers.put("Authorization", "" + SharedHelper.getKey(context, "token_type") + " " + SharedHelper.getKey(context, "access_token"));
                    return headers;
                }
            };

            DiffApplication.getInstance().addToRequestQueue(jsonObjectRequest);
        } else {
            displayMessage(getString(R.string.something_went_wrong_net));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        accessTokenTracker.stopTracking();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class RetrieveTokenTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String accountName = params[0];
            String scopes = "oauth2:profile email";
            String token = null;
            try {
                token = GoogleAuthUtil.getToken(getApplicationContext(), accountName, scopes);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_SIGN_IN_REQUIRED);
            } catch (GoogleAuthException e) {
                Log.e(TAG, e.getMessage());
            }
            return token;
        }

        @Override
        protected void onPostExecute(String GoogleaccessToken) {
            super.onPostExecute(GoogleaccessToken);
            Log.e("Token", GoogleaccessToken);
            accessToken = GoogleaccessToken;
            loginBy = "google";
            phoneLogin();
        }
    }


    private void loginAPI(final String accesstoken, final String URL, final String Loginby) {

        customDialog = new CustomDialog(ActivitySocialLogin.this);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();

        VolleyMultipartRequest jsonObjectRequest = new VolleyMultipartRequest(Request.Method.POST, URL, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();

                String res = new String(response.data);

                try {
                    JSONObject jsonObject = new JSONObject(res);
                    String status = jsonObject.optString("status");
                    if (status.equalsIgnoreCase("true")) {
                        SharedHelper.putKey(ActivitySocialLogin.this, "token_type", jsonObject.optString("token_type"));
                        SharedHelper.putKey(ActivitySocialLogin.this, "access_token", jsonObject.optString("access_token"));
                        if (Loginby.equalsIgnoreCase("facebook"))
                            SharedHelper.putKey(ActivitySocialLogin.this, "login_by", "facebook");
                        if (Loginby.equalsIgnoreCase("google"))
                            SharedHelper.putKey(ActivitySocialLogin.this, "login_by", "google");

                        if (!jsonObject.optString("currency").equalsIgnoreCase("") && jsonObject.optString("currency") != null)
                            SharedHelper.putKey(context, "currency", jsonObject.optString("currency"));
                        else
                            SharedHelper.putKey(context, "currency", "$");
                        //phoneLogin();
                        getProfile();
                    } else {
                        JSONObject errorObject = new JSONObject(result.toString());
                        String strMessage = errorObject.optString("message");
                        displayMessage(strMessage);
                        GoToBeginActivity();
                    }

                } catch (JSONException e1) {
                    e1.printStackTrace();
                    if (Loginby.equalsIgnoreCase("facebook")) {
                        displayMessage(getResources().getString(R.string.fb_error));
                    } else {
                        displayMessage(getResources().getString(R.string.google_login));
                    }
                }

                utils.print("loginResponse", response.toString());
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
                params.put("device_token", device_token);
                params.put("accessToken", accesstoken);
                params.put("device_id", device_UDID);
                params.put("login_by", Loginby);
                params.put("mobile", mobileNumber);

                params.put("first_name", first_name.getText().toString());
                params.put("referral_code", referral_code.getText().toString());
                params.put("pass_num", passport_number.getText().toString());

                utils.print("InputLogin", "" + params);

                return params;
            }

            @Override
            protected Map<String, VolleyMultipartRequest.DataPart> getByteData() throws AuthFailureError {
                Map<String, VolleyMultipartRequest.DataPart> params = new HashMap<>();
                PassportFront = AppHelper.getFileDataFromDrawable(passportFront.getDrawable());
                PassportBack = AppHelper.getFileDataFromDrawable(passportBack.getDrawable());
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


    @OnClick({R.id.passport_front, R.id.passport_back, R.id.nextIcon})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.passport_front:
                currentImageView = passportFront;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkStoragePermission()) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    } else {
                        goToImageIntent();
                    }
                } else {
                    goToImageIntent();
                }
                break;
            case R.id.passport_back:
                currentImageView = passportBack;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkStoragePermission()) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                    } else {
                        goToImageIntent();
                    }
                } else {
                    goToImageIntent();
                }
                break;
            case R.id.nextIcon:

                if (first_name.getText().toString().isEmpty()) {
                    Toast.makeText(context, R.string.invaild_name, Toast.LENGTH_SHORT).show();
                    return;
                } else if (passport_number.getText().toString().isEmpty()) {
                    Toast.makeText(context, R.string.passport_number_required, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (loginBy.equalsIgnoreCase("facebook")) {
                    loginAPI(accessToken, AccessDetails.serviceurl + URLHelper.FACEBOOK_LOGIN, loginBy);
                } else if (loginBy.equalsIgnoreCase("google")) {
                    loginAPI(accessToken, AccessDetails.serviceurl + URLHelper.GOOGLE_LOGIN, loginBy);
                }

                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private boolean checkStoragePermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    if (!isPermissionGivenAlready) {
                        goToImageIntent();
                    }
                }
            }
        }
    }


    private static Bitmap getBitmapFromUri(@NonNull Context context, @NonNull Uri uri) throws IOException {
        Log.e(TAG, "getBitmapFromUri: Resize uri" + uri);
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        assert parcelFileDescriptor != null;
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        Log.e(TAG, "getBitmapFromUri: Height" + deviceHeight);
        Log.e(TAG, "getBitmapFromUri: width" + deviceWidth);
        int maxSize = Math.min(deviceHeight, deviceWidth);
        if (image != null) {
            Log.e(TAG, "getBitmapFromUri: Width" + image.getWidth());
            Log.e(TAG, "getBitmapFromUri: Height" + image.getHeight());
            int inWidth = image.getWidth();
            int inHeight = image.getHeight();
            int outWidth;
            int outHeight;
            if (inWidth > inHeight) {
                outWidth = maxSize;
                outHeight = (inHeight * maxSize) / inWidth;
            } else {
                outHeight = maxSize;
                outWidth = (inWidth * maxSize) / inHeight;
            }
            return Bitmap.createScaledBitmap(image, outWidth, outHeight, false);
        } else {
            Toast.makeText(context, context.getResources().getString(R.string.valid_image), Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    public void goToImageIntent() {
        isPermissionGivenAlready = true;
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PHOTO);*/
        EasyImage.openChooserWithGallery(ActivitySocialLogin.this,"Select Picture", 0);
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
