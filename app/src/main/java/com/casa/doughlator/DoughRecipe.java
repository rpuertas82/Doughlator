package com.casa.doughlator;

import java.io.Serializable;

/**
 * Created by Casa on 20/11/15.
 */
public class DoughRecipe extends Recipe implements Serializable
{
    /* serialVersionUID has to be overloaded in order to
    * avoid InvalidClassException in serialization */
    private final static long serialVersionUID = 103L;
    private boolean useAsPreferment;

    public DoughRecipe(String recipeName)
    {
        super(recipeName);

        //Create basis dough ingredients
        ingredients.add(new Ingredient("Harina", "500", "100", true));
        ingredients.add(new Ingredient("Agua", "300", "60", false, true));
        ingredients.add(new Ingredient("Levadura", "10", "2", false));
        ingredients.add(new Ingredient("Sal", "10", "2", false));
    }

    @Override
    public String toString()
    {
        return getRecipeName();
    }

    private Ingredient getReferenceIngredient()
    {
        return ingredients.get(0);
    }

    public float getDoughHydration()
    {
        float doughHydration;

        doughHydration = ConstantContainer.ZERO;

        for(Ingredient i:ingredients)
        {
            if(i.isLiquid()==true)
            {
                doughHydration += i.getPer();
            }
        }

        return doughHydration;
    }

    public String getFormattedDoughHydration()
    {
        String formattedValue = String.format("%.1f", getDoughHydration());

        return formattedValue +="%";
    }

    public void updateIngredientsValues()
    {
        for(Ingredient i:ingredients)
        {
            if(i.isReferenceIngredient()==false)
                updateIngredientQty(i);
        }
    }

    private void updateIngredientQty(Ingredient i)
    {
        Ingredient refIngredient = getReferenceIngredient();
        float qty;

        qty = (refIngredient.getQty()*i.getPer())/
                ConstantContainer.ONE_HUNDRED;

        i.setQty(qty);
    }

    private void updateIngredientPer(Ingredient i)
    {
        Ingredient refIngredient = getReferenceIngredient();
        float per;

        per = (i.getQty()*ConstantContainer.ONE_HUNDRED)/
                refIngredient.getQty();

        i.setPer(per);
    }

}
