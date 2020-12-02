package ton;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeatMIDI implements MidiInListener {

    private static final String METHOD_NAME = "beat";
    private static final int BPM_SAMPLER_SIZE = 12;
    public static boolean VERBOSE = false;
    private final Object mListener;
    private Method mMethod = null;
    private int mTickPPQNCounter = 0;
    private boolean mIsRunning = false;
    private float mBPMEstimate = 0;
    private long mBPMMeasure;
    private final float[] mBPMSampler = new float[BPM_SAMPLER_SIZE];
    private int mBPMSamplerCounter = 0;

    public BeatMIDI(Object pListener, int pBPM) {
        this(pListener);
    }

    public BeatMIDI(Object pListener) {
        mListener = pListener;
        try {
            mMethod = pListener.getClass().getDeclaredMethod(METHOD_NAME, Integer.TYPE);
        } catch (NoSuchMethodException | SecurityException ex) {
            ex.printStackTrace();
        }
        mBPMMeasure = _timer();
        start();
    }

    public boolean running() {
        return mIsRunning;
    }

    public int beat_count() {
        return mTickPPQNCounter;
    }

    /**
     * returns an estimate of the current BPM deduced from the duration between two ticks ( or pulses )
     *
     * @return estimated BPM ( might be imprecise in the first few beats )
     */
    public float bpm() {
        return mBPMEstimate;
    }

    public void invoke() {
        if (mIsRunning) {
            try {
                mMethod.invoke(mListener, mTickPPQNCounter);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                System.err.println("+++ @BeatMIDI / problem calling `" + METHOD_NAME + "`");
                ex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void receiveProgramChange(int channel, int number, int value) {
    }

    @Override
    public void receiveControlChange(int channel, int number, int value) {
    }

    @Override
    public void receiveNoteOff(int channel, int pitch) {
    }

    @Override
    public void receiveNoteOn(int channel, int pitch, int velocity) {
    }

    @Override
    public void clock_tick() {
        if (mIsRunning) {
            mTickPPQNCounter++;
            estimate_bpm();
            invoke();
        }
    }

    public void stop() {
        mIsRunning = false;
    }

    public void start() {
        mIsRunning = true;
        mBPMMeasure = System.currentTimeMillis();
    }

    @Override
    public void clock_start() {
        if (VERBOSE) {
            System.out.println("clock_start");
        }
        mTickPPQNCounter = 0;
        start();
//        invoke();
    }

    @Override
    public void clock_continue() {
        if (VERBOSE) {
            System.out.println("clock_continue");
        }
        start();
//        clock_tick();
    }

    @Override
    public void clock_stop() {
        if (VERBOSE) {
            System.out.println("clock_stop");
        }
//        if (mIsRunning) {
//            // @TODO(check if this is desired behavior)
//            mTickCounter++;
//        }
        stop();
    }

    @Override
    public void clock_song_position_pointer(int pOffset16th) {
        final int mPPQN = pOffset16th / 4 * 24;
        mTickPPQNCounter = mPPQN;
        if (VERBOSE) {
            System.out.println("clock_song_position_pointer: " + mTickPPQNCounter + "(" + pOffset16th + ")");
        }
    }

    private void estimate_bpm() {
        float mBPMEstimateFragment = 60 / ((float) ((_timer() - mBPMMeasure) / _timer_divider()) * 24); // 24 PPQN *
        // 4 QN * 60 SEC
        mBPMSampler[mBPMSamplerCounter % BPM_SAMPLER_SIZE] = mBPMEstimateFragment;
        mBPMSamplerCounter++;
        mBPMEstimate = 0;
        for (float mBPMSample : mBPMSampler) {
            mBPMEstimate += mBPMSample;
        }
        mBPMEstimate /= BPM_SAMPLER_SIZE;
        mBPMMeasure = _timer();
    }

    public static BeatMIDI start(Object pListener, String pMidiInput) {
        final BeatMIDI mBeatMIDI = new BeatMIDI(pListener);
        MidiIn mMidiIn = new MidiIn(pMidiInput);
        mMidiIn.addListener(mBeatMIDI);
        return mBeatMIDI;
    }

    private static long _timer() {
        return System.nanoTime();
    }

    private static double _timer_divider() {
        return 1000000000;
    }
}
