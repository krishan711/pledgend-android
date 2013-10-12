package com.cheerspal;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.cheerspal.adapter.CheerAdapter;
import com.cheerspal.model.Cheer;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OverviewActivity extends Activity
{
    private CheerAdapter cheerAdapter;
    private List<Cheer> cheers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        cheerAdapter = new CheerAdapter(this);

        new GetCheersTask().execute(((CheersPalApplication) getApplication()).userId, ((CheersPalApplication) getApplication()).accessToken);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_add:
                Toast.makeText(this, "Add Clicked", Toast.LENGTH_LONG).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetCheersTask extends AsyncTask<String, Void, List<Cheer>>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected List<Cheer> doInBackground(String... params)
        {
            JsonObject sendme = new JsonObject();
            sendme.addProperty("userid", params[0]);
            sendme.addProperty("accessToken", params[1]);

            try
            {
                DefaultHttpClient httpClient = new DefaultHttpClient();
//                HttpPost httpPost = new HttpPost(WebStuff.CHEERS_URL);
//                httpPost.setEntity(new StringEntity(sendme.toString()));

                HttpGet httpPost = new HttpGet(WebStuff.CHEERS_URL);
                HttpResponse response = httpClient.execute(httpPost);
                Log.i("Cheerspal", response.getStatusLine().toString());

                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);

                if (result != null)
                {
                    Log.i("Cheerspal", "result: " + result);

                    JsonElement responseJson = new JsonParser().parse(result);
                    JsonArray responseArray = responseJson.getAsJsonArray();

                    List<Cheer> ret = new ArrayList<Cheer>();

                    for (JsonElement responseElement : responseArray)
                    {
                        Log.i("Cheerspal", "parsing: " + responseElement.toString());

                        JsonObject responseObject = responseElement.getAsJsonObject();

                        int id = responseObject.get("id").getAsInt();
                        String sender = responseObject.get("sender").getAsString();
                        String receiver = responseObject.get("receiver").getAsString();
                        int amount = responseObject.get("amount").getAsInt();
                        String title = responseObject.get("title").getAsString();
                        Date sentTime = new Date(responseObject.get("sent_time").getAsLong());

                        Cheer cheer = new Cheer(id, sender, receiver, amount, title, sentTime);

                        try
                        {
                            cheer.claimTime = new Date(responseObject.get("claim_time").getAsLong());
                            cheer.charity = responseObject.get("charity").getAsBoolean();
                        }
                        catch (Exception e)
                        {
                        }

                        ret.add(cheer);
                    }

                    return ret;
                }
                else
                {
                    Log.i("Cheerspal", "result: null");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<Cheer> cheers)
        {
            super.onPostExecute(cheers);

            if (cheers != null)
            {
                OverviewActivity.this.cheers = cheers;
                OverviewActivity.this.cheerAdapter.setCheers(cheers);
            }
            else
            {
                Toast.makeText(OverviewActivity.this, "Woops, something went wrong!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
