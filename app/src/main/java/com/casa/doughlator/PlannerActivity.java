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
            logger.toast(getString(R.string.notes_not_found));
        }
        else
        {
            /* Fill notes board with recovered notes */
            notesBoard.getText().clear();
            notesBoard.setText(notesBuffer);
        }

        /* Disabled by default */
        setNotesBoardInEditMode(false, null);
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

                 /* Enable / disable edition */
                setNotesBoardInEditMode(notesBoardInEditMode, item);

                logger.toast(getString(R.string.edit_mode_enabled));
            }
            else
            {
                notesBoardInEditMode = false;

                /* Enable / disable edition */
                setNotesBoardInEditMode(notesBoardInEditMode, item);

                logger.toast(getString(R.string.edit_mode_disabled));
            }

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

    private void setNotesBoardInEditMode(boolean enabled, MenuItem item)
    {
        if(enabled)
        {
            if(item!=null) {
                item.setTitle(R.string.ok);

                if (Build.VERSION.SDK_INT < 21) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_save_white_24dp));
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_save_white_24dp, null));
                }
            }

            /* Enable edition */
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    getApplicationContext().INPUT_METHOD_SERVICE);

            imm.showSoftInput(notesBoard, InputMethodManager.SHOW_IMPLICIT);

            if(cookie!=null)
                notesBoard.setKeyListener(cookie);

            notesBoard.requestFocus();
        }
        else
        {
            if(item!=null) {
                item.setTitle("Edit");

                if (Build.VERSION.SDK_INT < 21) {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_create_white_24dp));
                } else {
                    item.setIcon(getResources().getDrawable(R.drawable.ic_create_white_24dp, null));
                }
            }

            /* Disable edition */
            InputMethodManager imm = (InputMethodManager)getSystemService(
                    getApplicationContext().INPUT_METHOD_SERVICE);

            imm.hideSoftInputFromWindow(notesBoard.getWindowToken(), 0);

            /* Save reference to listener */
            cookie = notesBoard.getKeyListener();
            notesBoard.setKeyListener(null);

            notesBoard.clearFocus();
        }
    }

    private void deleteNotes()
    {
        final AlertDialog.Builder b = new AlertDialog.Builder(PlannerActivity.this);
        b.setIcon(android.R.drawable.ic_dialog_alert);
        b.setMessage(R.string.remove_notes_msg);
        b.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
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

                            //ds.save(getApplicationContext());
                            retVal = true;

                        }
                    }
                }

                /* Delete edit text content */
                notesBoard.getText().clear();

                  /* Disable edition */
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        getApplicationContext().INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(notesBoard.getWindowToken(), 0);

                if (retVal) {
                    logger.toast(getString(R.string.notes_deleted));
                } else {
                   /* Do nothing */
                }
            }
        });

        b.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
