package com.cheerspal;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.cheerspal.model.Cheer;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SendCheerActivity extends Activity implements View.OnClickListener
{
    private ImageView ivCheers;
    private Button btnCheers;
    private File imageFile;
    private Cheer cheer;
    private TextView tvError;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        dispatchTakePictureIntent(1000);

        cheer = (Cheer) getIntent().getExtras().getSerializable("CHEER");

        setContentView(R.layout.activity_cheer);

        ivCheers = (ImageView) findViewById(R.id.iv_cheers);
        btnCheers = (Button) findViewById(R.id.btn_say_cheers);
        btnCheers.setOnClickListener(this);
        tvError = (TextView) findViewById(R.id.tv_error);
        tvError.setVisibility(View.GONE);
    }

    private void dispatchTakePictureIntent(int actionCode)
    {
        File pictureFileDir = getDir();
        pictureFileDir.mkdirs();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());
        String photoFile = "cheerspal_" + date + ".jpg";
        String filename = pictureFileDir.getPath() + File.separator + photoFile;

        imageFile = new File(filename);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(takePictureIntent, actionCode);
    }

    private File getDir()
    {
        File sdDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(sdDir, "cheerspal");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 1000 && resultCode == RESULT_OK)
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth()*0.4), (int) (bitmap.getHeight()*0.4), false);

            FileOutputStream out = null;
            try
            {
                out = new FileOutputStream(imageFile.getAbsolutePath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (out != null)
                {
                    try
                    {
                        out.close();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            Picasso.with(this).load(imageFile).into(ivCheers);
        }
        else
        {
            finish();
        }
    }

    @Override
    public void onClick(View v)
    {
        if (!isFinishing())
        {
            if (v == btnCheers)
            {
                btnCheers.setEnabled(false);
                dialog = ProgressDialog.show(SendCheerActivity.this, null, "Uploading your image...");
                new SendImageTask().execute();
            }
        }
    }

    private class SendImageTask extends AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... params)
        {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(WebStuff.PHOTO_URL);

            try
            {
                MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

                entity.addPart("id", new StringBody(cheer.id + ""));
                entity.addPart("image", new FileBody(imageFile));

                httpPost.setEntity(entity);

                HttpResponse response = httpClient.execute(httpPost);

                HttpEntity httpEntity = response.getEntity();
                String result = EntityUtils.toString(httpEntity);

                if (result != null)
                {
                    new JsonParser().parse(result).getAsJsonObject().get("Success").getAsString();
                    return true;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean success)
        {
            dialog.dismiss();
            if (success)
            {
                finish();
            }
            else
            {
                tvError.setVisibility(View.VISIBLE);
            }
        }
    }
}
