package app.intelehealth.client.utilities;

import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import app.intelehealth.client.app.AppConstants;


/**
 * Updated by mahiti on 24/02/16.
 */
public class Logger {
    /**
     * Default constructors
     */
    private Logger() {
        // This Constructor is not Used
    }

    /**
     * function to use in catch block....
     *
     * @param tag
     * @param desc
     * @param e
     */
    public static void logE(String tag, String desc, Exception e) {
        Log.e(tag, desc, e);
        FirebaseCrashlytics.getInstance().recordException(e);
        //logToFile(tag + "\t" + desc+"\t"+e.getLocalizedMessage());
    }

    /**
     * function to use for debug and showing in console..
     *
     * @param tag
     * @param desc
     */
    public static void logD(String tag, String desc) {
        Log.d(tag, "" + desc);
        //logToFile(tag + "" + desc);
    }

    /**
     * function to use for debug and showing in console....
     *
     * @param tag
     * @param desc
     */
    public static void logV(String tag, String desc) {
        Log.v(tag, "" + desc);
        //logToFile(tag + "" + desc);
    }

    public static void logToFile(String message) {
        try {
            File logFile = new File(AppConstants.DOC_PATH + "log.txt");
            // BufferedWriter for performance, true to set append to file
            // flag
            //
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd HH:mmZ");
            String time = sourceFormat.format(new Date());
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
                    true));
            //TODO: stop log writing in file during live release
            buf.write(time + ":\n" + message + "\r\n");
            buf.newLine();
            buf.flush();
            buf.close();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

}
