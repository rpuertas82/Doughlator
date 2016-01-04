package com.casa.doughlator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Casa on 18/11/15.
 */
public class PrefermentDialog extends DialogFragment
{
    private PrefermentDialogListener mCallBack;
    Bundle bundle;
    private int rowPosition;
    private int ajdustmentMode;
    private ArrayList<DoughRecipe> doughRecipes;
    private DoughRecipeStore ds;

    public PrefermentDialog()
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
        View v = inflater.inflate(R.layout.prefermentdialog_view, null);

        builder.setView(v);

        bundle = this.getArguments();

        rowPosition = bundle.getInt(ConstantContainer.POSITION_KEY);
        ajdustmentMode = bundle.getInt(ConstantContainer.DOUGH_ADJUSTMENT);

        TextView qtyPerTv = (TextView)v.findViewById(R.id.infoQtyTv);
        Button addIngBtn = (Button) v.findViewById(R.id.addBtn);
        Button cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
        final EditText qtyEt = (EditText) v.findViewById(R.id.addQtyEt);
        final CheckBox isLiquidCb = (CheckBox)v.findViewById(R.id.isLiquidCb);
        final CheckBox isReferenceCb = (CheckBox)v.findViewById(R.id.isReferenceCb);
        final TextView dialogTitle = (TextView)v.findViewById(R.id.dialogTitleTv);

        Spinner spinner = (Spinner) v.findViewById(R.id.selectPrefermentSp);
        String[] valores = {"uno","dos","tres","cuatro","cinco","seis", "siete", "ocho"};

        /* Load prefermentents */
        ds = DoughRecipeStore.getInstance();
        doughRecipes = ds.getDoughRecipes();
        ArrayList<String> prefermentRecipes = new ArrayList();

        for(DoughRecipe d:doughRecipes)
        {
            if(d.isUseAsPreferment())
            {
                prefermentRecipes.add(d.getRecipeName());
            }
        }

        spinner.setAdapter(new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_item, prefermentRecipes));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                // vacio

            }
        });

          /* Code below implements toggle behaviour avoiding set both checkbox*/
        isLiquidCb.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {

                                              if(isLiquidCb.isChecked())
                                              {
                                                  isReferenceCb.setChecked(false);
                                              }
                                          }
                                      }
        );

        isReferenceCb.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {

                                                 if(isReferenceCb.isChecked()){
                                                     isLiquidCb.setChecked(false);
                                                 }
                                             }
                                         }
        );

        String valuePassed;

        if(rowPosition==ConstantContainer.ZERO)
        {
            valuePassed = bundle.getString(ConstantContainer.QTY_KEY);
            isReferenceCb.setEnabled(false);
            isLiquidCb.setEnabled(false);
        }
        else
        {
            isReferenceCb.setEnabled(true);
            isLiquidCb.setEnabled(true);

            if(ajdustmentMode==DoughRecipe.ADJUST_BY_PER) {
                valuePassed = bundle.getString(ConstantContainer.PER_KEY);

                qtyPerTv.setText(R.string.percentage);
                qtyEt.setHint(R.string.put_percentage);

            }else{
                valuePassed = bundle.getString(ConstantContainer.QTY_KEY);

                qtyPerTv.setText(R.string.weight);
                qtyEt.setHint(R.string.put_weight);
            }
        }

        dialogTitle.setText(R.string.add_preferment);

        qtyEt.setText(valuePassed);
        isLiquidCb.setChecked(bundle.getBoolean(ConstantContainer.LIQUID_KEY));
        isReferenceCb.setChecked(bundle.getBoolean(ConstantContainer.REFERENCE_KEY));

        addIngBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean parseOK = true;

                        /* Parse numeric field */
                        try {
                            float parsedValue = Float.parseFloat(qtyEt.getText().toString());
                        } catch (NumberFormatException e) {
                            parseOK = false;
                            qtyEt.setTextColor(Color.RED);
                        }

                        if(!parseOK)
                        {
                            /* Do nothing */
                        }
                        else
                        {
                            Bundle bundle = new Bundle();

                            bundle.putInt(ConstantContainer.POSITION_KEY, rowPosition);

                            if (rowPosition == ConstantContainer.ZERO) {
                                bundle.putString(ConstantContainer.QTY_KEY, qtyEt.getText().toString());
                                bundle.putBoolean(ConstantContainer.REFERENCE_KEY, isReferenceCb.isChecked());
                            } else {

                                /* Bundle parameters */
                                if(ajdustmentMode==DoughRecipe.ADJUST_BY_PER) {
                                    bundle.putString(ConstantContainer.PER_KEY, qtyEt.getText().toString());
                                }else{
                                    bundle.putString(ConstantContainer.QTY_KEY, qtyEt.getText().toString());
                                }

                                bundle.putBoolean(ConstantContainer.LIQUID_KEY, isLiquidCb.isChecked());
                                bundle.putBoolean(ConstantContainer.REFERENCE_KEY, isReferenceCb.isChecked());
                            }

                            mCallBack.onOkButtonClickPrefermentDialogListener(bundle);

                            dismiss();
                        }
                    }
                }
        );

        cancelBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mCallBack.onCancelButtonClickPrefermentDialogListener();

                        dismiss();
                    }
                }

        );

        return builder.create();
    }

    public interface PrefermentDialogListener
    {
        void onOkButtonClickPrefermentDialogListener(Bundle bundle);
        void onCancelButtonClickPrefermentDialogListener();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try
        {
            mCallBack = (PrefermentDialogListener)activity;
        }
        catch (ClassCastException e)
        {
            e.printStackTrace();
        }
    }
}
