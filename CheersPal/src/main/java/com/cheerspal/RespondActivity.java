package com.cheerspal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.cheerspal.model.Cheer;

public class RespondActivity extends Activity implements View.OnClickListener
{
    private Button btnCharity, btnSpend;
    private Cheer cheer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_respond);

        cheer = (Cheer) getIntent().getExtras().getSerializable("CHEER");

        btnCharity = (Button) findViewById(R.id.btn_charity);
        btnCharity.setOnClickListener(this);
        btnSpend = (Button) findViewById(R.id.btn_spend);
        btnSpend.setOnClickListener(this);
        btnSpend.setText("Buy a " + cheer.title);
    }

    @Override
    public void onClick(View view)
    {
        if (view == btnSpend)
        {
            Intent intent = new Intent(this, SendCheerActivity.class);
            intent.putExtra("CHEER", cheer);
            startActivity(intent);
            finish();
        }
        else if (view == btnCharity)
        {
            Toast.makeText(this, "Not implemented yet", Toast.LENGTH_LONG).show();
        }
    }
}
