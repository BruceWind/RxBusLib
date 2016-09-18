package rxbusdemo.wei.rxbusdemo;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import rxbusdemo.wei.rxbus.RxBus;
import rxbusdemo.wei.rxbus.component.OnEventMainThread;

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

            RxBus.getInstance().post(filer, "scream");

            RxBus.getInstance().unRegister(filer);

        }).start();

    }
}
