package jp.tomiyama.noir.myapplication2;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class SoundSwitch implements Runnable {

    long startTime = 0;

    // ボーダー音量を検知した時のためのリスナー
    public interface OnReachedVolumeListener {
        // ボーダー音量を超える音量を検知した時に
        // 呼び出されるメソッド
        void onReachedVolume(short volume,boolean isTimer);
    }

    // ボリューム感知リスナー
    private OnReachedVolumeListener mListener;
    // 録音中フラグ
    private boolean isRecoding = true;
    // サンプリングレート
    private static final int SAMPLE_RATE = 8000; //80.0KHz
    // ボーダー音量
    private short mBorderVolume = 5000;

    // ボーダー音量をセット
    public void setBorderVolume(short volume) {
        mBorderVolume = volume;
    }
    // ボーダー音量を取得
    public short getBorderVolume() {
        return mBorderVolume;
    }
    // 録音を停止
    public void stop() {
        isRecoding = false;
    }

    // OnReachedVolumeListenerをセット
    public void setOnVolumeReachedListener(OnReachedVolumeListener listener) {
        mListener = listener;
    }

    // スレッド開始（録音を開始）
    @Override
    public void run() {

        // スレッドの優先度を変更
        // オーディオ処理を最優先で行うための設定値
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

        //  bufferSizeの準備
        int bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO ,
                AudioFormat.ENCODING_PCM_16BIT);

        // AudioRecordのインスタンス生成
        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC, // 音声のソース
                SAMPLE_RATE, // サンプリングレート
                AudioFormat.CHANNEL_IN_MONO, // チャネル設定. MONO and STEREO が全デバイスサポート保障
                AudioFormat.ENCODING_PCM_16BIT, // PCM16が全デバイスサポート保障
                bufferSize); // バッファ

        short[] buffer = new short[bufferSize];
        audioRecord.startRecording();

        while(isRecoding) {
            audioRecord.read(buffer, 0, bufferSize);
            short max = 0;
            for (int i = 0; i < bufferSize; i++) {

                // 最大音量を計算
                max = (short)Math.max(max, buffer[i]);

                // 現在時刻の取得
                long currentTime = System.currentTimeMillis();
//                Log.d("currentTime", String.valueOf(currentTime));

                // 最大音量がボーダーを超えていたら
                if (max > mBorderVolume) {

                    // 閾値を超えた時刻を更新
                    startTime = currentTime;
//                    Log.d("startTime", String.valueOf(startTime));

                    if (mListener != null) {
                        // リスナーを実行
                        // 第2引数もいれる
                        mListener.onReachedVolume(max,true);

                        break;
                    }
                } else {

                    // 処理後の時刻を取得
                    long endTime = System.currentTimeMillis();

//                        System.out.println("終了時刻：" + endTime + " ms");
                    long diffTime = endTime - startTime;

                    // 2秒以上たったら
                    if(diffTime != endTime && diffTime > 2000){
                        System.out.println("経過時間：" + diffTime + " ms");
                        // 初期化処理
                        // bool値の変更(Timerの停止)
                        mListener.onReachedVolume(max, false);
                        startTime = 0; //startTimeの初期化
                    }

                }
            }
        }

        audioRecord.stop();
        audioRecord.release();
    }
}
