package rxbusdemo.wei.rxbusdemo;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.androidyuan.rxbus.RxBus;
import com.androidyuan.rxbus.component.Subscribe;
import com.androidyuan.rxbus.component.ThreadMode;

import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rxbusdemo.wei.model.DriverEvent;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxBus.getInstance().register(this);

        new Thread(() -> {

            RxBus.getInstance().post("scream3");

            RxBus.getInstance().post(new DriverEvent("scream1"));
        }).start();
        RxBus.getInstance().post("scream2");

    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handleEvent(String event) {
        Log.d("RXJAVA",
                "handleEvent info = " + event + ", is MainThread : " + (Looper.getMainLooper()
                        == Looper.myLooper()));
    }


    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void handle(String event) {
        Log.d("RXJAVA", "handle info = " + event + ", is MainThread : " + (Looper.getMainLooper()
                == Looper.myLooper()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unRegister(this);
    }
}
