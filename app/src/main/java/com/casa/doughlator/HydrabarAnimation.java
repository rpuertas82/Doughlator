package com.casa.doughlator;

import android.view.View;

/**
 * Created by Casa on 6/12/15.
 */
public class HydrabarAnimation
{
    private float xDiff;
    private float aPoint;
    private float bPoint;
    View v;

    public HydrabarAnimation(View v, float aPoint, float bPoint)
    {
        this.v = v;
        this.aPoint = aPoint;
        this.bPoint = bPoint;
    }

    public void moveToPer(float per)
    {
        float widht = v.getWidth();
        float xLeft;

        xLeft = per*xDiff / 100;

        /* Translate view to new x,y coordinates */
        v.setLeft((int) xLeft);
        v.setRight((int)(xLeft+widht));
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
            setxDiff(100-0);
        }
    }



}
