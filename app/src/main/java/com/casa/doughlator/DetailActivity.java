package com.casa.doughlator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity implements EditDialog.EditDialogListener
{
    private static final String TAG = "DetailActivity";
    private TextView tv;
    private TextView weightTv;
    private TextView currHydrationTv;
    private TextView maxHydrationTv;
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

        setTitle("Detail of " + getIntent().getStringExtra(ConstantContainer.NAME_KEY));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                sendEmail();
            }
        });

        /* Create Logger */
        logger = new Logger(getApplicationContext());

        /* Get list index from mainactivity */
        recipeIndex = getIntent().getIntExtra(ConstantContainer.POSITION_KEY, 0);

        /* Get Dough recipe store instance */
        ds = DoughRecipeStore.getInstance();

        /* Get recipe and ingredients  */
        doughRecipe = ds.getDoughRecipes().get(recipeIndex);
        ingList = doughRecipe.getIngredientsList();

        /* Get reference to widgets */
        weightTv = (TextView)findViewById(R.id.weightTv);
        currHydrationTv = (TextView)findViewById(R.id.currHydrationTv);
        maxHydrationTv = (TextView)findViewById(R.id.maxHydrationTv);
        hydrabarIv = (ImageView)findViewById(R.id.hydrabarIv);

        list = (ListView)findViewById(R.id.list);
        adapter = new ItemAdapter(this, ingList);
        list.setAdapter(adapter);

        /* Create hydrabar with empty values */
        hydrabarAnimation = new HydrabarAnimation();

        /* Update textview contents */
        weightTv.setText(String.valueOf(doughRecipe.getRecipeWeight()) + "gr.");

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

                bundle.putBoolean(ConstantContainer.BOOLEAN_KEY,
                        ingList.get(position).isLiquid());

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
                currHydrationTv.setText(doughRecipe.getDoughHydration() + "%");
            }
        });
    }

    public void addIngredient(View v)
    {
        EditDialog editDialog;
        Bundle bundle;

        bundle = new Bundle();

        /* NO_POSITION means that a new row has to be added */
        bundle.putInt(ConstantContainer.POSITION_KEY, ConstantContainer.NO_POSITION);

        /* Create dialog object, set bundle and show */
        editDialog = new EditDialog();
        editDialog.setArguments(bundle);
        editDialog.show(getFragmentManager(), "EditDialog");
    }

    public void launchPlanner(View v)
    {
        Intent i = new Intent(DetailActivity.this, PlannerActivity.class);

        /* Send recipe index */
        i.putExtra(ConstantContainer.POSITION_KEY, recipeIndex);

        startActivity(i);
    }


    @Override
    public void onOkButtonClick(Bundle bundle)
    {
        Ingredient ingredient;
        int currentPosition;
        Ingredient newIngredient;

        currentPosition = bundle.getInt(ConstantContainer.POSITION_KEY, ConstantContainer.ZERO);

        /* Add new ingredient */
        if(currentPosition == ConstantContainer.NO_POSITION)
        {
            newIngredient = new Ingredient(
                    bundle.getString(ConstantContainer.NAME_KEY),
                    "0",/* Default but not used*/
                    bundle.getString(ConstantContainer.PER_KEY),
                    false,
                    bundle.getBoolean(ConstantContainer.BOOLEAN_KEY)
            );

            /* Add new ingredient */
            ingList.add(newIngredient);
        }
        /* Edit existing ingredient */
        else
        {
            ingredient = ingList.get(lastPosition);

            ingredient.setName(bundle.getString(ConstantContainer.NAME_KEY));

            if (lastPosition == ConstantContainer.ZERO) {
                ingredient.setQty(bundle.getString(ConstantContainer.QTY_KEY));
            } else {
                ingredient.setPer(bundle.getString(ConstantContainer.PER_KEY));
                ingredient.setIsLiquid(bundle.getBoolean(ConstantContainer.BOOLEAN_KEY));
            }

            ingList.set(lastPosition, ingredient);
        }

        /* Notify changes to recipe store */
        doughRecipe.updateIngredientsValues();

        /* Update textview contents */
        weightTv.setText(String.valueOf(doughRecipe.getRecipeWeight())+"gr.");

        /* Save changes */
        //ds.save(this);

        /* Move hydrationbar */
        currHydrationTv.setText(doughRecipe.getDoughHydration() + "%");
        hydrabarAnimation.moveToPer(doughRecipe.getDoughHydration());

        /* Notify changes to listview */
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCancelButtonClick() {

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
                String.valueOf(doughRecipe.getRecipeWeight()) + " gr" + "\n";
        emailBody += "Tasa de hidratación: " +
                String.valueOf(doughRecipe.getDoughHydration()) + "%" + "\n";

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
    protected void onPause()
    {
        super.onPause();

        Log.d(TAG, "saving data...");

        ds.save(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(hydrabarAnimation!=null &&
                hydrabarAnimation.isReady()) {
            hydrabarAnimation.moveToPer(doughRecipe.getDoughHydration());
            currHydrationTv.setText(doughRecipe.getDoughHydration() + "%");
        }
    }
}
