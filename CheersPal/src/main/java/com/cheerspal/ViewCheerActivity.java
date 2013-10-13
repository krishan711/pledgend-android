package com.cheerspal;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.cheerspal.model.Cheer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class ViewCheerActivity extends Activity
{

    private Cheer cheer;
    private ImageView ivCheers;
    private TextView tvTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_cheer);

        cheer = (Cheer) getIntent().getExtras().getSerializable("CHEER");
        tvTitle = (TextView) findViewById(R.id.tv_title);
        ivCheers = (ImageView) findViewById(R.id.iv_cheers);

        tvTitle.setText(cheer.receiver.firstName + " says Cheers!");

        new GetImageTask().execute();
    }

    public class GetImageTask extends AsyncTask<Void, Void, String>
    {
        @Override
        protected String doInBackground(Void... persons)
        {
            try
            {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpPost = new HttpGet(WebStuff.PHOTO_URL + "/" + cheer.id);

                HttpResponse response = httpClient.execute(httpPost);
                Log.i("Cheerspal", response.getStatusLine().toString());

                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);

                if (result != null)
                {
                    Log.i("Cheerspal", "result: " + result);

                    JsonElement responseJson = new JsonParser().parse(result);
                    JsonObject responseObject = responseJson.getAsJsonObject();
                    return responseObject.get("image_url").getAsString();
                }
            }
            catch (Exception e)
            {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            Log.i("cheerspal", s);
            Picasso.with(ViewCheerActivity.this).load(s).into(ivCheers);
        }
    }
}
