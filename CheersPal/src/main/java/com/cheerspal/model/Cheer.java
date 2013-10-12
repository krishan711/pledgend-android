package com.cheerspal.model;

import java.util.Date;

public class Cheer
{
    public long id;
    public String sender;
    public String receiver;
    public int amount;
    public String title;
    public Date sentTime;
    public Date claimTime;
    public boolean charity;

    public Cheer(long id, String sender, String receiver, int amount, String title, Date sentTime)
    {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.title = title;
        this.sentTime = sentTime;
    }

}