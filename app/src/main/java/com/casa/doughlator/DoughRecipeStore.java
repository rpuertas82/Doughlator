package com.casa.doughlator;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Casa on 13/11/15.
 */
public class DoughRecipeStore
{
    private final static String TAG = "DoughRecipeStore";
    private ArrayList<DoughRecipe> doughRecipes;
    private static DoughRecipeStore ds;
    private boolean dataLoaded;

    DoughRecipeStore()
    {
        doughRecipes = new ArrayList<DoughRecipe>();
        dataLoaded = false;
    }

    public static DoughRecipeStore getInstance()
    {
        if(ds==null)
        {
            ds = new DoughRecipeStore();
        }
        else
        {

        }

        return  ds;
    }

    public int load(Context context)
    {
        FileOutputStream fo = null;
        ObjectOutputStream oos = null;
        FileInputStream fi = null;
        int loadFile = 0;

        try
        {
            fi = context.openFileInput(
                    context.getResources().getString(R.string.RECIPE_LIST_FILE));
            fi.close();

            loadFile = ConstantContainer.ONE;
        }
        catch (FileNotFoundException e)
        {
            loadFile = ConstantContainer.ZERO;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        /* Load recipes from file */
        if(loadFile==ConstantContainer.ONE)
        {
            try
            {
                FileInputStream fInput =
                        context.openFileInput(context.getResources().getString(R.string.RECIPE_LIST_FILE));
                ObjectInputStream ois = new ObjectInputStream(fInput);

                /* Load into recipe store  */
                while(true)
                {
                    try
                    {
                        doughRecipes.add((DoughRecipe) ois.readObject());
                    }
                    catch (EOFException e)
                    {
                        ois.close();
                        break;
                    }
                    catch (InvalidClassException e)
                    {
                        e.printStackTrace();
                    }
                    catch (ClassNotFoundException e)
                    {
                        e.printStackTrace();
                    }
                }

                fInput.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            Log.d(TAG, "Data loaded from " + context.getResources().getString(R.string.RECIPE_LIST_FILE));

            dataLoaded = true;
        }
        else
        {
            /* Load into main pool */
            doughRecipes.add(new DoughRecipe("Pan básico"));
            doughRecipes.add(new DoughRecipe("Poolish"));
            doughRecipes.add(new DoughRecipe("Biga"));
            doughRecipes.add(new DoughRecipe("Pizza"));

            /* If it fails, make a toast*/
            Log.d(TAG,
                    "Creating recipe file " + context.getResources().getString(R.string.RECIPE_LIST_FILE));

            /* Save into recipe file */
            save(context);
        }

        return 1;
    }

    public int save(Context context)
    {
        FileOutputStream fo = null;
        ObjectOutputStream o = null;

        /* Create default file with default recipes */
        try
        {
            fo = context.openFileOutput(
                    context.getResources().getString(R.string.RECIPE_LIST_FILE), context.MODE_PRIVATE);
            o = new ObjectOutputStream(fo);

            /* Save into file */
            for (DoughRecipe d : doughRecipes)
            {
                o.writeObject(d);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Log.d(TAG, "Data saved on " + context.getResources().getString(R.string.RECIPE_LIST_FILE));

        return 1;
    }

    public ArrayList<String> getDoughRecipeNameList()
    {
        ArrayList<String> recipeNames;

        recipeNames = new ArrayList<String>();

        for(DoughRecipe dr:doughRecipes)
        {
            recipeNames.add(dr.getRecipeName());
        }

        return recipeNames;
    }

    public String loadNotes(Context context, String notesFileName, String recipeName)
    {
        String notes = null;
        String line;
        FileInputStream fi = null;
        int loadFile = 0;

        /* Check if notes file name was composed */
        if(notesFileName!=null)
        {

        }
        else
        {
            /* Compose file name */
            notesFileName = recipeName + "_notes.txt";
        }

        try
        {
            fi = context.openFileInput(notesFileName);
            fi.close();

            loadFile = ConstantContainer.ONE;
        }
        catch (FileNotFoundException e)
        {
            loadFile = ConstantContainer.ZERO;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        /* Load recipes from file */
        if(loadFile==ConstantContainer.ONE)
        {
            try
            {
                FileInputStream fInput =
                        context.openFileInput(notesFileName);
                InputStreamReader isr = new InputStreamReader(fInput);
                BufferedReader br = new BufferedReader(isr);

                /* Initialize notes */
                notes = "";

                /* Read line by line */
                while((line = br.readLine())!=null)
                {
                    notes += line +"\n";
                }

                br.close();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            Log.d(TAG, "Notes loaded from " + notesFileName);
        }
        else
        {
            /* User recipe file doesn´ exist, open default file */

            //TODO

            notes = null;
        }

        return notes;
    }

    public void saveNotes(Context context, String notesFileName, String notes)
    {
        FileOutputStream fo = null;
        OutputStreamWriter osw = null;

        /* Create default file with default recipes */
        try
        {
            fo = context.openFileOutput(notesFileName, context.MODE_PRIVATE);
            osw = new OutputStreamWriter(fo);

            osw.write(notes);

            osw.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        Log.d(TAG, "Notes saved on " + notesFileName);

    }

    public ArrayList<DoughRecipe> getDoughRecipes()
    {
        return doughRecipes;
    }

    public ArrayList<Ingredient> getIngredientsFromRecipeIndex(int index)
    {
        return doughRecipes.get(index).getIngredientsList();
    }

    public boolean isDataLoaded() {
        return dataLoaded;
    }
}
