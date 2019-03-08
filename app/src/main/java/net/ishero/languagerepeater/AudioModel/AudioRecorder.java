package net.ishero.languagerepeater.AudioModel;


import android.app.Activity;
import android.graphics.Color;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import omrecorder.AudioChunk;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.Recorder;


public class AudioRecorder implements PullTransport.OnAudioChunkPulledListener{

    protected static final String EXTRA_FILE_PATH = "filePath";
    protected static final String EXTRA_COLOR = "color";
    protected static final String EXTRA_SOURCE = "source";
    protected static final String EXTRA_CHANNEL = "channel";
    protected static final String EXTRA_SAMPLE_RATE = "sampleRate";
    protected static final String EXTRA_AUTO_START = "autoStart";
    protected static final String EXTRA_KEEP_DISPLAY_ON = "keepDisplayOn";

    private Activity activity;
    private Fragment fragment;
    private Recorder recorder;

    private String filePath = Environment.getExternalStorageDirectory() + "/recorded_audio.wav";
    private AudioSource source = AudioSource.MIC;
    private AudioChannel channel = AudioChannel.STEREO;
    private AudioSampleRate sampleRate = AudioSampleRate.HZ_44100;
    private int color = Color.parseColor("#546E7A");
    private int requestCode = 0;
    private boolean autoStart = false;
    private boolean keepDisplayOn = false;

    private AudioRecorder(Activity activity) {
        this.activity = activity;
    }

    private AudioRecorder(Fragment fragment) {
        this.fragment = fragment;
    }

    public static net.ishero.languagerepeater.AudioModel.AudioRecorder with(Activity activity) {
        return new net.ishero.languagerepeater.AudioModel.AudioRecorder(activity);
    }

    public static net.ishero.languagerepeater.AudioModel.AudioRecorder with(Fragment fragment) {
        return new net.ishero.languagerepeater.AudioModel.AudioRecorder(fragment);
    }

    public net.ishero.languagerepeater.AudioModel.AudioRecorder setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }



    public net.ishero.languagerepeater.AudioModel.AudioRecorder setSource(AudioSource source) {
        this.source = source;
        return this;
    }

    public net.ishero.languagerepeater.AudioModel.AudioRecorder setChannel(AudioChannel channel) {
        this.channel = channel;
        return this;
    }

    public net.ishero.languagerepeater.AudioModel.AudioRecorder setSampleRate(AudioSampleRate sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    public net.ishero.languagerepeater.AudioModel.AudioRecorder setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
        return this;
    }

    public net.ishero.languagerepeater.AudioModel.AudioRecorder setKeepDisplayOn(boolean keepDisplayOn) {
        this.keepDisplayOn = keepDisplayOn;
        return this;
    }



    public AudioRecorder record() {

        if (recorder == null){
            recorder = OmRecorder.wav(new PullTransport.Default(Util.getMic(source, channel, sampleRate), this), new File(filePath));
        }
        recorder.startRecording();

        Log.d("r1", "record start");
        return this;
    }

    public void stop() throws IOException {
        if (recorder != null){
            recorder.stopRecording();
            Log.d("r2", "record stop");

        }
    }

    public void pause(){
        if(recorder != null){
            recorder.pauseRecording();
            Log.d("r3", "record pause");

        }
    }

    public void resume(){
        if(recorder != null){
            recorder.resumeRecording();
            Log.d("r4", "record resume");

        }
    }


    @Override
    public void onAudioChunkPulled(AudioChunk audioChunk) {

    }
}