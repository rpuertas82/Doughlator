package com.casa.doughlator;

import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;

/**
 * Created by Casa on 6/12/15.
 */
public class HydrabarAnimation
{
    private static final String TAG = "DetailActivity";
    private float xDiff;
    private float aPoint;
    private float bPoint;
    private float viewLeft;
    private float viewWidth;
    View v;

    public HydrabarAnimation()
    {
        v = null;
    }

    public HydrabarAnimation(View v, float aPoint, float bPoint)
    {
        this.v = v;
        this.aPoint = aPoint;
        this.bPoint = bPoint;

        /* Keep track view position */
        this.viewLeft = v.getLeft();
        this.viewWidth = v.getWidth();

        setXAxle(aPoint,bPoint);
    }

    public boolean isReady()
    {
        boolean retVal;

        retVal = false;

        if(v!=null)
        {
            retVal = true;
        }

        return retVal;
    }
    public void setValues(View v, float aPoint, float bPoint)
    {
        this.v = v;
        this.aPoint = aPoint;
        this.bPoint = bPoint;

        /* Keep track view position */
        this.viewLeft = v.getLeft();
        this.viewWidth = v.getWidth();

        setXAxle(aPoint,bPoint);
    }

    public void moveToPer(float per)
    {
        //float widht = v.getWidth();
        float widht = this.viewWidth;
        float xLeft;

        xLeft = per*xDiff / ConstantContainer.ONE_HUNDRED;

        if((xLeft+widht)>=this.bPoint)
        {
            xLeft = this.bPoint - (widht);
        }
        else if((xLeft)<=this.aPoint)
        {
            xLeft = this.aPoint;
        }
        else
        {
            if(per>ConstantContainer.FIVE &&
                    per<ConstantContainer.NINETYFIVE)
            {
                /* Get the view center */
                xLeft = xLeft - widht / ConstantContainer.TWO;
            }
        }

        TranslateAnimation ta = new TranslateAnimation(this.viewLeft,xLeft,v.getTop(),v.getTop());
        ta.setFillAfter(true);
        ta.setDuration(ConstantContainer.ONE_THOUSAND);
        v.startAnimation(ta);

        Log.d(TAG, "Moving to viewLeft: " + xLeft +
                " viewWidth: " + widht);

        /* Save new view posistion */
        this.viewLeft = xLeft;
        this.viewWidth = widht;
    }

    private void setxDiff(float xDiff)
    {
        this.xDiff = xDiff;
    }

    public void setXAxle(float aPoint, float bPoint)
    {
        this.aPoint = aPoint;
        this.bPoint = bPoint;

        if(this.bPoint>=this.aPoint)
        {
            setxDiff(this.bPoint-this.aPoint);
        }
        else
        {
            /* Default values */
            setxDiff(ConstantContainer.ONE_HUNDRED - ConstantContainer.ZERO);
        }
    }



}
