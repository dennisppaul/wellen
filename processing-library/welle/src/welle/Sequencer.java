package welle;

public class Sequencer<T> {

    // @TODO(should `T` be limited to `Number` e.g `T extends Number` )

    private final T[] mSequence;
    private int mStep;

    public Sequencer(int pSteps) {
        mSequence = (T[]) new Object[pSteps];
        reset();
    }

    @SafeVarargs
    public Sequencer(T... pData) {
        mSequence = pData;
        reset();
    }

    public T[] data() {
        return mSequence;
    }

    public T step() {
        final T mValue = mSequence[mStep];
        mStep++;
        mStep %= mSequence.length;
        return mValue;
    }

    public T get_current() {
        return mSequence[mStep];
    }

    public void set_current(T pValue) {
        set(mStep, pValue);
    }

    public void set(int pStep, T pValue) {
        if (pStep >= 0 && pStep < mSequence.length) {
            mSequence[pStep] = pValue;
        }
    }

    public T get(int pStep) {
        if (pStep >= 0 && pStep < mSequence.length) {
            return mSequence[pStep];
        } else {
            return mSequence[0];
        }
    }

    public void reset() {
        mStep = 0;
    }

    public int get_step() {
        return mStep;
    }

}
