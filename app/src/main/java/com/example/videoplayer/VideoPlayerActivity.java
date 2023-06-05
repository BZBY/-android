package com.example.videoplayer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class VideoPlayerActivity extends AppCompatActivity {

    private TextView videoNameTV, videoTimeTV, subtitleTV;
    private ImageButton backIB, forwardIB, playPauseIB, volumeDownIB, volumeUpIB, selectSubtitleBtn;
    private SeekBar videoSeekBar;
    private VideoView videoView;
    private RelativeLayout controlsRL, videoRL;
    private boolean isOpen = true;
    private String videoName, videoPath, subtitlePath;
    private float currentPlaybackSpeed = 1.0f; // 默认为1倍速

    private static final int REQUEST_CODE_SELECT_SUBTITLE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoName = getIntent().getStringExtra("videoName");
        videoPath = getIntent().getStringExtra("videoPath");

        videoNameTV = findViewById(R.id.idTVVideoTitle);
        videoTimeTV = findViewById(R.id.idTVTime);
        backIB = findViewById(R.id.idIBBack);
        playPauseIB = findViewById(R.id.idIBPlay);
        forwardIB = findViewById(R.id.idIBForward);
        videoSeekBar = findViewById(R.id.idSeekBarProgress);
        videoView = findViewById(R.id.idVideoView);
        controlsRL = findViewById(R.id.idRLControls);
        videoRL = findViewById(R.id.idRLVideo);
        volumeDownIB = findViewById(R.id.idIBVolumeDown);
        volumeUpIB = findViewById(R.id.idIBVolumeUp);
        selectSubtitleBtn = findViewById(R.id.idBtnSelectSubtitle);
        subtitleTV = findViewById(R.id.idTVSubtitle);

        videoView.setVideoURI(Uri.parse(videoPath));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoSeekBar.setMax(videoView.getDuration());
                videoView.start();
            }
        });

        videoNameTV.setText(videoName);

        backIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.seekTo(videoView.getCurrentPosition() - 10000);
            }
        });

        forwardIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.seekTo(videoView.getCurrentPosition() + 10000);
            }
        });

        playPauseIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    playPauseIB.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                } else {
                    videoView.start();
                    playPauseIB.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                }
            }
        });

        volumeDownIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }
        });

        volumeUpIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            }
        });

        videoRL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    hideControls();
                    isOpen = false;
                } else {
                    showControls();
                    isOpen = true;
                }
            }
        });

        selectSubtitleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        setHandler();
        initializeSeekBar();
    }

    private void setHandler() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (videoView.getDuration() > 0) {
                    int curPos = videoView.getCurrentPosition();
                    videoSeekBar.setProgress(curPos);
                    videoTimeTV.setText("" + convertTime(videoView.getDuration() - curPos));
                    displaySubtitle(curPos);
                }
                handler.postDelayed(this, 500);
            }
        };
        handler.postDelayed(runnable, 500);
    }

    private String convertTime(int ms) {
        String time;
        int x, seconds, minutes, hours;
        x = ms / 1000;
        seconds = x % 60;
        x /= 60;
        minutes = x % 60;
        x /= 60;
        hours = x % 24;
        if (hours != 0) {
            time = String.format("%02d", hours) + " : " + String.format("%02d", minutes) + " : " + String.format("%020", seconds);
        } else {
            time = String.format("%02d", minutes) + " : " + String.format("%02d", seconds);
        }
        return time;
    }

    private void initializeSeekBar() {
        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (videoSeekBar.getId() == R.id.idSeekBarProgress) {
                    if (fromUser) {
                        videoView.seekTo(progress);
                        videoView.start();
                        int curPos = videoView.getCurrentPosition();
                        videoTimeTV.setText("" + convertTime(videoView.getDuration() - curPos));
                        displaySubtitle(curPos);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void showControls() {
        TextView idTVSubtitle = findViewById(R.id.idTVSubtitle);
        idTVSubtitle.setVisibility(View.VISIBLE);
        controlsRL.setVisibility(View.VISIBLE);

        final Window window = this.getWindow();
        if (window == null) {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        View decorView = window.getDecorView();
        if (decorView != null) {
            int uiOption = decorView.getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= 14) {
                uiOption &= ~View.SYSTEM_UI_FLAG_LOW_PROFILE;
            }

            if (Build.VERSION.SDK_INT >= 16) {
                uiOption &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }

            if (Build.VERSION.SDK_INT >= 19) {
                uiOption &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            decorView.setSystemUiVisibility(uiOption);
        }
    }

    private void hideControls() {
        controlsRL.setVisibility(View.GONE);

        final Window window = this.getWindow();
        if (window == null) {
            return;
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        View decorView = window.getDecorView();
        if (decorView != null) {
            int uiOption = decorView.getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= 14) {
                uiOption |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
            }

            if (Build.VERSION.SDK_INT >= 16) {
                uiOption |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }

            if (Build.VERSION.SDK_INT >= 19) {
                uiOption |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            decorView.setSystemUiVisibility(uiOption);
        }
        TextView idTVSubtitle = findViewById(R.id.idTVSubtitle);
        idTVSubtitle.setVisibility(View.VISIBLE);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/x-subrip"); // 设置只显示srt字幕文件
        startActivityForResult(intent, REQUEST_CODE_SELECT_SUBTITLE);
    }

    private void displaySubtitle(int curPos) {
        if (subtitlePath == null || subtitlePath.isEmpty()) {
            subtitleTV.setText("");
            return;
        }

        try {
            File file = new File(subtitlePath);
            FileInputStream fis = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            StringBuilder subtitleBuilder = new StringBuilder();
            boolean subtitleDisplayed = false;

            while ((line = reader.readLine()) != null) {
                if (line.matches("\\d+")) {
                    // 行数，跳过
                    continue;
                } else if (line.matches("\\d{2}:\\d{2}:\\d{2},\\d{3}\\s-->\\s\\d{2}:\\d{2}:\\d{2},\\d{3}")) {
                    // 时间码行
                    String[] timeCodes = line.split("\\s-->\\s");
                    long startTime = parseTimecode(timeCodes[0]);
                    long endTime = parseTimecode(timeCodes[1]);
//                    Log.d("Subtitle", "Start Time: " + startTime);
//                    Log.d("Subtitle", "End Time: " + endTime);

                    if (curPos >= startTime && curPos <= endTime) {
                        subtitleDisplayed = true;
                    } else {
                        subtitleDisplayed = false;
                    }
                } else if (line.isEmpty()) {
                    // 空行，跳过
                    continue;
                } else if (subtitleDisplayed) {
                    // 显示当前时间范围内的字幕内容
                    subtitleBuilder.append(line).append("\n");
                }
            }

            reader.close();
            subtitleTV.setText(subtitleBuilder.toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
            subtitleTV.setText("");
            Toast.makeText(this, "Failed to load subtitle", Toast.LENGTH_SHORT).show();
        }
    }

    private long parseTimecode(String timecode) {
        String[] parts = timecode.split(":|,");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);
        int milliseconds = Integer.parseInt(parts[3]);

        long totalTime = hours * 3600000 + minutes * 60000 + seconds * 1000 + milliseconds;
        return totalTime;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_SUBTITLE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                subtitlePath = uri.getPath();
                subtitleTV.setText("");
                Toast.makeText(this, "Subtitle selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
}
