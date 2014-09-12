package com.crakac.ofuton.util;

import android.content.Context;
import android.util.Log;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Util {
    public static void closeQuietly(Closeable c) {
        if(c == null) return;
        try {
            c.close();
        } catch (Exception e) {
        }
    }

    public static <T extends Serializable> T restoreFile(Context context, String fileName){
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        T obj = null;
        try{
            fis = context.openFileInput(fileName);
            ois = new ObjectInputStream(fis);
            obj = (T)ois.readObject();
        } catch (ClassNotFoundException|IOException ioe){
            ioe.printStackTrace();
        } finally {
            closeQuietly(fis);
            closeQuietly(ois);
        }
        return obj;
    }

    public static<T extends Serializable> void saveFile(Context context, T file, String fileName){
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(file);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            closeQuietly(fos);
            closeQuietly(oos);
        }
    }

    public static int daysPast(long fromMs){
        int days = (int) ((System.currentTimeMillis() - fromMs) / (1000 * 60 * 60 * 24));
        Log.d("Days Past", days + "days past from " + fromMs);
        return days;
    }

    public static String blanks(int n){
        StringBuilder sb = new StringBuilder(n);
        for(int i = 0; i < n; i++){
            sb.append(' ');
        }
        return sb.toString();
    }
}