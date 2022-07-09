package com.example.music2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {
    Button btnplay, btnnext, btnprev, btnff, btnfr;
    TextView txtsname, txtsstart, txtsstop;
    SeekBar seekmusic;
    ImageView imageView;
    BarVisualizer visualizer;
    Button voiceEnabledBtn;
    RelativeLayout relativeLayout;
    String mode = "ON";
    private LinearLayout parentLinearLayout;
    private Button buttonpress;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private String keeper = "";
    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateseekbar;



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        parentLinearLayout= findViewById(R.id.parentLinearLayout);
        checkVoiceCommandPermission();
        btnprev = findViewById(R.id.btnprev);
        btnnext = findViewById(R.id.btnnext);
        btnplay=findViewById(R.id.playbtn);
        btnff=findViewById(R.id.btnff);
        btnfr=findViewById(R.id.btnfr);
        txtsname=findViewById(R.id.txtsn);
        txtsstart=findViewById(R.id.txtsstart);
        txtsstop=findViewById(R.id.txtsstop);
        seekmusic=findViewById(R.id.seekbar);
        imageView=findViewById(R.id.imageview);
        voiceEnabledBtn=findViewById(R.id.voice_enabled_btn);
        relativeLayout = findViewById(R.id.lower);
        

        speechRecognizer=SpeechRecognizer.createSpeechRecognizer(PlayerActivity.this);
        speechRecognizerIntent= new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());


        validateReceiveValuesAndStartPlaying();


        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> matchesFound = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matchesFound != null) {
                    if (mode.equals("ON"))
                    {
                        keeper = matchesFound.get(0);
                        if (keeper.equals("pause the song"))
                        {
                            playPauseSong();
                            Toast.makeText(PlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();

                        }
                        else if (keeper.equals("play the song"))
                        {
                            playPauseSong();
                            Toast.makeText(PlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();

                        }
                        else if (keeper.equals("play next song"))
                        {
                            playNextSong();
                            Toast.makeText(PlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();

                        }
                        else if (keeper.equals("play previous song"))
                        {
                            playPreviousSong();
                            Toast.makeText(PlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();

                        }
                        else if (keeper.equals("forward the song"))
                        {
                            playForward();
                            Toast.makeText(PlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();

                        }
                        else if (keeper.equals("back"))
                        {
                            playRewind();
                            Toast.makeText(PlayerActivity.this, "Command = " + keeper, Toast.LENGTH_LONG).show();

                        }

                    }
                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });


        parentLinearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        speechRecognizer.startListening(speechRecognizerIntent);
                        keeper="";
                        break;
                    case MotionEvent.ACTION_UP:
                        speechRecognizer.stopListening();
                        break;
                }
                return  false;
            }
        });



        voiceEnabledBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode.equals("ON"))
                {
                    mode = "OFF";
                    voiceEnabledBtn.setText("Voice Enabled Mode - OFF");


                }
                else{
                    mode = "ON";
                    voiceEnabledBtn.setText("Voice Enabled Mode - ON");

                }
            }
        });
        updateseekbar= new Thread()
        {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentposition =0;

                while (currentposition<totalDuration)
                {
                    try{
                        sleep(500);
                        currentposition=mediaPlayer.getCurrentPosition();
                        seekmusic.setProgress(currentposition);
                    }
                    catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekmusic.setMax(mediaPlayer.getDuration());
        updateseekbar.start();
        seekmusic.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        seekmusic.getThumb().setColorFilter(getResources().getColor(R.color.colorPrimary),PorterDuff.Mode.SRC_IN);

        seekmusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String  endTime = createTime(mediaPlayer.getDuration());
        txtsstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtsstart.setText(currentTime);
                handler.postDelayed(this,delay);
            }
        },delay);

        btnplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               playPauseSong();
            }
        });
        //next listener
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnnext.performClick();
            }
        });
        btnnext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNextSong();
            }
        });
        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playPreviousSong();
            }
        });
        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playForward();
            }
         });

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playRewind();
            }
        });


    }
    private void validateReceiveValuesAndStartPlaying()
    {
        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        mySongs=(ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position= bundle.getInt("pos",0);
        txtsname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname= mySongs.get(position).getName();
        txtsname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

    }

    private void checkVoiceCommandPermission()
    {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            if (!(ContextCompat.checkSelfPermission(PlayerActivity.this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_GRANTED))
            {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+getPackageName()));
                startActivity(intent);
                finish();
            }
        }
    }


    private  void playPauseSong(){
        {
            if (mediaPlayer.isPlaying())
            {
                btnplay.setBackgroundResource(R.drawable.ic_play);
                mediaPlayer.pause();
            }
            else {
                btnplay.setBackgroundResource(R.drawable.ic_pause);
                mediaPlayer.start();
            }
        }
    }
    private void playNextSong()
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        position = ((position+1)%mySongs.size());
        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaPlayer=mediaPlayer.create(getApplicationContext(),u);
        sname=mySongs.get(position).getName();
        txtsname.setText(sname);
        mediaPlayer.start();
        btnplay.setBackgroundResource(R.drawable.ic_pause);
        startAnimation(imageView);

    }
    private void playPreviousSong()
    {
        mediaPlayer.stop();
        mediaPlayer.release();
        position = ((position-1)<0)?(mySongs.size()-1):(position-1);
        Uri u = Uri.parse(mySongs.get(position).toString());
        mediaPlayer = MediaPlayer.create(getApplicationContext(),u);
        sname=mySongs.get(position).getName();
        txtsname.setText(sname);
        mediaPlayer.start();
        btnplay.setBackgroundResource(R.drawable.ic_pause);
        startAnimation(imageView);
    }
    private void playForward(){
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
        }
    }

    private void playRewind(){
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
        }
    }

    public void startAnimation(View view)
    {
        ObjectAnimator animator =  ObjectAnimator.ofFloat(imageView, "rotation", 0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }
    public String createTime(int duration)
    {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time += min +":";

        if(sec<10)
        {
            time+="0";
        }
        time+=sec;
        return time;
    }

}
