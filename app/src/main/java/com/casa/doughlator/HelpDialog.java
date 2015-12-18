package com.casa.doughlator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Casa on 18/11/15.
 */
public class HelpDialog extends DialogFragment
{
    Bundle bundle;
    private int rowPosition;

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

        /* Set help icon */
        //builder.setIcon(getResources().getDrawable());

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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }
}
