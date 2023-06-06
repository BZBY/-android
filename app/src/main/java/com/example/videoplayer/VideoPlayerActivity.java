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
import android.provider.MediaStore;
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

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

public class VideoPlayerActivity extends AppCompatActivity {

    private TextView videoNameTV, videoTimeTV, subtitleTV;
    private ImageButton backIB, forwardIB, playPauseIB, volumeDownIB, volumeUpIB, selectSubtitleBtn,speedButton;
    private SeekBar videoSeekBar;
    private PlayerView playerView;
    private RelativeLayout controlsRL, videoRL;
    private boolean isOpen = true;
    private String videoName, videoPath, subtitlePath;
    private SimpleExoPlayer player;
    private PlaybackParameters playbackParameters;
    private static final int REQUEST_CODE_SELECT_SUBTITLE = 1;
    private float currentPlaybackSpeed = 1.0f;
    private List<MediaStore.Video> videoList;
    private int currentVideoIndex;

    private ImageButton speedButton075x, speedButton1x, speedButton125x, speedButton15x;
    private boolean isSpeedButtonsVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoName = getIntent().getStringExtra("videoName");
        videoPath = getIntent().getStringExtra("videoPath");
// 其他初始化和视图引用的初始化

        speedButton075x = findViewById(R.id.speedButton075x);
        speedButton1x = findViewById(R.id.speedButton1x);
        speedButton125x = findViewById(R.id.speedButton125x);
        speedButton15x = findViewById(R.id.speedButton15x);



        videoNameTV = findViewById(R.id.idTVVideoTitle);
        videoTimeTV = findViewById(R.id.idTVTime);
        backIB = findViewById(R.id.idIBBack);
        speedButton = findViewById(R.id.speedButton);
        playPauseIB = findViewById(R.id.idIBPlay);
        forwardIB = findViewById(R.id.idIBForward);
        videoSeekBar = findViewById(R.id.idSeekBarProgress);
        playerView = findViewById(R.id.playerView);
        controlsRL = findViewById(R.id.idRLControls);
        videoRL = findViewById(R.id.idRLVideo);
        volumeDownIB = findViewById(R.id.idIBVolumeDown);
        volumeUpIB = findViewById(R.id.idIBVolumeUp);
        selectSubtitleBtn = findViewById(R.id.idBtnSelectSubtitle);
        subtitleTV = findViewById(R.id.idTVSubtitle);

        initializePlayer();

        videoNameTV.setText(videoName);

        backIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.seekTo(player.getCurrentPosition() - 10000);
            }
        });
        // 点击 speedButton 显示/隐藏倍速按钮
        speedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSpeedButtons();
            }
        });

        // 设置倍速按钮的点击事件
        speedButton075x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlaybackSpeed(0.75f);
            }
        });

        speedButton1x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlaybackSpeed(1.0f);
            }
        });

        speedButton125x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlaybackSpeed(1.25f);
            }
        });

        speedButton15x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPlaybackSpeed(1.5f);
            }
        });


        forwardIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.seekTo(player.getCurrentPosition() + 10000);
            }
        });

        playPauseIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getPlayWhenReady()) {
                    player.setPlayWhenReady(false);
                    playPauseIB.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));
                } else {
                    player.setPlayWhenReady(true);
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

        controlsRL.setOnClickListener(new View.OnClickListener() {
            boolean isControlsVisible = true;

            @Override
            public void onClick(View v) {
                if (isControlsVisible) {
                    hideControls();
                } else {
                    showControls();
                }
                isControlsVisible = !isControlsVisible;
            }
        });

        playerView.setOnClickListener(new View.OnClickListener() {
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

    private void setPlaybackSpeed(float speed) {
        if (player != null) {
            currentPlaybackSpeed = speed;
            PlaybackParameters parameters = new PlaybackParameters(speed);
            player.setPlaybackParameters(parameters);
            Toast.makeText(this, "Playback speed changed: " + speed + "x", Toast.LENGTH_SHORT).show();

            // 隐藏倍速按钮
            toggleSpeedButtons();
        }
    }
    private void toggleSpeedButtons() {
        if (isSpeedButtonsVisible) {
            // 隐藏倍速按钮
            speedButton075x.setVisibility(View.GONE);
            speedButton1x.setVisibility(View.GONE);
            speedButton125x.setVisibility(View.GONE);
            speedButton15x.setVisibility(View.GONE);
        } else {
            // 显示倍速按钮
            speedButton075x.setVisibility(View.VISIBLE);
            speedButton1x.setVisibility(View.VISIBLE);
            speedButton125x.setVisibility(View.VISIBLE);
            speedButton15x.setVisibility(View.VISIBLE);
        }

        isSpeedButtonsVisible = !isSpeedButtonsVisible;
    }
    private void togglePlaybackSpeed() {
        if (player != null) {
            if (currentPlaybackSpeed == 1.0f) {
                currentPlaybackSpeed = 2.0f;
            } else {
                currentPlaybackSpeed = 1.0f;
            }
            PlaybackParameters parameters = new PlaybackParameters(currentPlaybackSpeed);
            player.setPlaybackParameters(parameters);
            Toast.makeText(this, "Playback speed changed: " + currentPlaybackSpeed + "x", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializePlayer() {

        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoPath));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    private void setHandler() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (player.getDuration() > 0) {
                    int curPos = (int) player.getCurrentPosition();
                    videoSeekBar.setProgress(curPos);
                    videoTimeTV.setText("" + convertTime((int) (player.getDuration() - curPos)));
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
                        player.seekTo(progress);
                        player.play();
                        int curPos = (int) player.getCurrentPosition();
                        videoTimeTV.setText("" + convertTime((int) (player.getDuration() - curPos)));
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
