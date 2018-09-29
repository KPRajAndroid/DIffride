package com.diff.user.app.Activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.diff.user.app.FCM.MyFirebaseMessagingService;
import com.diff.user.app.Helper.CustomDialog;
import com.diff.user.app.Helper.SharedHelper;
import com.diff.user.app.Models.AccessDetails;
import com.diff.user.app.Models.ChatMessage;
import com.diff.app.R;
import com.diff.user.app.Utils.ChatView;
import com.diff.user.app.Utils.MyBoldTextView;
import com.diff.user.app.keys.PubnubKeys;
import com.google.gson.Gson;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by RAJKUMAR
 * on 03-Oct-17.
 */
public class ChatActivity extends AppCompatActivity /*implements View.OnClickListener */{

    public static final String TAG = "ChatActivity";
    SharedPreferences sharedPreferences;
//    CustomCallback callback;
    Context context;
    Pubnub pubnub;
    ChatView chatView;
    ImageView imgBack;
    ImageView imgProvider;
    MyBoldTextView username_txt;
    ArrayList<ChatMessage> chatMessageList;
    Gson gson;
    JSONObject messageObject;
    JSONArray msgHistoryObjArray;
    String request_id;
    boolean isMinimized = false;

    CustomDialog customDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = ChatActivity.this;
        request_id = SharedHelper.getKey(context,"request_id");
        Log.e(TAG, "onCreate: Request_id" + request_id);
        username_txt = (MyBoldTextView) findViewById(R.id.username_txt);
        imgBack = (ImageView) findViewById(R.id.backArrow);
        imgProvider = (ImageView) findViewById(R.id.imgProvider);
        chatView = (ChatView)findViewById(R.id.chat_view);
        customDialog = new CustomDialog(context);
        customDialog.show();

//        Driver provider = getIntent().getParcelableExtra("driver");

        if (SharedHelper.getKey(context,"Pro_img").equalsIgnoreCase("http"))
            Picasso.get().load(SharedHelper.getKey(context,"Pro_img")).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);
        else
            Picasso.get().load(AccessDetails.serviceurl + "/storage/"+SharedHelper.getKey(context,"Pro_img")).memoryPolicy(MemoryPolicy.NO_CACHE).placeholder(R.drawable.ic_dummy_user).error(R.drawable.ic_dummy_user).into(imgProvider);

        username_txt.setText(String.format(SharedHelper.getKey(context,"provider_name")));
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedHelper.putKey(context,"is_open_chat","true");
               finish();
            }
        });
        sharedPreferences = context.getSharedPreferences("details", Context.MODE_PRIVATE);
        gson = new Gson();
        chatView.setOnSentMessageListener(new ChatView.OnSentMessageListener(){
            @Override
            public boolean sendMessage(ChatMessage chatMessage){
                // perform actual message sending
                Log.e(TAG, "sendMessage: Click" + chatMessage);
                String message = chatMessage.getMessage();
                if (chatMessage.getMessage().length() != 0) {
                    message = gson.toJson(new ChatMessage(message, System.currentTimeMillis(),ChatMessage.Type.up,
                            Integer.parseInt(SharedHelper.getKey(context,"id"))));
                    Log.e(TAG, "onClick: Message" + message);
                    try {
                        messageObject = new JSONObject(message);
                    } catch (JSONException je) {
                        Log.d(TAG, je.toString());
                    }
                    pubnub.publish(request_id, messageObject, new Callback() {
                        @Override
                        public void successCallback(String channel, Object message) {
                            super.successCallback(channel, message);
                            Log.d("successCallback", "message " + message);
                        }

                        @Override
                        public void errorCallback(String channel, PubnubError error) {
                            super.errorCallback(channel, error);
                            Log.d("errorCallback", "error " + error);
                        }
                    });
                } else {
                    Toast.makeText(context, "Please enter message", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        pubnub = new Pubnub(PubnubKeys.PUBLISH_KEY, PubnubKeys.SUBSCRIBE_KEY);

        try {
            try {
                pubnub.subscribe(request_id, new Callback() {
                    @Override
                    public void successCallback(String channel, Object message) {
                        super.successCallback(channel, message);
                        Log.e(TAG, "successCallback: Message Subscribe" + message);
                        if (message!= null) {
                            try {
                                messageObject = new JSONObject(message.toString());
                            } catch (JSONException je) {
                                Log.d(TAG, je.toString());
                            }
                        }
                      runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "run: Receive msg" + messageObject);
                                if (messageObject != null) {
                                    if (SharedHelper.getKey(context, "id").equalsIgnoreCase(String.valueOf(messageObject.optInt("userId")))) {
                                        chatView.addMessage(new ChatMessage(messageObject.optString("message"), System.currentTimeMillis(),
                                                ChatMessage.Type.SENT, Integer.parseInt(SharedHelper.getKey(context, "id"))));
                                    } else {
                                        chatView.addMessage(new ChatMessage(messageObject.optString("message"), System.currentTimeMillis(),
                                                ChatMessage.Type.RECEIVED, Integer.parseInt(SharedHelper.getKey(context, "id"))));
                                        addNotification();
                                    }
                                }
                            }
                        });
                    }

                    @Override
                    public void successCallback(String channel, Object message, String timetoken) {
                        super.successCallback(channel, message, timetoken);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                        super.errorCallback(channel, error);
                        Log.d("errorCallback", "error " + error);
                    }

                    @Override
                    public void connectCallback(String channel, Object message) {
                        super.connectCallback(channel, message);
                        Log.d("connectCallback", "message " + message);
                    }

                    @Override
                    public void reconnectCallback(String channel, Object message) {
                        super.reconnectCallback(channel, message);
                        Log.d("reconnectCallback", "message " + message);
                    }

                    @Override
                    public void disconnectCallback(String channel, Object message) {
                        super.disconnectCallback(channel, message);
                        Log.d("disconnectCallback", "message " + message);
                    }
                });
            } catch (PubnubException e) {
                e.printStackTrace();
            }
        } catch (Exception pe) {
            Log.d(TAG, pe.toString());
        }

        //Chat History
        pubnub.history(request_id, true, 100, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                super.successCallback(channel, message);
                Log.e(TAG, "successCallback: History Messages" + message);
                customDialog.dismiss();
                if (message!= null) {
                    try {
                        JSONArray msgObjArray = new JSONArray(message.toString());
                        for (int i=0;i<1;i++){
                            msgHistoryObjArray  = msgObjArray.getJSONArray(0);
                            for (int j=0;j<msgHistoryObjArray.length();j++){
                                JSONObject msgObj = msgHistoryObjArray.getJSONObject(j);
                                final JSONObject msgDataObj = msgObj.optJSONObject("message");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e(TAG, "run: Receive msg" + msgDataObj);
                                        if (msgDataObj != null) {
                                            if (SharedHelper.getKey(context, "id").equalsIgnoreCase(String.valueOf(msgDataObj.optInt("userId")))) {
                                                chatView.addMessage(new ChatMessage(msgDataObj.optString("message"), System.currentTimeMillis(),
                                                        ChatMessage.Type.SENT, Integer.parseInt(SharedHelper.getKey(context, "id"))));
                                            } else {
                                                chatView.addMessage(new ChatMessage(msgDataObj.optString("message"), System.currentTimeMillis(),
                                                        ChatMessage.Type.RECEIVED, Integer.parseInt(SharedHelper.getKey(context, "id"))));
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    } catch (JSONException je) {
                        Log.d(TAG, je.toString());
                    }
                }
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                super.errorCallback(channel, error);
                customDialog.dismiss();
                Log.e(TAG, "successCallback: History Messages error" + error);
            }
        });
    }

    private void addNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(),request_id)
                        .setSmallIcon(R.drawable.info_icon)
                        .setContentTitle("Diff Chat")
                        .setContentText("You got a new message");

        Intent notificationIntent = new Intent(this, ChatActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

/*
    private JSONObject publishPushNotification(JSONObject messageObject) {
        String messageString = messageObject.optString("message");
        long timestamp = messageObject.optLong("timestamp");
        int userId = messageObject.optInt("userId");
        String type = messageObject.optString("type");
        Log.e(TAG, "publishPushNotification: " + messageString);
        try {
            JSONObject pushPayload = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject notification = new JSONObject();

            notification.put("body", messageString);
            notification.put("title", "You got a new message");

            data.put("message", messageString);
            data.put("timestamp", timestamp);
            data.put("userId", userId);
            data.put("type", type);
            pushPayload.put("data", data);
            pushPayload.put("notification", notification);

            JSONObject pngcm = new JSONObject();
            pngcm.put("pn_gcm", pushPayload);
            return pngcm;
        } catch (Exception e) {
            Log.e(TAG, "publishPushNotification: " + "No push generate");
        }
        return messageObject;
    }
*/

    @Override
    protected void onResume() {
        super.onResume();
        SharedHelper.putKey(context,"is_open_chat","false");
        MyFirebaseMessagingService.value = 0;
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedHelper.putKey(context,"is_open_chat","true");
        pubnub.unsubscribe(request_id);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedHelper.putKey(context,"is_open_chat","true");
    }
}