package hr.ivica.android.ocr.alerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import hr.ivica.android.ocr.R;

public class AlertDialogFactory {

    public AlertDialog getAlertAndExitDialog(final Activity activity, int msgResourceId) {
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(activity.getResources().getText(R.string.alert).toString());
        alertDialog.setMessage(activity.getResources().getText(msgResourceId).toString());

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        activity.finish();
                    }
                });

        return alertDialog;
    }

    public AlertDialog getAlertDialog(final Context context, int msgResourceId) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(context.getResources().getText(R.string.alert).toString());
        alertDialog.setMessage(context.getResources().getText(msgResourceId).toString());

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return alertDialog;
    }
}
