package com.casa.doughlator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Casa on 20/11/15.
 */
public class Recipe implements Serializable, Cloneable
{
    /* serialVersionUID has to be overloaded in order to
    * avoid InvalidClassException in serialization */
    private final static long serialVersionUID = 100L;
    private String recipeName;
    private float recipeWeight;
    private Planner recipePlanner;
    private String recipeNotes;
    private boolean builtInRecipe;
    protected ArrayList<Ingredient> ingredients;

    public Recipe()
    {
        recipePlanner = new Planner();
    }

    public Recipe(String recipeName)
    {
        recipePlanner = new Planner();
        this.recipeName = recipeName;
        ingredients = new ArrayList<Ingredient>();
    }

    public float getRecipeWeight()
    {
        recipeWeight = 0;

        for(Ingredient i:ingredients)
        {
            if(i.isUsedAsPreferment())
            {
                recipeWeight += i.getPrefermentQty();
            }
            else
            {
                recipeWeight += i.getQty();
            }
        }

        return recipeWeight;
    }

    public String getFormattedRecipeWeight()
    {
        String formattedValue = String.format(Locale.US, "%.1f gr.", getRecipeWeight());

        return formattedValue;
    }

    public void setIngredientQtyAtIndex(int index, String qty)
    {
        Ingredient i = ingredients.get(index);

        i.setQty(qty);

        ingredients.set(index, i);
    }

    public void setIngredientNameAtIndex(int index, String name)
    {
        Ingredient i = ingredients.get(index);

        i.setName(name);

        ingredients.set(index, i);
    }

    public void setIngredientPerAtIndex(int index, String per)
    {
        Ingredient i = ingredients.get(index);

        i.setPer(per);

        ingredients.set(index, i);
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public ArrayList<Ingredient> getIngredientsList()
    {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setRecipePlanner(Planner recipePlanner) {
        this.recipePlanner = recipePlanner;
    }

    public Planner getRecipePlanner() {
        return recipePlanner;
    }

    public boolean isBuiltInRecipe() {
        return builtInRecipe;
    }

    public void setBuiltInRecipe(boolean builtInRecipe) {
        this.builtInRecipe = builtInRecipe;
    }

    public Recipe duplicate()
    {
        Recipe recipeCloned = null;

        try {
            recipeCloned = (DoughRecipe) this.clone();
        }catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
        }

        return recipeCloned;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {

        Recipe recipe = (Recipe)super.clone();
        ArrayList<Ingredient> clonedList = new ArrayList<>(recipe.getIngredientsList().size());

        /* Copy ingredient list */
        for(Ingredient i:recipe.getIngredientsList())
        {
            clonedList.add((Ingredient)i.clone());
        }

        /* Copy planner */
        recipe.setRecipePlanner((Planner) recipe.getRecipePlanner().clone());

        /* Reference to new cloned ingredient list */
        recipe.setIngredients(clonedList);

        return recipe;
    }
}
