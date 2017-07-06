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

import android.app.Application;
import android.content.Context;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import ai.api.util.BluetoothController;
@ReportsCrashes(
        formUri = "http://acra.kiddoware.com/submit.php",
        formUriBasicAuthLogin = "acrauser", // optional
        formUriBasicAuthPassword = "spbQNgx", // optional
        mode = ReportingInteractionMode.SILENT
)
public class AIApplication extends Application {

    private static final String TAG = AIApplication.class.getSimpleName();

    private int activitiesCount;
    private BluetoothControllerImpl bluetoothController;
    private SettingsManager settingsManager;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            bluetoothController = new BluetoothControllerImpl(this);
            settingsManager = new SettingsManager(this);
            Utility.setLogFolder(getApplicationContext());
            Utility.setCurrentAppVersion(getApplicationContext());
            Bootstrap.startAlwaysOnService(getApplicationContext(), null);

        } catch (Exception ex) {


        }
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // The following line triggers the initialization of ACRA
        try {
            ACRA.init(this);
        }
        catch (Exception ex){
            //acra failed
        }
    }

    public BluetoothController getBluetoothController() {
        return bluetoothController;
    }

    public SettingsManager getSettingsManager() {
        return settingsManager;
    }

    protected void onActivityResume() {
        if (activitiesCount++ == 0) { // on become foreground
            if (settingsManager.isUseBluetooth()) {
                bluetoothController.start();
            }
        }
    }

    protected void onActivityPaused() {
        if (--activitiesCount == 0) { // on become background
            bluetoothController.stop();
        }
    }

    private boolean isInForeground() {
        return activitiesCount > 0;
    }

    private class BluetoothControllerImpl extends BluetoothController {

        public BluetoothControllerImpl(Context context) {
            super(context);
        }

        @Override
        public void onHeadsetDisconnected() {
            Log.d(TAG, "Bluetooth headset disconnected");
        }

        @Override
        public void onHeadsetConnected() {
            Log.d(TAG, "Bluetooth headset connected");

            if (isInForeground() && Utility.useBlueTooth(getApplicationContext())
                    && !bluetoothController.isOnHeadsetSco()) {
                bluetoothController.start();
            }
        }

        @Override
        public void onScoAudioDisconnected() {
            Log.d(TAG, "Bluetooth sco audio finished");
            bluetoothController.stop();

            if (isInForeground() && Utility.useBlueTooth(getApplicationContext())) {
                bluetoothController.start();
            }
        }

        @Override
        public void onScoAudioConnected() {
            Log.d(TAG, "Bluetooth sco audio started");
        }

    }


}
