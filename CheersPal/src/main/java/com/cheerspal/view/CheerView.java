package com.cheerspal.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.cheerspal.R;
import com.cheerspal.model.Cheer;

public class CheerView extends FrameLayout
{
    private Cheer cheer;
    private TextView tvTitle;

    public CheerView(Context context)
    {
        this(context, null);
    }

    public CheerView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.view_cheer, this);

        tvTitle = (TextView) findViewById(R.id.tv_title);
    }

    public void setCheer(Cheer cheer)
    {
        this.cheer = cheer;

//        boolean isSent =

//        tvTitle.setText();
    }
}
