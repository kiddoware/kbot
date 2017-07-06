package com.kiddoware.kbot;

/***********************************************************************************************************************
 * API.AI Android SDK -  API.AI libraries usage example
 * =================================================
 * <p/>
 * Copyright (C) 2015 by Speaktoit, Inc. (https://www.speaktoit.com)
 * https://www.api.ai
 * <p/>
 * **********************************************************************************************************************
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ***********************************************************************************************************************/

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class BaseActivity extends AppCompatActivity {

    private AIApplication app;

    private static final long PAUSE_CALLBACK_DELAY = 500;
    protected static final int REQUEST_PERMISSIONS_ID = 33;

    private final Handler handler = new Handler();
    private Runnable pauseCallback = new Runnable() {
        @Override
        public void run() {
            app.onActivityPaused();
        }
    };

    private String[] requestedPermissions = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (AIApplication) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();
        app.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.postDelayed(pauseCallback, PAUSE_CALLBACK_DELAY);
    }

    protected final void checkRequiredPermissions(){
        checkRequiredPermissions(true);
    }

    protected final void checkRequiredPermissions(boolean prompt) {

        final String[] requiredPermissions = {
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO
        };

        ArrayList<String> permissionsToBeRequested = new ArrayList<String>();

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                    permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToBeRequested.add(permission);
            }
        }

        if (!permissionsToBeRequested.isEmpty()) {
            requestedPermissions = permissionsToBeRequested.toArray(
                    new String[permissionsToBeRequested.size()]);

            if (prompt)
                ActivityCompat.requestPermissions(this,
                        requestedPermissions,
                        REQUEST_PERMISSIONS_ID);
        } else {
            requiresPermissions(null);
        }
    }

    public void requiresPermissions(String[] permissions) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS_ID: {

                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            requiresPermissions(requestedPermissions);
                            return;
                        }
                    }

                    requiresPermissions(null);

                } else {
                    requiresPermissions(requestedPermissions);
                }
            }
        }
    }
}
