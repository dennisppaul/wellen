package wellen.tests.dsp.stk;

public class StkFrames {

    //! The default ructor initializes the frame data structure to size zero.
    public StkFrames(float value, int nFrames, int nChannels) {
        nFrames_ = nFrames;
        nChannels_ = nChannels;
        size_ = nFrames_ * nChannels_;
        bufferSize_ = size_;

        if (size_ > 0) {
            data_ = new float[size_];
            for (int i = 0; i < size_; i++) {
                data_[i] = value;
            }
        }

        dataRate_ = Stk.sampleRate();
    }


    //! Return an interpolated value at the fractional frame index and channel.
  /*!
    This function performs linear interpolation.  The \c frame
    index must be between 0.0 and frames() - 1.  The \c channel index
    must be between 0 and channels() - 1.  No range checking is
    performed unless _STK_DEBUG_ is defined.
  */
    public float interpolate(float frame, int channel) {
        int iIndex = (int) frame;                    // integer part of index
        float output, alpha = frame - (float) iIndex;  // fractional part of index

        iIndex = iIndex * nChannels_ + channel;
        output = data_[iIndex];
        if (alpha > 0.0) {
            output += (alpha * (data_[iIndex + nChannels_] - output));
        }
        return output;
    }

    public float interpolate(float frame) {
        return interpolate(frame, 0);
    }

    //! Returns the total number of audio samples represented by the object.
    public int size() {
        return size_;
    }

    //! Returns \e true if the object size is zero and \e false otherwise.
    public boolean empty() {
        return size_ <= 0;
    }

    //! Resize self to represent the specified number of channels and frames.
  /*!
    Changes the size of self based on the number of frames and
    channels.  No element assignment is performed.  No memory
    deallocation occurs if the new size is smaller than the previous
    size.  Further, no new memory is allocated when the new size is
    smaller or equal to a previously allocated size.
  */
    public void resize(int nFrames, int nChannels) {
        nFrames_ = nFrames;
        nChannels_ = nChannels;

        size_ = nFrames_ * nChannels_;
        if (size_ > bufferSize_) {
            data_ = new float[size_];
        }
    }

    public void resize(int nFrames) {
        resize(nFrames, 1);
    }

    //! Resize self to represent the specified number of channels and frames and perform element initialization.
  /*!
    Changes the size of self based on the number of frames and
    channels, and assigns \c value to every element.  No memory
    deallocation occurs if the new size is smaller than the previous
    size.  Further, no new memory is allocated when the new size is
    smaller or equal to a previously allocated size.
  */
    public void resize(int nFrames, int nChannels, float value) {
        resize(nFrames, nChannels);
        for (int i = 0; i < size_; i++) {
            data_[i] = value;
        }
    }

    //! Retrieves a single channel
  /*!
    Copies the specified \c channel into \c destinationFrames's \c destinationChannel. \c destinationChannel must be
    between 0 and destination.channels() - 1 and
    \c channel must be between 0 and channels() - 1. destination.frames() must be >= frames().
    No range checking is performed unless _STK_DEBUG_ is defined.
  */
    public StkFrames getChannel(int sourceChannel, StkFrames destinationFrames, int destinationChannel) {

        int sourceHop = nChannels_;
        int destinationHop = destinationFrames.nChannels_;
        for (int i = sourceChannel, j = destinationChannel; i < nFrames_ * nChannels_; i += sourceHop,
                j += destinationHop) {
            destinationFrames.set(j, data_[i]);
        }
        return destinationFrames;
    }

    //! Sets a single channel
  /*!
    Copies the \c sourceChannel of \c sourceFrames into the \c channel of self.
    SourceFrames.frames() must be equal to frames().
    No range checking is performed unless _STK_DEBUG_ is defined.
  */
    public void setChannel(int destinationChannel, StkFrames sourceFrames, int sourceChannel) {
        int sourceHop = sourceFrames.nChannels_;
        int destinationHop = nChannels_;
        for (int i = destinationChannel, j = sourceChannel; i < nFrames_ * nChannels_; i += destinationHop,
                j += sourceHop) {
            data_[i] = sourceFrames.get(j);
        }
    }

    //! Return the number of channels represented by the data.
    public int channels() {
        return nChannels_;
    }

    //! Return the number of sample frames represented by the data.
    public int frames() {
        return nFrames_;
    }

    //! Set the sample rate associated with the StkFrames data.
    /*!
    By default, this value is set equal to the current STK sample
    rate at the time of instantiation.
    */
    public void setDataRate(float rate) {
        dataRate_ = rate;
    }

    //! Return the sample rate associated with the StkFrames data.
    /*!
    By default, this value is set equal to the current STK sample
    rate at the time of instantiation.
    */
    public float dataRate() {
        return dataRate_;
    }

    public void set(int i, float value) {
        data_[i] = value;
    }

    public float get(int i) {
        return data_[i];
    }

    protected float[] data_;
    protected float dataRate_;
    protected int nFrames_;
    protected int nChannels_;
    protected int size_;
    protected int bufferSize_;
}