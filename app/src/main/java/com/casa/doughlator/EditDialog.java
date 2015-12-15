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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Casa on 18/11/15.
 */
public class EditDialog extends DialogFragment
{
    private EditDialogListener mCallBack;
    Bundle bundle;
    private int rowPosition;
    private int ajdustmentMode;

    public EditDialog()
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
        View v = inflater.inflate(R.layout.ingredientdialog_view, null);

        builder.setView(v);

        bundle = this.getArguments();

        rowPosition = bundle.getInt(ConstantContainer.POSITION_KEY);
        ajdustmentMode = bundle.getInt(ConstantContainer.DOUGH_ADJUSTMENT);

        TextView qtyPerTv = (TextView)v.findViewById(R.id.infoQtyTv);
        Button addIngBtn = (Button) v.findViewById(R.id.addBtn);
        Button cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
        final EditText ingEt = (EditText) v.findViewById(R.id.addIngEt);
        final EditText qtyEt = (EditText) v.findViewById(R.id.addQtyEt);
        final CheckBox isLiquidCb = (CheckBox)v.findViewById(R.id.isLiquidCb);
        final TextView dialogTitle = (TextView)v.findViewById(R.id.dialogTitleTv);

        String valuePassed;

        if(rowPosition==ConstantContainer.ZERO)
        {
            valuePassed = bundle.getString(ConstantContainer.QTY_KEY);
        }
        else
        {
            if(ajdustmentMode==DoughRecipe.ADJUST_BY_PER) {
                valuePassed = bundle.getString(ConstantContainer.PER_KEY);

                qtyPerTv.setText("Porcentaje:");
                qtyEt.setHint("Introduce el porcentaje");

            }else{
                valuePassed = bundle.getString(ConstantContainer.QTY_KEY);

                qtyPerTv.setText("Peso:");
                qtyEt.setHint("Introduce el peso");
            }
        }

        if(rowPosition == ConstantContainer.NO_POSITION)
        {
            dialogTitle.setText("Añadir ingrediente:");
        }
        else
        {
            dialogTitle.setText("Editar ingrediente:");

            ingEt.setText(bundle.getString(ConstantContainer.NAME_KEY));
            qtyEt.setText(valuePassed);
            isLiquidCb.setChecked(bundle.getBoolean(ConstantContainer.BOOLEAN_KEY));

            qtyEt.requestFocus();
        }

        addIngBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean parseOK = true;

                        /* Parse name field */
                        if(ingEt.getText().toString().equals(""))
                        {
                            ingEt.setHint("Especifique un nombre");
                            ingEt.setHintTextColor(Color.RED);

                            parseOK = false;
                        }
                        else {

                            /* Parse numeric field */
                            try {
                                float parsedValue = Float.parseFloat(qtyEt.getText().toString());
                            } catch (NumberFormatException e) {
                                parseOK = false;
                                qtyEt.setTextColor(Color.RED);
                            }
                        }

                        if(!parseOK)
                        {
                            /* Do nothing */
                        }
                        else
                        {
                            Bundle bundle = new Bundle();

                            bundle.putString(ConstantContainer.NAME_KEY, ingEt.getText().toString());
                            bundle.putInt(ConstantContainer.POSITION_KEY, rowPosition);

                            if (rowPosition == ConstantContainer.ZERO) {
                                bundle.putString(ConstantContainer.QTY_KEY, qtyEt.getText().toString());
                            } else {

                                /* Bundle parameters */
                                if(ajdustmentMode==DoughRecipe.ADJUST_BY_PER) {
                                    bundle.putString(ConstantContainer.PER_KEY, qtyEt.getText().toString());
                                }else{
                                    bundle.putString(ConstantContainer.QTY_KEY, qtyEt.getText().toString());
                                }

                                bundle.putBoolean(ConstantContainer.BOOLEAN_KEY, isLiquidCb.isChecked());
                            }

                            mCallBack.onOkButtonClickEditDialogListener(bundle);

                            dismiss();
                        }
                    }
                }
        );

        cancelBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mCallBack.onCancelButtonClickEditDialogListener();

                        dismiss();
                    }
                }

        );

        return builder.create();
    }

    public interface EditDialogListener
    {
        void onOkButtonClickEditDialogListener(Bundle bundle);
        void onCancelButtonClickEditDialogListener();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try
        {
            mCallBack = (EditDialogListener)activity;
        }
        catch (ClassCastException e)
        {
            e.printStackTrace();
        }
    }
}
