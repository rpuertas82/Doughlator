package com.casa.doughlator;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Casa on 20/11/15.
 */
public class Recipe implements Serializable
{
    /* serialVersionUID has to be overloaded in order to
    * avoid InvalidClassException in serialization */
    private final static long serialVersionUID = 100L;
    private String recipeName;
    private float recipeWeight;
    private Planner recipePlanner;
    private String recipeNotes;
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
            recipeWeight += i.getQty();
        }

        return recipeWeight;
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

    public Planner getRecipePlanner() {
        return recipePlanner;
    }
}
