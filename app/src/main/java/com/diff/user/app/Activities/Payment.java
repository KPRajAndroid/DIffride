package com.diff.user.app.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.diff.user.app.Constants.NewPaymentListAdapter;
import com.diff.user.app.DiffApplication;
import com.diff.user.app.Helper.ConnectionHelper;
import com.diff.user.app.Helper.CustomDialog;
import com.diff.user.app.Helper.SharedHelper;
import com.diff.user.app.Helper.URLHelper;
import com.diff.user.app.Models.AccessDetails;
import com.diff.user.app.Models.CardDetails;
import com.diff.user.app.Models.CardInfo;
import com.diff.app.R;
import com.diff.user.app.Utils.MyBoldTextView;
import com.diff.user.app.Utils.Utilities;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.diff.user.app.DiffApplication.trimMessage;


/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class Payment extends AppCompatActivity {

    private final int ADD_CARD_CODE = 435;
    Activity activity;
    Context context;
    CustomDialog customDialog;
//    MyBoldTextView addCard;
    MyBoldTextView lblPaymentMethods;
//    ListView payment_list_view;
    ArrayList<JSONObject> listItems;
    NewPaymentListAdapter paymentAdapter;
    Utilities utils = new Utilities();
    JSONObject deleteCard = new JSONObject();
    String strType = "";

    LinearLayout cashLayout, llMolLayer;

    boolean check = true;

    //Internet
    ConnectionHelper helper;
    Boolean isInternet;

    ImageView backArrow;

    private ArrayList<CardDetails> cardArrayList;

    public Payment() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        context = Payment.this;
        activity = Payment.this;

        if (getIntent().hasExtra("type")) strType = getIntent().getStringExtra("type");

        findViewByIdAndInitialize();
//        getCardList();

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        addCard.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                GoToAddCard();
//            }
//        });

        cashLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardInfo cardInfo = new CardInfo();
                cardInfo.setLastFour("CASH");
                Intent intent = new Intent();
                intent.putExtra("card_info", cardInfo);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        llMolLayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CardInfo cardInfo = new CardInfo();
                cardInfo.setLastFour("MOLPAY");
                Intent intent = new Intent();
                intent.putExtra("card_info", cardInfo);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

//        payment_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                try {
//                    JSONObject object = new JSONObject(listItems.get(position).toString());
//                    Utilities.print("MyTest", "" + listItems.get(position));
//
//                    CardInfo cardInfo = new CardInfo();
//                    cardInfo.setLastFour(object.optString("last_four"));
//                    cardInfo.setCardId(object.optString("card_id"));
//                    cardInfo.setCardType(object.optString("brand"));
//                    if (check) {
//                        Intent intent = new Intent();
//                        intent.putExtra("card_info", cardInfo);
//                        setResult(RESULT_OK, intent);
//                        finish();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        payment_list_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                try {
//                    check = false;
//                    String json = new Gson().toJson(paymentAdapter.getItem(i));
//                    JSONObject object = new JSONObject(json);
//                    Utilities.print("MyTest", "" + paymentAdapter.getItem(i));
//                    DeleteCardDailog(object);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                return false;
//            }
//        });
    }

    private void DeleteCardDailog(final JSONObject object) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(getString(R.string.are_you_sure))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteCard = object;
                        check = true;
                        deleteCard();
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        check = true;
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void deleteCard() {
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("card_id", deleteCard.optString("card_id"));
            object.put("_method", "DELETE");

        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AccessDetails.serviceurl
                + URLHelper.DELETE_CARD_FROM_ACCOUNT_API, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Utilities.print("SendRequestResponse", response.toString());
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                getCardList();
                deleteCard = new JSONObject();
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
                                displayMessage(errorObj.getString("message"));
                            } catch (Exception e) {
                                displayMessage(errorObj.optString("error"));
                                //displayMessage(getString(R.string.something_went_wrong));
                            }
                            Utilities.print("MyTest", "" + errorObj.toString());
                        } else if (response.statusCode == 401) {
                            refreshAccessToken("DELETE_CARD");
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
                        deleteCard();
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

    public void getCardList() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();

        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(AccessDetails.serviceurl + URLHelper.CARD_PAYMENT_LIST,
                new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                Utilities.print("GetPaymentList", response.toString());
                if (response != null && response.length() > 0) {
                    listItems = getArrayListFromJSONArray(response);
//                    if (listItems.isEmpty()) {
//                        payment_list_view.setVisibility(View.GONE);
//                    } else {
//                        payment_list_view.setVisibility(View.VISIBLE);
//                    }
                    cardArrayList = new ArrayList<>();
                    for (JSONObject jsonObject : listItems) {
                        Gson gson = new Gson();
                        CardDetails card = gson.fromJson(jsonObject.toString(), CardDetails.class);
                        card.setSelected("false");
                        card.setSelected("true");
                        cardArrayList.add(card);
                    }

                    Log.e("", "onResponse: " + cardArrayList.toString());
                    paymentAdapter = new NewPaymentListAdapter(context, cardArrayList, activity);
//                    payment_list_view.setAdapter(paymentAdapter);
                } else {
//                    payment_list_view.setVisibility(View.GONE);
                }
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                String json;
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
                            refreshAccessToken("PAYMENT_LIST");
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
                    if (error instanceof NoConnectionError)
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    else if (error instanceof NetworkError)
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    else if (error instanceof TimeoutError) getCardList();
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
        DiffApplication.getInstance().addToRequestQueue(jsonArrayRequest);
    }

    private void refreshAccessToken(final String tag) {
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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                AccessDetails.serviceurl + URLHelper.login,
                object,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Utilities.print("SignUpResponse", response.toString());
                SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
                SharedHelper.putKey(context, "token_type", response.optString("token_type"));
                if (tag.equalsIgnoreCase("PAYMENT_LIST")) getCardList();
                else deleteCard();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {
                    SharedHelper.putKey(context, "loggedIn", getString(R.string.False));
                    GoToBeginActivity();
                } else {
                    if (error instanceof NoConnectionError)
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    else if (error instanceof NetworkError)
                        displayMessage(getString(R.string.oops_connect_your_internet));
                    else if (error instanceof TimeoutError) refreshAccessToken(tag);
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

    private ArrayList<JSONObject> getArrayListFromJSONArray(JSONArray jsonArray) {

        ArrayList<JSONObject> aList = new ArrayList<JSONObject>();

        try {
            if (jsonArray != null)
                for (int i = 0; i < jsonArray.length(); i++) aList.add(jsonArray.getJSONObject(i));
        } catch (JSONException je) {
            je.printStackTrace();
        }

        return aList;

    }

    public void findViewByIdAndInitialize() {
//        addCard = findViewById(R.id.addCard);
        lblPaymentMethods = findViewById(R.id.lblPaymentMethods);
//        payment_list_view = findViewById(R.id.payment_list_view);
        helper = new ConnectionHelper(context);
        isInternet = helper.isConnectingToInternet();
        cashLayout = findViewById(R.id.cash_layout);
        llMolLayer = findViewById(R.id.llMolLayer);

        if (strType.equalsIgnoreCase("wallet")) {
            cashLayout.setVisibility(View.GONE);
            lblPaymentMethods.setVisibility(View.GONE);
        } else {
            cashLayout.setVisibility(View.VISIBLE);
            lblPaymentMethods.setVisibility(View.VISIBLE);
        }

        backArrow = findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void displayMessage(String toastString) {
        try {
            if (getCurrentFocus() != null)
                Snackbar.make(getCurrentFocus(), toastString, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GoToBeginActivity() {
        Intent mainIntent;
        if (AccessDetails.demo_build) mainIntent = new Intent(activity, AccessKeyActivity.class);
        else mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    public void GoToMainActivity() {
        Intent mainIntent = new Intent(activity, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
        activity.finish();
    }

    public void GoToAddCard() {
        Intent mainIntent = new Intent(activity, AddCard.class);
        startActivityForResult(mainIntent, ADD_CARD_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_CARD_CODE) if (resultCode == Activity.RESULT_OK) {
            boolean result = data.getBooleanExtra("isAdded", false);
            if (result) getCardList();
        }
    }
}
