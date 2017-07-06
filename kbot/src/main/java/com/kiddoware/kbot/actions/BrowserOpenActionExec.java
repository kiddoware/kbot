package com.kiddoware.kbot.actions;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;

import java.util.List;

/**
 * Created by Shardul on 08/05/17.
 */

public class BrowserOpenActionExec extends ActionExecutor {

    private static final String KPSB_PACKAGE_NAME = "com.kiddoware.kidsafebrowser";

    @Override
    public void run() {

        String url = getParameters().get("url");

        final Activity activity = getController().getActivity();

        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://" + url;

        PackageManager packageManager = getActivity().getPackageManager();

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage(KPSB_PACKAGE_NAME); // set KPSB

        ResolveInfo info = packageManager.resolveActivity(intent,PackageManager.MATCH_DEFAULT_ONLY);

        if (info != null) { // KPSB is not installed / available
            activity.startActivity(intent);
        } else {
            intent.setPackage(null); // RESET to other applications

            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(intent);
            }
        }
    }
}
