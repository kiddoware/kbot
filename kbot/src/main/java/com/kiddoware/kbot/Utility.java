package com.kiddoware.kbot;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;


import org.acra.ErrorReporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.kiddoware.kbot.BuildConfig;

public class Utility {

	// Global debug flag - use when you have alternate logic so we can easily
	// switch
	// everything at once - change this when release
	public static boolean DEBUG_MODE = true;
	private static boolean LOGGING_ERR = true;

	public static String KIDSPLACE_PKG_NAME = "com.kiddoware.kidsplace";
	public static String KPRC_PKG_NAME = "com.kiddoware.kidsplace.remotecontrol";

	// APP MARKETS Constants
	protected static final int ANDROID_MARKET = 1;
	protected static final int AMAZON_MARKET = 2;
	protected static final int SAMSUNG_MARKET = 3;
	protected static final int NOOK_MARKET = 4;

	// market switch logic
	protected static int APP_MARKET = ANDROID_MARKET;// comment this for Amazon
														// market

	// APP MARKETS Package Name Constants
	protected static final String ANDROID_MARKET_PKG = "com.android.vending";
	protected static final String AMAZON_MARKET_PKG = "com.amazon.venezia";
	public static final String KEY_EULA_ACCEPTED_SETTING = "eula.accepted";// flag
																			// to
																			// indicate
																			// if
																			// user
																			// is
																			// using
																			// app
																			// for
																			// first
																			// time
	public static final String KEY_INSTALL_DATE_SETTING = "installDateValue";// date
																				// (in
																				// milli
																				// seconds)
																				// when
																				// app
																				// was
																				// installed
																				// on
																				// device

	public static final String KEY_CHILD_LOCK_SETTING = "childLockEnabled";// This
																			// flag
																			// controls
																			// if
																			// Kids
																			// Place
																			// will
																			// be
																			// started
																			// on
																			// launch
																			// or


	public static final String KEY_ORG_APP_VERSION = "orgAppVersion";// App
																		// version
																		// when
																		// user
																		// fist
																		// installed
																		// the
																		// app
	public static final String KEY_CURRENT_APP_VERSION = "appVersion";// Current
																		// app
																		// version
	public static final String KEY_LICENSED_VERSION = "licensedVersion";// Current
																		// app
																		// version
	public static final String KEY_USAGE_COUNTER = "usageCounter";// Counter to
																	// track how
																	// many
																	// times
																	// user used
																	// the app

	public static final String KEY_LOCK_BACK_BTN_SETTING = "lockBackBtn";// setting
																			// to
																			// store
																			// if
																			// back
																			// btn
																			// is
																			// locked
																			// on
																			// video
																			// player// activity

	private static final String KEY_FIRST_TIME = "firstTime";// for t

	protected static final Integer[] EXIT_CODE = new Integer[] {
			KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_3,
			KeyEvent.KEYCODE_1 };

	public static final String EXIT_PIN = "7531";
	protected static final String DEFAULT_PIN = "4321";
	protected static final String DEFAULT_PIN_HINT = "Initial Pin is 4321";

	private static final Utility INSTANCE = new Utility();
	private boolean isOrgAirplaneModeSet = false;
	private boolean orgAirplaneMode;
	private boolean isAppActive; // flag to indicate app is in foreground or
									// background
	boolean isAirplaneModeEnabled = false;
	private static String TOKEN;// KBSB Server token

	public static String LOG_FILE_FOLDER = "/data/data/com.kiddoware.kidsplace.remotecontrol/files";
	public static String LOG_FILE_NAME = "/kpremotecontrol_log.txt";
	private static final String TAG = "Utility";

	private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	public static final String KPREMOTECONTROL_LICENSE_PKG_NAME = "com.kiddoware.kidsplace.remotecontrol.license";
	private static final String SETTINGS_KEY = "K1p762s2187b8";// secret key to
																// encrypt
																// password
	private static final String KEY_DEVICE_ID = "device_id";
	public static final String DEFAULT_DEVICE_ID = null;
	private static final String KEY_INSTALL_SOURCE = "source";
	private static final String KEY_INSTALL_SOURCE_DEFAULT = "PlayStore";
	private static final String KEY_DEVICE_NAME = "device_name";
	private static final String KEY_DEFAULT_DEVICE_NAME = android.os.Build.MODEL;
	static final String SOURCE = KEY_INSTALL_SOURCE_DEFAULT;// hpsa;
																	// esi;
																	// acer;

	private static final String KEY_LICENSE_DAYS = "license_days";
	private static final int KEY_DEFAULT_LICENSE_DAYS = 31;

	private static boolean errorReported = false;
	protected static String FACEBOOK_FAN_PAGE = "http://m.facebook.com/profile.php?id=134235640009964";
	protected static String CONTACT_EMAIL = "support@kiddoware.com";

	private static boolean isLicensed;



	private static final boolean DEF_LICENSE_VALUE = false;
	private static final String KEY_BLUETOOTH_SETTING = "USE_BLUETOOTH";




	public static Utility GetInstance() {
		return INSTANCE;
	}

	private Utility() {
	}

	public static void setLogFolder(Context ctxt) {
		LOG_FILE_FOLDER = ctxt.getFilesDir().getAbsolutePath();
	}




	protected static boolean eulaAccepted(Context ctxt) {
		boolean value = false;
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			value = settings.getBoolean(KEY_EULA_ACCEPTED_SETTING, value);
		} catch (Exception ex) {
			Utility.logErrorMsg("getFirstTimeSetting:", TAG, ex);

		}
		return value;
	}

	protected static void setEulaAccepted(Context ctxt, boolean value) {
		try {
			// Save user preferences. We need an Editor object to
			// make changes. All objects are from android.context.Context
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);

			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(KEY_EULA_ACCEPTED_SETTING, value);
			editor.putLong(KEY_INSTALL_DATE_SETTING, System.currentTimeMillis());

			// Don't forget to commit your edits!!!
			editor.commit();
		} catch (Exception ex) {
			Utility.logErrorMsg("setFirstTimeSetting:", TAG, ex);

		}

	}

	protected long getInstalledDate(Context ctxt) {
		long value = System.currentTimeMillis();
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			value = settings.getLong(KEY_INSTALL_DATE_SETTING, value);
		} catch (Exception ex) {
			Utility.logErrorMsg("getInstalledDate:", TAG, ex);

		}
		return value;
	}

	protected static boolean setCurrentAppVersion(Context ctxt) {
		boolean isNewVersion = false;

		try {
			String storedAppVersion = getCurrentAppVersion(ctxt);
			// get current app version
			String versionNo = "";
			PackageInfo pInfo = null;
			try {
				pInfo = ctxt.getPackageManager().getPackageInfo(
						ctxt.getPackageName(), PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				pInfo = null;
			}
			if (pInfo != null) {
				versionNo = "" + pInfo.versionCode;
			}
			if (!storedAppVersion.equals(versionNo)) {
				// Save user preferences. We need an Editor object to
				// make changes. All objects are from android.context.Context
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(ctxt);

				SharedPreferences.Editor editor = settings.edit();
				editor.putString(KEY_CURRENT_APP_VERSION, versionNo);
				if (storedAppVersion.equals("")) {
					// This is the original install so set the original version
					// in settings
					editor.putString(KEY_ORG_APP_VERSION, versionNo);
				}
				// Don't forget to commit your edits!!!
				editor.commit();
				isNewVersion = true;
			}
		} catch (Exception ex) {
			Utility.logErrorMsg("setCurrentAppVersion:", TAG, ex);

		}
		return isNewVersion;

	}

	public static String getCurrentAppVersionFromPkg(Context ctxt) {
		String versionNo = "";
		PackageInfo pInfo = null;
		try {
			try {
				pInfo = ctxt.getPackageManager().getPackageInfo(
						ctxt.getPackageName(), PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				pInfo = null;
			}
			if (pInfo != null) {
				versionNo = "" + pInfo.versionCode;
			}
		} catch (Exception ex) {
			Utility.logErrorMsg("getCurrentAppVersionFromPkg:", TAG, ex);

		}
		return versionNo;
	}

	public static String getCurrentAppVersion(Context ctxt) {
		String appVersion = "";
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);

			appVersion = settings.getString(KEY_CURRENT_APP_VERSION, "");
		} catch (Exception ex) {
			Utility.logErrorMsg("getCurrentAppVersion:", TAG, ex);

		}
		return appVersion;
	}


	public static void setFirstTime(Context ctxt, boolean firsTime) {

		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);

			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(KEY_FIRST_TIME, firsTime);

			editor.commit();

		} catch (Exception ex) {
			Utility.logErrorMsg("setKPSBToken:", TAG, ex);

		}
	}

	public static boolean isFirstTime(Context ctxt) {
		boolean value = true;
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);

			value = settings.getBoolean(KEY_FIRST_TIME, true);
		} catch (Exception ex) {
			Utility.logErrorMsg("getKPSBToken:", TAG, ex);

		}
		return value;
	}


	// Returns the version user installed first time
	private String getOriginalAppVersion(Context ctxt) {
		String appVersion = "";
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);

			appVersion = settings.getString("appVersion", "");
		} catch (Exception ex) {
			Utility.logErrorMsg("getCurrentAppVersion:", TAG, ex);

		}
		return appVersion;
	}

	public static void clearLogMsg() {
		FileWriter localFileWriter = null;
		try {
			localFileWriter = new FileWriter(LOG_FILE_FOLDER + LOG_FILE_NAME,
					false);
			writeLogHeader(localFileWriter);
			localFileWriter.write("");// clear the file

		} catch (FileNotFoundException fileNotFoundException) {
			Log.e(TAG, now() + ": " + "writeCrashLog:filenotfound:"
					+ fileNotFoundException.getMessage());

			return;
		} catch (IOException iOException) {
			Log.e(TAG, now() + ": " + "writeCrashLog:ioexception:"
					+ iOException.getMessage());

		} finally {
			if (localFileWriter != null)
				try {
					localFileWriter.flush();
					localFileWriter.close();
				} catch (IOException ignored) {
				}
		}
	}

	public static void logMsg(String messgae, String tag) {
		if (DEBUG_MODE || BuildConfig.DEBUG){
			Log.v(tag, now() + ": " + messgae);
		}
	}

	public static void logMsgToFile(String messgae, String tag, Context ctxt) {
		if (DEBUG_MODE )
			writeCrashLog(LOG_FILE_FOLDER, now() + ": " + tag + ": " + messgae);
	}

	public static void logErrorMsg(String messgae, String tag) {
		if (LOGGING_ERR) {
			Log.e(tag, now() + ": " + messgae);
			writeCrashLog(LOG_FILE_FOLDER, now() + ": " + tag + ": " + messgae);
		}
	}

	public static void logErrorMsg(String messgae, String tag,
								   Throwable throwable) {
		if (LOGGING_ERR) {
			logErrorMsg(messgae, TAG);
			StackTraceElement[] arrayOfStackTraceElement = throwable
					.getStackTrace();
			int stackLength = arrayOfStackTraceElement.length;
			StringBuilder stringBuilder = new StringBuilder();

			stringBuilder.append("\nException ActionMessage:"
					+ throwable.getMessage() + "\n");

			for (int i = 0; i < stackLength; i++) {
				stringBuilder.append("Stack Trace Metadata:" + "\n");
				stringBuilder.append(arrayOfStackTraceElement[i].getClassName()
						+ "::");
				stringBuilder.append(arrayOfStackTraceElement[i]
						.getMethodName() + "::");
				stringBuilder.append(arrayOfStackTraceElement[i]
						.getLineNumber() + "::");
			}
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			stringBuilder.append("\nRaw Stack Trace:" + sw.toString()
					+ "\n*******END OF ERROR****\n");

			logErrorMsg(stringBuilder.toString(), TAG);
			if (!Utility.errorReported) {
				// Report only once per run
				ErrorReporter.getInstance().handleSilentException(throwable);
				Utility.errorReported = true;
			}
		}
	}

	public static void writeCrashLog(String msg1, String msg2) {
		FileWriter localFileWriter = null;
		try {
			cleanLogFile(msg1);
			msg1 = msg1 + LOG_FILE_NAME;
			localFileWriter = new FileWriter(msg1, true);
			writeLogHeader(localFileWriter);
			localFileWriter.append(msg2);

		} catch (FileNotFoundException fileNotFoundException) {
			Log.e(TAG, now() + ": " + "writeCrashLog:filenotfound:"
					+ fileNotFoundException.getMessage());

			return;
		} catch (IOException iOException) {
			Log.e(TAG, now() + ": " + "writeCrashLog:ioexception:"
					+ iOException.getMessage());

		} finally {
			if (localFileWriter != null)
				try {
					localFileWriter.flush();
					localFileWriter.close();
				} catch (IOException ignored) {
				}
		}
	}
	private static void cleanLogFile(String filePathFolder) {
		try {
			// if more than 200 K, delete the file

			String filePath = filePathFolder + LOG_FILE_NAME;
			File errorFile;
			// length is > 200 K delete the file
			PrintWriter pw = null;
			try {
				errorFile = new File(filePath);
				if (errorFile != null && errorFile.exists()
						&& errorFile.length() > 200000) {
					pw = new PrintWriter(filePath);
				}
			} catch (FileNotFoundException e) {
				Utility.logErrorMsg("getErrorText:fileNotFoundException", TAG,
						e);

			} catch (Exception e) {
				Utility.logErrorMsg("getErrorText:exception", TAG, e);

			} finally {
				if (pw != null)
					try {
						pw.close();
					} catch (Exception ignored) {
					}
			}

		} catch (Exception ex) {

		}
	}
	private static void writeLogHeader(FileWriter fileWriter) {
		try {
			String msg = String.valueOf(Build.DISPLAY);
			msg += "\n";
			msg += String.valueOf(Build.FINGERPRINT) + "\n";
			fileWriter.write(msg);
			return;
		} catch (Exception exception) {
			Log.e(TAG,
					now() + ": " + "writeLogHeader:" + exception.getMessage());
		}
	}

	public static String getErrorText(String filePath) {
		filePath = filePath + LOG_FILE_NAME;
		String errorText = "";
		byte[] buffer;
		File errorFile;

		// length is > 200 K delete the file
		BufferedInputStream f = null;
		try {
			errorFile = new File(filePath);
			if (errorFile != null && errorFile.exists()) {
				buffer = new byte[(int) errorFile.length()];
				f = new BufferedInputStream(new FileInputStream(filePath));
				f.read(buffer);
				errorText = new String(buffer);
			}
		} catch (FileNotFoundException e) {
			Utility.logErrorMsg("getErrorText:fileNotFoundException", TAG, e);

		} catch (IOException e) {
			Utility.logErrorMsg("getErrorText:ioexception", TAG, e);

		} finally {
			if (f != null)
				try {
					f.close();
				} catch (IOException ignored) {
				}
		}
		return errorText;

	}

	private static String now() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		String time = sdf.format(cal.getTime());
		return time;

	}

	protected static boolean getChildLockSetting(Context ctxt) {
		boolean value = true;
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			value = settings.getBoolean(KEY_CHILD_LOCK_SETTING, value);
		} catch (Exception ex) {
			Utility.logErrorMsg("getChildLockSetting:", TAG, ex);

		}
		return value;
	}

	protected static void setChildLockSetting(Context ctxt, boolean value) {
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(KEY_CHILD_LOCK_SETTING, value);
			editor.commit();
			trackThings("/ChildLock_Enabled", ctxt);

		} catch (Exception ex) {
			Utility.logErrorMsg("setChildLockSetting:", TAG, ex);

		}
	}

	public static boolean isLicencedVersion(Context ctxt) {
		boolean value = true;
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			value = settings
					.getBoolean(KEY_LICENSED_VERSION, DEF_LICENSE_VALUE);
		} catch (Exception ex) {
			Utility.logErrorMsg("isLicencedVersion:", TAG, ex);

		}
		return value;
	}

	public static void setLicencedVersion(Context ctxt, boolean value) {
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(KEY_LICENSED_VERSION, value);
			editor.commit();
		} catch (Exception ex) {
			Utility.logErrorMsg("setLicencedVersion:", TAG, ex);

		}
	}



	protected static String getMarketURL(boolean forExternalUse) {
		String appGoogleURI = "market://details?id=" + KPRC_PKG_NAME;// Android
																		// market
		if (forExternalUse) {
			// used for sending link outside of android phone - for social
			// sharing
			appGoogleURI = "https://market.android.com/details?id="
					+ KPRC_PKG_NAME;// Android market
		}
		String appAmazonURI = "http://www.amazon.com/gp/mas/dl/android?p="
				+ KPRC_PKG_NAME;// amazon market
		String appSaumsungURI = "samsungapps://ProductDetail/" + KPRC_PKG_NAME;// samsung
																				// market

		String appURI = appGoogleURI; // default to android
		if (Utility.APP_MARKET == Utility.AMAZON_MARKET) {
			appURI = appAmazonURI;
		} else if (Utility.APP_MARKET == Utility.SAMSUNG_MARKET) {
			appURI = appSaumsungURI;
		}
		return appURI;
	}

	public static String getMarketURL(boolean forExternalUse, String packageName) {
		String appGoogleURI = "market://details?id="
				+ packageName
				+ "&referrer=utm_source%3DBTT_app%26utm_medium%3Dandroid_app%26utm_term%3DBTT_app%26utm_campaign%3Dbtt_app";// Android
																															// Market//Android
																															// market
		if (forExternalUse) {
			// used for sending link outside of android phone - for social
			// sharing
			appGoogleURI = "https://market.android.com/details?id="
					+ packageName;// Android market
		}
		String appAmazonURI = "http://www.amazon.com/gp/mas/dl/android?p="
				+ packageName;// amazon market
		String appURI = appGoogleURI; // default to android
		String appSamsungURI = "samsungapps://ProductDetail/" + packageName;// amazon
																			// market
		String appNookURI = "samsungapps://ProductDetail/" + packageName;// amazon
																			// market
		String appSocioURI = appGoogleURI;// Soc.io market
		String vodafoneURI = "samsungapps://ProductDetail/" + packageName;// amazon
																			// market
		String verizonURI = "http://mall.soc.io/MyApps/1003281811";// Soc.io
																	// market

		appURI = appGoogleURI; // default to android
		if (Utility.APP_MARKET == Utility.AMAZON_MARKET) {
			appURI = appAmazonURI;
		} else if (Utility.APP_MARKET == Utility.SAMSUNG_MARKET) {
			appURI = appSamsungURI;
		} else if (Utility.APP_MARKET == Utility.NOOK_MARKET) {
			appURI = appNookURI;
		}
		return appURI;
	}

	protected static void trackThings(String trackingName, Context ctxt) {


	}

	// check if specified package exists or not
	public static boolean isPackageExists(String targetPackage,
			final Context mContext) {
		boolean retValue = true;
		try {
			// check if package exists
			mContext.getPackageManager().getApplicationInfo(targetPackage,
					PackageManager.GET_META_DATA);
			Utility.logMsg(targetPackage + " exists", TAG);

		} catch (NameNotFoundException nameNotFoundEx) {
			Utility.logMsg(targetPackage + "does not exists", TAG);
			// package does not exists
			retValue = false;
		} catch (Exception ex) {
			// DO Nothing
		}
		return retValue;
	}



	protected static boolean isKidsPlaceInstalled(Context context) {
		return isPackageExists(KIDSPLACE_PKG_NAME, context);
	}

	protected static boolean isBackBtnLocked(Context ctxt) {
		boolean value = false;
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			value = settings.getBoolean(KEY_LOCK_BACK_BTN_SETTING, false);
		} catch (Exception ex) {
			Utility.logErrorMsg("isBackBtnLocked:", TAG, ex);

		}
		return value;
	}



	// returns cached value
	public static boolean isLicencedVersion() {
		return isLicensed;
	}

	// check if specified package exists or not and returns package info if
	// found otherwise nu,,
	private static PackageInfo getPackageInfo(String targetPackage,
											  final Context mContext) {
		PackageInfo packageInfo = null;
		try {
			// check if package exists
			packageInfo = mContext.getPackageManager().getPackageInfo(
					targetPackage, PackageManager.GET_META_DATA);
			Utility.logMsg(targetPackage + " exists", TAG);

		} catch (NameNotFoundException nameNotFoundEx) {
			Utility.logMsg(targetPackage + "does not exists", TAG);
			// package does not exists
			packageInfo = null;
		} catch (Exception ex) {
			// DO Nothing
		}
		return packageInfo;
	}

	public static String getMarketAppPackageName() {
		String appMarketPkg = ANDROID_MARKET_PKG;
		if (Utility.APP_MARKET == Utility.AMAZON_MARKET) {
			appMarketPkg = AMAZON_MARKET_PKG;
		}
		return appMarketPkg;
	}



	public static void setSource(Context ctxt) {

		try {
			if (getSource(ctxt).equalsIgnoreCase(KEY_INSTALL_SOURCE_DEFAULT)) {
				// source not set so add to preference
				// this will make sure original source will always be used
				// Save user preferences. We need an Editor object to
				// make changes. All objects are from android.context.Context
				SharedPreferences settings = PreferenceManager
						.getDefaultSharedPreferences(ctxt);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString(KEY_INSTALL_SOURCE, SOURCE);

				editor.commit();
			}

		} catch (Exception ex) {
			Utility.logErrorMsg("setDeviceId:", TAG, ex);

		}
	}

	public static String getSource(Context ctxt) {
		String installSource = KEY_INSTALL_SOURCE_DEFAULT;
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);

			installSource = settings.getString(KEY_INSTALL_SOURCE,
					KEY_INSTALL_SOURCE_DEFAULT);
		} catch (Exception ex) {
			Utility.logErrorMsg("getSource:", TAG, ex);

		}
		return installSource;

	}





	protected static boolean isInternetOn(Context ctxt) {
		try {
			ConnectivityManager cm = (ConnectivityManager) ctxt
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnectedOrConnecting()) {
				return true;
			}
			return false;
		} catch (Exception ex) {
			Utility.logErrorMsg("isInternetOn", TAG, ex);
		}
		return false;
	}

	public static void printCursor(Cursor cursor) {
		try {
			String cursorData = DatabaseUtils.dumpCursorToString(cursor);
			Utility.logMsg(cursorData, "CURSOR_DATA");
		} catch (Exception ex) {

		}
	}

	protected static boolean useBlueTooth(Context ctxt) {
		boolean value = true;
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			value = settings.getBoolean(KEY_BLUETOOTH_SETTING, value);
		} catch (Exception ex) {
			Utility.logErrorMsg("getChildLockSetting:", TAG, ex);

		}
		return value;
	}

	protected static void setUseBlueTooth(Context ctxt, boolean value) {
		try {
			SharedPreferences settings = PreferenceManager
					.getDefaultSharedPreferences(ctxt);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(KEY_BLUETOOTH_SETTING, value);
			editor.commit();
			trackThings("/ChildLock_Enabled", ctxt);

		} catch (Exception ex) {
			Utility.logErrorMsg("setChildLockSetting:", TAG, ex);

		}
	}

}