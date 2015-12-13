package com.casa.doughlator;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

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

        if(notesFileName==null)
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_planner, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_settings)
        {
            return true;
        }

        // Respond to the action bar's Up/Home button
        if(id == android.R.id.home)
        {
            NavUtils.navigateUpFromSameTask(this);

            return true;
        }

        return super.onOptionsItemSelected(item);
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
