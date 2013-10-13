package com.cheerspal;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.cheerspal.model.Person;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.util.Date;

public class CreateActivity extends Activity implements TextWatcher, View.OnClickListener
{
    private View vInitial;
    private EditText etRecipientId;
    private TextView tvRecipientError;
    private Button btnContinue;

    private View vProgress;
    private TextView tvProgress;

    private View vConfirm;
    private TextView tvRecipientName;
    private TextView tvRecipientEmail;
    private View btnPint;
    private View btnCoffee;
    private View btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        tvRecipientError = (TextView) findViewById(R.id.tv_recipient_error);

        etRecipientId = (EditText) findViewById(R.id.et_recipient_id);
        etRecipientId.addTextChangedListener(this);

        btnContinue = (Button) findViewById(R.id.btn_check_recipient);
        btnContinue.setOnClickListener(this);

        tvProgress = (TextView) findViewById(R.id.tv_progress);

        vInitial = findViewById(R.id.v_initial);
        vProgress = findViewById(R.id.v_progress);
        vConfirm = findViewById(R.id.v_confirm);

        tvRecipientName = (TextView) findViewById(R.id.tv_recipient_name);
        tvRecipientEmail = (TextView) findViewById(R.id.tv_recipient_email);
        btnPint = findViewById(R.id.btn_pint);
        btnPint.setOnClickListener(this);
        btnCoffee = findViewById(R.id.btn_coffee);
        btnCoffee.setOnClickListener(this);
        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);
        btnSend.setEnabled(false);

        goToInitial(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create, menu);
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

    }

    @Override
    public void afterTextChanged(Editable s)
    {
        String recipientString = etRecipientId.getText().toString();
        btnContinue.setEnabled(recipientString != null && recipientString.length() > 0
                && recipientString.indexOf("@") > 0
                && recipientString.indexOf(".") > recipientString.indexOf("@"));
    }

    @Override
    public void onClick(View view)
    {
        if (view == btnContinue)
        {
            new CheckUserTask().execute(etRecipientId.getText().toString());
        }
        else if (view == btnPint)
        {
            Log.i("cheerspal", "btn pint");
            btnPint.setSelected(true);
            btnCoffee.setSelected(false);

            btnSend.setEnabled(true);
        }
        else if (view == btnCoffee)
        {
            Log.i("cheerspal", "btn coffee");
            btnPint.setSelected(false);
            btnCoffee.setSelected(true);

            btnSend.setEnabled(true);
        }
        else if (view == btnSend)
        {
            new CreatePaymentTask().execute(etRecipientId.getText().toString());
        }
    }

    public void goToInitial(boolean showError)
    {
        vInitial.setVisibility(View.VISIBLE);
        vProgress.setVisibility(View.GONE);
        vConfirm.setVisibility(View.GONE);

        tvRecipientError.setVisibility(showError ? View.VISIBLE : View.GONE);
    }

    public void goToConfirm()
    {
        vInitial.setVisibility(View.GONE);
        vProgress.setVisibility(View.GONE);
        vConfirm.setVisibility(View.VISIBLE);
    }

    public class CheckUserTask extends AsyncTask<String, Void, Person>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            vInitial.setVisibility(View.GONE);
            vProgress.setVisibility(View.VISIBLE);
            vConfirm.setVisibility(View.GONE);

            tvProgress.setText("Looking for your friend...");
        }

        @Override
        protected Person doInBackground(String... params)
        {
            try
            {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(WebStuff.USER_URL + "?email=" + params[0]);

                HttpResponse response = httpClient.execute(httpGet);
                Log.i("Cheerspal", response.getStatusLine().toString());

                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);

                if (result != null)
                {
                    Log.i("Cheerspal", "result: " + result);

                    JsonObject responseObject = new JsonParser().parse(result).getAsJsonObject();

                    String firstName = responseObject.get("firstname").getAsString();
                    String lastName = responseObject.get("lastname").getAsString();

                    return new Person(firstName, lastName, params[0]);
                }
                else
                {
                    Log.i("Cheerspal", "result: null");
                }
            }
            catch (Exception e)
            {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Person person)
        {
            super.onPostExecute(person);

            if (person == null)
            {
                goToInitial(true);
            }
            else
            {
                goToConfirm();
            }
        }
    }

    public class CreatePaymentTask extends AsyncTask<String, Void, Person>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            vInitial.setVisibility(View.GONE);
            vProgress.setVisibility(View.VISIBLE);
            vConfirm.setVisibility(View.GONE);

            String title = btnPint.isSelected() ? "Pint" : "Coffee";
            tvProgress.setText("Sending your " + title + "...");
        }

        @Override
        protected Person doInBackground(String... recipientId)
        {
            String params = "?" + "title=" + (btnPint.isSelected() ? "Pint" : "Coffee")
                    + "&" + "amount=" + 350
                    + "&" + "sender_id=" + ((CheersPalApplication) getApplication()).user.id
                    + "&" + "reciever_id=" + recipientId[0]
                    + "&" + "sent_time=" + new Date().getTime();
            try
            {
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(WebStuff.CHEERS_URL + params);

                HttpResponse response = httpClient.execute(httpPost);
                Log.i("Cheerspal", response.getStatusLine().toString());

                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);

                if (result != null)
                {
                    Log.i("Cheerspal", "result: " + result);

                    JsonObject responseObject = new JsonParser().parse(result).getAsJsonObject();

                    String firstName = responseObject.get("first_name").getAsString();
                    String lastName = responseObject.get("last_name").getAsString();

                    return new Person(firstName, lastName, recipientId[0]);
                }
                else
                {
                    Log.i("Cheerspal", "result: null");
                }
            }
            catch (Exception e)
            {
            }

            return null;
        }

        @Override
        protected void onPostExecute(Person person)
        {
            super.onPostExecute(person);

            if (person == null)
            {
                goToInitial(true);
            }
            else
            {
                goToConfirm();
            }
        }
    }

}
