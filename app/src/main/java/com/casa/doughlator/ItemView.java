package com.casa.doughlator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Casa on 15/11/15.
 */
public class ItemView extends RelativeLayout
{
    private TextView mIngNameTV;
    private TextView mIngPerTV;
    private TextView mIngQtyTV;
    private Ingredient ingredient;

    public ItemView(Context c) {
        this(c, null);
    }

    public ItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.item_view_children, this, true);
        setupChildren();
    }

    private void setupChildren() {
        mIngNameTV = (TextView) findViewById(R.id.item_IngNameTextView);
        mIngPerTV = (TextView) findViewById(R.id.item_IngPerTextView);
        mIngQtyTV = (TextView) findViewById(R.id.item_IngQtyTextView);
    }

    public static ItemView inflate(ViewGroup parent)
    {
        ItemView itemView = (ItemView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view, parent, false);
        return itemView;
    }

    public Ingredient getItem() {
        return ingredient;
    }

    public void setItem(Ingredient item)
    {
        mIngNameTV.setText(item.getName());
        mIngPerTV.setText(item.getPerString() + "%");
        mIngQtyTV.setText(item.getQtyString() + " gr");
    }

    public TextView getmIngQtyTV() {
        return mIngQtyTV;
    }

    public void setmIngQtyTV(TextView mIngQtyTV) {
        this.mIngQtyTV = mIngQtyTV;
    }

    public TextView getmIngPerTV() {
        return mIngPerTV;
    }

    public void setmIngPerTV(TextView mIngPerTV) {
        this.mIngPerTV = mIngPerTV;
    }

    public TextView getmIngNameTV() {
        return mIngNameTV;
    }

    public void setmIngNameTV(TextView mIngNameTV) {
        this.mIngNameTV = mIngNameTV;
    }
}
