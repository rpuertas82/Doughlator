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
import android.widget.TextView;

/**
 * Created by Casa on 18/11/15.
 */
public class RecipeDialog extends DialogFragment
{
    private RecipeDialogListener mCallBack;
    Bundle bundle;
    private int rowPosition;

    public RecipeDialog()
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
        View v = inflater.inflate(R.layout.recipedialog_view, null);

        builder.setView(v);

        bundle = this.getArguments();

        rowPosition = bundle.getInt(ConstantContainer.POSITION_KEY);

        Button addIngBtn = (Button) v.findViewById(R.id.addBtn);
        Button cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
        final EditText recipeNameEt = (EditText) v.findViewById(R.id.recipeNameEt);
        final TextView dialogTitle = (TextView)v.findViewById(R.id.dialogTitleTv);

        if(rowPosition == ConstantContainer.NO_POSITION)
        {
            dialogTitle.setText("Nueva receta:");
        }
        else
        {
            /* Recipe name edit */
            String recipeName = bundle.getString(ConstantContainer.NAME_KEY);

            dialogTitle.setText("Nuevo nombre:");

            recipeNameEt.setText(recipeName);
        }

        addIngBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Bundle bundle = new Bundle();

                        bundle.putString(ConstantContainer.NAME_KEY, recipeNameEt.getText().toString());
                        bundle.putInt(ConstantContainer.POSITION_KEY, rowPosition);

                        mCallBack.onOkButtonClickRecipeDialogListener(bundle);

                        dismiss();
                    }
                }
        );

        cancelBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mCallBack.onCancelButtonClickRecipeDialogListener();

                        dismiss();
                    }
                }

        );

        return builder.create();
    }

    public interface RecipeDialogListener
    {
        void onOkButtonClickRecipeDialogListener(Bundle bundle);
        void onCancelButtonClickRecipeDialogListener();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try
        {
            mCallBack = (RecipeDialogListener)activity;
        }
        catch (ClassCastException e)
        {
            e.printStackTrace();
        }
    }
}
