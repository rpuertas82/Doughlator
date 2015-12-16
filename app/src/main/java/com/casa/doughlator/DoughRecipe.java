package com.casa.doughlator;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created by Casa on 20/11/15.
 */
public class DoughRecipe extends Recipe implements Serializable, Comparable<DoughRecipe>
{
    /* serialVersionUID has to be overloaded in order to
    * avoid InvalidClassException in serialization */
    private final static long serialVersionUID = 103L;

    public final static int ADJUST_BY_PER = 0;
    public final static int ADJUST_BY_QTY = 1;
    private boolean useAsPreferment;
    private int adjustmentMode;

    public DoughRecipe(String recipeName)
    {
        super(recipeName);

        //Create basis dough ingredients
        ingredients.add(new Ingredient("Harina", "500", "100", true));
        ingredients.add(new Ingredient("Agua", "300", "60", false, true));
        ingredients.add(new Ingredient("Levadura", "10", "2", false));
        ingredients.add(new Ingredient("Sal", "10", "2", false));

        /* Default value */
        adjustmentMode = ADJUST_BY_PER;
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

    /* Return the sum of all reference ingredients */
    private float getReferencedQty()
    {
        float referencedQty;
        int currentRow;

        referencedQty = ConstantContainer.ZERO;
        currentRow = ConstantContainer.ZERO;

        /* Update reference quantities */
        for(Ingredient i:ingredients)
        {
            if(currentRow==ConstantContainer.ZERO)
            {
                currentRow++;
            }
            else
            {
                if(i.isReferenceIngredient())
                    updateIngredientQty(i);
            }
        }

        /* perform sum */
        for(Ingredient i:ingredients)
        {
            if(i.isReferenceIngredient()) {
                referencedQty += i.getQty();
            }
        }

        return referencedQty;
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
        String formattedValue = String.format(Locale.US, "%.1f", getDoughHydration());

        return formattedValue +="%";
    }

    public void updateIngredientsValues()
    {
        int currentRow = ConstantContainer.ZERO;

         /* Finally, update others based on values obtained before */
        for(Ingredient i:ingredients)
        {
            if(currentRow==ConstantContainer.ZERO) {
                //There is no need to update
                //first ingredient
                currentRow++;
            } else {
                updateIngredientQty(i);
            }
        }
    }

    public void updateIngredientQty(Ingredient i)
    {
        Ingredient refIngredient = getReferenceIngredient();
        float qty;

        if(i.isReferenceIngredient()){

            qty = (refIngredient.getQty() * i.getPer()) /
                    ConstantContainer.ONE_HUNDRED;
        }
        else {

            qty = (getReferencedQty() * i.getPer()) /
                    ConstantContainer.ONE_HUNDRED;
        }

        i.setQty(qty);
    }

    public void updateIngredientPer(Ingredient i)
    {
        Ingredient refIngredient = getReferenceIngredient();
        float per;

        if(i.isReferenceIngredient()){

            per = (i.getQty()*ConstantContainer.ONE_HUNDRED)/
                    refIngredient.getQty();
        }
        else {

            per = (i.getQty()*ConstantContainer.ONE_HUNDRED)/
                    getReferencedQty();
        }

        i.setPer(per);
    }

    @Override
    public int compareTo(DoughRecipe another)
    {
        return getRecipeName().compareTo(another.getRecipeName());
    }

    public int getAdjustmentMode() {
        return adjustmentMode;
    }

    public void setAdjustmentMode(int adjustmentMode) {
        this.adjustmentMode = adjustmentMode;
    }
}
