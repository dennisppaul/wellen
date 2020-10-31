package de.hfkbremen.ton;

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

    public T[] data() { return mSequence; }

    public T step() {
        final T mValue = mSequence[mStep];
        mStep++;
        mStep %= mSequence.length;
        return mValue;
    }

    public T current() {
        return mSequence[mStep];
    }

    public void reset() {
        mStep = 0;
    }

    public T at(int pIndex) {
        if (pIndex >= 0 && pIndex < mSequence.length) {
            return mSequence[pIndex];
        } else {
            return mSequence[0];
        }
    }
}
