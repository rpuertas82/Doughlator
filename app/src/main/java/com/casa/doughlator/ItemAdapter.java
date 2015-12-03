package com.casa.doughlator;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by Casa on 15/11/15.
 */
public class ItemAdapter extends ArrayAdapter<Ingredient> {

    public ItemAdapter(Context c, List<Ingredient> items)
    {
        super(c, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ItemView itemView = (ItemView)convertView;

        if (null == itemView)
            itemView = ItemView.inflate(parent);

        itemView.setItem(getItem(position));

        return itemView;
    }
}
