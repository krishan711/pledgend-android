package com.cheerspal.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.cheerspal.model.Cheer;
import com.cheerspal.view.CheerView;

import java.util.List;

public class CheerAdapter extends BaseAdapter
{
    private final Context context;
    private List<Cheer> cheers;

    public CheerAdapter(Context context)
    {
        this.context = context;
    }

    public void setCheers(List<Cheer> cheers)
    {
        this.cheers = cheers;
    }

    @Override
    public int getCount()
    {
        return cheers.size();
    }

    @Override
    public Cheer getItem(int position)
    {
        return cheers.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        CheerView view = (CheerView) convertView;
        if (view == null)
            view = new CheerView(context);

        view.setCheer(getItem(position));

        return view;
    }
}
