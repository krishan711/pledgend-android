package com.cheerspal;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.cheerspal.adapter.CheerAdapter;
import com.cheerspal.model.Cheer;
import com.cheerspal.model.Person;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OverviewActivity extends Activity implements AdapterView.OnItemClickListener
{
    private CheerAdapter cheerAdapter;
    private List<Cheer> cheers;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        cheerAdapter = new CheerAdapter(this);
        ListView listView = (ListView) findViewById(R.id.lv_cheers);
        listView.setAdapter(cheerAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onResume()
    {
        new GetCheersTask().execute(((CheersPalApplication) getApplication()).user);

        super.onResume();
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
                startActivity(new Intent(this, CreateActivity.class));
                return true;
            case R.id.menu_refresh:
                new GetCheersTask().execute(((CheersPalApplication) getApplication()).user);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Cheer cheer = cheers.get(position);
        Intent intent = null;
        if (cheer.receiver.id.equals(((CheersPalApplication) getApplication()).user.id) && cheer.claimTime == null)
        {
            intent = new Intent(this, RespondActivity.class);
        }
        else if (!(cheer.sender.id.equals(((CheersPalApplication) getApplication()).user.id) && cheer.claimTime == null))
        {
            intent = new Intent(this, ViewCheerActivity.class);
        }

        if (intent !=  null)
        {
            intent.putExtra("CHEER", cheer);
            startActivity(intent);
        }
    }

    public class GetCheersTask extends AsyncTask<Person, Void, List<Cheer>>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected List<Cheer> doInBackground(Person... persons)
        {
            JsonObject sendme = new JsonObject();
            sendme.addProperty("email", persons[0].id);
            sendme.addProperty("firstname", persons[0].firstName);
            sendme.addProperty("lastname", persons[0].lastName);

            String params = "?" + "email=" + persons[0].id + "&" + "firstname=" + persons[0].firstName + "&" + "lastname=" + persons[0].lastName;

            try
            {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(WebStuff.LOGIN_URL + params);

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

                        int id = responseObject.get("gift_id").getAsInt();
                        int amount = responseObject.get("amount").getAsInt();
                        String title = responseObject.get("title").getAsString();
                        Date sentTime = new Date(responseObject.get("sent_time").getAsLong());

                        JsonObject senderObj = responseObject.get("sender").getAsJsonObject();

                        String firstName = senderObj.get("firstname").getAsString();
                        String lastName = senderObj.get("lastname").getAsString();
                        String email = senderObj.get("email").getAsString();

                        Person sender = new Person(firstName, lastName, email);

                        JsonObject receiverObj = responseObject.get("receiver").getAsJsonObject();

                        firstName = receiverObj.get("firstname").getAsString();
                        lastName = receiverObj.get("lastname").getAsString();
                        email = receiverObj.get("email").getAsString();

                        Person receiver = new Person(firstName, lastName, email);

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
