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
public class Planner implements Serializable
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
        String fileName;
        int rndNumber;
        int firstBlank;

        rndNumber = rnd.nextInt((1000 - 0) + 1) + 0;

        fileName = new String(recipeName).substring(0,4);

        fileName += "_" + String.format("%04d",rndNumber);
        fileName += ext;

        return fileName;
    }

    public void setNotesFileName(String notesFileName) {
        this.notesFileName = notesFileName;
    }
}
