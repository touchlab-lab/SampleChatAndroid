package co.touchlab.andclass.cc;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import co.touchlab.android.util.App;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class Login extends Activity
{
//    public static final String SERVER_URL = "http://192.168.1.5:9000";
    public static final String SERVER_URL = "http://touchlab-samplechat.herokuapp.com";
    private TextView nickname;
    private Button joinChat;
    private View loading;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        nickname = (TextView) findViewById(R.id.nicknameInput);
        joinChat = (Button) findViewById(R.id.joinChat);
        loading = findViewById(R.id.loading);

        joinChat.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                openChatActivity();
            }
        });
    }

    private void openChatActivity()
    {
        final String nicknameVal = nickname.getText().toString().trim();

        if (nicknameVal.length() == 0)
        {
            Toast.makeText(this, "Nickname cannot be empty", Toast.LENGTH_LONG).show();
            return;
        }

        ThreadInfo.printThreadInfo("UI Thread");

        //Register user with server
        joinChat.setEnabled(false);
        loading.setVisibility(View.VISIBLE);

        new AsyncTask()
        {

            @Override
            protected Object doInBackground(Object... objects)
            {
                ThreadInfo.printThreadInfo("doInBackground");

                callServer();
                Prefs.setNickname(Login.this, nicknameVal);

                return null;
            }

            @Override
            protected void onPostExecute(Object o)
            {
                ThreadInfo.printThreadInfo("onPostExecute");
                joinChat.setEnabled(true);
                loading.setVisibility(View.INVISIBLE);
                Intent chatIntent = new Intent(Login.this, Chat.class);
                startActivity(chatIntent);
            }
        }.execute();
    }

    private void callServer()
    {
        try
        {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user", nickname.getText().toString()));

            HttpClient httpClient = new DefaultHttpClient();
            String jsonUrl = SERVER_URL + "/longpolling/roomMobile";
            HttpPost httpPost = new HttpPost(jsonUrl);

            httpPost.setEntity(new UrlEncodedFormEntity(params));

            HttpResponse response = httpClient.execute(httpPost);
            String resultJsonString = IOUtils.toString(response.getEntity().getContent());
            App.log(resultJsonString);
        }
        catch (Exception e)
        {
            App.logException(e);
            throw new RuntimeException(e);
        }
    }
}
