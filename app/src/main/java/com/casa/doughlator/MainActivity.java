package com.casa.doughlator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.casa.doughlator.RecipeDialog.RecipeDialogListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecipeDialogListener {

    private final static String TAG = "MainActivity";
    private ListView list;
    private ArrayAdapter<DoughRecipe> adapter;
    private ArrayList<DoughRecipe> doughRecipes;
    private DoughRecipeStore ds;
    private Logger logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                RecipeDialog addRecipeDialog;
                Bundle bundle;

                bundle = new Bundle();

                /* NO_POSITION means that a new row has to be added */
                bundle.putInt(ConstantContainer.POSITION_KEY, ConstantContainer.NO_POSITION);

                /* Create dialog object, set bundle and show */
                addRecipeDialog = new RecipeDialog();
                addRecipeDialog.setArguments(bundle);
                addRecipeDialog.show(getFragmentManager(), "RecipeDialog");
            }
        });

        /* Create Logger */
        logger = new Logger(getApplicationContext());

         /* Get recipe store instance */
        ds = DoughRecipeStore.getInstance();

        /* Only load recipes the first time activity is created */
        if(!ds.isDataLoaded())
        {
            ds.load(this);
        }

        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);

        /* Get data from data source */
        doughRecipes = ds.getDoughRecipes();

        /* Setup listview and set data */
        list = (ListView) findViewById(R.id.list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, doughRecipes);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                i.putExtra(ConstantContainer.NAME_KEY, doughRecipes.get(position).getRecipeName());
                i.putExtra(ConstantContainer.POSITION_KEY, position);
                startActivity(i);
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                final int lPosition = position;

                final AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
                b.setIcon(android.R.drawable.ic_dialog_alert);
                b.setMessage(R.string.delete_recipe_answer);
                b.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        DoughRecipe dr;
                        String notesFileName;

                        /* Delete attached notes file */
                        dr = doughRecipes.get(lPosition);
                        notesFileName = dr.getRecipePlanner().getNotesFileName();

                        if (notesFileName != null)
                        {
                            File notesFile = getBaseContext().getFileStreamPath(notesFileName);

                            if (notesFile.exists())
                            {
                                if(!notesFile.delete())
                                {
                                    logger.toast(getString(R.string.error_removing_recipe));

                                }
                            }
                        }

                        doughRecipes.remove(lPosition);

                        ds.save(getApplicationContext());

                        adapter.notifyDataSetChanged();

                        logger.toast(getString(R.string.recipe_removed));

                    }
                });
                b.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });

                b.show();

                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_restore_recipes){

            final AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setIcon(android.R.drawable.ic_dialog_alert);
            b.setMessage(R.string.delete_all_recipes_msg);
            b.setTitle(R.string.delete_recipes_answer);
            b.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    ds.restoreRecipesList(MainActivity.this, false /* Remove all*/);

                    adapter.notifyDataSetChanged();

                    logger.toast(getString(R.string.settings_restablished));
                }
            });

            b.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });

            b.show();

            return true;
        }

        if(id == R.id.action_restore_builtin)
        {
            final AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
            b.setIcon(android.R.drawable.ic_dialog_alert);
            b.setMessage(R.string.delete_only_original_msg);
            b.setTitle(R.string.delete_only_original_title);
            b.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    ds.restoreRecipesList(MainActivity.this, true /* Remove only builtin*/);

                    adapter.notifyDataSetChanged();

                    logger.toast(getString(R.string.settings_restablished));
                }
            });

            b.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });

            b.show();

            return true;
        }

        if(id == R.id.action_help)
        {
            showHelpDialog();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showHelpDialog()
    {
        HelpDialog helpDialog;
        Bundle bundle;

        /* Default, empty bundle */
        bundle = new Bundle();

            /* Create dialog object, set bundle and show */
        helpDialog = new HelpDialog();
        helpDialog.setArguments(bundle);
        helpDialog.show(getFragmentManager(), "HelpDialog");
    }

    @Override
    public void onOkButtonClickRecipeDialogListener(Bundle bundle)
    {
        String recipeName;
        boolean nameExist;

        recipeName = bundle.getString(ConstantContainer.NAME_KEY);

        if(recipeName!=null)
        {
            if (recipeName.isEmpty())
            {
            /* Do nothing */
            }
            else
            {
                /* Check for duplicated recipe name */
                nameExist = ds.checkForDuplicatedRecipeName(recipeName, true);

                if (nameExist == false) {
                    /* Create new dough recipe */
                    DoughRecipe doughRecipe = new DoughRecipe(recipeName);

                    /* Add recipe to Dough recipe store */
                    doughRecipes.add(doughRecipe);

                    /* Rearrange recipe container */
                    ds.sortRecipeList();

                    /* Save recipe */
                    ds.save(this);

                    /* Update List view */
                    adapter.notifyDataSetChanged();
                } else {
                    logger.toast(getString(R.string.duplicated_recipe_name_error));
                }
            }
        }
    }

    @Override
    public void onCancelButtonClickRecipeDialogListener() {

    }

    @Override
    protected void onResume()
    {
        super.onResume();

        /* Repaint list */
        adapter.notifyDataSetChanged();

        Log.d(TAG, "onResume called");
    }

    @Override
    protected void onPause() {

        /* Save data */
        ds.save(this);

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        /* Free memory */
        ds.unload();

        super.onDestroy();
    }
}
