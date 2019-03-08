package net.ishero.languagerepeater;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.ishero.languagerepeater.AudioModel.AudioChannel;
import net.ishero.languagerepeater.AudioModel.AudioRecorder;
import net.ishero.languagerepeater.AudioModel.AudioSampleRate;
import net.ishero.languagerepeater.AudioModel.AudioSource;
import net.ishero.languagerepeater.RecordButtonView.RecordButton;
import net.ishero.languagerepeater.WordView.GetWordTextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cmu.sphinx.alignment.LongTextAligner;
import edu.cmu.sphinx.api.SpeechAligner;
import edu.cmu.sphinx.linguist.acoustic.Unit;
import edu.cmu.sphinx.result.WordResult;
public class MainActivity extends AppCompatActivity {
    private RecordButton recordButton;
    private GetWordTextView wordText;
    private static String ACOUSTIC_MODEL_PATH =
            "resource:/edu/cmu/sphinx/models/en-us/en-us";
    private static String DICTIONARY_PATH =
            "resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict";
    private static final String TEXT = "Millions of people watch TV.";
    private AudioRecorder audioRecorder;
    private MediaPlayer player;
    private static SpeechAligner aligner;
    private String redStr = "";
    private String greenStr = "";
    private static Handler handler;
    private static LongTextAligner textAligner;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, "正在进行初始化，请勿操作！", Toast.LENGTH_SHORT).show();
        init();
        initTitle();
        writeAssets();

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        notify_title(redStr, greenStr);
                        break;

                }
                super.handleMessage(msg);
            }
        };

    }

    private void init() {
        Permission.requestPermission(this, Manifest.permission.RECORD_AUDIO);
        Permission.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        recordButton = findViewById(R.id.recordButton);
        wordText = findViewById(R.id.wordText);
        recordButton.setOnRecordStateChangedListener(new RecordButton.OnRecordStateChangedListener() {
            @Override
            public void onRecordStart() {
                record();
            }

            @Override
            public void onRecordStop() {
                try {
                    audioRecorder.stop();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                spinx4(Environment.getExternalStorageDirectory() + "/test.wav");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }


                    }).start(
                    );
                    startPlaying(Environment.getExternalStorageDirectory() + "/test.wav");

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onZoom(float percentage) {

            }
        });
    }

    private void spinx4(String filename) throws IOException {
        URL audioUrl = new URL("file://" + filename);
        String transcript = TEXT;
        List<WordResult> results = aligner.align(audioUrl, transcript);
        List<String> stringResults = new ArrayList<String>();
        for (WordResult wr : results) {
            stringResults.add(wr.getWord().getSpelling());
            Log.e("result_word", wr.getWord().getSpelling());
            Log.e("result_word_p", "prob:" + wr.getWord().getMostLikelyPronunciation().getProbability() +
                    "score:" + wr.getConfidence() + "most:" + wr.getWord().getMostLikelyPronunciation().getProbability());

            String uStr = "";
            for (Unit unit : wr.getWord().getMostLikelyPronunciation().getUnits()) {
                uStr = uStr + " " + unit.getName();
            }
            Log.e("result_unit", uStr);
        }


        textAligner =
                new LongTextAligner(stringResults, 1);
        List<String> sentences = aligner.getTokenizer().expand(transcript);
        List<String> words = aligner.sentenceToWords(sentences);

        redStr = "";
        greenStr = "";

        int[] aid = textAligner.align(words);
        int lastId = -1;
        for (int i = 0; i < aid.length; ++i) {
            if (aid[i] == -1) {
                redStr += words.get(i) + " ";
                System.out.format("- %s\n", words.get(i));
            } else {
                greenStr += words.get(i) + " ";
                if (aid[i] - lastId > 1) {
                    for (WordResult result : results.subList(lastId + 1,
                            aid[i])) {

                        System.out.format("+ %-25s [%s]\n", result.getWord()
                                .getSpelling(), result.getTimeFrame());
                    }
                }
                System.out.format("  %-25s [%s]\n", results.get(aid[i])
                        .getWord().getSpelling(), results.get(aid[i])
                        .getTimeFrame());
                lastId = aid[i];
            }
        }

        if (lastId >= 0 && results.size() - lastId > 1) {
            for (WordResult result : results.subList(lastId + 1,
                    results.size())) {
                System.out.format("+ %-25s [%s]\n", result.getWord()
                        .getSpelling(), result.getTimeFrame());
            }
        }
        Message message = new Message();
        message.what = 0;
        handler.sendMessage(message);

    }

    private void record() {
        String filePath = Environment.getExternalStorageDirectory() + "/test.wav";
        Log.d("p", filePath);

        audioRecorder = AudioRecorder.with(this)
                // Required
                .setFilePath(filePath)
                // Optional
                .setSource(AudioSource.MIC)
                .setChannel(AudioChannel.STEREO)
                .setSampleRate(AudioSampleRate.HZ_48000)
                .setAutoStart(true)
                .setKeepDisplayOn(true)
                // Start recording
                .record();

    }

    private void startPlaying(String filePath) throws IOException {
        player = new MediaPlayer();
        player.setDataSource(filePath);
        player.prepare();
        player.start();
        Log.d("player", "start playing");
    }

    private void stopPlaying() {
        Log.d("player", "stop playing");

        player.stop();
    }

    private boolean copyAssetAndWrite(String fileName) {
        try {
            File cacheDir = getCacheDir();
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File outFile = new File(cacheDir, fileName);
            if (!outFile.exists()) {
                boolean res = outFile.createNewFile();
                if (!res) {
                    return false;
                }
            } else {
                if (outFile.length() > 10) {//表示已经写入一次
                    return true;
                }
            }
            InputStream is = getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void writeAssets() {
        if (!copyAssetAndWrite("10001-90210-01803.wav"))
            Log.e("write_dir_error", "write error");
        if (!copyAssetAndWrite("feat.params"))
            Log.e("write_dir_error", "error");
        if (!copyAssetAndWrite("mdef"))
            Log.e("write_dir_error", "error");
        if (!copyAssetAndWrite("means"))
            Log.e("write_dir_error", "error");
        if (!copyAssetAndWrite("mixture_weights"))
            Log.e("write_dir_error", "error");
        if (!copyAssetAndWrite("noisedict"))
            Log.e("write_dir_error", "error");
        if (!copyAssetAndWrite("sendump"))
            Log.e("write_dir_error", "error");
        if (!copyAssetAndWrite("transition_matrices"))
            Log.e("write_dir_error", "error");
        if (!copyAssetAndWrite("variances"))
            Log.e("write_dir_error", "error");
        if (!copyAssetAndWrite("cmudict-en-us.dict"))
            Log.e("write_dir_error", "error");
        ACOUSTIC_MODEL_PATH = getCacheDir().getAbsolutePath();
        DICTIONARY_PATH = getCacheDir().getAbsolutePath() + "/cmudict-en-us.dict";
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (aligner == null) {
                    try {

                        aligner = new SpeechAligner(ACOUSTIC_MODEL_PATH, DICTIONARY_PATH, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Looper.prepare();
                Toast.makeText(getApplicationContext(), "模型初始化完毕！", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();

    }

    private void initTitle() {
        assert wordText != null;

        wordText.setText(TEXT);
        wordText.setOnWordClickListener(new GetWordTextView.OnWordClickListener() {
            @Override
            public void onClick(String word) {
                Toast.makeText(MainActivity.this, "" + word, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void notify_title(String redStr, String greenStr) {
        wordText.setHighLightText(redStr + greenStr);
        Map<String, Integer> color_map = new HashMap<>();
        for (String w : redStr.split(" ")) {
            color_map.put(w, Color.RED);
        }
        for (String w : greenStr.split(" ")) {
            color_map.put(w, Color.GREEN);
        }

        wordText.setHighLightArrayColor(color_map);

        wordText.setText(TEXT);


    }


}
