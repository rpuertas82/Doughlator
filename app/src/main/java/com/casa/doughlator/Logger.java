package com.casa.doughlator;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Casa on 24/11/15.
 */
public class Logger
{
    private Context context;

    Logger(Context context)
    {
        this.context = context;
    }

    public void toast(String msg)
    {
        Toast toast1 =
                Toast.makeText(context,
                        msg, Toast.LENGTH_SHORT);

        toast1.show();
    }
}
