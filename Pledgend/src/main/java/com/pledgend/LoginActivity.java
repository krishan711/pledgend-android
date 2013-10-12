package com.pledgend;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LoginActivity extends Activity
{
//    private EditText etUsername;
//    private View mLoginFormView;
//    private View mLoginStatusView;
//    private TextView mLoginStatusMessageView;

    private WebView webView;
    private AccessHelperConnect helper;
    private ProgressDialog progress;

    private static final String	CLIENT_ID		= "YOUR ID";
    private static final String	CLIENT_SECRET	= "YOUR SECRET";
    private static final String	ACCESS_DENIED	= "access_denied";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new PPWebViewClient());

        setContentView(webView);

        helper = AccessHelperConnect.init(CLIENT_ID, CLIENT_SECRET);

        progress = ProgressDialog.show(LoginActivity.this, "Loading", "Loading");

        webView.loadUrl(helper.getAuthUrl());
    }

    private class PPWebViewClient extends WebViewClient
    {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (progress != null && progress.isShowing()) {
                progress.dismiss();
                progress = null;
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            System.out.println("URL: " + url);
            if (url.contains(ACCESS_DENIED)) {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            } else if (url.startsWith(helper.getAccessCodeUrl())
                    && url.contains(helper.getCodeParameter())) {
                getAccessToken(url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        private void getAccessToken(String url) {
            final Uri uri = Uri.parse(url);
            final String code = uri.getQueryParameter("code");
            final String urlParams = helper.getTokenServiceParameters(code);
            final String urlString = helper.getTokenServiceUrl();

            new AsyncConnection(new AsyncConnectionListener() {
                public void connectionDone(String result) {
                    try {
                        final JSONObject object = new JSONObject(result);
                        final String accessToken = object
                                .getString("access_token");

                        if (accessToken != null && !accessToken.equals("")) {
                            getProfile(accessToken);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).execute(AsyncConnection.METHOD_POST, urlString, urlParams);
        }

        private void getProfile(String accessToken) {
            final String urlString = helper.getProfileUrl(accessToken);

            new AsyncConnection(new AsyncConnectionListener() {
                public void connectionDone(String result) {
                    setResult(RESULT_OK, new Intent().putExtra(
                            AccessHelperConnect.DATA_PROFILE, result));
                    finish();
                }
            }).execute(AsyncConnection.METHOD_GET, urlString);
        }
    }

    public static class AccessHelperConnect {
        public static final String	DATA_PROFILE		= Uri.encode("profile email address https://uri.paypal.com/services/paypalattributes");
        private static final String	URL_REDIRECT		= "http://localhost:3000/";

        private static final String	PARAM_CLIENT_ID		= "client_id=";
        private static final String	PARAM_CLIENT_SECRET	= "client_secret=";
        private static final String	PARAM_REDIRECT_URI	= "redirect_uri=";
        private static final String	PARAM_SCOPE			= "scope=";
        private static final String	PARAM_SCHEMA		= "schema=";
        private static final String	PARAM_RESPONSE_TYPE	= "response_type=";
        private static final String	PARAM_CODE			= "code=";
        private static final String	PARAM_ACCESS_TOKEN	= "access_token=";
        private static final String	PARAM_GRANT_TYPE	= "grant_type=authorization_code";
        private static final String	VALUE_RESPONSE_TYPE	= "code";
        private static final String	URL_AUTHORIZE		= "https://www.paypal.com/webapps/auth/protocol/openidconnect/v1/authorize";
        private static final String	URL_TOKENSERVICE	= "https://api.paypal.com/v1/identity/openidconnect/tokenservice";
        private static final String	URL_PROFILE			= "https://api.paypal.com/v1/identity/openidconnect/userinfo";

        private static final String	SCHEMA				= "openid";

        private static String		valueClientId		= null;
        private static String		valueClientSecret	= null;

        public static final String	TOKEN_URL			= URL_REDIRECT + "?scope";

        /**
         * Not going to be exposed.
         *
         * @param clientId
         * @param clientSecret
         */
        public AccessHelperConnect(final String clientId, final String clientSecret) {
            valueClientId = clientId;
            valueClientSecret = clientSecret;
        }

        /**
         * Initializes an instance of AccessHelper and returns it.
         *
         * @param clientId
         * @param clientSecret
         * @return the AccessHelper
         */
        public static AccessHelperConnect init(final String clientId,
                                               final String clientSecret) {
            return new AccessHelperConnect(clientId, clientSecret);
        }

        /**
         * Returns the application's authorization URL for PayPal Access.
         *
         * @return the authorization URL as {@link String}
         */
        public String getAuthUrl() {
            final StringBuilder authUrlBuilder = new StringBuilder();
            authUrlBuilder.append(URL_AUTHORIZE).append("?")
                    .append(PARAM_CLIENT_ID).append(valueClientId).append("&")
                    .append(PARAM_SCOPE).append(DATA_PROFILE).append("&")
                    .append(PARAM_REDIRECT_URI).append(Uri.encode(URL_REDIRECT))
                    .append("&").append(PARAM_RESPONSE_TYPE)
                    .append(VALUE_RESPONSE_TYPE);
            return authUrlBuilder.toString();
        }

        /**
         * Returns the Access Token url.
         *
         * @return the Access Token url
         */
        public String getTokenServiceUrl() {
            return URL_TOKENSERVICE;
        }

        /**
         * Creates the needed parameters to get the Authorization Token.
         *
         * @param code
         *            the code from the Token Service
         * @return the needed parameters
         */
        public String getTokenServiceParameters(final String code) {
            final StringBuilder paramsBuilder = new StringBuilder();
            paramsBuilder.append(PARAM_CLIENT_ID).append(valueClientId).append("&")
                    .append(PARAM_REDIRECT_URI).append(Uri.encode(URL_REDIRECT))
                    .append("&").append(PARAM_GRANT_TYPE).append("&")
                    .append(PARAM_CLIENT_SECRET).append(valueClientSecret)
                    .append("&").append(PARAM_CODE).append(code);
            return paramsBuilder.toString();
        }

        /**
         * Returns the URL for requesting profile information.
         *
         * @param accessToken
         * @return the profile url including the Access Token
         */
        public String getProfileUrl(final String accessToken) {
            final StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(URL_PROFILE).append("?").append(PARAM_SCHEMA)
                    .append(SCHEMA).append("&").append(PARAM_ACCESS_TOKEN)
                    .append(accessToken);
            return urlBuilder.toString();
        }

        /**
         * Returns the URL which can be converted to an URI to extract the access
         * code
         *
         * @return the callback URL
         */
        public String getAccessCodeUrl() {
            return TOKEN_URL;
        }

        /**
         * Returns the code parameter that can be used to check incoming URLs
         *
         * @return the code parameter
         */
        public String getCodeParameter() {
            return PARAM_CODE;
        }
    }

    public interface AsyncConnectionListener {
        public void connectionDone(String result);
    }

    public static class AsyncConnection extends AsyncTask<String, Void, String>
    {
        public static final String		METHOD_GET	= "GET";
        public static final String		METHOD_POST	= "POST";

        private AsyncConnectionListener	listener;

        public AsyncConnection(AsyncConnectionListener listener) {
            this.listener = listener;
        }

        @Override
        public String doInBackground(String... params) {
            final String method = params[0];
            final String urlString = params[1];

            final StringBuilder builder = new StringBuilder();

            try {
                final URL url = new URL(urlString);

                final HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.setRequestMethod(method);

                if (method.equals(METHOD_POST)) {
                    final String urlParams = params[2];
                    conn.setRequestProperty(HTTP.CONTENT_LEN,
                            "" + Integer.toString(urlParams.getBytes().length));
                    System.out.println(urlParams);
                    // Send request
                    final DataOutputStream wr = new DataOutputStream(
                            conn.getOutputStream());
                    wr.writeBytes(urlParams);
                    wr.flush();
                    wr.close();
                }

                // Get Response
                final InputStream is = conn.getInputStream();
                final BufferedReader rd = new BufferedReader(new InputStreamReader(
                        is));

                String line;
                while ((line = rd.readLine()) != null) {
                    builder.append(line);
                }
                rd.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return builder.toString();
        }

        @Override
        public void onPostExecute(String result) {
            listener.connectionDone(result);
        }

    }
}



//        setContentView(R.layout.activity_login);
//
//        // Set up the login form.
//        etUsername = (EditText) findViewById(R.id.text_username);
//
//        mLoginFormView = findViewById(R.id.login_form);
//        mLoginStatusView = findViewById(R.id.login_status);
//        mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
//
//        findViewById(R.id.btn_sign_in).setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                attemptLogin();
//            }
//        });
//    }
//
//    public void attemptLogin()
//    {
//        // Reset errors.
//        etUsername.setError(null);
//
//        String username = etUsername.getText().toString();
//
//        // Check for a valid email address.
//        if (TextUtils.isEmpty(username)) {
//            etUsername.setError(getString(R.string.login_error_username_required));
//            etUsername.requestFocus();
//            finish();
//        }
//
//        mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
//        showProgress(true);
//
//        JsonObject json = new JsonObject();
//        json.addProperty("userid", username);
//
//        try
//        {
//            JsonObject result = Ion.with(LoginActivity.this, WebStuff.LOGIN_URL)
//                    .setJsonObjectBody(json)
//                    .asJsonObject().get();
//
//            if (result == null)
//            {
//                Log.i("Pledgend", "result: null");
//            }
//            else
//            {
//                Log.i("Pledgend", "result: " + result.toString());
//            }
//
//            loggedIn();
//        }
//        catch (InterruptedException e)
//        {
//            e.printStackTrace();
//        }
//        catch (ExecutionException e)
//        {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Shows the progress UI and hides the login form.
//     */
//    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
//    private void showProgress(final boolean show) {
//        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
//        // for very easy animations. If available, use these APIs to fade-in
//        // the progress spinner.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginStatusView.setVisibility(View.VISIBLE);
//            mLoginStatusView.animate()
//                    .setDuration(shortAnimTime)
//                    .alpha(show ? 1 : 0)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
//                        }
//                    });
//
//            mLoginFormView.setVisibility(View.VISIBLE);
//            mLoginFormView.animate()
//                    .setDuration(shortAnimTime)
//                    .alpha(show ? 0 : 1)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                        }
//                    });
//        } else {
//            // The ViewPropertyAnimator APIs are not available, so simply show
//            // and hide the relevant UI components.
//            mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
//    }
//
//    public void loggedIn()
//    {
//        finish();
//    }
//
//}
