package com.voole.screensaver;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.service.dreams.DreamService;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class ScreenServer extends DreamService {
    private static final String TAG = "ScreenServer";
    private static final int WHAT_SETBG = 1;
    private String PACKAGENAME;
    protected static final long SLEEP_TIME = 14000;
    protected static final int WHAT_CHANGE = 2;
    private int mCount;
    private static final String IMAG_PATH = "/system/media/screensaver";
    private SimpleDraweeView mImg;
    private int mPos = 0;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            Log.d(TAG, "mPos=" + mPos);
            switch (msg.what) {
                case WHAT_CHANGE:
                    mPos++;
                    if (mPos >= mCount) {
                        mPos = 0;
                    }
                    switchImg();
                    break;
                case WHAT_SETBG:
                    setBg();
                    break;
                default:
                    break;
            }

        }

    };

    private Thread mThread;
    protected boolean mFlag = true;
    private List<String> mImgPaths;
    private SimpleDraweeView mBg;
    private Drawable mDrawable;
    private int[] mImgIDs;
    private int mType = 0;
    private boolean FROMID = false;

    @Override
    public void onDreamingStarted() {
        mFlag = true;
        Log.e(TAG, "onDreamingStarted");
        Fresco.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initData();
        PACKAGENAME = getPackageName();
        mImg = (SimpleDraweeView) findViewById(R.id.screensaver);
        mBg = (SimpleDraweeView) findViewById(R.id.screensaverbg);
        mImg.setScaleType(ImageView.ScaleType.FIT_XY);
        mBg.setScaleType(ImageView.ScaleType.FIT_XY);
        mCount = mImgPaths.size();
        if (mCount <= 0) {
            mCount = mImgIDs.length;
            FROMID = true;
        }
        switchImg();
        startAnima();
    }

    @Override
    public void onDreamingStopped() {
        mFlag = false;
        super.onDreamingStopped();
    }

    private void initData() {
        mImgPaths = new ArrayList<String>();
        File file = new File(IMAG_PATH);
        File[] files = file.listFiles();
        String path = "";
        for (int i = 0; i < files.length; i++) {
            path = files[i].getAbsolutePath();
            if (!path.endsWith(".jpg") && !(path.endsWith(".bmp")))
                continue;
            mImgPaths.add(path);
        }
        if (mImgPaths.size() > 0) {
            return;
        }
        mImgIDs = new int[]{R.drawable.screen1, R.drawable.screen2};
    }

    private void startAnima() {
        mThread = new Thread() {
            @Override
            public void run() {
                while (mFlag) {
                    try {
                        Thread.sleep(SLEEP_TIME);
                        mHandler.sendEmptyMessage(WHAT_SETBG);
                        Thread.sleep(1000);
                        mHandler.sendEmptyMessage(WHAT_CHANGE);
                    } catch (InterruptedException e) {
                        Log.e(TAG, "InterruptedException:" + e.getMessage());
                    }
                }
            }
        };
        mThread.start();
    }

    private void setBg() {
        Log.d(TAG, "setBg:" + mPos);
        if (FROMID)
            setImag(mBg, mImgIDs[mPos]);
        else
            setImag(mBg, mImgPaths.get(mPos));
    }

    protected void switchImg() {
        Log.d(TAG, "switchImg");
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(mImg, "scaleX", 1, 1.1F);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(mImg, "scaleY", 1, 1.1F);
        scaleX.setDuration(6000);
        scaleY.setDuration(6000);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(mImg, "alpha", 0, 1);
        alpha.setDuration(2000);
        ObjectAnimator scaleXIN = ObjectAnimator.ofFloat(mImg, "scaleX", 1.1F,
                1);
        ObjectAnimator scaleYIN = ObjectAnimator.ofFloat(mImg, "scaleY", 1.1F,
                1);
        scaleXIN.setDuration(4000);
        scaleYIN.setDuration(4000);
        AnimatorSet animSet = new AnimatorSet();
        AnimatorSet animSet2 = new AnimatorSet();
        animSet2.playTogether(scaleXIN, scaleYIN);
        animSet.play(scaleX).with(scaleY).after(alpha).before(animSet2);
        animSet.start();
        if (FROMID)
            setImag(mImg, mImgIDs[mPos]);
        else
            setImag(mImg, mImgPaths.get(mPos));
    }

    private void setImag(SimpleDraweeView sdv, String path) {
        Uri uri = Uri.parse("file://" + path);
        sdv.setImageURI(uri);
    }

    private void setImag(SimpleDraweeView sdv, int id) {
        Log.i(TAG, "setImg from drawable PACKAGENAME=" + PACKAGENAME);
        sdv.setBackgroundResource(id);
    }
}
