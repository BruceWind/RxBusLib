package rxbusdemo.wei.rxbusdemo;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.androidyuan.rxbus.RxBus;
import com.androidyuan.rxbus.component.OnEventMainThread;

public class MainActivity extends AppCompatActivity {

    private final String filer="testfilter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxBus.getInstance().register(filer, new OnEventMainThread((o) ->
                Log.d("onEvent=" + o, "is MainThread:" + (Looper.myLooper() == Looper.getMainLooper())))
        );

        new Thread( ()->{

            RxBus.getInstance().post(filer, "scream1");
            RxBus.getInstance().post(filer, "scream2");

            RxBus.getInstance().unRegister(filer);// stop code run
            RxBus.getInstance().post(filer, "scream3");



        }).start();

    }
}
