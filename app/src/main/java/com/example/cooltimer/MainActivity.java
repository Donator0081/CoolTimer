package com.example.cooltimer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private Button buttonForStartAndPause;
    private SeekBar seekBarForTimer;
    private TextView textViewForTimer;
    private boolean isTimerOn;
    private CountDownTimer countDownTimer;
    private int defaultInterval;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonForStartAndPause = findViewById(R.id.buttonForStartAndPause);
        seekBarForTimer = findViewById(R.id.seekBarForTimer);
        seekBarForTimer.setMax(600);
        isTimerOn = false;
        textViewForTimer = findViewById(R.id.textViewTimer);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        setIntervalFromSharedPreferences(sharedPreferences);
        seekBarChangeListener();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void seekBarChangeListener() {
        seekBarForTimer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                long progressInMil = progress * 1000;
                timerUpdate(progressInMil);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void startAndPause(View view) {

        if (!isTimerOn) {
            buttonForStartAndPause.setText(R.string.pause);
            seekBarForTimer.setEnabled(false);
            isTimerOn = true;
            countDownTimer = new CountDownTimer(seekBarForTimer.getProgress() * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timerUpdate(millisUntilFinished);
                }

                @Override
                public void onFinish() {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (sharedPreferences.getBoolean("enable_sound", true)) {
                        String melodyName = sharedPreferences.getString("timer_melody", "bell");
                        if (melodyName.equals("bell")) {
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
                            mediaPlayer.start();
                        } else if (melodyName.equals("alarm_siren")) {
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_siren_sound);
                            mediaPlayer.start();
                        } else if (melodyName.equals("bip")) {
                            MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                            mediaPlayer.start();
                        }
                    }
                    resetTimer();
                }
            };
            countDownTimer.start();
        } else {
            resetTimer();
        }
    }

    private void resetTimer() {
        buttonForStartAndPause.setText(R.string.start);
        countDownTimer.cancel();
        seekBarForTimer.setEnabled(true);
        setIntervalFromSharedPreferences(sharedPreferences);
        isTimerOn = false;
    }

    private void timerUpdate(long millisUntilFinished) {
        int minutes = (int) millisUntilFinished / 1000 / 60;
        int seconds = (int) millisUntilFinished / 1000 - (minutes * 60);
        String minutesString;
        String secondsString;
        if (minutes < 10) {
            minutesString = "0" + minutes;
        } else {
            minutesString = String.valueOf(minutes);
        }
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = String.valueOf(seconds);
        }
        textViewForTimer.setText(minutesString + ":" + secondsString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        } else if (id == R.id.action_about) {
            Intent openAbout = new Intent(this, AboutActivity.class);
            startActivity(openAbout);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void setIntervalFromSharedPreferences(SharedPreferences sharedPreferences) {
        defaultInterval = Integer.valueOf(sharedPreferences.getString("timer_default_interval", "30"));
        long defaultIntervalInMil = defaultInterval * 1000;
        timerUpdate(defaultIntervalInMil);
        seekBarForTimer.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("timer_default_interval")) {
            setIntervalFromSharedPreferences(sharedPreferences);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
