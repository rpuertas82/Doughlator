package com.casa.doughlator;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.io.File;

public class PlannerActivity extends AppCompatActivity {

    private final static String TAG = "PlannerAct";
    DoughRecipeStore ds;
    DoughRecipe doughRecipe;
    Planner recipePlanner;
    private int recipeIndex;
    private String notesBuffer;
    private String notesFileName;
    Logger logger;
    EditText notesBoard;
    private boolean notesBoardInEditMode = false;
    private KeyListener cookie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_planner);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Configure toolbar */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recipeIndex = getIntent().getIntExtra(ConstantContainer.POSITION_KEY, 0);
        notesBoard = (EditText)findViewById(R.id.notesBoardEt);
        logger = new Logger(PlannerActivity.this);

        /* Get dough recipe store reference */
        ds = DoughRecipeStore.getInstance();

        /* Get dough recipe */
        doughRecipe = ds.getDoughRecipes().get(recipeIndex);

        setTitle(doughRecipe.getRecipeName());

        /* Get recipe planner */
        recipePlanner = doughRecipe.getRecipePlanner();

        /* Get recipe file name */
        notesFileName = recipePlanner.getNotesFileName();

        if(notesFileName==null ||
                notesFileName.equals(""))
        {
            /* We need to create a new fileName */
            notesFileName = recipePlanner.composeNotesFileName(doughRecipe.getRecipeName(), ".nts");

            /* Set new file name */
            recipePlanner.setNotesFileName(notesFileName);

            Log.d(TAG, "saving data...");

            /* We need to store in recipeList to ensure notesFileName is
             * saved  */
            ds.save(this);
        }

        /* Load notes into activity string buffer */
        notesBuffer = ds.loadNotes(PlannerActivity.this, notesFileName, doughRecipe.getRecipeName());

        if(notesBuffer == null)
        {
            logger.toast("No hay ninguna nota");
        }
        else
        {
            /* Fill notes board with recovered notes */
            notesBoard.getText().clear();
            notesBoard.setText(notesBuffer);
        }

        /* Disabled by default */
        setNotesBoardInEditMode(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_planner, menu);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Respond to the action bar's Up/Home button
        if(id == android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);

            return true;
        }

        if(id == R.id.action_delete)
        {
            deleteNotes();

            return true;
        }

        if(id == R.id.action_edit)
        {
            /* Toggle edition */
            if(notesBoardInEditMode == false)
            {
                notesBoardInEditMode = true;

                item.setTitle("OK");
            }
            else
            {
                notesBoardInEditMode = false;

                item.setTitle("Edit");

                if(Build.VERSION.SDK_INT < 21)
                {
                    item.setIcon(getResources().getDrawable(R.drawable.abc_edit_text_material));
                }
                else {
                    item.setIcon(getResources().getDrawable(R.drawable.abc_edit_text_material, null));
                }
            }

            setNotesBoardInEditMode(notesBoardInEditMode);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setNotesBoardInEditMode(boolean enabled)
    {
        if(enabled)
        {
            /* Enable edition */
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    getApplicationContext().INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(notesBoard.getWindowToken(), 0);

            if(cookie!=null)
                notesBoard.setKeyListener(cookie);
        }
        else
        {
            /* Disable edition */
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    getApplicationContext().INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(notesBoard.getWindowToken(), 0);

            /* Save reference to listener */
            cookie = notesBoard.getKeyListener();
            notesBoard.setKeyListener(null);
        }
    }

    private void deleteNotes()
    {
        final AlertDialog.Builder b = new AlertDialog.Builder(PlannerActivity.this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage("¿Desea borrar las notas?");
        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                boolean retVal = false;
                DoughRecipe dr;
                String notesFileName;

                    /* Delete attached notes file */
                dr = doughRecipe;

                notesFileName = dr.getRecipePlanner().getNotesFileName();

                if (notesFileName != null) {
                    File notesFile = getBaseContext().getFileStreamPath(notesFileName);

                    if (notesFile.exists()) {
                        if (!notesFile.delete()) {
                            retVal = false;
                        } else {
                                /* Re-compose notes file name */
                            notesFileName = recipePlanner.composeNotesFileName(doughRecipe.getRecipeName(), ".nts");
                            dr.getRecipePlanner().setNotesFileName(notesFileName);

                                /* Delete edit text content */
                            notesBoard.getText().clear();

                            //ds.save(getApplicationContext());
                            retVal = true;

                        }
                    }
                }

                if (retVal) {
                    logger.toast("Notas eliminadas");
                } else {
                    logger.toast("Error eliminando las notas");
                }
            }
        });

        b.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        b.show();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.d(TAG, "saving notes...");

        notesBuffer = notesBoard.getText().toString();

        /* Save notes into file */
        ds.saveNotes(PlannerActivity.this, notesFileName, notesBuffer);

        notesBuffer = null;
    }
}
