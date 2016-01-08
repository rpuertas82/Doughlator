package com.casa.doughlator;

import java.io.Serializable;
import java.util.Collections;
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
    private Ingredient preferment;

    public DoughRecipe(String recipeName)
    {
        super(recipeName);

        //Create basis dough ingredients
        ingredients.add(new Ingredient("Harina", "500", "100",true, true, false));
        ingredients.add(new Ingredient("Agua", "300", "60", false, false, true));
        ingredients.add(new Ingredient("Levadura", "10", "2", false, false, false));
        ingredients.add(new Ingredient("Sal", "10", "2", false, false, false));

        /* Default value */
        adjustmentMode = ADJUST_BY_PER;

        /* Default value, not preferment added */
        preferment = null;
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

    public void notifyIngredientChanged(Ingredient i, boolean wasReference)
    {
         /* We have to proceed updating depending the ingredient
        * that have changed.
        *
        * If reference ingredient or was reference before changes:
        *   1 - Adjust reference ingredient
        *   2 - Adjust others
        * If not reference ingredient
        *   1 - Adjust the ingredient
        **/
        if(i.isReferenceIngredient() || wasReference)
        {
            if(getAdjustmentMode()==DoughRecipe.ADJUST_BY_QTY) {
                updateIngredientPer(i);
            }
            else {
                updateIngredientQty(i);
            }

            updateIngredientsValues();
        }
        else
        {
            if(getAdjustmentMode()==DoughRecipe.ADJUST_BY_QTY) {
                updateIngredientPer(i);
            }
            else {
                updateIngredientQty(i);
            }
        }
    }

    private void updateReferencedIngredients()
    {
        int currentRow;

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
    }

    /* Return the sum of all reference ingredients */
    private float getReferencedQty()
    {
        float referencedQty;

        referencedQty = ConstantContainer.ZERO;

        /* Update reference quantities */
        updateReferencedIngredients();

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

        /* If used as preferment */
        i.scale(qty);
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

        /* If used as preferment */
        i.scale(i.getQty());
    }

    public void sortByIngredientsQuantity(boolean reverseOrder)
    {
          /* Sort list with new ingredient,
          * base ingredient is excluded (always first)*/
        if(!reverseOrder)
        {
            Collections.sort(ingredients.subList(1, ingredients.size()));
        }
        else {
            Collections.sort(ingredients.subList(1, ingredients.size()),
                    Collections.reverseOrder());
        }
    }

    public void sortByReferenceIngredientsFirst()
    {
          /* Sort list with new ingredient,
          * base ingredient is excluded (always first)*/
        Collections.sort(
                ingredients.subList(1, ingredients.size()),
                Ingredient.ReferenceIngredientsFirst);
    }

    @Override
    public int compareTo(DoughRecipe another)
    {
        return getRecipeName().compareToIgnoreCase(another.getRecipeName());
    }

    public int getAdjustmentMode() {
        return adjustmentMode;
    }

    public void setAdjustmentMode(int adjustmentMode) {
        this.adjustmentMode = adjustmentMode;
    }

    public float getLiquidIngredientsWeight()
    {
        float weight = 0;

        for(Ingredient i:ingredients)
        {
            if(i.isLiquid())
                weight += i.getQty();
        }

        return weight;
    }

    public float getReferencedIngredientsWeight()
    {
        float weight = 0;

        for(Ingredient i:ingredients)
        {
            if(i.isReferenceIngredient())
                weight += i.getQty();
        }

        return weight;
    }

    public String getFormattedReferencedIngredientsWeight()
    {
        String formattedValue = String.format(Locale.US, " %.1f gr.",
                getReferencedIngredientsWeight());

        return formattedValue;
    }

    public String getFormattedLiquidIngredientsWeight()
    {
        String formattedValue = String.format(Locale.US, " %.1f gr.",
                getLiquidIngredientsWeight());

        return formattedValue;
    }

    public Ingredient synthesize()
    {
        return new Ingredient(
                this.getRecipeName(),
                getRecipeWeight(),
                getReferencedQty(),
                getDoughHydration());
    }

    public Ingredient getPreferment() {
        return preferment;
    }

    public void setPreferment(Ingredient preferment) {

        this.preferment = preferment;

        /* We have to mark main ingredients (base flour and main liquid item)
        to substract preferment quantities when they are represented */

        /* Check for flour base ingredient */
        for(Ingredient i:ingredients)
        {
            if(i.isBaseIngredient()==true)
                i.setSubstractPrefermentQty(true);

            break;
        }

        /* Check for main liquid ingredient, get the
         * major */
        Ingredient auxIng = null;

        for(Ingredient i:ingredients)
        {
            if(i.isLiquid())
            {
                if(auxIng==null)
                {
                   auxIng = i;
                }
                else
                {
                    if(i.getQty()>auxIng.getQty())
                    {
                        auxIng = i;
                    }
                }
            }
        }

        /* It should be the major */
        if(auxIng!=null)
            auxIng.setSubstractPrefermentQty(true);
    }

    public boolean isUseAsPreferment() {
        return useAsPreferment;
    }

    public void setUseAsPreferment(boolean useAsPreferment) {
        this.useAsPreferment = useAsPreferment;
    }
}
