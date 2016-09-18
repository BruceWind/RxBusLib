package rxbusdemo.wei.rxbus.component;

/**
 * Created by wei on 16/9/19.
 */
public class OnEventIO extends OnEvent {

    public OnEventIO(REvent onEv) {

        super(onEv);
    }

    @Override
    protected int getThreadMode() {

        return BusThreadModel.THREAD_IO;
    }
}
