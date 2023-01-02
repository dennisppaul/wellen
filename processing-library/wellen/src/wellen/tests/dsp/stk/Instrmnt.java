package wellen.tests.dsp.stk;

public abstract class Instrmnt {

    /***************************************************/
    /*! \class Instrmnt
    \brief STK instrument abstract base class.

    This class provides a common interface for
    all STK instruments.

      by Perry R. Cook and Gary P. Scavone, 1995--2021.
    */

    protected StkFrames lastFrame_;

    /***************************************************/

    //! Class ructor.
    public Instrmnt() {
        lastFrame_.resize(1, 1, 0.0f);
    }

    //! Reset and clear all internal state (for subclasses).
    /*!
    Not all subclasses implement a clear() function.
    */
    public void clear() {
    }

    //! Start a note with the given frequency and amplitude.
    public abstract void noteOn(float frequency, float amplitude);

    //! Stop a note with the given amplitude (speed of decay).
    public abstract void noteOff(float amplitude);

    //! Set instrument parameters for a particular frequency.
    public abstract void setFrequency(float frequency);

    //! Perform the control change specified by \e number and \e value (0.0 - 128.0).
    public abstract void controlChange(int number, float value);

    //! Return the number of output channels for the class.
    public int channelsOut() {
        return lastFrame_.channels();
    }

    //! Return an StkFrames reference to the last output sample frame.
    public StkFrames lastFrame() {
        return lastFrame_;
    }

    //! Return the specified channel value of the last computed frame.
    /*!
    The \c channel argument must be less than the number of output
    channels, which can be determined with the channelsOut() function
    (the first channel is specified by 0).  However, range checking is
    only performed if _STK_DEBUG_ is defined during compilation, in
    which case an out-of-range value will trigger an StkError
    exception. \sa lastFrame()
    */
    public float lastOut(int channel) {
        return lastFrame_.get(channel);
    }

    public float lastOut() {
        return lastOut(0);
    }

    //! Compute one sample frame and return the specified \c channel value.
    /*!
    For monophonic instruments, the \c channel argument is ignored.
  */
    public abstract float tick(int channel);

    public float tick() {
        return tick(0);
    }

    //! Fill the StkFrames object with computed sample frames, starting at the specified channel.
    /*!
    The \c channel argument plus the number of output channels must
    be less than the number of channels in the StkFrames argument (the
    first channel is specified by 0).  However, range checking is only
    performed if _STK_DEBUG_ is defined during compilation, in which
    case an out-of-range value will trigger an StkError exception.
    */
    public abstract StkFrames tick(StkFrames frames, int channel);

    public StkFrames tick(StkFrames frames) {
        return tick(frames, 0);
    }

}
