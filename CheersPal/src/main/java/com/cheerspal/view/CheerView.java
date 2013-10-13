package com.cheerspal.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.cheerspal.CheersPalApplication;
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

        boolean isSent = CheersPalApplication.getInstance().user.id.equals(cheer.sender.id);
        boolean isClaimed = cheer.claimTime != null;

        String text;
        int backgroundColor;

        if (isSent)
        {
            if (!isClaimed)
            {
                text = cheer.receiver.firstName + " hasn't claimed your " + cheer.title + " yet!";
                backgroundColor = Color.rgb(158, 113, 205);
            }
            else
            {
                text = cheer.receiver.firstName + " claimed your " + cheer.title;
                backgroundColor = Color.rgb(205, 113, 154);
            }
        }
        else
        {
            if (!isClaimed)
            {
                text = "You still need to claim " + cheer.sender.firstName + "'s " + cheer.title + "!";
                backgroundColor = Color.rgb(15, 183, 148);
            }
            else
            {
                text = "You enjoyed " + cheer.sender.firstName + "'s " + cheer.title;
                backgroundColor = Color.rgb(211, 207, 210);
            }
        }

        tvTitle.setText(text);
        setBackgroundColor(backgroundColor);
    }
}
