package com.casa.doughlator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Casa on 18/11/15.
 */
public class AddRecipeDialog extends DialogFragment
{
    private AddRecipeDialogListener mCallBack;
    Bundle bundle;
    private int rowPosition;

    public AddRecipeDialog()
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
        View v = inflater.inflate(R.layout.addrecipe_view, null);

        builder.setView(v);

        bundle = this.getArguments();

        rowPosition = bundle.getInt(ConstantContainer.POSITION_KEY);

        Button addIngBtn = (Button) v.findViewById(R.id.addBtn);
        Button cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
        final EditText recipeNameEt = (EditText) v.findViewById(R.id.recipeNameEt);

        addIngBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Bundle bundle = new Bundle();

                        bundle.putString(ConstantContainer.NAME_KEY, recipeNameEt.getText().toString());
                        bundle.putInt(ConstantContainer.POSITION_KEY, rowPosition);

                        mCallBack.onOkButtonClick(bundle);

                        dismiss();
                    }
                }
        );

        cancelBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mCallBack.onCancelButtonClick();

                        dismiss();
                    }
                }

        );

        return builder.create();
    }

    public interface AddRecipeDialogListener
    {
        void onOkButtonClick(Bundle bundle);
        void onCancelButtonClick();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try
        {
            mCallBack = (AddRecipeDialogListener)activity;
        }
        catch (ClassCastException e)
        {
            e.printStackTrace();
        }
    }
}
