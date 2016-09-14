package rxbusdemo.wei.rxbusdemo;

import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import rx.Observable;
import rxbusdemo.wei.rxbus.RxBus;

public class MainActivity extends AppCompatActivity {

    private final String filer="testfilter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxBus.getInstance().register(filer,(s) -> Log.d("callTestRxBus","is MainThread:"+(Looper.myLooper()==Looper.getMainLooper())) );


        getWindow().getDecorView().postDelayed(
                () -> RxBus.getInstance().sendBroadCast(filer, Observable.just(",now  is reciver")),
                4000 );

        new Thread(){
            @Override
            public void run() {
                super.run();
                //代码不会执行 因为被unRegster了
                RxBus.getInstance().sendBroadOnUI(filer, Observable.just(",now  is new thread"));
                RxBus.getInstance().unRegister(filer);
            }
        }.start();

    }
}
