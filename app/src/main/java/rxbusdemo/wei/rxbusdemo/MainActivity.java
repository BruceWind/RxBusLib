package rxbusdemo.wei.rxbusdemo;

import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private final String filer="testfilter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxBus.getInstance().register(this);

        startActivity(new Intent(this,SecondActivity.class));



    }


    @Subscribe(threadMode = ThreadMode.POSTING, sticky = true)
    public void handleEvent(DriverEvent event) {
        Log.d("RXJAVA", "event info = "+event.info+", is MainThread : "+(Looper.getMainLooper()==Looper.myLooper()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unRegister(this);
    }
}
