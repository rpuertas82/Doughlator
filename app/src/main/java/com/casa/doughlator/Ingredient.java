package com.casa.doughlator;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Casa on 20/11/15.
 */
public class Ingredient implements Serializable, Cloneable//, Comparable<Ingredient>
{
    /* serialVersionUID has to be overloaded in order to
    * avoid InvalidClassException in serialization */
    private final static long serialVersionUID = 101L;

    private String name;
    private float qty;
    private float per;
    private boolean baseIngredient;
    private boolean referenceIngredient;
    private boolean isLiquid;
    private long id;

    Ingredient()
    {

    }

    @Override
    public String toString()
    {
        String ingString;
        int nSpaces;
        int maxSpace;

        maxSpace = 25;

        ingString = "- " + getName() + "(" + getPer()+" %)";

        nSpaces = maxSpace - ingString.length();

        for(int i=0;i<nSpaces;i++)
            ingString += " ";

        ingString += getQtyString()+" gr";

        return ingString;
    }

    Ingredient(String name, String qty, String per,
               boolean baseIngredient, boolean refIng, boolean isLiquid)
    {
        this.name = name;
        setQty(qty);
        setPer(per);
        setIsLiquid(isLiquid);
        this.referenceIngredient = refIng;
        this.baseIngredient = baseIngredient;
    }

    Ingredient(String name, String qty, String per, boolean refIng, boolean isLiquid)
    {
        this.name = name;
        setQty(qty);
        setPer(per);
        setIsLiquid(isLiquid);
        referenceIngredient = refIng;
    }

    Ingredient(String name, String qty, String per, boolean refIng)
    {
        this.name = name;
        setQty(qty);
        setPer(per);
        setIsLiquid(false);
        referenceIngredient = refIng;
    }

    Ingredient(String name, int qty, float per)
    {
        this.name = name;
        this.qty = qty;
        this.per = per;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getQty() {
        return qty;
    }

    public String getQtyString()
    {
        String formattedValue = String.format(Locale.US, "%.1f", getQty());

        return formattedValue;
    }

    public String getQtyFormattedString()
    {
        String formattedValue = String.format(Locale.US, "%.1f gr", getQty());

        return formattedValue;
    }


    public void setQty(String qty)
    {
        try
        {
            this.qty = Float.parseFloat(qty);
        }
        catch (NumberFormatException e)
        {
            /* Do nothing */
        }
    }

    public void setQty(float qty) {
        this.qty = qty;
    }

    public String getPerString()
    {
        String formattedValue;

        if(isReferenceIngredient())
        {
            formattedValue = String.format(Locale.US, "%.1f", this.per);
        }
        else
        {
            formattedValue = String.format(Locale.US, "%.1f", this.per);
        }

        return formattedValue;
    }

    public String getPerFormattedString()
    {
        String formattedValue;

        if(isReferenceIngredient())
        {
            formattedValue = String.format(Locale.US, "%.1f%%(R)", this.per);
        }
        else if(isLiquid())
        {
            formattedValue = String.format(Locale.US, "%.1f%%(L)", this.per);
        }
        else
        {
            formattedValue = String.format(Locale.US, "%.1f%%", this.per);
        }

        return formattedValue;
    }


    public float getPer() {
        return per;
    }

    public void setPer(float per) {
        this.per = per;
    }

    public void setPer(String per)
    {
        try
        {
            this.per = Float.parseFloat(per);
        }
        catch (NumberFormatException e)
        {
            /* Do nothing */
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isBaseIngredient() {
        return baseIngredient;
    }

    public void setReferenceIngredient(boolean value)
    {
        referenceIngredient = value;
    }

    public boolean isReferenceIngredient()
    {
        return referenceIngredient;
    }

    public void setIsLiquid(boolean isLiquid) {
        this.isLiquid = isLiquid;
    }

    public boolean isLiquid() {
        return isLiquid;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static Comparator<Ingredient> ReferenceIngredientsFirst = new Comparator<Ingredient>() {

        public int compare(Ingredient i1, Ingredient i2) {

            int retVal = -1;

            /* Arranged by reference field */
            if(i1.isReferenceIngredient() && i2.isReferenceIngredient())
            {
                retVal = i1.getName().compareTo(i2.getName());
            }
            else if(i1.isReferenceIngredient() && !i2.isReferenceIngredient())
            {
                retVal = -1;
            }
            else if(!i1.isReferenceIngredient() && i2.isReferenceIngredient())
            {
                retVal = 1;
            }

            return retVal;
        }

    };
}
