package com.diff.user.app.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.diff.user.app.Activities.AccessKeyActivity;
import com.diff.user.app.Activities.HistoryDetails;
import com.diff.user.app.Activities.WelcomeScreenActivity;
import com.diff.user.app.DiffApplication;
import com.diff.user.app.Helper.ConnectionHelper;
import com.diff.user.app.Helper.CustomDialog;
import com.diff.user.app.Helper.SharedHelper;
import com.diff.user.app.Helper.URLHelper;
import com.diff.user.app.Models.AccessDetails;
import com.diff.app.R;
import com.diff.user.app.Utils.MyBoldTextView;
import com.diff.user.app.Utils.MyButton;
import com.diff.user.app.Utils.MyTextView;
import com.diff.user.app.Utils.Utilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.diff.user.app.DiffApplication.trimMessage;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */


public class OnGoingTrips extends Fragment {

    Boolean isInternet;
    Activity activity;
    Context context;
    View rootView;
    UpcomingsAdapter upcomingsAdapter;
    RecyclerView recyclerView;
    RelativeLayout errorLayout;
    ConnectionHelper helper;
    CustomDialog customDialog;
    String cancelReason;
    Utilities utils = new Utilities();

    public OnGoingTrips() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_on_going_trips, container, false);
        findViewByIdAndInitialize();

        if (helper.isConnectingToInternet()) {
            getUpcomingList();
        }

        return rootView;
    }


    public void findViewByIdAndInitialize() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        errorLayout = (RelativeLayout) rootView.findViewById(R.id.errorLayout);
        errorLayout.setVisibility(View.GONE);
        helper = new ConnectionHelper(getActivity());
        isInternet = helper.isConnectingToInternet();
    }

    @Override
    public void onResume() {
        if (upcomingsAdapter != null) {
            getUpcomingList();
        }
        super.onResume();
    }

    public void getUpcomingList() {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(AccessDetails.serviceurl + URLHelper.UPCOMING_TRIPS, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                utils.print("GetHistoryList", response.toString());
                if (response != null) {
                    upcomingsAdapter = new UpcomingsAdapter(response);
                    //  recyclerView.setHasFixedSize(true);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                    if (upcomingsAdapter != null && upcomingsAdapter.getItemCount() > 0) {
                        recyclerView.setVisibility(View.VISIBLE);
                        errorLayout.setVisibility(View.GONE);
                        recyclerView.setAdapter(upcomingsAdapter);
                    } else {
                        errorLayout.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    }

                } else {
                    errorLayout.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();

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
                            refreshAccessToken("UPCOMING_TRIPS");
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
                        getUpcomingList();
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

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AccessDetails.serviceurl + URLHelper.login, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                utils.print("SignUpResponse", response.toString());
                SharedHelper.putKey(context, "access_token", response.optString("access_token"));
                SharedHelper.putKey(context, "refresh_token", response.optString("refresh_token"));
                SharedHelper.putKey(context, "token_type", response.optString("token_type"));
                if (tag.equalsIgnoreCase("UPCOMING_TRIPS")) {
                    getUpcomingList();
                }


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
                        refreshAccessToken(tag);
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

    public void GoToBeginActivity() {
        Intent mainIntent;
        if (AccessDetails.demo_build) {
            mainIntent = new Intent(activity, AccessKeyActivity.class);
        } else {
            mainIntent = new Intent(activity, WelcomeScreenActivity.class);
        }

        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
        activity.finish();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void displayMessage(String toastString) {
        try {
            Snackbar.make(getView(), toastString, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        } catch (Exception e) {
            try {
                Toast.makeText(context, "" + toastString, Toast.LENGTH_SHORT).show();
            } catch (Exception ee) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    public void cancelRequest(final String request_id) {

        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        if (customDialog != null)
            customDialog.show();
        JSONObject object = new JSONObject();
        try {
            object.put("request_id", request_id);
            object.put("cancel_reason", cancelReason);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AccessDetails.serviceurl + URLHelper.CANCEL_REQUEST_API, object, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                utils.print("CancelRequestResponse", response.toString());
                if ((customDialog != null) && (customDialog.isShowing()))
                    customDialog.dismiss();
                getUpcomingList();
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
                            refreshAccessToken("CANCEL_REQUEST");
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
                        cancelRequest(request_id);
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
    }

    private String getMonth(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String monthName = new SimpleDateFormat("MMM").format(cal.getTime());
        return monthName;
    }

    private String getDate(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String dateName = new SimpleDateFormat("dd").format(cal.getTime());
        return dateName;
    }

    private String getYear(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String yearName = new SimpleDateFormat("yyyy").format(cal.getTime());
        return yearName;
    }

    private String getTime(String date) throws ParseException {
        Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        String timeName = new SimpleDateFormat("hh:mm a").format(cal.getTime());
        return timeName;
    }

    private class UpcomingsAdapter extends RecyclerView.Adapter<UpcomingsAdapter.MyViewHolder> {
        JSONArray jsonArray;

        public UpcomingsAdapter(JSONArray array) {
            this.jsonArray = array;
        }

        public void append(JSONArray array) {
            try {
                for (int i = 0; i < array.length(); i++) {
                    this.jsonArray.put(array.get(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public UpcomingsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.upcoming_list_item, parent, false);
            return new UpcomingsAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(UpcomingsAdapter.MyViewHolder holder, final int position) {
//            Utilities.loadImage(jsonArray.optJSONObject(position).optString("static_map"), holder.tripImg);

            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder)
                    .priority(Priority.HIGH);

            Glide.with(DiffApplication.getInstance())
                    .load(jsonArray.optJSONObject(position).optString("static_map"))
                    .apply(options)
                    .into(holder.tripImg);

            if (jsonArray.optJSONObject(position).optString("booking_id") != null &&
                    !jsonArray.optJSONObject(position).optString("booking_id").equalsIgnoreCase("")) {
                holder.booking_id.setText("Booking ID:" + jsonArray.optJSONObject(position).optString("booking_id"));
            }
            try {
                if (!jsonArray.optJSONObject(position).optString("schedule_at", "").isEmpty()) {
                    String form = jsonArray.optJSONObject(position).optString("schedule_at");
                    try {
                        holder.tripDate.setText(getDate(form) + "th " + getMonth(form) + " " + getYear(form) + " at " + getTime(form));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                JSONObject serviceObj = jsonArray.getJSONObject(position).optJSONObject("service_type");
                if (serviceObj != null) {
                    holder.car_name.setText(serviceObj.optString("name"));
                    //holder.tripAmount.setText(SharedHelper.getKey(context, "currency")+serviceObj.optString("price"));
                    holder.tripAmount.setVisibility(View.GONE);
                    Utilities.loadImage(serviceObj.optString("image"), holder.driver_image);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            holder.btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    View view = LayoutInflater.from(context).inflate(R.layout.cancel_dialog, null);
                    final EditText reasonEtxt = (EditText) view.findViewById(R.id.reason_etxt);
                    Button submitBtn = (Button) view.findViewById(R.id.submit_btn);
                    submitBtn.setVisibility(View.GONE);
                    builder
                            .setIcon(AccessDetails.site_icon)
                            .setTitle(AccessDetails.siteTitle)
                            .setMessage(getString(R.string.cencel_request))
                            .setView(view)
                            .setCancelable(true);
                    builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            cancelReason = reasonEtxt.getText().toString();
                            cancelRequest(jsonArray.optJSONObject(position).optString("id"));
                        }
                    })
                            .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            });

        }

        @Override
        public int getItemCount() {
            return jsonArray.length();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            MyTextView tripTime, car_name;
            TextView tripDate, tripAmount, booking_id;
            ImageView tripImg, driver_image;
            MyButton btnCancel;

            public MyViewHolder(View itemView) {
                super(itemView);
                tripDate = (MyBoldTextView) itemView.findViewById(R.id.tripDate);
                tripTime = (MyTextView) itemView.findViewById(R.id.tripTime);
                tripAmount = (TextView) itemView.findViewById(R.id.tripAmount);
                booking_id = (TextView) itemView.findViewById(R.id.booking_id);
                tripImg = (ImageView) itemView.findViewById(R.id.tripImg);
                car_name = (MyTextView) itemView.findViewById(R.id.car_name);
                driver_image = (ImageView) itemView.findViewById(R.id.driver_image);
                btnCancel = (MyButton) itemView.findViewById(R.id.btnCancel);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (helper.isConnectingToInternet()) {
                            Intent intent = new Intent(getActivity(), HistoryDetails.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            utils.print("Intent", "" + jsonArray.optJSONObject(getAdapterPosition()).toString());
                            intent.putExtra("post_value", jsonArray.optJSONObject(getAdapterPosition()).toString());
                            intent.putExtra("tag", "upcoming_trips");
                            startActivity(intent);
                        } else {
                            Toast.makeText(context, "Oops, Connect your internet", Toast.LENGTH_LONG).show();
                        }

                    }
                });
            }
        }
    }
}
