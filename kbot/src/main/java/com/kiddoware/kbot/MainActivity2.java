package com.kiddoware.kbot;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kiddoware.kbot.actions.Action;
import com.kiddoware.kbot.actions.ActionController;
import com.kiddoware.kbot.controllers.KPAIMediaController;
import com.kiddoware.kbot.models.ActionMessage;
import com.kiddoware.kbot.models.ActionSection;
import com.kiddoware.kbot.views.ActionMessagesAdapter;

import java.util.List;
import java.util.Locale;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.ui.AIDialog;

/**
 * Created by Shardul on 24/05/17.
 */

public class MainActivity2 extends BaseActivity
        implements View.OnClickListener, TextWatcher, ActionController.ActionControllerListener, AIDialog.AIDialogListener {

    private static final String TAG = MainActivity2.class.getName();
    private static final int SEARCH_MODE_MIC = 1;
    private static final int SEARCH_MODE_TEXT = 2;
    private static final int REQUEST_HEARING_ACTUATOR = 9987;
    private static final String ACCESS_TOKEN = "b8a5ea9938ee461582785d97338a8939"; //api.ai bot access token

    public static String EXTRA_START_LISTENING_MODE = "StartListeningMode";

    final AIConfiguration mConfig = new AIConfiguration(ACCESS_TOKEN,
            AIConfiguration.SupportedLanguages.English);
    final AIDataService mAiDataService = new AIDataService(mConfig);
    final AIRequest gaiRequest = new AIRequest();

    private AIDialog aiDialog;

    private Toolbar toolbar;
    private View content;
    private View permission;
    private AppBarLayout appBarLayout;
    private EditText keywordEditText;
    private FloatingActionButton searchButton;
    private Button grantPermissionButton;
    private Button settingsButton;
    private ProgressBar progressBar;
    private KPAIMediaController mediaController;

    private RecyclerView recyclerView;
    private ActionMessagesAdapter adapter;

    private ActionController actionController;

    private ActionSection actionSection;
    public static String START_LISNENING_MODE = "StartListeningMode";

    private boolean startListeningMode;
    private Bundle bundle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionController = new ActionController(this);

        final ai.api.android.AIConfiguration config = new ai.api.android.AIConfiguration(Config.ACCESS_TOKEN,
                ai.api.android.AIConfiguration.SupportedLanguages.English,
                ai.api.android.AIConfiguration.RecognitionEngine.System);
        config.setRecognizerStartSound(getResources().openRawResourceFd(com.kiddoware.kbot.R.raw.test_start));
        config.setRecognizerStopSound(getResources().openRawResourceFd(com.kiddoware.kbot.R.raw.test_stop));
        config.setRecognizerCancelSound(getResources().openRawResourceFd(com.kiddoware.kbot.R.raw.test_cancel));

        aiDialog = new AIDialog(this, config);
        aiDialog.setResultsListener(this);

        startListeningMode = getIntent().getBooleanExtra(EXTRA_START_LISTENING_MODE, false);

        setContentView(R.layout.main2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mediaController = (KPAIMediaController) findViewById(R.id.main_media_controller);
        content = findViewById(R.id.content);
        permission = findViewById(R.id.main_permission);
        appBarLayout = (AppBarLayout) findViewById(R.id.main_app_bar_layout);
        keywordEditText = (EditText) findViewById(R.id.main_et_keyword);
        searchButton = (FloatingActionButton) findViewById(R.id.main_bnt_mic);
        grantPermissionButton = (Button) findViewById(R.id.main_btn_grant_permissions);
        settingsButton = (Button) findViewById(R.id.main_btn_open_settings);
        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        progressBar = (ProgressBar) findViewById(R.id.main_search_progress);

        adapter = new ActionMessagesAdapter(this);

        setSupportActionBar(toolbar);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);

        keywordEditText.setOnClickListener(this);
        searchButton.setOnClickListener(this);
        grantPermissionButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        keywordEditText.addTextChangedListener(this);
        keywordEditText.setHint(getResources().getString(R.string.main_keyphrase) + VoiceRecognitionService.KEYPHRASE);
        searchButton.setTag(SEARCH_MODE_MIC);

        actionController.setListener(this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        showSuggestions();


        //start wake word service
        Intent pIntent = new Intent(getApplicationContext(), VoiceRecognitionService.class);
        startService(pIntent);

        // enable 10 secs restart
        Intent mIntent = new Intent(getApplicationContext(), AlarmBroadcastReceiver.class);

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (aiDialog != null) {
            aiDialog.pause();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        actionController.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (startListeningMode) {
            startAutoListening();
        }

        if (aiDialog != null) {
            aiDialog.resume();
        }

        checkRequiredPermissions(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkRequiredPermissions(true);

        actionController.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        actionController.release();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Utility.logMsg("onNewIntent :: old startListeningMode :: " + startListeningMode, TAG);
        if (intent != null) {
            startListeningMode = intent.getBooleanExtra(EXTRA_START_LISTENING_MODE, false);
            Utility.logMsg("onNewIntent :: new startListeningMode :: " + startListeningMode, TAG);
            intent = null; //as we do not want this to pop up on orientation change
        }

    }


    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(com.kiddoware.kbot.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        final int id = item.getItemId();
        if (id == com.kiddoware.kbot.R.id.action_settings) {
            startActivity(SettingsActivity.class);
            return true;
        } else if (id == R.id.contact_activity) {
            startActivity(ContactActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startActivity(Class<?> cls) {
        final Intent intent = new Intent(this, cls);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_bnt_mic:
                if (searchButton.getTag() instanceof Integer) {
                    switch ((int) searchButton.getTag()) {
                        case SEARCH_MODE_MIC:
//                            openVoiceRecognizationPrompt();
                            actionController.pause();
                            aiDialog.showAndListen();
                            break;
                        case SEARCH_MODE_TEXT:


                            sendRequestToAI(keywordEditText.getText().toString());
                            keywordEditText.setText("");
                            break;
                    }

                }

                break;
            case R.id.main_et_keyword:
                v.setOnClickListener(null);
                appBarLayout.setExpanded(false);
                break;
            case R.id.main_btn_grant_permissions:
                checkRequiredPermissions();
                break;
            case R.id.main_btn_open_settings:
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                break;
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s != null && s.length() > 0) {
            searchButton.setImageResource(R.drawable.ic_kpbot_black_48dp);
            searchButton.setTag(SEARCH_MODE_TEXT);
        } else {
            searchButton.setImageResource(R.drawable.ic_mic_black_48dp);
            searchButton.setTag(SEARCH_MODE_MIC);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onActionMessage(ActionController controller, ActionMessage actionMessage) {

        mediaController.setVisibility(View.GONE);

        ActionSection section;

        if (actionSection == null || !actionSection.getExecutor().equals(actionMessage.getExecutor())) {

            String sectionTitle = null;

            if (controller == null) {
                sectionTitle = getString(R.string.user_executor_name);
            } else {
                if (actionMessage.getSource() == null || actionMessage.getSource().isEmpty()) {
                    sectionTitle = getString(R.string.app_name);
                } else {
                    sectionTitle = actionMessage.getSource();
                }
            }

            section = new ActionSection(sectionTitle);

            section.setExecutor(actionMessage.getExecutor());
            section.getMessages().add(actionMessage);
            adapter.addSection(section);
        } else {
            section = actionSection;
            section.getMessages().add(actionMessage);
            adapter.notifyDataSetChanged();
        }

        actionSection = section;
    }

    @Override
    public void onMediaPlayerPrepared(ActionController controller, MediaPlayer mediaPlayer) {
        mediaController.setVisibility(View.VISIBLE);
        mediaController.setMediaPlayer(controller);
    }

    //utility methods to process AI Request/Response
    private void startAutoListening() {
        startListeningMode = false;
        showAndListen();
    }

    private void showAndListen() {
        actionController.pause();
        aiDialog.showAndListen();
    }

    public void openVoiceRecognizationPrompt() {

        try {


            // check if there is recognition Activity
            if (isSpeechRecognitionActivityPresented() == false) {

                // if no, then showing notification to install Voice Search
                Toast.makeText(this, getString(com.kiddoware.kbot.R.string.in_order_to_activate_voice_recognition), Toast.LENGTH_LONG).show();
                // start installing process
                installGoogleVoiceSearch();

                return;
            }


            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);


            if (bundle != null) {
                String languageLocale = bundle.getString(RecognizerIntent.EXTRA_LANGUAGE);
                if (languageLocale == null)
                    languageLocale = Locale.getDefault().getLanguage(); //  planguage;

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, languageLocale);
            } else
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault().getLanguage() /*planguage*/);


            if (intent.resolveActivity(this.getPackageManager()) != null)
                startActivityForResult(intent, REQUEST_HEARING_ACTUATOR);
            else
                Toast.makeText(this, getString(com.kiddoware.kbot.R.string.sorry_i_can_not_find), Toast.LENGTH_LONG).show();


        } catch (Exception ex) {

        }

    }

    @Override
    public void requiresPermissions(String[] permissions) {
        if (permissions == null) {
            content.setVisibility(View.VISIBLE);
            permission.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.GONE);
            permission.setVisibility(View.VISIBLE);
        }
    }

    private void showSearchProgress(boolean b) {
        if (b) {
            searchButton.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            keywordEditText.setEnabled(false);
        } else {
            searchButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            keywordEditText.setEnabled(true);
        }
    }

    /**
     * Veerify if Google  SpeechRecognition is available in device
     *
     * @return true if    SpeechRecognition Activity Presented in device , false if not
     */
    private boolean isSpeechRecognitionActivityPresented() {
        try {
            // getting an instance of package manager
            PackageManager pm = this.getPackageManager();
            // a list of activities, which can process speech recognition Intent
            List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);

            if (activities.size() != 0) {    // if list not empty
                return true;                // then we can recognize the speech
            }
        } catch (Exception e) {

        }

        return false; // we have no activities to recognize the speech
    }


    /***
     * install Google Voice package from Play Store
     */
    private void installGoogleVoiceSearch() {
        try {
            // creating a dialog asking user if he want
            // to install the Voice Search
            Dialog dialog = new AlertDialog.Builder(this)
                    .setMessage(getString(com.kiddoware.kbot.R.string.for_recognition_install))    // dialog message
                    .setTitle(getString(com.kiddoware.kbot.R.string.install_voice_search_))    // dialog header
                    .setPositiveButton(getString(com.kiddoware.kbot.R.string.install), new DialogInterface.OnClickListener() {    // confirm button

                        // Install Button click handler
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                // creating an Intent for opening applications page in Google Play
                                // Voice Search package name: com.google.android.voicesearch
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.googlequicksearchbox"));
                                // setting flags to avoid going in application history (Activity call stack)
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                                // sending an Intent
                                MainActivity2.this.startActivity(intent);
                            } catch (Exception ex) {
                                // if something going wrong
                                // doing nothing
                            }
                        }
                    })

                    .setNegativeButton(getString(com.kiddoware.kbot.R.string.cancel), null)    // cancel button
                    .create();

            dialog.show();    // showing dialog
        } catch (Exception ex) {

        }
    }

    /**
     * request speech2text message to api.ai
     *
     * @param pmessage : speech2text message
     */
    public void sendRequestToAI(String pmessage) {
        try {


            gaiRequest.setQuery(pmessage);


            new AsyncTask<AIRequest, Void, AIResponse>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    MainActivity2.this.showSearchProgress(true);
                }

                @Override
                protected AIResponse doInBackground(AIRequest... requests) {
                    final AIRequest request = requests[0];
                    try {
                        final AIResponse response = mAiDataService.request(gaiRequest);
                        return response;
                    } catch (AIServiceException e) {
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(AIResponse aiResponse) {
                    MainActivity2.this.showSearchProgress(false);

                    if (aiResponse != null) {

                        onResult(aiResponse);
                    }

                }


            }.execute(gaiRequest);

        } catch (Exception ex) {
            Toast.makeText(MainActivity2.this, ex.getMessage(), Toast.LENGTH_LONG).show();

        }
    }


    @Override
    public void onResult(AIResponse result) {

        if (result.getResult() != null && result.getResult().getResolvedQuery() != null) {
            ActionMessage actionMessage = new ActionMessage();
            actionMessage.setSource(getString(R.string.user_executor_name));
            actionMessage.setExecutor(getString(R.string.user_executor_name));
            actionMessage.setMessage(result.getResult().getResolvedQuery());
            onActionMessage(null, actionMessage);
        }


        actionController.enqueueAction(result);
    }

    @Override
    public void onError(AIError error) {
        showSearchProgress(false);
        actionController.resume();
    }

    @Override
    public void onCancelled() {
        showSearchProgress(false);
        actionController.resume();
    }

    @Override
    public void onFinished() {
        showSearchProgress(false);
    }

    @Override
    public void onStarted() {
        showSearchProgress(true);
        actionController.pause();
    }

    private void showSuggestions() {
        adapter.clear();

        ActionSection section = new ActionSection(getString(R.string.kp_ai_suggestion));

        Action[] sugggestedActions = {Action.APPS_OPEN,  Action.BROWSER_OPEN};

        for (Action action : sugggestedActions) {

            ActionMessage actionMessage = new ActionMessage();
            actionMessage.setSuggestions(new String[]{action.getTitle(this),
                    action.getDescription(this)});

            section.getMessages().add(actionMessage);
        }

        adapter.addSection(section);

    }
}
