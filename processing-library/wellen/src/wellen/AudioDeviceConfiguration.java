package wellen;

public class AudioDeviceConfiguration {
    public int sample_rate = Wellen.DEFAULT_SAMPLING_RATE;
    public int sample_buffer_size = Wellen.DEFAULT_AUDIOBLOCK_SIZE; // i.e audio_block_size
    public int output_device = Wellen.DEFAULT_AUDIO_DEVICE;
    public int number_of_output_channels = Wellen.STEREO;
    public int input_device = Wellen.DEFAULT_AUDIO_DEVICE;
    public int number_of_input_channels = Wellen.MONO;
}
