package rxbusdemo.wei.rxbusdemo;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidyuan.rxbus.RxBus;
import com.androidyuan.rxbus.component.Subscribe;
import com.androidyuan.rxbus.component.ThreadMode;

import rxbusdemo.wei.model.DriverEvent;

public class SecondActivity extends AppCompatActivity {

    private final String filer="testfilter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxBus.getInstance().register(this);

        new Thread( ()->{


           // RxBus.getInstance().post(new DriverEvent("scream1"));
        }).start();

        //RxBus.getInstance().postRx(new DriverEvent("scream1"));
        RxBus.getInstance().postRx("scream2");
        RxBus.getInstance().postRx("scream3");
    }


    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    public void handleEvent(String event) {
        Log.d(TAG, "RXJAVA event info = "+event+", is MainThread : "+(Looper.getMainLooper()==Looper.myLooper()));
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handle(String event) {
        Log.d(TAG, "RXJAVA event info = "+event+", is MainThread : "+(Looper.getMainLooper()==Looper.myLooper()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unRegister(this);
    }
}
