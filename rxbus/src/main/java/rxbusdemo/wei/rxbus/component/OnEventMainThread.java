package rxbusdemo.wei.rxbus.component;

/**
 * Created by wei on 16/9/19.
 */
public class OnEventMainThread extends OnEvent {

    public OnEventMainThread(REvent onEv) {

        super(onEv);
    }

    @Override
    protected int getThreadMode() {

        return BusThreadModel.THREAD_MAINTHREAD;
    }
}