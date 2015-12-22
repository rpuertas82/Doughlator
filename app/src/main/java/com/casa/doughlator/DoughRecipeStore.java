package com.casa.doughlator;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

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

    public boolean duplicateAndAddToList(Context context, int recipeIndex)
    {
        DoughRecipe drOrig;
        DoughRecipe drCloned;
        boolean nameExist;
        boolean retVal;

        drOrig = ds.getDoughRecipes().get(recipeIndex);

        String newName = drOrig.getRecipeName()+context.getString(R.string.append_copy);

        /* Check for duplicated name */
        nameExist = ds.checkForDuplicatedRecipeName(newName, true);

        if(!nameExist)
        {
            drCloned = (DoughRecipe) drOrig.duplicate();

            drCloned.setRecipeName(newName);

            /*  Set flag to not builtin recipe */
            drCloned.setBuiltInRecipe(false);

            /* Add duplicated recipe to container */
            ds.getDoughRecipes().add(drCloned);

            /* Create a new notesfile name and attach to recipe  */
            String notesFileName = drCloned.getRecipePlanner().composeNotesFileName(newName,".nts");
            drCloned.getRecipePlanner().setNotesFileName(notesFileName);

            /* Copy to new notes file */
            if(drOrig.getRecipePlanner().getNotesFileName()!=null &&
                    !drOrig.getRecipePlanner().getNotesFileName().equals("")) {

                File notesFile = context.getFileStreamPath(drOrig.getRecipePlanner().getNotesFileName());

                if(notesFile.exists()) {
                    ds.copyFile(context,
                            drOrig.getRecipePlanner().getNotesFileName(),
                            drCloned.getRecipePlanner().getNotesFileName());
                }
            }

            retVal = true;
        }
        else
        {
            retVal = false;
        }

        return retVal;
    }


    public int loadRecipeListFromResource(Context context, int rawFileNameId, String loadedVolFlag)
    {
        /* Check associated loaded flag */
        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.app_load_prefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        boolean loadedVol = preferences.getBoolean(loadedVolFlag, false /*Default value*/);

        if(!loadedVol)
        {
            try
            {
                InputStream is = context.getResources().openRawResource(rawFileNameId);
                ObjectInputStream ois = new ObjectInputStream(is);

              /* Load into recipe store  */
                while (true)
                {
                    try
                    {
                        /* Add recipes to doughRecipe container */
                        DoughRecipe dr = (DoughRecipe) ois.readObject();
                        doughRecipes.add(dr);

                        /* Copy notes file to user directory */
                        if(dr.getRecipePlanner().getNotesFileName()!=null &&
                                !dr.getRecipePlanner().getNotesFileName().isEmpty()) {

                            copyNotesFromResource(
                                    context,
                                    dr.getRecipePlanner().getNotesFileName(), /* From */
                                    dr.getRecipePlanner().getNotesFileName());/* To */
                        }
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
            } catch (IOException e) {
                e.printStackTrace();
            }

            /* If load OK, set loaded_volX_flag to true */
            editor.putBoolean(loadedVolFlag,true);
            editor.commit();

            Log.d(TAG, "Recipe list added to user list");
        }
        else
        {
            /* Vol already loaded */
        }

        return 1;
    }

    public int load(Context context)
    {
        FileOutputStream fo = null;
        ObjectOutputStream oos = null;
        FileInputStream fi = null;
        int loadFile = 0;

        /*
         * Recipes will be only loaded from
         * resource/raw directory the first time
         * app is started on device or user reset
         * preferences settings.
         *
         * Once loaded, loaded_volX_flag will be set
         * to true.
         * */

        /* 1 - Load recipe volumes (first time app is started or reset preferences) */
        loadRecipeListFromResource(context, R.raw.recipelist_vol1,
                context.getString(R.string.loaded_vol1_flag));

        /* Add more recipes volumes here */

        /* 2 - Check for user recipe list file existence */
        try
        {
            fi = context.openFileInput(
                    context.getResources().getString(R.string.user_recipe_list));
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

        /* 3 - Load recipes from user recipe list */
        if(loadFile==ConstantContainer.ONE)
        {
            try
            {
                FileInputStream fInput =
                        context.openFileInput(context.getResources().getString(R.string.user_recipe_list));
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

            Log.d(TAG, "Data loaded from " + context.getResources().getString(R.string.user_recipe_list));
        }
        else
        {
            /* Do nothing, we assume that if we reach here
             * app has installed for first time */
        }

        /* 4 - After loaded, sort dough recipes container */
        Collections.sort(this.getDoughRecipes());

        /* Set loaded flag to avoid reloads during activity life */
        dataLoaded = true;

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
                    context.getResources().getString(R.string.user_recipe_list), context.MODE_PRIVATE);
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

        Log.d(TAG, "Data saved on " + context.getResources().getString(R.string.user_recipe_list));

        return 1;
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

    public boolean copyFile(Context context, String fromFileName, String toFileName)
    {
        boolean retVal = false;

        try
        {
            FileInputStream inStream = context.openFileInput(fromFileName);
            FileOutputStream outStream = context.openFileOutput(toFileName, Context.MODE_PRIVATE);
            FileChannel inChannel = inStream.getChannel();
            FileChannel outChannel = outStream.getChannel();

            inChannel.transferTo(0, inChannel.size(), outChannel);

            inStream.close();
            outStream.close();

            retVal = true;
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return retVal;
    }

    public boolean copyNotesFromResource(Context context, String fromFileName, String toFileName)
    {
        boolean retVal = false;
        int idFile;

        if(fromFileName!=null &&
                toFileName!=null)
        {
            StringBuilder sb = new StringBuilder("raw/");
            sb.append(fromFileName);
            fromFileName = sb.substring(0,sb.toString().indexOf(".nts"));

            idFile = context.getResources().getIdentifier(fromFileName, "raw", context.getPackageName());

            if(idFile<=0) {
                /* Do nothing */
            } else{

                try
                {
                    InputStream inStream = context.getResources().openRawResource(idFile);
                    FileOutputStream outStream = context.openFileOutput(toFileName, Context.MODE_PRIVATE);
                    byte[] buffer = new byte[inStream.available()];

                    /* Checkout stream to buffer */
                    inStream.read(buffer);

                    /* Write entire buffer to Outstream */
                    outStream.write(buffer);

                    inStream.close();
                    outStream.close();

                    retVal = true;
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return retVal;
    }

    public void restoreRecipesList(Context context, boolean restoreOnlyBuiltIn)
    {
        /* Set shared preferences flags to false */
        SharedPreferences preferences = context.getSharedPreferences(
                context.getString(R.string.app_load_prefs), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(context.getResources().getString(R.string.loaded_vol1_flag), false);

        /* Not used */
        //editor.putBoolean(context.getResources().getString(R.string.loaded_vol2_flag),false);
        //editor.putBoolean(context.getResources().getString(R.string.loaded_vol3_flag),false);

        editor.commit();

        /* Remove notes */
        for(DoughRecipe d: doughRecipes)
        {
            if(restoreOnlyBuiltIn)
            {
                /* Clear only builtin recipes */
                if(d.isBuiltInRecipe())
                {
                    String notesFileName = d.getRecipePlanner().getNotesFileName();

                    if(notesFileName!=null &&
                            !notesFileName.equals("")) {
                        File notesFile = context.getFileStreamPath(notesFileName);

                        if (notesFile.exists()) {
                            notesFile.delete();
                        }
                    }
                }
            }
            else
            {
                /* Clear all */
                String notesFileName = d.getRecipePlanner().getNotesFileName();

                if(notesFileName!=null &&
                        !notesFileName.equals("")) {
                    File notesFile = context.getFileStreamPath(notesFileName);

                    if (notesFile.exists()) {
                        notesFile.delete();
                    }
                }
            }


        }

        if(restoreOnlyBuiltIn)
        {
            /* Remove from list only builtin recipes */
            for(int i=0;i<doughRecipes.size();i++)
            {
                if(doughRecipes.get(i).isBuiltInRecipe())
                {
                    doughRecipes.remove(i);
                    i--;
                }
            }

            /* Save list */
            save(context);

             /* Once removed from recipe list, reload original */
            reloadContainer(context);
        }
        else {
            /* Remove user recipe list file */
            File recipeList = context.getFileStreamPath(
                    context.getResources().getString(R.string.user_recipe_list));

            recipeList.delete();

            /* Reload with res/raw data */
            reloadContainer(context);
        }
    }

    public void reloadContainer(Context context)
    {
        unload();

        load(context);
    }

    public boolean checkForDuplicatedRecipeName(String name, boolean ignoreCase)
    {
        boolean nameExist = false;

        /* Check for duplicated recipe name */
        for (DoughRecipe rd : doughRecipes) {

            if(ignoreCase) {
                if (rd.getRecipeName().equalsIgnoreCase(name)) {
                    /* Name already created */
                    nameExist = true;
                }
            }else{
                if (rd.getRecipeName().equals(name)) {
                    /* Name already created */
                    nameExist = true;
                }
            }
        }

        return nameExist;
    }

    public void unload()
    {
        this.getDoughRecipes().clear();

        this.dataLoaded = false;
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

    public void sortRecipeList()
    {
        Collections.sort(this.getDoughRecipes());
    }

}
