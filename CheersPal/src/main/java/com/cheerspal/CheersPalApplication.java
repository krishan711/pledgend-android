package com.cheerspal;

import android.app.Application;
import com.cheerspal.model.Person;

public class CheersPalApplication extends Application
{
    public Person user;
    public String accessToken;

    public static CheersPalApplication instance;

    public static CheersPalApplication getInstance()
    {
        return instance;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        instance = this;
    }
}

