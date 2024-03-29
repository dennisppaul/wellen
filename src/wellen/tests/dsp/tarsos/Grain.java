package wellen.tests.dsp.tarsos;

/**
 * The nested class Grain. Stores information about the start time, current position, age, and grain size of the grain.
 */
class Grain {

    boolean active;
    /**
     * The age of the grain in milliseconds.
     */
    double age;
    /**
     * The grain size of the grain. Fixed at instantiation.
     */
    double grainSize;
    /**
     * The position in millseconds.
     */
    double position;

    /**
     * Sets the given Grain to start immediately.
     *
     * @param grainSize
     * @param randomness
     * @param position
     * @param timeStretchFactor
     * @param pitchShiftFactor
     */
    void reset(double grainSize,
               double randomness,
               double position,
               double timeStretchFactor,
               double pitchShiftFactor) {
        double randomTimeDiff = (Math.random() > 0.5 ? +1 : -1) * grainSize * randomness;
        double actualGrainSize = (grainSize + randomTimeDiff) * 1.0 / timeStretchFactor + 1;
        this.position = position - actualGrainSize;
        this.age = 0f;
        this.grainSize = actualGrainSize;
        this.active = true;
    }
}