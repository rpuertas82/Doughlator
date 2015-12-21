package com.casa.doughlator;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Random;

/**
 * Created by Casa on 20/11/15.
 */
public class Planner implements Serializable, Cloneable
{
    /* serialVersionUID has to be overloaded in order to
    * avoid InvalidClassException in serialization */
    private final static long serialVersionUID = 102L;
    private final static String TAG = "Planner";
    private String notesFileName;

    public Planner()
    {

    }

    public String getNotesFileName()
    {
        return notesFileName;
    }

    public String composeNotesFileName(String recipeName, String ext)
    {
        Random rnd = new Random();
        int rndNumber;

        rndNumber = rnd.nextInt((1000 - 0) + 1) + 0;

        StringBuilder sb = new StringBuilder(String.valueOf(this.hashCode()));
        sb.append("_");
        sb.append(String.format("%04d",rndNumber));
        sb.append(ext);

        return sb.toString();
    }

    public void setNotesFileName(String notesFileName) {
        this.notesFileName = notesFileName;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
