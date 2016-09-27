package rxbusdemo.wei.rxbusdemo;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.androidyuan.rxbus.RxBus;
import com.androidyuan.rxbus.component.Subscribe;
import com.androidyuan.rxbus.component.ThreadMode;

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


    @Subscribe(threadMode = ThreadMode.BACKGROUND, sticky = true)
    public void handleEvent(DriverEvent event) {
        Log.d(TAG, "event info = "+event.info+", is MainThread : "+(Looper.getMainLooper()==Looper.myLooper()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBus.getInstance().unRegister(this);
    }
}
