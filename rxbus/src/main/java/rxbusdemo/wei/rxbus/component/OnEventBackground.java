package rxbusdemo.wei.rxbus.component;

/**
 * Created by wei on 16/9/19.
 */
public class OnEventBackground extends OnEvent {

    public OnEventBackground(REvent onEv) {

        super(onEv);
    }

    @Override
    protected int getThreadMode() {

        return BusThreadModel.THREAD_COMPUTION;
    }
}
