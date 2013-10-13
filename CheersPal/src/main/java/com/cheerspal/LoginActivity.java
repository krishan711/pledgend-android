package com.cheerspal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.cheerspal.model.Person;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity
{
    private WebView webView;
    private AccessHelperConnect helper;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new PPWebViewClient());

        setContentView(webView);

        helper = AccessHelperConnect.init(Constants.WEB_PAYPAL_CLIENT_ID, Constants.WEB_PAYPAL_CLIENT_SECRET);

        progress = ProgressDialog.show(LoginActivity.this, null, "Loading PayPal Login");

        webView.loadUrl(helper.getAuthUrl());
    }

    private class PPWebViewClient extends WebViewClient
    {
        @Override
        public void onPageFinished(WebView view, String url)
        {
            super.onPageFinished(view, url);
            if (progress != null && progress.isShowing())
            {
                progress.dismiss();
                progress = null;
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            if (url.contains("access_denied"))
            {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
            else if (url.startsWith(helper.getAccessCodeUrl()) && url.contains(helper.getCodeParameter()))
            {
                getAccessToken(url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        private void getAccessToken(String url)
        {
            final Uri uri = Uri.parse(url);
            final String code = uri.getQueryParameter("code");
            final String urlParams = helper.getTokenServiceParameters(code);
            final String urlString = helper.getTokenServiceUrl();

            new AsyncConnection(new AsyncConnection.Listener()
            {
                public void connectionDone(String result)
                {
                    try
                    {
                        final JSONObject object = new JSONObject(result);
                        final String accessToken = object.getString("access_token");

                        if (accessToken != null && !accessToken.equals(""))
                        {
                            getProfile(accessToken);
                        }
                    }
                    catch (JSONException e)
                    {
                        e.printStackTrace();
                    }
                }
            }).execute(AsyncConnection.METHOD_POST, urlString, urlParams);
        }

        private void getProfile(final String accessToken)
        {
            final String urlString = helper.getProfileUrl(accessToken);

            new AsyncConnection(new AsyncConnection.Listener()
            {
                public void connectionDone(String result)
                {
                    Log.i("cheerspal", "result: " + result);
                    Log.i("cheerspal", "access token: " + accessToken);

                    JsonObject resultObj = new JsonParser().parse(result).getAsJsonObject();
                    String userEmail = resultObj.get("email").getAsString().toLowerCase();
                    String firstName = resultObj.get("given_name").getAsString().toLowerCase();
                    String lastName = resultObj.get("family_name").getAsString().toLowerCase();

                    ((CheersPalApplication) getApplication()).accessToken = accessToken;
                    ((CheersPalApplication) getApplication()).user = new Person(firstName, lastName, userEmail);

                    startActivity(new Intent(LoginActivity.this, OverviewActivity.class));
                    finish();
                }
            }).execute(AsyncConnection.METHOD_GET, urlString);
        }
    }
}

