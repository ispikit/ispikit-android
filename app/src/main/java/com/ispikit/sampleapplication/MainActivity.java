package com.ispikit.sampleapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ispikit.library.IspikitWrapper;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    // This is the sentence a user inputs in the text field
    // It will be used by the Ispikit library to assess the pronunciation
    private EditText m_text_to_be_analyzed;
    // This is the word that must be added to the dictionary, and its
    // corresponding pronunciation
    private EditText m_word_to_be_added;
    private EditText m_pron_of_word_to_be_added;

    // You should have one instance of SimpleIspikitWrapper
    private IspikitWrapper m_ispikit_wrapper;

    // You will need to pass your activity's context to the
    // IspikitWrapper constructor
    private Context m_Context = this;
    private int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Link the member variable to the UI for the sentence to be read,
        // and word to be added
        m_text_to_be_analyzed = (EditText) findViewById(R.id.editText1);
        m_word_to_be_added = (EditText) findViewById(R.id.newWord);
        m_pron_of_word_to_be_added = (EditText) findViewById(R.id.newPronunciation);

        // Sets the Initialized UI indicator to No
        TextView initialized = (TextView) findViewById(R.id.initialized);
        initialized.setText("No");

        // Create a new instance of SimpleIspikitWrapper and attach the handlers
        // defined below
        m_ispikit_wrapper = new IspikitWrapper(m_Context);
        final InitHandler m_HandlerInit = new InitHandler(this);
        m_ispikit_wrapper.setInitHandler((Handler) m_HandlerInit);
        final ResultHandler m_HandlerResult = new ResultHandler(this);
        m_ispikit_wrapper.setResultHandler((Handler) m_HandlerResult);
        final WordsHandler m_HandlerWords = new WordsHandler(this);
        m_ispikit_wrapper.setWordsHandler((Handler) m_HandlerWords);
        final CompletionHandler m_HandlerCompletion = new CompletionHandler(this);
        m_ispikit_wrapper.setCompletionHandler((Handler) m_HandlerCompletion);
        final AudioHandler m_HandlerAudio = new AudioHandler(this);
        m_ispikit_wrapper.setAudioHandler((Handler) m_HandlerAudio);
    }

    // You must create new classes deriving from Handler, one for each callback.
    // In the handlemessage, method, you can update the activity's UI
    static class InitHandler extends Handler {
        WeakReference<MainActivity> m_Activity;

        InitHandler(MainActivity a) {
            m_Activity = new WeakReference<MainActivity>(a);
        }

        @Override
        public void handleMessage(Message message) {
            // After you get this, the library is now usable, you can update the app's
            // UI to reflect it and allow users to start recording.
            MainActivity a = m_Activity.get();
            Toast.makeText(a, "Initialization Done", Toast.LENGTH_LONG).show();
            TextView initialized = (TextView) a.findViewById(R.id.initialized);
            initialized.setText("Yes");
        }
    }

    static class ResultHandler extends Handler {
        WeakReference<MainActivity> m_Activity;

        ResultHandler(MainActivity a) {
            m_Activity = new WeakReference<MainActivity>(a);
        }

        @Override
        public void handleMessage(Message message) {
                // Here you get the final pronunciation score and speed.
            MainActivity a = m_Activity.get();
            TextView score = (TextView) a.findViewById(R.id.score);
            score.setText(String.valueOf(message.arg1));
            TextView speed = (TextView) a.findViewById(R.id.speed);
            speed.setText(String.valueOf(message.arg2));
            TextView analyzed_words = (TextView) a.findViewById(R.id.analyzed_words);
            analyzed_words.setText((String) message.obj);
        }
    }

    static class WordsHandler extends Handler {
        WeakReference<MainActivity> m_Activity;

        WordsHandler(MainActivity a) {
            m_Activity = new WeakReference<MainActivity>(a);
        }

        @Override
        public void handleMessage(Message message) {
            // You can parse the string to detect if the last word is recognized.
            // If the last word is recognized, you can stop recognition calling
            // m_simple_ispikit_wrapper.StopRecording(false);
            MainActivity a = m_Activity.get();
            TextView words = (TextView) a.findViewById(R.id.words);
            words.setText((String) message.obj);
        }
    }

    static class CompletionHandler extends Handler {
        WeakReference<MainActivity> m_Activity;

        CompletionHandler(MainActivity a) {
            m_Activity = new WeakReference<MainActivity>(a);
        }

        @Override
        public void handleMessage(Message message) {
            // You can wire this to a ProgressBar to show completion to
            // the user.
            MainActivity a = m_Activity.get();
            TextView completion = (TextView) a.findViewById(R.id.completion);
            completion.setText(String.valueOf(message.arg1));
        }
    }

    static class AudioHandler extends Handler {
        WeakReference<MainActivity> m_Activity;

        AudioHandler(MainActivity a) {
            m_Activity = new WeakReference<MainActivity>(a);
        }

        @Override
        public void handleMessage(Message message) {
            // You can wire this to a ProgressBar to show completion to
            // the user.
            MainActivity a = m_Activity.get();
            TextView volume = (TextView) a.findViewById(R.id.volume);
            volume.setText(String.valueOf(message.arg1));
            TextView pitch = (TextView) a.findViewById(R.id.pitch);
            pitch.setText(TextUtils.join(",", (Object[]) message.obj));
        }
    }
    // Here be define what should be done when clicking buttons, this should be self-explanatory.
    // If there is anything wrong, we add the possible causes in a Toast.
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_init:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "We need to access the microphone", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                } else {

                    if (!m_ispikit_wrapper.Init()) {
                        Toast.makeText(this, "Error during initialization or ispikit already initialized", Toast.LENGTH_LONG).show();
                    } else {
                        TextView initialized = (TextView) findViewById(R.id.initialized);
                        initialized.setText("Initializing...");
                    }
                }
                break;
            case R.id.button_start_recording:
                if (m_ispikit_wrapper.SetSentence(m_text_to_be_analyzed.getText().toString())) {
                    m_ispikit_wrapper.Start();
                } else {
                    Toast.makeText(this, "Make sure ispikit is initialized and that the sentence is valid", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.button_stop_recording:
                if (!m_ispikit_wrapper.Stop(false)) {
                    Toast.makeText(this, "Application was not recording", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.button_start_replay:
                m_ispikit_wrapper.StartPlayback();
                break;
            case R.id.button_stop_replay:
                m_ispikit_wrapper.StopPlayback();
                break;
            case R.id.button_add_word:
                if (!m_ispikit_wrapper.AddWord(m_word_to_be_added.getText().toString(), m_pron_of_word_to_be_added.getText().toString())) {
                    Toast.makeText(this, "Error adding new word", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    // When the app goes to Pause state, make sure you stop recording and playback
    // in case anything is running. When stopping recognition, set the argument to true
    // so that no analysis starts.
    // Call onPause on the superclass first.
    @Override
    protected void onPause() {
        super.onPause();
        m_ispikit_wrapper.Stop(true);
        m_ispikit_wrapper.StopPlayback();
    }

    // When the app is destroyed, make sure you shut down the library first.
    // Make sure you call onDestroy on the superclass first
    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_ispikit_wrapper.Shutdown();
    }

    /**********************************************************
     * The remaining of this file is not related to the Ispikit library
     * but only intends to make it easier for users of this sample application
     * to hide the soft keyboard after they finish input the sentence.
     */

    private boolean isPointInsideView(float x, float y, View view) {
        int location[] = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        //point is inside view bounds
        if ((x > viewX && x < (viewX + view.getWidth())) &&
                (y > viewY && y < (viewY + view.getHeight()))) {
            return true;
        } else {
            return false;
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                if (!(
                        isPointInsideView(ev.getRawX(), ev.getRawY(), m_text_to_be_analyzed) ||
                                isPointInsideView(ev.getRawX(), ev.getRawY(), m_word_to_be_added) ||
                                isPointInsideView(ev.getRawX(), ev.getRawY(), m_pron_of_word_to_be_added))) {
                    hideSoftKeyboard(this);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

}
