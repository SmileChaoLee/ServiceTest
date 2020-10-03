package com.smile.servicetest;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by chaolee on 3/2/16.
 */
class AskPermission implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 211;
    private Activity activity=null;
    private String permission=new String("");
    private boolean permissionYN=false;

    public AskPermission(final Activity activity, final String permission) {
        this.activity = activity;
        this.permission = permission;
        checkPermission(this.permission);
        System.out.println("AskPermission created");
    }

    private void showMessageOKCancel(final String message, final DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity.getApplicationContext());
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton("OK", okListener);
        dialogBuilder.setNegativeButton("Cancel", null);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        System.out.println("OnRequestPermissionResult");
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    permissionYN = true;
                    Toast.makeText(activity.getApplicationContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    // Permission Denied
                    permissionYN = false;
                    Toast.makeText(activity.getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
        }
    }

    private void checkPermission(final String permission) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            permissionYN = false;
            int hasWriteSETTINGSPermission = ActivityCompat.checkSelfPermission(activity.getApplicationContext(), permission);
            if (hasWriteSETTINGSPermission != PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission not granted");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    showMessageOKCancel("You need to be allowed access to WRITE",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.requestPermissions(activity,
                                            new String[]{permission},
                                            REQUEST_CODE_ASK_PERMISSIONS);
                                    dialog.dismiss();
                                }
                            });
                } else {
                    ActivityCompat.requestPermissions(activity,
                            new String[]{permission},
                            REQUEST_CODE_ASK_PERMISSIONS);
                }
            } else {
                System.out.println("Permission granted");
                permissionYN = true;
            }
        } else {
            permissionYN = true;
        }

    }

    public boolean getPermissionYN() {
        return this.permissionYN;
    }
}
