package com.casa.doughlator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.casa.doughlator.AddRecipeDialog.AddRecipeDialogListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AddRecipeDialogListener{

    private final static String TAG = "com.example.casa.listviewsample";
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
                AddRecipeDialog addRecipeDialog;
                Bundle bundle;

                bundle = new Bundle();

                /* NO_POSITION means that a new row has to be added */
                bundle.putInt(ConstantContainer.POSITION_KEY, ConstantContainer.NO_POSITION);

                /* Create dialog object, set bundle and show */
                addRecipeDialog = new AddRecipeDialog();
                addRecipeDialog.setArguments(bundle);
                addRecipeDialog.show(getFragmentManager(), "AddRecipeDialog");
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

        setTitle("Doughlator Alpha 1.2");

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
                b.setMessage("¿Desea borrar la receta?");
                b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
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
                                    logger.toast("Error borrando las notas");

                                }
                            }
                        }

                        doughRecipes.remove(lPosition);

                        ds.save(getApplicationContext());

                        adapter.notifyDataSetChanged();

                        logger.toast("Receta borrada");

                    }
                });
                b.setNegativeButton("No", new DialogInterface.OnClickListener() {
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOkButtonClick(Bundle bundle)
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
                nameExist = false;

            /* Check for duplicated recipe name */
                for (DoughRecipe rd : doughRecipes) {
                    if (rd.getRecipeName().equals(recipeName)) {
                    /* Name already created */
                        nameExist = true;
                    }
                }

                if (nameExist == false) {
                /* Create new dough recipe */
                    DoughRecipe doughRecipe = new DoughRecipe(recipeName);

                /* Add recipe to Dough recipe store */
                    doughRecipes.add(doughRecipe);

                /* Save recipe */
                    ds.save(this);

                /* Update List view */
                    adapter.notifyDataSetChanged();
                } else {
                    logger.toast("Ya existe una receta con el mismo nombre");
                }
            }
        }
    }

    @Override
    public void onCancelButtonClick() {

    }
}
