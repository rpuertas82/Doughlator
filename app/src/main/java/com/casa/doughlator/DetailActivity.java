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
        RecipeDialog.RecipeDialogListener, PrefermentDialog.PrefermentDialogListener
{
    private static final String TAG = "DetailActivity";
    private TextView tv;
    private TextView weightTv;
    private TextView currHydrationTv;
    private TextView maxHydrationTv;
    private TextView adjustmentTv;
    private TextView flourWeightTv;
    private TextView liquidWeightTv;
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

        /* Update values before print into list */
        doughRecipe.updateIngredientsValues();

        /* Set toolbar title */
        getSupportActionBar().setTitle(doughRecipe.getRecipeName());

        /* Get reference to widgets */
        weightTv = (TextView)findViewById(R.id.weightTv);
        currHydrationTv = (TextView)findViewById(R.id.currHydrationTv);
        maxHydrationTv = (TextView)findViewById(R.id.maxHydrationTv);
        hydrabarIv = (ImageView)findViewById(R.id.hydrabarIv);
        adjustmentTv = (TextView)findViewById(R.id.adjustmentTextTv);
        flourWeightTv = (TextView)findViewById(R.id.flourTv);
        liquidWeightTv = (TextView)findViewById(R.id.liquidTv);

        list = (ListView)findViewById(R.id.list);
        adapter = new ItemAdapter(this, ingList, doughRecipe);
        list.setAdapter(adapter);

        /* Create hydrabar with empty values */
        hydrabarAnimation = new HydrabarAnimation();

        /* Update textview contents */
        weightTv.setText(doughRecipe.getFormattedRecipeWeight());
        flourWeightTv.setText(doughRecipe.getFormattedReferencedIngredientsWeight());
        liquidWeightTv.setText(doughRecipe.getFormattedLiquidIngredientsWeight());

        adjustmentTv.setText(
                doughRecipe.getAdjustmentMode()==DoughRecipe.ADJUST_BY_PER?
                getString(R.string.adjust_by_per):getString(R.string.adjust_by_weight));

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
                    logger.toast(getString(R.string.cannot_delete_reference_ingredient));
                } else {
                    final AlertDialog.Builder b = new AlertDialog.Builder(DetailActivity.this);
                    b.setIcon(android.R.drawable.ic_dialog_alert);
                    b.setMessage(R.string.delete_ingredient_answer);
                    b.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            if(ingList.get(lIndex).isUsedAsPreferment())
                                doughRecipe.setPreferment(null);

                            ingList.remove(lIndex);

                            doughRecipe.updateIngredientsValues();

                            ds.save(getApplicationContext());

                            adapter.notifyDataSetChanged();

                            logger.toast(getString(R.string.ingredient_deleted));

                        }
                    });
                    b.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
                item.setTitle(R.string.adjust_by_per_menu);
                adjustmentTv.setText(R.string.adjust_by_weight);

                doughRecipe.setAdjustmentMode(DoughRecipe.ADJUST_BY_QTY);
            }
            else if(doughRecipe.getAdjustmentMode()==DoughRecipe.ADJUST_BY_QTY)
            {
                item.setTitle(R.string.adjust_by_weight_menu);
                adjustmentTv.setText(R.string.adjust_by_per);

                doughRecipe.setAdjustmentMode(DoughRecipe.ADJUST_BY_PER);
            }
        }

        if(id == R.id.action_add_preferment)
        {
            if(doughRecipe.getPreferment()!=null)
            {
                logger.toast("La receta ya tiene un prefermento");
            }
            else
            {
                showPrefermentDialog();
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

    public void duplicateRecipe()
    {
        boolean retVal;

        retVal = ds.duplicateAndAddToList(getApplicationContext(), recipeIndex);

        if(retVal)
        {
            logger.toast(getString(R.string.recipe_duplicate_ok));
        }
        else
        {
            logger.toast(getString(R.string.recipe_duplicate_ko));
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

    public void showPrefermentDialog()
    {
        PrefermentDialog prefermentDialog;
        Bundle bundle;

        bundle = new Bundle();

        bundle.putInt(ConstantContainer.DOUGH_ADJUSTMENT, doughRecipe.getAdjustmentMode());

        /* Create dialog object, set bundle and show */
        prefermentDialog = new PrefermentDialog();
        prefermentDialog.setArguments(bundle);
        prefermentDialog.show(getFragmentManager(), "PrefermentDialog");
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
        flourWeightTv.setText(doughRecipe.getFormattedReferencedIngredientsWeight());
        liquidWeightTv.setText(doughRecipe.getFormattedLiquidIngredientsWeight());

        /* Save changes */
        //ds.save(this);

        /* Move hydrationbar */
        currHydrationTv.setText(doughRecipe.getFormattedDoughHydration());
        hydrabarAnimation.moveToPer(doughRecipe.getDoughHydration());

        /* 1 - Sort by ingredient quantity (Decreasing order)*/
        doughRecipe.sortByIngredientsQuantity(false);

        /* 2 - Sort list with new ingredient */
        doughRecipe.sortByReferenceIngredientsFirst();

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
            startActivity(Intent.createChooser(emailIntent, "Enviar e-mail"));
        }
        catch (android.content.ActivityNotFoundException ex)
        {
            logger.toast(getString(R.string.no_mail_clients_installed));
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

        /* 1 - Sort by ingredient quantity (Decreasing order)*/
        doughRecipe.sortByIngredientsQuantity(false);

        /* 2 - Sort list with new ingredient */
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
                /* Check for duplicated recipe name */
                nameExist= ds.checkForDuplicatedRecipeName(recipeName, false);

                if (nameExist == false) {

                    /* Set new name */
                    DoughRecipe dr = doughRecipes.get(rowPosition);
                    dr.setRecipeName(recipeName);

                    /* Save recipe */
                    ds.save(this);

                    /* Update toolbar title */
                    getSupportActionBar().setTitle(recipeName);

                } else {
                    logger.toast(getString(R.string.recipe_name_duplicated));
                }
            }
        }
    }

    @Override
    public void onCancelButtonClickRecipeDialogListener() {

    }

    @Override
    public void onOkButtonClickPrefermentDialogListener(Bundle bundle) {
        boolean selectedValidPreferment;
        boolean addToDough;
        Ingredient prefermentIng;
        DoughRecipe preferment;
        int rowPosition;
        String value;

        addToDough = bundle.getBoolean(ConstantContainer.ADD_TO_DOUGH);
        selectedValidPreferment = bundle.getBoolean(ConstantContainer.SELECTED_VALID_PREFERMENT);
        rowPosition = bundle.getInt(ConstantContainer.POSITION_KEY);
        value = bundle.getString(
                doughRecipe.getAdjustmentMode()==DoughRecipe.ADJUST_BY_PER?
                        ConstantContainer.PER_KEY:ConstantContainer.QTY_KEY);

        if(selectedValidPreferment==true)
        {
            /* Get preferment */
            preferment = ds.getDoughRecipes().get(rowPosition);

            /* Convert to ingredient */
            prefermentIng = preferment.synthesize();

            /* Set qty/percentage */
            if(doughRecipe.getAdjustmentMode()==DoughRecipe.ADJUST_BY_QTY)
            {
                prefermentIng.setQty(value);
            }
            else
            {
                prefermentIng.setPer(value);
            }

            /* Add to ingredient list */
            ingList.add(prefermentIng);

            /* Notify doughrecipe */
            doughRecipe.setPreferment(prefermentIng);
            doughRecipe.notifyIngredientChanged(prefermentIng,false);

            /* 1 - Sort by ingredient quantity (Decreasing order)*/
            doughRecipe.sortByIngredientsQuantity(false);

            /* 2 - Sort list with new ingredient */
            doughRecipe.sortByReferenceIngredientsFirst();

            adapter.notifyDataSetChanged();

            logger.toast("Se ha añadido el prefermento");
        }
    }

    @Override
    public void onCancelButtonClickPrefermentDialogListener() {

    }
}
