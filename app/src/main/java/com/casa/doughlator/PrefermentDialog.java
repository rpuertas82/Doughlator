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
    private boolean selectedValidPreferment;

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

        ajdustmentMode = bundle.getInt(ConstantContainer.DOUGH_ADJUSTMENT);

        TextView qtyPerTv = (TextView)v.findViewById(R.id.infoQtyTv);
        Button addIngBtn = (Button) v.findViewById(R.id.addBtn);
        Button cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
        final EditText qtyEt = (EditText) v.findViewById(R.id.addQtyEt);
        final TextView dialogTitle = (TextView)v.findViewById(R.id.dialogTitleTv);
        final Spinner spinner = (Spinner) v.findViewById(R.id.selectPrefermentSp);

        /* Load preferments */
        ds = DoughRecipeStore.getInstance();
        doughRecipes = ds.getDoughRecipes();
        final ArrayList<DoughRecipe> prefermentRecipes = new ArrayList();

        rowPosition = ConstantContainer.ZERO;
        selectedValidPreferment = true;

        for(DoughRecipe d:doughRecipes)
        {
            if(d.isUseAsPreferment())
            {
                prefermentRecipes.add(d);
            }
        }

        if(prefermentRecipes.isEmpty())
        {
            //prefermentRecipes.add("No hay prefermentos");

            selectedValidPreferment = false;
        }

        spinner.setAdapter(new ArrayAdapter<DoughRecipe>(v.getContext(), android.R.layout.simple_spinner_item, prefermentRecipes));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                rowPosition = 0;
                selectedValidPreferment = false;

                /* Search selected recipe */
                for (DoughRecipe d : doughRecipes) {
                    if (d.hashCode() == prefermentRecipes.get(position).hashCode()) {
                        selectedValidPreferment = true;

                        break;
                    }

                    rowPosition++;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // vacio

            }
        });

        if(ajdustmentMode==DoughRecipe.ADJUST_BY_PER) {

            qtyPerTv.setText(R.string.percentage);
            qtyEt.setHint(R.string.put_percentage);

        }else{

            qtyPerTv.setText(R.string.weight);
            qtyEt.setHint(R.string.put_weight);
        }

        dialogTitle.setText(R.string.add_preferment);

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

                        if (!parseOK) {
                            /* Do nothing */
                        } else {
                            Bundle bundle = new Bundle();

                            bundle.putInt(ConstantContainer.POSITION_KEY, rowPosition);
                            bundle.putBoolean(ConstantContainer.SELECTED_VALID_PREFERMENT, selectedValidPreferment);

                            /* Bundle parameters */
                            if (ajdustmentMode == DoughRecipe.ADJUST_BY_PER) {
                                bundle.putString(ConstantContainer.PER_KEY, qtyEt.getText().toString());
                            } else {
                                bundle.putString(ConstantContainer.QTY_KEY, qtyEt.getText().toString());
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
