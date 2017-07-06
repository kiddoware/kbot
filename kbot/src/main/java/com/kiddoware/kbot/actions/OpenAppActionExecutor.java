package com.kiddoware.kbot.actions;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.kiddoware.kbot.R;
import com.kiddoware.kbot.models.ActionMessage;
import com.kiddoware.kidsplace.sdk.KPUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Shardul on 18/05/17.
 */

public class OpenAppActionExecutor extends ActionExecutor {

    private final String APP_NAME = "app_name";
    private final String KP_KBOT_LAUNCH_ACTION = "com.kiddoware.kbot.open_app_action";
    private final String KP_KBOT_EXTRA_PACKAGE_NAME = "com.kiddoware.kbot.packagename";

    private final boolean KP_INTEGRATION_ENABLED = true;

    @Override
    public void run() {
        if (getParameters().containsKey(APP_NAME)) {
            final String appName = getParameters().get(APP_NAME);

            if (appName == null || appName.length() <= 0) {


                notifyControllerForMessage(getString(R.string.input_unknown_answer));

                return;
            }

            searchAndLaunchApp(appName);
        }
    }

    public void searchAndLaunchApp(String appName) {
        final ArrayList<AppData> availableApps = searchApp(appName);

        if (availableApps == null) {
            notifyControllerForMessage(getString(com.kiddoware.kbot.R.string.no_app_found));
        } else if (availableApps.size() == 0) {
            notifyControllerForMessage(getString(com.kiddoware.kbot.R.string.no_app_found));
        } else if (availableApps.size() == 1) {
            openApp(availableApps.get(0).pacakge);
        } else {

            ActionMessage actionMessage = new ActionMessage();
            actionMessage.setExecutor(this.getClass().getSimpleName());
            actionMessage.setSource(getString(R.string.app_name));
            actionMessage.setMessage(getString(R.string.which_app_to_launch));

            actionMessage.setOptions(availableApps.toArray());

            controller.notifyListener(actionMessage);

        }
    }


    private boolean openApp(String packageName) {

        if (KP_INTEGRATION_ENABLED) {

            String PACKAGE_NAME_COLUMN = "package_name";

            Cursor cursor = KPUtility.getSelectedApps(controller.getActivity());

            if (cursor == null) {
                // FATAL ERROR WITH KP INTEGRATION
                startLauncher(packageName);
                return true;
            }

            boolean appSelected = false;
            int columnIndex = cursor.getColumnIndex(PACKAGE_NAME_COLUMN);

            while (cursor.moveToNext()) { // TODO REVIEW we need to have direct select query
                if (cursor.getString(columnIndex).equals(packageName)) {
                    appSelected = true;
                    break;
                }
            }

            if (appSelected) {
                startLauncher(packageName);
            } else { // TODO check KP version before starting
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.kiddoware.kidsplace",
                        "com.kiddoware.kidsplace.LaunchActivity"));
                intent.setAction(KP_KBOT_LAUNCH_ACTION);
                intent.putExtra(KP_KBOT_EXTRA_PACKAGE_NAME,packageName);

                try {
                    controller.getActivity().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    openApp(packageName);
                }
            }
            return true;
        } else {
            return startLauncher(packageName);
        }
    }

    private boolean startLauncher(String packageName) {
        PackageManager manager = getActivity().getPackageManager();
        try {
            Intent i = manager.getLaunchIntentForPackage(packageName);
            if (i == null) {
                return false;
            }
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            getActivity().startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private ArrayList<AppData> searchApp(String appName) {
        appName = appName.toLowerCase().trim();

        ArrayList<AppData> appsAvailable = new ArrayList<AppData>();

        final PackageManager packageManager = getActivity().getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager
                .getInstalledApplications(PackageManager.GET_META_DATA);

        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(0);

        for (PackageInfo info : installedPackages) {
            String name = info.applicationInfo.loadLabel(packageManager).toString();

            if (name.toLowerCase(Locale.getDefault()).contains(appName)) {

                String packageName = info.packageName;

                appsAvailable.add(new AppData(name, packageName, this));
            }
        }


        if (appsAvailable.isEmpty()) {

            for (ApplicationInfo info : installedApplications) {

                if (info.packageName.toLowerCase().contains(appName)) {
                    AppData appData = new AppData(appName, info.packageName, this);
                    appsAvailable.add(appData);
                }
            }


        }

        return appsAvailable;
    }

    @Override
    public void handleOption(Object appData){
        openApp(((AppData)appData).pacakge);
    }

    public static class AppData {

        public String name;
        public String pacakge;
        public ActionExecutor executor;
        public int fallbackDrawable;

        AppData(String name, String pacakge,ActionExecutor executor) {
            this.name = name;
            this.pacakge = pacakge;
            this.executor = executor;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
