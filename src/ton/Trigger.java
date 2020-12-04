package ton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Trigger implements DSPNodeInput {

    private static final String METHOD_NAME = "trigger";
    private final Object mListener;
    private float mPreviousSignal = 0.0f;
    private Method mMethod = null;
    private boolean mEnableRisingEdge = true;
    private boolean mEnableFallingEdge = true;

    public Trigger(Object pListener) {
        mListener = pListener;
        try {
            mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME);
        } catch (NoSuchMethodException | SecurityException ex) {
            System.err.println("+++ @" + getClass().getSimpleName() + " / could not find `" + METHOD_NAME + "`");
        }
    }

    public static Trigger start(Object pListener) {
        return new Trigger(pListener);
    }

    public void trigger_rising_edge(boolean pEnableRisingEdge) {
        mEnableRisingEdge = pEnableRisingEdge;
    }

    public void trigger_falling_edge(boolean pEnableFallingEdge) {
        mEnableFallingEdge = pEnableFallingEdge;
    }

    public void input(float pSignal) {
        if (mEnableRisingEdge && mPreviousSignal <= 0 && pSignal > 0) {
            fireEvent();
        }
        if (mEnableFallingEdge && mPreviousSignal >= 0 && pSignal < 0) {
            fireEvent();
        }
        mPreviousSignal = pSignal;
    }

    private void fireEvent() {
        try {
            mMethod.invoke(mListener);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            ex.printStackTrace();
        }
    }
}
