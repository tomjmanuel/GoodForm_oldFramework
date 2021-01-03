package com.example.goodform;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.ToggleButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer.Provider;

public class MainActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    /// related to users local video
    // minimum video view width
    static final int MIN_WIDTH = 100;
    //  videocontainer's LayoutParams
    private ConstraintLayout.LayoutParams mVidContainer;
    // video container2
    private ConstraintLayout vidCont2;
    // Custom Video View
    private VodView mVodView;
    // mediacontroller
    private MediaController controller; //videoview
    // detector to pinch zoom in/out
    private ScaleGestureDetector mScaleGestureDetector;
    // detector to single tab
    private GestureDetector mGestureDetector;

    // related to youtube video
    private static final int RECOVERY_REQUEST = 1;
    private YouTubePlayerView youTubeView;
    private Button playButton;
    private Button pauseButton;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // this is like the main() method for python projects, everything starts here
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // setup pointers to things in the layout we want to modify interactively
        // video container (gets resized during zooms)
        mVidContainer = (ConstraintLayout.LayoutParams) ((View) findViewById(R.id.VidFrameContainer)).getLayoutParams();
        // video player (this is a custom class which extends the videoview class)
        // It had to be custom to allow zoom and pan
        mVodView = (VodView) findViewById(R.id.vodView1);
        // container of video container (fixed size to give cropping effect)
        vidCont2 = (ConstraintLayout) findViewById(R.id.VidContainer2);
        // play and pause buttons
        playButton = (Button) findViewById(R.id.play_button);
        pauseButton = (Button) findViewById(R.id.pause_button);

        // setup media controller
        controller = new MediaController(this);
        controller.setMediaPlayer(mVodView);
        // anchor it to vidcontainer2 (size doesn't change with zoom) for display
        controller.setAnchorView(vidCont2);


        // let the video container (outer) know that it is scrollable
        int foo = 1;
        int bar = -1;
        vidCont2.canScrollHorizontally(foo);
        vidCont2.canScrollHorizontally(bar);
        vidCont2.canScrollVertically(foo);
        vidCont2.canScrollVertically(bar);


        // set up gesture listeners these handle touching the video
        // One is for scaling (pinch) one for panning (drag)
        mScaleGestureDetector = new ScaleGestureDetector(this, new MyScaleGestureListener());
        mGestureDetector = new GestureDetector(this, new MySimpleOnGestureListener());
        mVodView.setOnTouchListener(new OnTouchListener() {

            @Override // custom onTouch function to appropriately fall methods on different gestures
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                mScaleGestureDetector.onTouchEvent(event);
                //controller.show();
                return true;
            }
        });

        ////////////////////
        // youtube related
        youTubeView = (YouTubePlayerView) findViewById(R.id.youtube_view);
        youTubeView.initialize(DeveloperKey.DEVELOPER_KEY, this);
        Log.d("uh","here");

    }

    //////youtube related classes
    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean wasRestored) {
        if (!wasRestored) {
            player.cueVideo("fhWaJi1Hsfo"); // Plays https://www.youtube.com/watch?v=fhWaJi1Hsfo
        }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_REQUEST).show();
        } else {
            String error = String.format(getString(R.string.player_error), errorReason.toString());
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_REQUEST) {
            // Retry initialization if user performed a recovery action
            getYouTubePlayerProvider().initialize(DeveloperKey.DEVELOPER_KEY, this);
        }
    }*/

    protected Provider getYouTubePlayerProvider() {
        return youTubeView;
    }

    public void getVideo(View view) {
        //This method starts the intent to import a video
        // it is called when the user hits the get video button

        // Intents are used to start separate android activities and use other apps functionality
        Intent pickVideo = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        //start activity activates the intent and returns calling an onActivityResult
        // the Intent is passed along with its data into that call
        startActivityForResult(pickVideo, 1);//one can be replaced with any action code
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // when the user selects a video, the intent calls this function
        // and sort of passes itself into the function with the video data
        super.onActivityResult(requestCode, resultCode, data);

        // URIs are used to point to files
        Uri selectedVideo = data.getData();
        // point the URI at the videoview structure
        mVodView.setVideoURI(selectedVideo);
    }

    @Override
    protected void onResume() {
        // resume video
        mVodView.start();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // pause video
        mVodView.pause();
        super.onPause();
    }

    private class MySimpleOnGestureListener extends SimpleOnGestureListener {
        // Listen for single taps, and scrolls
        // handle their occurences with custom instructions

        private int axisVal;
        private int axisVal2;

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            // crop button turns on and off crop mode
            ToggleButton cropbutton;
            boolean cropmode;
            cropbutton = (ToggleButton) findViewById(R.id.toggleButton);
            cropmode = cropbutton.isChecked();


            if (cropmode) {
                // if cropmode is on, hide the media controller
                controller.hide();
            } else {
                // if cropmode is off, show the media controller when user taps video
                controller.show();
            }

            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float velX, float velY) {

            // handler for when user scrolls along the video surface
            // if crop mode is on we want this to pan video
            ToggleButton cropbutton;
            boolean cropmode;
            cropbutton = (ToggleButton) findViewById(R.id.toggleButton);
            cropmode = cropbutton.isChecked();

            int scrollx; //holds current x scroll position
            int scrolly; // holds current y scroll position
            int frame; // holds the current frame
            int dur; // hlds duration of movie

            if (cropmode) {

                if (mVodView == null) {
                    return false;
                } else {
                    // scrolling code
                    // velX and velY capture users scroll motion
                    axisVal = (int) velX;
                    axisVal2 = (int) velY;
                    //Log.d("axisVal","axisVal1: " + axisVal + " axisVal2: " + axisVal2);
                    // get current scrolled position
                    scrollx = vidCont2.getScrollX();
                    scrolly = vidCont2.getScrollY();
                    //Log.d("scrollVal","scrollX: " + scrollx+ " scrollY: " + scrolly);
                    // this is where we need to modify the VidContainer to change position
                    // we want it to move around inside the constraintlayout2
                    // amount to scroll is current position + user input
                    vidCont2.scrollTo(axisVal + scrollx, axisVal2 + scrolly);
                    return true;
                }
            } else { // do nothing if not in crop mode
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

