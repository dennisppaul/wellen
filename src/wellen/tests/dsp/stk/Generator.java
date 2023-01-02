package wellen.tests.dsp.stk;

public abstract class Generator {

    /***************************************************/
    /*! \class Generator
    \brief STK abstract unit generator parent class.

    This class provides limited common functionality for STK unit
    generator sample-source subclasses.  It is general enough to
    support both monophonic and polyphonic output classes.

    by Perry R. Cook and Gary P. Scavone, 1995--2021.
    */

    protected StkFrames lastFrame_;

    /***************************************************/


    //! Class constructor.
    public Generator() {
        lastFrame_.resize(1, 1, 0.0f);
    }

    //! Return the number of output channels for the class.
    public int channelsOut() {
        return lastFrame_.channels();
    }

    //! Return an StkFrames reference to the last output sample frame.
    public StkFrames lastFrame() {
        return lastFrame_;
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
}


