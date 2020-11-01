import oscP5.OscMessage;
import oscP5.OscP5;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EventReceiver {
//    virtual void event_receive(const EVENT_TYPE event, const float* data) {}

    private static final String METHOD_NAME = "event_receive";
    private static EventReceiver mInstance = null;
    private final Object mParent;
    private Method mMethod = null;
    private final OscP5 mOscP5;

    public EventReceiver(Object pPApplet, int pPortReceive) {
        mParent = pPApplet;
        mOscP5 = new OscP5(pPApplet, pPortReceive);
        try {
            mMethod = pPApplet.getClass().getDeclaredMethod(METHOD_NAME, int.class, float[].class);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
        }
    }

    public static EventReceiver start(Object pParent, int pPortReceive) {
        if (mInstance == null) {
            mInstance = new EventReceiver(pParent, pPortReceive);
        }
        return mInstance;
    }

    public void received_osc_event(OscMessage pOSCMessage) {
        try {
            final int mEvent = 0;
            final float[] mData = new float[2];
            mMethod.invoke(mParent, mEvent, mData);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}
