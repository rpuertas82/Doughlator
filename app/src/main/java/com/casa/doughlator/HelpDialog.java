package com.casa.doughlator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Casa on 18/11/15.
 */
public class HelpDialog extends DialogFragment
{
    private int rowPosition;
    private String helpContent;
    Bundle bundle;

    public HelpDialog()
    {

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        return createDialog();
    }

    public AlertDialog createDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.helpdialog_view, null);

        builder.setView(v);

        bundle = this.getArguments();

        Button addIngBtn = (Button) v.findViewById(R.id.addBtn);
        final TextView dialogTitle = (TextView)v.findViewById(R.id.dialogTitleTv);
        final WebView helpContentWv = (WebView)v.findViewById(R.id.webViewWv);

        /* Set help icon */
        if(Build.VERSION.SDK_INT < 21)
        {
            builder.setIcon(getResources().getDrawable(R.drawable.ic_help_white_24dp));
        }
        else
        {
            builder.setIcon(getResources().getDrawable(R.drawable.ic_help_white_24dp,null));
        }

        /* Load and display help content */
        helpContent = loadHelpContentFromResource(
                getActivity().getApplicationContext(),R.raw.example);

        helpContentWv.loadData(helpContent,"text/html", "utf-8");

        addIngBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dismiss();
                    }
                }
        );

        return builder.create();
    }

    public String loadHelpContentFromResource(Context context, int rawFileNameId)
    {
        StringBuilder contentBuilder = new StringBuilder();
        String str;
        String content;

        try {

            InputStream is = context.getResources().openRawResource(rawFileNameId);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            while ((str = br.readLine()) != null) {
                contentBuilder.append(str);
            }
            br.close();
        } catch (IOException e) {
        }

        content = contentBuilder.toString();

        return content;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }
}
