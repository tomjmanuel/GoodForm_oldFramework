package com.example.goodform;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ToggleButton;
import android.widget.VideoView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

public class MainActivity extends Activity {
    // minimum video view width
    static final int MIN_WIDTH = 100;
    //  videocontainer's LayoutParams
    private ConstraintLayout.LayoutParams mVidContainer;
    // video container2
    private ConstraintLayout vidCont2;
    // Custom Video View
    private VodView mVodView;
    // media player
    private MediaPlayer mediaplayer;

    // detector to pinch zoom in/out
    private ScaleGestureDetector mScaleGestureDetector;
    // detector to single tab
    private GestureDetector mGestureDetector;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // setup pointers to things we want to modify interactively
        mVidContainer = (ConstraintLayout.LayoutParams) ((View) findViewById(R.id.VidFrameContainer)).getLayoutParams();
        mVodView = (VodView) findViewById(R.id.vodView1);
        vidCont2 = (ConstraintLayout) findViewById(R.id.VidContainer2);

        // let the video container (outer) know that it is scrollable
        int foo = 1;
        int bar = -1;
        vidCont2.canScrollHorizontally(foo);
        vidCont2.canScrollHorizontally(bar);
        vidCont2.canScrollVertically(foo);
        vidCont2.canScrollVertically(bar);

        // Video Uri
        //Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.allshare_video);
        //mVodView.setVideoURI(uri);

        // set up gesture listeners
        mScaleGestureDetector = new ScaleGestureDetector(this, new MyScaleGestureListener());
        mGestureDetector = new GestureDetector(this, new MySimpleOnGestureListener());
        mVodView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                mScaleGestureDetector.onTouchEvent(event);
                return true;
            }
        });

        // set up media player to check state of videoview
        mVodView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    @Override
                    public void onSeekComplete(MediaPlayer mp) {
                        //TODO: Your code here

                    }
                });

            }
        });
    }

    public void getVideo(View view) {
        //Intent pickVideo = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent pickVideo = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        //start activity activates the intent and returns calling an onActivityResult
        // the Intent is passed along with its data into that call
        startActivityForResult(pickVideo, 1);//one can be replaced with any action code
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedVideo = data.getData();
        mVodView.setVideoURI(selectedVideo);
    }

    @Override
    protected void onResume() {
        mVodView.start();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mVodView.pause();
        super.onPause();
    }

    private class MySimpleOnGestureListener extends SimpleOnGestureListener {

        private int axisVal;
        private int axisVal2;
        private int axisVal3;
        private int axisVal4;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mVodView == null)
                return false;
            if (mVodView.isPlaying())
                mVodView.pause();
            else
                mVodView.start();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float velX, float velY){

            // handler for when user scrolls along the video surface
            // if crop mode is on we want this to pan video
            // if crop mode is off we want this to change the frame of the video
            ToggleButton cropbutton;
            boolean cropmode;
            cropbutton = (ToggleButton) findViewById(R.id.toggleButton);
            cropmode = cropbutton.isChecked();

            int scrollx; //holds current x scroll position
            int scrolly; // holds current y scroll position
            int frame; // holds the current frame
            int dur; // hlds duration of movie

            if (cropmode) {

                if (mVodView == null)
                    return false;
                else
                    axisVal = (int) velX;
                axisVal2 = (int) velY;
                //axisVal = (int) e1.getAxisValue(axis1)/10; //captures up down scroll amount
                //axisVal2 = (int) e1.getAxisValue(axis2)/10; //captures left right scroll amount
                //axisVal3 = (int) e2.getAxisValue(axis1)/10; //captures up down scroll amount
                //axisVal4 = (int) e2.getAxisValue(axis2)/10; //captures left right scroll amount
                // have to cast to integer if want to log these

                //Log.d("axisVal","axisVal1: " + axisVal + " axisVal2: " + axisVal2);

                scrollx = vidCont2.getScrollX();
                scrolly = vidCont2.getScrollY();
                //Log.d("scrollVal","scrollX: " + scrollx+ " scrollY: " + scrolly);

                // this is where we need to modify the VidContainer to change position
                // we want it to move around inside the constraintlayout2

                vidCont2.scrollTo(axisVal + scrollx, axisVal2 + scrolly);
                return true;
            } else { // scrub mode
                //get horizontal scroll amount
                axisVal = (int) velX;

                // get current frame of Video
                //frame = (int) mediaplayer.getCurrentPosition();
                frame = (int) mVodView.getCurrentPosition();
                dur = mVodView.getDuration();
                //mVodView.resume();
                //mVodView.pause();
                Log.d("current frame: ","frame: " + frame + " dur: " + dur + " velX: " + velX);

                if (axisVal>0) {
                    //mediaplayer.seekTo(frame + 1);
                    mVodView.seekTo(frame + 1);
                } else {
                    //mediaplayer.seekTo(frame - 1);
                    mVodView.seekTo(frame - 1);
                }
                //mVodView.resume();
                //mVodView.pause();

                return true;
            }


        }


    }

    private class MyScaleGestureListener implements OnScaleGestureListener {
        private int mW, mH;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // scale our video view
            // handler for when user pinches the video surface
            // if crop mode is on we want this to zoom video
            // if crop mode is off we want this to do nothing
            ToggleButton cropbutton;
            boolean cropmode;
            cropbutton = (ToggleButton) findViewById(R.id.toggleButton);
            cropmode = cropbutton.isChecked();

            if (cropmode) {
                mW *= detector.getScaleFactor();
                mH *= detector.getScaleFactor();
                if (mW < MIN_WIDTH) { // limits width
                    mW = mVodView.getWidth();
                    mH = mVodView.getHeight();
                }
                //Log.d("onScale", "scale=" + detector.getScaleFactor() + ", w=" + mW + ", h=" + mH);
                mVodView.setFixedVideoSize(mW, mH); // important
                // why is this changing the root width
                // commenting these out doesn't break app but stops the zooming effect
                // so this zoom is really changing the root views width and height
                // width and height variables are changing, but the setFixedVideoSize isn't doing its job
                // my guess is that the mVodView constraints are locked to parent and need to be flexible
                mVidContainer.width = mW;
                mVidContainer.height = mH;
                return true;
            } else { // do nothing unless in crop mode
                return true;
            }
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mW = mVodView.getWidth();
            mH = mVodView.getHeight();
            Log.d("onScaleBegin", "scale=" + detector.getScaleFactor() + ", w=" + mW + ", h=" + mH);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            Log.d("onScaleEnd", "scale=" + detector.getScaleFactor() + ", w=" + mW + ", h=" + mH);
        }

    }
}