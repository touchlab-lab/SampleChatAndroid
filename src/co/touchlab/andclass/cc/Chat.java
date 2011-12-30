package co.touchlab.andclass.cc;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import co.touchlab.android.util.App;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kgalligan
 * Date: 10/13/11
 * Time: 9:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class Chat extends Activity
{
    private ListView chatData;
    private ArrayAdapter<ChatMessage> chatDataAdapter;
    private String nickname;
    public static String MESSAGES_URL = "/longpolling/room/messages";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        startChatService();

        setContentView(R.layout.chat);

        nickname = Prefs.getNickname(this);

        chatData = (ListView) findViewById(R.id.chatData);

        findViewById(R.id.postMessage).setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        final EditText myMessageView = (EditText) findViewById(R.id.myMessage);
                        String myMessage = myMessageView.getText().toString();

                        ChatMessage chatMessage = new ChatMessage(ChatMessage.Type.Message, nickname, myMessage);
                        postMessage(chatMessage);

                        myMessageView.setText("");
                    }
                }
        );
    }

    private void startChatService()
    {
        final Intent serviceIntent = new Intent(this, ChatService.class);
        startService(serviceIntent);
    }

    private ChatService boundService;
    private ServiceConnection conneciton = new ServiceConnection()
    {

        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            boundService = ((ChatService.LocalBinder) iBinder).getService();
            boundService.setScreenOn(true);
        }

        public void onServiceDisconnected(ComponentName componentName)
        {
            boundService = null;
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();

        if(boundService == null)
        {
            bindService(new Intent(this, ChatService.class), conneciton, Context.BIND_AUTO_CREATE);
        }

        chatDataAdapter = new ArrayAdapter<ChatMessage>(this, android.R.layout.simple_list_item_1, DatabaseHelper.getInstance(this).getAllMessages())
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                {
                    convertView = LayoutInflater.from(Chat.this).inflate(R.layout.chat_row, null);
                }

                ChatMessage chatMessage = chatDataAdapter.getItem(position);

                if(chatMessage.getType() == ChatMessage.Type.Message)
                {
                    final TextView chatNickname = (TextView) convertView.findViewById(R.id.chatNickname);
                    chatNickname.setText(chatMessage.getNickname());
                    chatNickname.setVisibility(View.VISIBLE);
                    convertView.findViewById(R.id.arrowImage).setVisibility(View.GONE);
                }
                else
                {
                    convertView.findViewById(R.id.chatNickname).setVisibility(View.GONE);
                    convertView.findViewById(R.id.arrowImage).setVisibility(View.VISIBLE);
                }

                ((TextView) convertView.findViewById(R.id.chatText)).setText(chatMessage.getPost());
                return convertView;
            }
        };

        chatData.setAdapter(chatDataAdapter);

        if (chatMessageReceiver == null)
        {
            chatMessageReceiver = new BroadcastReceiver()
            {
                @Override
                public void onReceive(Context context, Intent intent)
                {
                    final ChatMessage message = (ChatMessage) intent.getSerializableExtra(ChatService.CHAT_MESSAGE);
                    showChatMessage(message);
                }
            };

            final IntentFilter filter = new IntentFilter(ChatService.CHAT_MESSAGE);
            registerReceiver(chatMessageReceiver, filter);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (boundService != null)
        {
            boundService.setScreenOn(false);
            boundService = null;

            unbindService(conneciton);
            conneciton = null;
        }

        if (chatMessageReceiver != null)
        {
            unregisterReceiver(chatMessageReceiver);
            chatMessageReceiver = null;
        }
    }

    private BroadcastReceiver chatMessageReceiver;

    private void showChatMessage(ChatMessage chatMessage)
    {
        chatDataAdapter.add(chatMessage);
        chatDataAdapter.notifyDataSetChanged();
    }

    private void postMessage(ChatMessage chatMessage)
    {
        try
        {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user", chatMessage.getNickname()));
            params.add(new BasicNameValuePair("message", chatMessage.getPost()));

            HttpClient httpClient = new DefaultHttpClient();
            String jsonUrl = Login.SERVER_URL + MESSAGES_URL;
            HttpPost httpPost = new HttpPost(jsonUrl);

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = httpClient.execute(httpPost);
        }
        catch (Exception e)
        {
            App.logException(e);
            throw new RuntimeException(e);
        }
    }
}
