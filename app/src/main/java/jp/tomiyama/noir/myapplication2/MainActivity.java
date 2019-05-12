package jp.tomiyama.noir.myapplication2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private SoundSwitch mSoundSwitch;
    private LightView mLightView;

    // メインスレッドでのインスタンス生成
    private Handler mHandler = new Handler();

    // Timerの用意
    Timer mTimer = null;
    double mTimerSec = 0.0;

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //LightViewの呼び出し
        mLightView = new LightView(this);
        setContentView(mLightView);//画面に表示するviewを指定

        // 暗くならないようにする
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // パーミッションの許可状態を確認する
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                Log.d("ANDROID", "許可されている");

            } else {
                Log.d("ANDROID", "許可されていない");
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("ANDROID", "許可された");

                    //LightViewの呼び出し
                    mLightView = new LightView(this);
                    setContentView(mLightView);//画面に表示するviewを指定

                    // 暗くならないようにする
                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                } else {
                    Log.d("ANDROID", "許可されなかった");
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d("status","onResume()");

        mSoundSwitch = new SoundSwitch();

        // リスナーを登録して音を感知できるように
        mSoundSwitch.setOnVolumeReachedListener(
                new SoundSwitch.OnReachedVolumeListener() {

                    // 音を感知したら呼び出される
                    public void onReachedVolume(short volume,boolean isTimer) {

                        // 引数にbool値を導入
                        // bool変数がtrueなら普通に挙動
                        // bool変数がfalseならtimer停止

                        if(isTimer){
                            if(mTimer == null){
                                // 1秒ごとでの実行
                                mTimer = new Timer();
                                mTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        mTimerSec += 1.0;
                                        Log.d("mTimerSec", String.valueOf(mTimerSec));
//                                    mHandler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                        mTimerText.setText(String.format("%.1f", mTimerSec));
//                                            Log.d("mTimerSec", String.valueOf(mTimerSec));
//                                        }
//                                    });
                                    }
                                }, 0, 1000);
                            }

                            mHandler.post(new Runnable() {
                                public void run() {
                                    mLightView.randomDraw();
                                }
                            });

                        }else{
                            if(mTimer != null){
                                Log.d("Timerキャンセル","come here?");
                                mTimer.cancel();
                                mTimer = null;
                            }
                        }
                    }
                });

        // 別スレッドとしてSoundSwitchを開始（録音を開始）
        new Thread(mSoundSwitch).start();
    }

    //Activityの状態がonPauseの時の処理
    @Override
    public void onPause() {
        //superクラスのonPauseを呼び出す
        super.onPause();

        // 録音を停止
        mSoundSwitch.stop();
    }
}
