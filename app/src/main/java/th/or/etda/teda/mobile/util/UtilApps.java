package th.or.etda.teda.mobile.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.icu.text.SimpleDateFormat;
import android.view.inputmethod.InputMethodManager;

import java.util.Date;
import java.util.Locale;


/**
 * Created by Fafour on 8/8/2560.
 */

public class UtilApps {
    public static int getScreenWidth(Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size.x;
    }

    public static int getScreenHeight(Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return size.y;
    }

    public static void alertDialog(Context c, String message) {
        new AlertDialog.Builder(c)
                .setMessage(message)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                                dialog.dismiss();
                            }
                        }).show();
    }

    public static void alertDialogOpenClose(Context c, String message) {
        new AlertDialog.Builder(c)
                .setMessage(message)
                .setPositiveButton("เรียบร้อย",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // continue with delete
                            }
                        }).show();

    }

    public static String currentDate() {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date());
        return currentDate;
    }
    public static String timestampName() {
        String currentDate = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault()).format(new Date());
        return currentDate;
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

}
