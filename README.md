Ispikit Library for Android - Version 2.0
=========================================

This package contains the Ispikit Library for Android. It also includes a simple application that illustrates the use of the library. The library is intended to recognize what users say and assess their pronunciation. All computations are made on the device, and no Internet call is made.

You can open this project in Android Studio to compile and test the sample application. The library itself is located at `./library-release/library-release.aar`.

This version is free to use and includes two limitations compared to the full version:

* Number of sentences for recognition is limited to 3
* Number of words per sentence is limited to 4

Contact us at info@ispikit.com for the full version.


# 1. Features

* Audio recording: records user's voice through internal or external microphone.
* Speech recognition: recognizes what the user said among several possible inputs. Recognized words are available in real-time.
* Pronunciation assessment: returns overall pronunciation score.
* Playback of user's input: recorded voice can be played back.
* Mispronounced words detection: detects and flags words that have been mispronounced.
* Audio volume: During recording, audio volume callbacks can be used to display the audio input level.
* Waveform: during recording, waveform callbacks can be used to draw and display the recorded audio.
* Pitch tracking: during recording, pitch callbacks can be used to plot user's pitch contour (intonation).
* Local-only: everything happens locally, no network call made.

Note: This build includes binaries for all Android architectures (x86 and ARM).


# 2. Compile and use the sample application

* Open the Android Studio project.
* Build and run the application on a device or AVD with API level of, at least, 10.
* Once the app is up, you must first initialize the library (press the `Initialize` button). It takes a few seconds depending on your device or emulator. On Android 6.0+, the first time you try to initialize, it will ask you to authorize access to the microphone. Press the `Initialize` button again once you have accepted.
* The field `Initialized` changes to `Yes` once completed.
* Enter one or several sentences in the text field. Possible sentences to recognize are comma-separated, without spaces except between words.
* Press the `Start Recording` button and say one of the sentences.
* While you read, you will see the recognized words as they are reported by the library (in the format `x-y-z-k`, explained later in this document).
* While you read, you will see the audio volume reported by the library.
* While you read, you will see the pitch samples reported by the library (also explained in more details later).
* Press `Stop Recording` to stop.
* Analysis is starting, you can see the completion status in the `Completion` field.
* At the end of analysis, you will see the score (between 0 and 100) and speech tempo (measure of speed, explained later) in the corresponding fields. You will also see the analyzed words (in the format `i-j-k`, explained in section 4. of this document). If you are using the emulator, the result is not accurate as it records at 8 kHz instead of 16 kHz.
* You can replay the last recording with the `Start Replay` button.
* You can also add words that are not in the built-in dictionary, more on this later in this document.


# 3. What the Ispikit Library does

The library records a user's audio input, recognizes it and computes a pronunciation score based on the words that the user was expected to say. The score is a number between 0 and 100, 100 being the ideal score of a native-like pronunciation. It also gives the reading speed (speech tempo) that can be used to measure the reader's fluency. It is measured by "number of spoken phonemes in 10 seconds", and in practice should be more than 80 for a fluent reader or speaker.

Recognition is made among a set of possible sentences, and recognized words are returned in real-time.

Expected sentences can be of any length, using any word that exists in the pronunciation dictionary. The library ships with a pronunciation dictionary which is fairly large and should contain most words used in language learning context. However, it can be expanded to add new words, either before it is packaged into an app or at run-time (See Appendix).


# 4. Integrate the library into an Android application

The library is given as a `.aar` file, located at `./library-release/library-release.aar`. To import the library into your application, in Android Studio, open `File -> New -> New Module... -> Import .JAR/.AAR Package` and select the library file. Then edit the dependency with `Build -> Edit Libraries and Dependencies`, select your application, add a module, select the library. You can now include the package name inside your Java files:

```java
import com.ispikit.library.IspikitWrapper;
```

The Android SDK will handle the packaging of the library and resources inside your application.

You can then use the API, paying attention to:

* Create one instance of `IspikitWrapper`.
* Initialize it. Initialization is a process that can take seconds to complete, so it is done asynchronously. Make sure you register its callback (by setting handlers as explained later) to show the status in the application's UI. Also, audio recording permission should have been granted before initialization, so make sure your application uses those permissions, and on Android 6+, make sure those permissions are accepted before calling the initialization function.
* Implement the functionalities as desired by adding proper calls.
* Make sure you call the `Shutdown` method in the `onDestroy` method of your activity.
* Make sure you call the `Stop` and `StopPlayback` methods in the `onPause` of your activity. For `Stop`, call it with its parameter set to `true` so that no analysis is started.
* Make sure you handle the change of device's orientation correctly. By default, Android will call `onPause`, `onDestroy`, then `onCreate` when orientation changes. So you would need to go through initialization again. To avoid that, you can either lock the orientation or override Android's behavior.

You can take the sample application as a basis for your implementation. The sample application includes comments to explain what it does. The whole source code is in one Java file (`./app/src/main/java/com/ispikit/sampleapplication/MainActivity.java`) and one layout file (`./app/src/main/res/layout/activity_main.xml`).


# 5. API documentation

All functionalities are accessed through the `IspikitWrapper` class. You must import `com.ispikit.library.IspikitWrapper` to access it.

## a. Constructor:

```java
public IspikitWrapper(Context c);
```

You must provide the context of your activity so that the native library can find the resource files.

## b. Public methods

Following are the public methods exposed by the `IspikitWrapper` class. They usually return a boolean, where false indicates that the call was not successful.

* `public boolean Init();`

  This will start the initialization of the library. It calls back with the `onInit` function. On slow devices or in the emulator it can take a few dozens of seconds. Returns false if already initialized.
  
  Note that microphone access should be granted before calling that function. In particular, On Android 6.0+, make sure you request and set it before the call (see sample application).
* `public boolean SetSentence(String sentences);`

  Sets the current sentences which will be the one used when the next recognition starts. The possible sentences to be expected are comma separated, no other space than one space between each word. If there is an issue with the sentences, it returns false.
* `public boolean Start();`

  Starts recording on the current sentences. If there are no valid current sentences, it returns false. During recording, each time a new word is recognized, it calls back with the `onNewWords` function, which can be useful to automatically stop recognition when the last word of the sentence is recognized. It also calls back with the `onAudio` callback, which provides audio volume and pitch samples.
* `public boolean Stop(boolean force);`

  Stops recording. If force is set to false, analysis of the audio starts. If force is set to true, no analysis will be done. This is useful for instance if the app is set to the `Pause` state while recording, and you do not want the result of the current audio. During analysis, it calls back with the `onCompletion` function to indicate progress (as a percentage). Once analysis is done, it calls back with `onNewResult`.
* `public boolean StartPlayback();`

  Starts playing back the last recorded audio. It calls back once done with the `onPlaybackDone` function, unless it is stopped with a call to `StopPlayback`.
* `public boolean StopPlayback();`

  Stops playback.
* `public boolean AddWord(String word, String pronunciation);`

  Adds words to the dictionary at run-time. If a word already exists, the new pronunciation is added to the existing ones, otherwise a new word is created. Words added through this API do not persist. See Appendix for details.
* `public boolean Shutdown();`

  Completely shuts down the library and release resources. This should be called in the activity's `onDestroy` method. It is synchronous.

## c. Callbacks

The library uses callback methods that are associated to handlers so that your application can register them and update the UI accordingly. So, in your application, you should declare the new handlers and call the "set...Handler" functions to attach them. You can see examples in the sample application.

* `public void setInitHandler(Handler h);`

  Called once initialization is done. If the returned status is 0, the initialization is successful. This value is set in `arg1` in the associated handler.
* `public void setCompletionHandler(Handler h);`

  These are called after recording is stopped, while analysis is performed. It shows the progress of analysis with the percentage of completion. The completion percentage is set in the `arg1` in the associated handler. It can be used, for instance, to display a progress bar.
* `public void setWordsHandler(Handler h);`

  During recognition, words are recognized in real time and each time a new word is recognized, it sends a message, giving the string of all recognized words. This string is passed to `obj` in the associated handler.

  The words are given in the following format: `x-y-z-k`. `x`, the first index, is the index of the sentence that was recognized (starting with 0, the first sentence given to the call to `setSentence`). The second index `y` is the word index within the sentence, also starting with 0. The remaining two indexes `z` and `k` are not useful.

  So, if you give a sentence with 10 words, you can stop recording once 'y' is equal to 9. Accuracy of this detection is not perfect, and if you want to make use of it to stop recording, you might want to experiment with it to determine if the accuracy is good enough for your needs.
* `public void setPlaybackDoneHandler(Handler h);`

  This is called once playback is done, there is no argument.
* `public void setResultHandler(Handler h);`

  This is called once analysis is done, giving the score of the pronunciation (between 0 and 100, 100 being the highest score, showing native-like pronunciation) as well as the measure of speed (speech tempo). Speed is measured by "number of spoken phonemes in 10 seconds". A possible satisfactory value could be 80, slower values being associated to slow or not fluent speech.

  The words String gives the list of recognized words together with a flag telling whether the word has been mispronounced. Each word is coded with three indexes: The first index is the index of the sentence that was recognized (starting with 0, the first sentence given to the call to `setSentence`). The second index is the word index within the sentence, also starting with 0, and the third number is a flag: 0 if the word was correctly said, 1 if it was mispronounced. Word level mispronunciation detection is not 100% accurate, especially for short words.

  For instance, if the sentences are: "one two three,four five,six seven eight" and words String is `2-0-0 2-2-1`, it means that the user said "six eight", "eight" being mispronounced.

  In the message, score is stored in `arg1`, speed is stored in `arg2` and the words String is stored in `obj`.
* `public void setAudioHandler(Handler h);`

  During recording, this gives informations on the audio coming in. The `arg1` field gives the audio volume (between 0 and 100) and the `obj` field is an array of strings, where each element is a value of pitch sample. Each message would then give a few samples of pitch contour that can be used for instance to draw the pitch contour to show the user's intonation.

# 6. Important requirements for applications that use the Ispikit Library

Following are hard requirements for your application:

* The Ispikit Library requires Android API 10 (2.3.3) to work. So your application should enforce at least this version, for instance:

  ```xml
<uses-sdk
    android:minSdkVersion="10"
    android:targetSdkVersion="22" />
```
* The library is compiled for ARM architecture (both armeabi-v7a and armeabi) and Intel (x86) it will not work on MIPS architecture.
* Your application must request for the permissions to access audio recording devices, for instance:

  ```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

Following are soft requirements or advises for your application:

* In the `onPause` method of the activity, stop playback and recording, in case anything is running. Stop recording with "true" as argument so that no analysis is launched.
* In the `onDestroy` method of the activity, call the `Shutdown` method on the library to properly release resources.
* By default, Android restarts the application each time the device orientation changes, calling the `onDestroy`, then the `onCreate` methods. Since initializing the Ispikit Library takes times, it is desirable to lock the orientation (if possible) or override this behavior.
* Implement an automatic way of stopping recognition to avoid consuming resources if users did not press a `Stop` button. It could be done for instance using the real-time callback of recognized words, as explained previously, or set a fix timeout (say a few seconds), or a push-to-talk-like interface.


# 7. Additional notes

The Ispikit Library can work on an Android Virtual Device (AVD) with audio recording capabilities. However, it could be very slow and will probably not give significant assessment scores as recording is done in 8 kHz, and the application requires a sample rate of 16 kHz.

# Appendix: Pronunciation Dictionary

In order to recognize and analyze speech, assuming the speaker is saying one of the provided sentences, the library must know how each of the word used in the sentence is pronounced. The library ships with a pronunciation dictionary that contains most words used in English.

Other words can be added at run-time using the provided API (See the `AddWord` function). Words added at run-time do not persist when the library or app restarts. The syntax for pronunciation is the CMU Pronunciation dictionary: http://www.speech.cs.cmu.edu/cgi-bin/cmudict. Words and phonemes are case insensitive.
