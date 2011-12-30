package co.touchlab.andclass.cc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import co.touchlab.android.util.App;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/15/11
 * Time: 2:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChatService extends Service
{
    public static final String CHAT_MESSAGE = "chatMessage";
    public static final int NOTIFICATION_ID = 1234;
    private long lastReceived = 0;
    private Thread checkThread;
    private boolean screenOn;

    public class LocalBinder extends Binder
    {
        public ChatService getService()
        {
            return ChatService.this;
        }
    }

    private IBinder binder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    public boolean isScreenOn()
    {
        return screenOn;
    }

    public void setScreenOn(boolean screenOn)
    {
        this.screenOn = screenOn;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        startCheckingServer();

        return START_STICKY;
    }

    private synchronized void startCheckingServer()
    {
        if(checkThread == null)
        {
            checkThread = new Thread()
            {
                @Override
                public void run()
                {
                    lastReceived = 0;
                    DatabaseHelper.getInstance(ChatService.this).deleteAllMessages();

                    while (true)
                    {
                        try
                        {
                            checkMessagesFromServer();
                        }
                        catch (Exception e)
                        {
                            App.logException(e);
                            try
                            {
                                Thread.sleep(5000);
                            }
                            catch (InterruptedException e1)
                            {
                                App.logException(e1);
                            }
                        }
                    }
                }
            };
            checkThread.start();
        }
    }

    private void checkMessagesFromServer()
    {
        App.log("called checkMessagesFromServer");
        try
        {
            HttpClient httpClient = new DefaultHttpClient();
            String jsonUrl = Login.SERVER_URL + Chat.MESSAGES_URL + "?lastReceived=" + lastReceived;
            HttpRequestBase httpRequest = new HttpGet(jsonUrl);

            HttpResponse response = httpClient.execute(httpRequest);
            String resultJsonString = IOUtils.toString(response.getEntity().getContent());

            JSONArray jsonResult = new JSONArray(resultJsonString);

            for(int i=0; i<jsonResult.length(); i++)
            {
                JSONObject eventJson = jsonResult.getJSONObject(i);
                long thisId = eventJson.getLong("id");
                if(thisId > lastReceived)
                    lastReceived = thisId;
                final JSONObject chatEventJson = eventJson.getJSONObject("data");

                String chatMessageType = chatEventJson.getString("type");
                ChatMessage chatMessage;
                if(chatMessageType.equals("join"))
                    chatMessage = new ChatMessage(ChatMessage.Type.Join, "[system]", chatEventJson.getString("user") + " joined");
                else if(chatMessageType.equals("leave"))
                    chatMessage = new ChatMessage(ChatMessage.Type.Leave, "[system]", chatEventJson.getString("user") + " left");
                else
                    chatMessage = new ChatMessage(ChatMessage.Type.Message, chatEventJson.getString("user"), chatEventJson.getString("text"));

                final Intent broadcastIntent = new Intent(CHAT_MESSAGE);
                broadcastIntent.putExtra(CHAT_MESSAGE, chatMessage);

                writeToDb(chatMessage);

                sendBroadcast(broadcastIntent);

                showNotification(chatMessage);
            }
        }
        catch (Exception e)
        {
            App.logException(e);
            throw new RuntimeException(e);
        }
    }

    private void showNotification(ChatMessage chatMessage)
    {
        if(!screenOn)
        {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            final Notification notification = new Notification(R.drawable.notifyicon, "New Chat Message!", System.currentTimeMillis());
            final Intent notificationIntent = new Intent(this, Chat.class);
            final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            notification.setLatestEventInfo(this, "New Chat Message!", chatMessage.getPost(), contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void writeToDb(ChatMessage myMessage)
    {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance(this);
        databaseHelper.addChatPost(myMessage.getNickname(), myMessage.getPost());
    }


}
