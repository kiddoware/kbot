package com.kiddoware.kbot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class ContactActivity extends Activity {

	private Utility utility;
	private static final String TAG = "ContactActivity";

	public void onCreate(Bundle paramBundle) {
		try {		
			super.onCreate(paramBundle);
			this.getWindow().setFlags(
					WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.contact_us);
			utility = Utility.GetInstance();
	
		
		} catch (Exception ex) {
			Utility.logErrorMsg("onCreate", TAG, ex);

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Utility.logMsg("onResume", TAG);
	}





	public void onDestroy() {
		super.onDestroy();
		Utility.logMsg("onDestroy", TAG);

	}

	public void onPause() {
		super.onPause();
		Utility.logMsg("onPause", TAG);
	}

	/*
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	*/
	public void btnClickHandler(View v) {
		Button myBtn = (Button) v;
		if (myBtn != null) {
			if (myBtn.getId() == R.id.btnEmail) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL,
						new String[] { Utility.CONTACT_EMAIL});
				String emailSubject = getResources().getString(R.string.feedback_email_subject);
				emailSubject = String.format(emailSubject, utility.getCurrentAppVersion(getApplicationContext()));
				i.putExtra(Intent.EXTRA_SUBJECT, emailSubject );
				try {
					startActivity(Intent.createChooser(i, getResources().getString(R.string.email_chooserTitle)));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(this,
							R.string.noEmailClient,
							Toast.LENGTH_SHORT).show();
				}

			} else if (myBtn.getId() == R.id.btnEmailError) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("message/rfc822");
				i.putExtra(Intent.EXTRA_EMAIL,
						new String[] { Utility.CONTACT_EMAIL });
				String emailSubject = getResources().getString(R.string.error_email_subject);
				emailSubject = String.format(emailSubject, utility.getCurrentAppVersion(getApplicationContext()));

				i.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
				i.putExtra(Intent.EXTRA_TEXT, Utility
						.getErrorText(getApplicationContext().getFilesDir()
								.getAbsolutePath()));
				try {
					startActivity(Intent.createChooser(i, getResources().getString(R.string.email_chooserTitle)));
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(this,
							R.string.noEmailClient,
							Toast.LENGTH_SHORT).show();
				}

			} 
		}
	}



}
