package com.casa.doughlator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class DetailActivity extends AppCompatActivity implements EditDialog.EditDialogListener,
        RecipeDialog.RecipeDialogListener
{
    private static final String TAG = "DetailActivity";
    private TextView tv;
    private TextView weightTv;
    private TextView currHydrationTv;
    private TextView maxHydrationTv;
    private TextView adjustmentTv;
    private ArrayList<Ingredient> ingList;
    private ListView list;
    private ItemAdapter adapter;
    private int lastPosition;
    private DoughRecipeStore ds;
    private DoughRecipe doughRecipe;
    private int recipeIndex;
    private Logger logger;
    private HydrabarAnimation hydrabarAnimation;
    private ImageView hydrabarIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView)findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);

        /* Configure actionbar */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setClickable(true);
        toolbar.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               RecipeDialog editRecipeDialog;
               Bundle bundle;

               bundle = new Bundle();

                /* send Recipe index and recipe name */
               bundle.putInt(ConstantContainer.POSITION_KEY, recipeIndex);
               bundle.putString(ConstantContainer.NAME_KEY, doughRecipe.getRecipeName());

                /* Create dialog object, set bundle and show */
               editRecipeDialog = new RecipeDialog();
               editRecipeDialog.setArguments(bundle);
               editRecipeDialog.show(getFragmentManager(), "RecipeDialog");

           }
        }
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmail();
            }
        });

        /* Create Logger */
        logger = new Logger(getApplicationContext());

        if(savedInstanceState==null)
        {
            /* We come up from parent activity */
            recipeIndex = getIntent().getIntExtra(ConstantContainer.POSITION_KEY, 0);
        }
        else
        {
            /* We come back from child activity */
            recipeIndex = savedInstanceState.getInt(ConstantContainer.POSITION_KEY);
        }

        /* Get Dough recipe store instance */
        ds = DoughRecipeStore.getInstance();

        /* Get recipe and ingredients  */
        doughRecipe = ds.getDoughRecipes().get(recipeIndex);
        ingList = doughRecipe.getIngredientsList();

        /* Set default adjustment mode */
        doughRecipe.setAdjustmentMode(DoughRecipe.ADJUST_BY_PER);

        /* Set toolbar title */
        getSupportActionBar().setTitle(doughRecipe.getRecipeName());

        /* Get reference to widgets */
        weightTv = (TextView)findViewById(R.id.weightTv);
        currHydrationTv = (TextView)findViewById(R.id.currHydrationTv);
        maxHydrationTv = (TextView)findViewById(R.id.maxHydrationTv);
        hydrabarIv = (ImageView)findViewById(R.id.hydrabarIv);
        adjustmentTv = (TextView)findViewById(R.id.adjustmentTextTv);

        list = (ListView)findViewById(R.id.list);
        adapter = new ItemAdapter(this, ingList);
        list.setAdapter(adapter);

        /* Create hydrabar with empty values */
        hydrabarAnimation = new HydrabarAnimation();

        /* Update textview contents */
        weightTv.setText(doughRecipe.getFormattedRecipeWeight());

        adjustmentTv.setText(
                doughRecipe.getAdjustmentMode()==DoughRecipe.ADJUST_BY_PER?
                "Ajuste por porcentaje":"Ajuste por peso");

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    EditDialog editDialog;
                    Bundle bundle = new Bundle();

                    //Pass string values and row position
                    bundle.putString(ConstantContainer.NAME_KEY,
                            ingList.get(position).getName());

                    bundle.putString(ConstantContainer.QTY_KEY,
                            ingList.get(position).getQtyString());

                    bundle.putString(ConstantContainer.PER_KEY,
                            ingList.get(position).getPerString());

                    bundle.putInt(ConstantContainer.POSITION_KEY,
                            position);

                    bundle.putBoolean(ConstantContainer.LIQUID_KEY,
                            ingList.get(position).isLiquid());

                    bundle.putBoolean(ConstantContainer.REFERENCE_KEY,
                            ingList.get(position).isReferenceIngredient());

                    bundle.putInt(ConstantContainer.DOUGH_ADJUSTMENT,
                            doughRecipe.getAdjustmentMode());

                    lastPosition = position;

                    /* Create dialog object, set bundle and show */
                    editDialog = new EditDialog();
                    editDialog.setArguments(bundle);
                    editDialog.show(getFragmentManager(), "EditDialog");
            }
        });

        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int index, long arg3) {
                final int lIndex;

                lIndex = index;

                if (lIndex == ConstantContainer.ZERO) {
                    logger.toast("No se puede borrar el ingrediente referencia");
                } else {
                    final AlertDialog.Builder b = new AlertDialog.Builder(DetailActivity.this);
                    b.setIcon(android.R.drawable.ic_dialog_alert);
                    b.setMessage("¿Desea borrar el ingrediente?");
                    b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            ingList.remove(lIndex);

                            doughRecipe.updateIngredientsValues();

                            ds.save(getApplicationContext());

                            adapter.notifyDataSetChanged();

                            logger.toast("Ingrediente borrado");

                        }
                    });
                    b.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    });

                    b.show();
                }

                return true;
            }
        });

        final ViewTreeObserver observer = maxHydrationTv.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.d(TAG, "onCreate(): View ready on parent view!!");

                // On these listener we know view positions
                float aPoint = hydrabarIv.getLeft();
                float bPoint = hydrabarIv.getWidth();
                hydrabarAnimation.setValues(currHydrationTv, aPoint, bPoint);

                ViewTreeObserver vto = maxHydrationTv.getViewTreeObserver();
                vto.removeOnGlobalLayoutListener(this);

                hydrabarAnimation.moveToPer(doughRecipe.getDoughHydration());
                currHydrationTv.setText(String.valueOf(doughRecipe.getFormattedDoughHydration()));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if(id == R.id.action_add_ingredient)
        {
            addIngredient();

            return true;
        }

        if(id == R.id.action_planner)
        {
            launchPlanner();

            return true;
        }

        if(id == R.id.action_recipe_copy)
        {
            duplicateRecipe();

            return true;
        }

        if(id == R.id.action_adjustment)
        {
            /* Toggle modes */
            if(doughRecipe.getAdjustmentMode()==DoughRecipe.ADJUST_BY_PER)
            {
                item.setTitle("Ajustar por porcentaje");
                adjustmentTv.setText("Ajuste por peso");

                doughRecipe.setAdjustmentMode(DoughRecipe.ADJUST_BY_QTY);
            }
            else if(doughRecipe.getAdjustmentMode()==DoughRecipe.ADJUST_BY_QTY)
            {
                item.setTitle("Ajustar por peso");
                adjustmentTv.setText("Ajuste por porcentaje");

                doughRecipe.setAdjustmentMode(DoughRecipe.ADJUST_BY_PER);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void duplicateRecipe()
    {
        boolean retVal;

        retVal = ds.duplicateAndAddToList(getApplicationContext(), recipeIndex);

        if(retVal)
        {
            logger.toast("Se ha duplicado la receta");
        }
        else
        {
            logger.toast("Error duplicando receta");
        }
    }

    public void addIngredient()
    {
        EditDialog editDialog;
        Bundle bundle;

        bundle = new Bundle();

        /* NO_POSITION means that a new row has to be added */
        bundle.putInt(ConstantContainer.POSITION_KEY, ConstantContainer.NO_POSITION);
        bundle.putInt(ConstantContainer.DOUGH_ADJUSTMENT,
                doughRecipe.getAdjustmentMode());

        /* Create dialog object, set bundle and show */
        editDialog = new EditDialog();
        editDialog.setArguments(bundle);
        editDialog.show(getFragmentManager(), "EditDialog");
    }

    public void launchPlanner()
    {
        Intent i = new Intent(DetailActivity.this, PlannerActivity.class);

        /* Send recipe index */
        i.putExtra(ConstantContainer.POSITION_KEY, recipeIndex);

        startActivity(i);
    }


    @Override
    public void onOkButtonClickEditDialogListener(Bundle bundle)
    {
        Ingredient ingredient;
        int currentPosition;
        Ingredient newIngredient;
        boolean wasReferenceIngredient = false;

        currentPosition = bundle.getInt(ConstantContainer.POSITION_KEY, ConstantContainer.ZERO);

        /* Add new ingredient */
        if(currentPosition == ConstantContainer.NO_POSITION)
        {
            if(doughRecipe.getAdjustmentMode()==DoughRecipe.ADJUST_BY_PER){

                newIngredient = new Ingredient(
                        bundle.getString(ConstantContainer.NAME_KEY),
                        "0",/* Default but not used*/
                        bundle.getString(ConstantContainer.PER_KEY),
                        bundle.getBoolean(ConstantContainer.REFERENCE_KEY),
                        bundle.getBoolean(ConstantContainer.LIQUID_KEY)
                        );
            }
            else{

                newIngredient = new Ingredient(
                        bundle.getString(ConstantContainer.NAME_KEY),
                        bundle.getString(ConstantContainer.QTY_KEY),/* Default but not used*/
                        "0",
                        bundle.getBoolean(ConstantContainer.REFERENCE_KEY),
                        bundle.getBoolean(ConstantContainer.LIQUID_KEY)
                );
            }

            /* Add new ingredient */
            ingList.add(newIngredient);

            ingredient = newIngredient;

            /* Sort list with new ingredient */
            doughRecipe.sortByReferenceIngredientsFirst();
        }
        /* Edit existing ingredient */
        else
        {
            ingredient = ingList.get(lastPosition);

            ingredient.setName(bundle.getString(ConstantContainer.NAME_KEY));

            if (lastPosition == ConstantContainer.ZERO) {
                ingredient.setQty(bundle.getString(ConstantContainer.QTY_KEY));
                ingredient.setReferenceIngredient(bundle.getBoolean(ConstantContainer.REFERENCE_KEY));
            } else {

                if(doughRecipe.getAdjustmentMode()==DoughRecipe.ADJUST_BY_PER) {
                    ingredient.setPer(bundle.getString(ConstantContainer.PER_KEY));
                }else{
                    ingredient.setQty(bundle.getString(ConstantContainer.QTY_KEY));
                }

                ingredient.setIsLiquid(bundle.getBoolean(ConstantContainer.LIQUID_KEY));

                 /* If ingredient was marked as referenced before */
                if(ingredient.isReferenceIngredient())
                {
                    /* And change status to not reference ingredient */
                    if(bundle.getBoolean(ConstantContainer.REFERENCE_KEY)==false)
                    {
                        /* Notify ingredient status changed */
                        wasReferenceIngredient = true;
                    }
                }

                ingredient.setReferenceIngredient(bundle.getBoolean(ConstantContainer.REFERENCE_KEY));
            }

            ingList.set(lastPosition, ingredient);
        }

        /* Re-compose recipe values */
        doughRecipe.notifyIngredientChanged(ingredient, wasReferenceIngredient);

        /* Update textview contents */
        weightTv.setText(doughRecipe.getFormattedRecipeWeight());

        /* Save changes */
        //ds.save(this);

        /* Move hydrationbar */
        currHydrationTv.setText(doughRecipe.getFormattedDoughHydration());
        hydrabarAnimation.moveToPer(doughRecipe.getDoughHydration());

        /* Notify changes to listview */
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelButtonClickEditDialogListener() {

    }

    protected void sendEmail()
    {
        Logger logger;
        String emailBody = null;
        String emailSubject;

        logger = new Logger(DetailActivity.this);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.setType("message/rfc822");

        /* Compose email body */
        emailSubject = doughRecipe.getRecipeName();

        emailBody = "¡Hola!\n\n";

        emailBody += "Prueba esta receta: ";

        emailBody += "\n\n";

        emailBody += "Ingredientes: \n\n";

        for(Ingredient i:ingList)
        {
            emailBody += i.toString();
            emailBody += "\n";
        }

        emailBody += "\n";
        emailBody += "Peso aproximado: " +
                doughRecipe.getFormattedRecipeWeight() + "\n";
        emailBody += "Tasa de hidratación: " +
                doughRecipe.getFormattedDoughHydration() + "\n";

        /* Load notes if exist */
        if(doughRecipe.getRecipePlanner().getNotesFileName()!=null)
        {
            String notes = ds.loadNotes(
                    this, doughRecipe.getRecipePlanner().getNotesFileName(),
                    "");

            if (notes != null)
            {
                emailBody += "\n";
                emailBody += "Elaboración:";
                emailBody += "\n";

                emailBody += notes;
            }
        }

        emailBody += "\n\n";
        emailBody += "Enviado desde Doughlator";

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

        try
        {
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            logger.toast("There is no email client installed.");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putInt(ConstantContainer.POSITION_KEY,recipeIndex);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        Log.d(TAG, "saving data...");

        ds.save(this);
    }

    @Override
    protected void onResume() {

        super.onResume();

       /* Sort list with new ingredient */
        doughRecipe.sortByReferenceIngredientsFirst();

        if(hydrabarAnimation!=null &&
                hydrabarAnimation.isReady()) {
            hydrabarAnimation.moveToPer(doughRecipe.getDoughHydration());
            currHydrationTv.setText(doughRecipe.getFormattedDoughHydration());
        }
    }

    @Override
    public void onOkButtonClickRecipeDialogListener(Bundle bundle)
    {
        String recipeName;
        boolean nameExist;
        int rowPosition;
        ArrayList<DoughRecipe> doughRecipes;

        doughRecipes = ds.getDoughRecipes();

        recipeName = bundle.getString(ConstantContainer.NAME_KEY);
        rowPosition = bundle.getInt(ConstantContainer.POSITION_KEY);

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
                for (DoughRecipe dr : doughRecipes) {
                    if (dr.getRecipeName().equals(recipeName)) {
                        /* Name already created */
                        nameExist = true;
                    }
                }

                if (nameExist == false) {

                    /* Set new name */
                    DoughRecipe dr = doughRecipes.get(rowPosition);
                    dr.setRecipeName(recipeName);

                    /* Save recipe */
                    ds.save(this);

                    /* Update toolbar title */
                    getSupportActionBar().setTitle(recipeName);

                } else {
                    logger.toast("Ya existe una receta con el mismo nombre");
                }
            }
        }
    }

    @Override
    public void onCancelButtonClickRecipeDialogListener() {

    }
}
