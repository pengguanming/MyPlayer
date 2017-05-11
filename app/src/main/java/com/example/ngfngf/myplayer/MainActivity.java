package com.example.ngfngf.myplayer;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ngfngf.myplayer.util.DimensionTransition;
import com.example.ngfngf.myplayer.view.MyVideoView;

/**
 * 1、视屏黑块
 * 2、视频转码工具
 * 3、百度媒体云，乐视云播放器
 * 4、视频分享
 * 5、视屏下载
 */

public class MainActivity extends AppCompatActivity {
    private MyVideoView mVideoView;
    private LinearLayout mControllerBarLayout;
    private ImageView mIvPlayControl, mIvVolumeControl;
    private ImageView mIvFull;
    private TextView mTvCurrentTime;
    private TextView mTvTimeTotal;
    private TextView mTvPercent;
    private SeekBar mVolumeSeekBar;
    private SeekBar mPlayProgressSeekBar;
    private static final int UPDATE_UI = 1;
    private int screen_width, screen_height;
    private RelativeLayout mVideoLayout;
    private LinearLayout mProgressLayout;
    private ImageView operate_bg;
    private AudioManager mAudioManager;
    private boolean isFullScreen;//是否全屏
    private boolean isAdjust;//触屏是否合法
    private int threshold = 54;//误触临界值
    private float lastX = 0, lastY = 0;//触点位置
    private float screenBrightness;//屏幕亮度

    private static final String TAG = "JJY";
    private Handler UIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_UI) {
                //获取视频当前的播放进度
                int currentTime = mVideoView.getCurrentPosition();
                //获取视频的总时长
                int totalDuration = mVideoView.getDuration();
                //格式化视频播放时间
                updateTextViewWithTimeFormat(mTvCurrentTime, currentTime);
                updateTextViewWithTimeFormat(mTvTimeTotal, totalDuration);
                //更新播放进度
                mPlayProgressSeekBar.setMax(totalDuration);
                mPlayProgressSeekBar.setProgress(currentTime);
                //自己刷新
                UIHandler.sendEmptyMessageDelayed(UPDATE_UI, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取音频服务
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test.mp4";
        initView();
        initEvent();
        //本地视频播放
        mVideoView.setVideoPath(path);
        mVideoView.start();
        UIHandler.sendEmptyMessage(UPDATE_UI);



      /*  //网络视频播放
        //mVideoView.setVideoURI(Uri.parse("http://192.168.43.7:8080/test.mp4"));
        //使用MediaController控制视屏播放
        MediaController mediaController = new MediaController(this);
        //VideoView与MediaController相互关联
        mVideoView.setMediaController(mediaController);
        mediaController.setMediaPlayer(mVideoView);*/
    }

    //设置播放器的宽高
    private void setVideoViewScale(int width, int height) {
        ViewGroup.LayoutParams layoutParams = mVideoView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        mVideoView.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams layoutParams1 = mVideoLayout.getLayoutParams();
        layoutParams1.width = width;
        layoutParams1.height = height;
        mVideoLayout.setLayoutParams(layoutParams1);
    }

    //格式化时间
    private void updateTextViewWithTimeFormat(TextView textView, int millisecond) {
        int second = millisecond / 1000;
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;

        String str = null;
        if (hh != 0) {
            str = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            str = String.format("%02d:%02d", mm, ss);
        }
        textView.setText(str);
    }

    private void initEvent() {
        //控制暂停和播放
        mIvPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoView.isPlaying()) {
                    mIvPlayControl.setImageResource(R.drawable.play_style);
                    //暂停播放
                    mVideoView.pause();
                    UIHandler.removeMessages(UPDATE_UI);
                } else {
                    mIvPlayControl.setImageResource(R.drawable.pause_style);
                    //开始播放
                    mVideoView.start();
                    UIHandler.sendEmptyMessage(UPDATE_UI);
                }
            }
        });

        mPlayProgressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //实时更新当前时间
                updateTextViewWithTimeFormat(mTvCurrentTime, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //滑动时移除进度刷新
                UIHandler.removeMessages(UPDATE_UI);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                //将进度跳转到滑动的进度
                mVideoView.seekTo(progress);
                UIHandler.sendEmptyMessage(UPDATE_UI);
            }
        });
        //控制音量大小
        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //横竖屏切换
        mIvFull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullScreen) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    mIvFull.setImageResource(R.mipmap.full);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    mIvFull.setImageResource(R.mipmap.full_exit);
                }
            }
        });
        //音量，亮度控制
        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        lastX = x;
                        lastY = y;
                        Log.d(TAG, "X:: " + x + ";Y" + y);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        float detlaX = x - lastX;
                        float detlaY = y - lastY;
                        float absDetalX = Math.abs(detlaX);
                        float absDetalY = Math.abs(detlaY);
                        Log.d(TAG, "absDetalX: " + absDetalX + ";absDetalY:" + absDetalY + ";threshold:" + threshold);
                        //判断手势是否合法
                        if (absDetalX > threshold && absDetalY > threshold) {
                            if (absDetalY > absDetalX) {
                                isAdjust = true;
                            } else {
                                isAdjust = false;
                            }
                        } else if (absDetalX > threshold && absDetalY < threshold) {
                            isAdjust = false;
                        } else if (absDetalX < threshold && absDetalY > threshold) {
                            isAdjust = true;
                        }
                        Log.d(TAG, "手势是否合法: " + isAdjust);
                        //判断手势合法，区分手势调节的是亮度还是音量
                        if (isAdjust) {
                            if (x < screen_width / 2) {//调节亮度
                                if (detlaY > 0) {
                                    Log.d(TAG, "降低亮度: " + detlaY);
                                } else {
                                    Log.d(TAG, "提高亮度: " + detlaX);
                                }
                                //注意此处为负数是为因为安卓Y轴朝下是负，chageBrightness，故此处解决
                                chageBrightness(-detlaY);
                            } else {//调节音量
                                if (detlaY > 0) {
                                    Log.d(TAG, "降低音量: " + detlaY);
                                } else {
                                    Log.d(TAG, "提高音量: " + detlaX);
                                }
                                //注意此处为负数是为因为安卓Y轴朝下是负，在changeVolume运算中并没有处理，故此处解决
                                changeVolume(-detlaY);
                            }
                        }
                        //更新坐标
                        lastX = x;
                        lastY = y;
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        mProgressLayout.setVisibility(View.GONE);
                        break;
                    }
                }
                return true;
            }
        });
    }

    //调节音量
    private void changeVolume(float detalY) {
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //换算Y轴偏移量与音量的百分比增强3倍,即增加的音量乘以3
        int index = (int) (detalY / screen_height * maxVolume * 3);
        //防止音量调节小于0
        int volume = Math.max(currentVolume + index, 0);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        //音量条同步
        mVolumeSeekBar.setProgress(volume);
        /*ViewGroup.LayoutParams layoutParams = operate_percent.getLayoutParams();
        //设置进度条的长度=94*当前音量的百分比
        layoutParams.width = (int) (DimensionTransition.dip2px(getApplicationContext(), 94) * (float) volume * maxVolume);
        operate_percent.setLayoutParams(layoutParams);*/
        //设置视屏中心的调节提示图标
        if (mProgressLayout.getVisibility() == View.GONE) {
            mProgressLayout.setVisibility(View.VISIBLE);
        }
        operate_bg.setImageResource(R.mipmap.voice_up);
        int p = mVolumeSeekBar.getProgress();
        mTvPercent.setText((p * 20) / 3 + "%");
    }

    //调节亮度
    private void chageBrightness(float detalY) {
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        screenBrightness = attributes.screenBrightness;
        float index = detalY / screen_height / 3;//0~1
        screenBrightness += index;
        if (screenBrightness > 1.0f) {
            screenBrightness = 1.0f;
        } else if (screenBrightness < 0.01f) {
            screenBrightness = 0.01f;
        }
        attributes.screenBrightness = screenBrightness;
        getWindow().setAttributes(attributes);

        if (mProgressLayout.getVisibility() == View.GONE) {
            mProgressLayout.setVisibility(View.VISIBLE);
        }
        operate_bg.setImageResource(R.mipmap.brightness);
        int p = (int) (screenBrightness * 100);
        mTvPercent.setText(p + "%");
    }

    private void initView() {
        mVideoLayout = (RelativeLayout) findViewById(R.id.video_layout);
        mVideoView = (MyVideoView) findViewById(R.id.my_videoview);
        mControllerBarLayout = (LinearLayout) findViewById(R.id.controllerBar_layout);
        mIvPlayControl = (ImageView) findViewById(R.id.pause_img);
        mIvVolumeControl = (ImageView) findViewById(R.id.volum_img);
        mIvFull = (ImageView) findViewById(R.id.srceen_img);
        mTvCurrentTime = (TextView) findViewById(R.id.time_current_tv);
        mTvTimeTotal = (TextView) findViewById(R.id.time_tatol_tv);
        mVolumeSeekBar = (SeekBar) findViewById(R.id.volum_seekBsr);
        mPlayProgressSeekBar = (SeekBar) findViewById(R.id.paly_seekBar);
        operate_bg = (ImageView) findViewById(R.id.operation_bg);
        mTvPercent = (TextView) findViewById(R.id.tv_percent);
        mProgressLayout = (LinearLayout) findViewById(R.id.progress_layout);
        //获取屏幕的宽高
        screen_width = getResources().getDisplayMetrics().widthPixels;
        screen_height = getResources().getDisplayMetrics().heightPixels;
        //当前设备的最大音量
        int streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前设备的音量
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVolumeSeekBar.setMax(streamMaxVolume);
        mVolumeSeekBar.setProgress(streamVolume);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        UIHandler.removeMessages(UPDATE_UI);
    }

    //屏幕方向改变
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //横屏
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            //显示音量
            mVolumeSeekBar.setVisibility(View.VISIBLE);
            mIvVolumeControl.setVisibility(View.VISIBLE);
            isFullScreen = true;
            //移除半屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            //设置全屏状态
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {//竖屏
            setVideoViewScale(ViewGroup.LayoutParams.MATCH_PARENT, DimensionTransition.dip2px(getApplicationContext(), 240));
            //影藏音量
            mVolumeSeekBar.setVisibility(View.GONE);
            mIvVolumeControl.setVisibility(View.GONE);
            isFullScreen = false;
            //移除全屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //设置半屏状态
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }
}
