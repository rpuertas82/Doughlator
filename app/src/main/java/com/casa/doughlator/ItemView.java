package com.casa.doughlator;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by Casa on 15/11/15.
 */
public class ItemView extends RelativeLayout
{
    private ImageView mIngIconIV;
    private TextView mIngNameTV;
    private TextView mIngPerTV;
    private TextView mIngQtyTV;
    private TextView mIngPrefTV;
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
        mIngPrefTV = (TextView)findViewById(R.id.item_PrefermentTextView);
        mIngIconIV = (ImageView)findViewById(R.id.itemIconIV);
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

    public void setItem(Ingredient item, DoughRecipe doughRecipe)
    {
        float qty = 0;
        float toSubstract = 0;

        /* Set icon */
        if(item.isReferenceIngredient())
        {
            mIngIconIV.setImageResource(R.drawable.trigo);
        }
        else if(item.isLiquid())
        {
            mIngIconIV.setImageResource(R.drawable.gotas);
        }
        else if(item.isUsedAsPreferment())
        {
            mIngIconIV.setImageResource(R.drawable.prefermento);
        }
        else
        {
            mIngIconIV.setImageResource(R.drawable.null_image);
        }

        if(item.getName().length()>18)
        {
            StringBuilder sb = new StringBuilder(18);
            sb.append(item.getName().substring(0,15));
            sb.append("...");
            String trimmedName = sb.toString();

            mIngNameTV.setText(trimmedName);
        }
        else
        {
            mIngNameTV.setText(item.getName());
        }

        mIngPerTV.setText(item.getPerFormattedString());

        if(item.shouldSubstractPrefermentQty() &&
                doughRecipe.getPreferment()!=null)
        {
            if(item.isBaseIngredient())
            {
                toSubstract = doughRecipe.getPreferment().getPrefermentFlourQty();
            }

            if(item.isLiquid())
            {
                toSubstract = doughRecipe.getPreferment().getPrefermentHydrationQty();
            }

            qty = item.getQty() - toSubstract;

            String qtyValue = String.format(Locale.US, "%.1f gr", qty);

            if(qty<0)
            {
                mIngQtyTV.setTextColor(Color.RED);
            }
            else
            {
                mIngQtyTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            }

            mIngQtyTV.setText(qtyValue);

            String prefermentText = String.format("[%.1f - %.1f]",item.getQty(),toSubstract);

            mIngPrefTV.setVisibility(View.VISIBLE);
            mIngQtyTV.setTextSize(TypedValue.COMPLEX_UNIT_PT,15);
            mIngPrefTV.setText(prefermentText);
        }
        else
        {
            mIngPrefTV.setVisibility(View.GONE);
            mIngQtyTV.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            mIngQtyTV.setTextSize(TypedValue.COMPLEX_UNIT_PT, 15);
            mIngQtyTV.setText(item.getQtyFormattedString());
        }

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
