package wellen;

import processing.core.PApplet;

import java.util.ArrayList;

/**
 * parses sample data to WAV file format ( as bytes ) and vice versa.
 */
public class WAVConverter {

    // @TODO(write header could also support `WAVE_FORMAT_PCM_32BIT_FLOAT`)
    // @TODO(currently fixed to little endianness)

    private static final String WAV_CHUNK_DATA = "data";
    private static final String WAV_CHUNK_FMT_ = "fmt ";
    private static final String WAV_CHUNK_RIFF = "RIFF";
    private static final String WAV_CHUNK_WAVE = "WAVE";
    public static boolean VERBOSE = false;
    private final int mChannels;
    private final int mBitsPerSample;
    private final int mSampleRate;
    private final ArrayList<Byte> mHeader;
    private final ArrayList<Byte> mData;
    private final int mCompressionFormat;

    public WAVConverter(Info pInfo) {
        this(pInfo.channels, pInfo.bits_per_sample, pInfo.sample_rate, pInfo.format);
    }

    public WAVConverter(int pChannels, int pBitsPerSample, int pSampleRate, int pCompressionFormat) {
        mChannels = pChannels;
        mBitsPerSample = pBitsPerSample;
        mSampleRate = pSampleRate;
        mCompressionFormat = pCompressionFormat;
        mData = new ArrayList<>();
        mHeader = new ArrayList<>();
    }

    public static byte[] convert_samples_to_bytes(float[][] pBuffer, int pChannels, int pBitsPerSample,
                                                  int pSampleRate) {
        return convert_samples_to_bytes(pBuffer, pChannels, pBitsPerSample, pSampleRate, Wellen.WAV_FORMAT_PCM);
    }

    public static byte[] convert_samples_to_bytes(Info pInfo) {
        WAVConverter mWAVConverter = new WAVConverter(pInfo);
        mWAVConverter.appendData(pInfo.samples);
        mWAVConverter.writeHeader();
        return mWAVConverter.getByteData();
    }

    public static byte[] convert_samples_to_bytes(float[][] pBuffer, int pChannels, int pBitsPerSample, int pSampleRate,
                                                  int pCompressionCode) {
        Info mInfo = new Info();
        mInfo.samples = pBuffer;
        mInfo.channels = pChannels;
        mInfo.bits_per_sample = pBitsPerSample;
        mInfo.sample_rate = pSampleRate;
        mInfo.format = pCompressionCode;
        return convert_samples_to_bytes(mInfo);
    }

    public static Info convert_bytes_to_samples(byte[] pHeader) {
        final Info mWAVStruct = new Info();
        // see http://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
        // see https://sites.google.com/site/musicgapi/technical-documents/wav-file-format
        /* RIFF Chunk */
        int mOffset = 0x00;
        final String mRIFFChunkName = WAVConverter.read_string(pHeader, mOffset + 0x00);
        if (!mRIFFChunkName.equalsIgnoreCase(WAV_CHUNK_RIFF)) {
            System.err.println("+++ WARNING @" + WAVConverter.class.getSimpleName() + " / expected `" + WAV_CHUNK_RIFF + "`" + " in header.");
        }
        final String mRIFFType = WAVConverter.read_string(pHeader, mOffset + 0x08);
        if (!mRIFFType.equalsIgnoreCase(WAV_CHUNK_WAVE)) {
            System.err.println("+++ WARNING @" + WAVConverter.class.getSimpleName() + " / expected `" + WAV_CHUNK_WAVE + "`" + " in header.");
        }
        int mFileLength = read__int32(pHeader, mOffset + 0x04);
        if (VERBOSE) {
            System.out.println("+++ CHUNK: " + mRIFFChunkName);
            System.out.println("    file length: " + mFileLength);
            System.out.println("    RIFF type  : " + mRIFFType);
        }

        /* format chunk */
        mOffset = 0x0C;
        final String mFormatChunkName = WAVConverter.read_string(pHeader, mOffset + 0x00);
        if (!mFormatChunkName.equalsIgnoreCase(WAV_CHUNK_FMT_)) {
            System.err.println("+++ WARNING @" + WAVConverter.class.getSimpleName() + " / expected `" + WAV_CHUNK_FMT_ + "` " + "in header.");
        }
        final int mFormatChunkSize = WAVConverter.read__int32(pHeader, mOffset + 0x04);
        mWAVStruct.format = WAVConverter.read__int16(pHeader, mOffset + 0x08);
        mWAVStruct.channels = WAVConverter.read__int16(pHeader, mOffset + 0x0A);
        mWAVStruct.sample_rate = WAVConverter.read__int32(pHeader, mOffset + 0x0C);
        mWAVStruct.bits_per_sample = WAVConverter.read__int16(pHeader, mOffset + 0x16);
        if (VERBOSE) {
            System.out.println("+++ CHUNK: " + mFormatChunkName);
            System.out.println("    chunk size : " + mFormatChunkSize);
            System.out.println("    format code: " + mWAVStruct.format);
            System.out.println("    channels   : " + mWAVStruct.channels);
            System.out.println("    sample rate: " + mWAVStruct.sample_rate);
            System.out.println("    byte/sec   : " + WAVConverter.read__int32(pHeader, mOffset + 0x10));
            System.out.println("    block align: " + WAVConverter.read__int16(pHeader, mOffset + 0x14));
            System.out.println("    bits/sample: " + mWAVStruct.bits_per_sample);
        }
        if (mWAVStruct.format != Wellen.WAV_FORMAT_PCM && mWAVStruct.format != Wellen.WAV_FORMAT_IEEE_FLOAT_32BIT) {
            System.err.println("+++ WARNING @" + WAVConverter.class.getSimpleName() + " / format not " + "supported. "
                                       + "currently only `WAV_FORMAT_PCM` + `WAV_FORMAT_IEEE_FLOAT_32BIT` " + "works." +
                                       " " + "(" + mWAVStruct.format + ")");
        }

        /* data chunk */
        mOffset = 0x0C + 0x18;
        if (WAVConverter.read_string(pHeader, mOffset + 0x00).equalsIgnoreCase("fact")) {
            // @TODO(hack! skipping `fact` chunk … handle this a bit more elegantly)
            final int mFactChunkSize = WAVConverter.read__int32(pHeader, mOffset + 0x04);
            final int mPeakOffset = WAVConverter.read__int32(pHeader, mOffset + 0x10);
            if (VERBOSE) {
                System.out.println("+++ skipping `fact` chunk");
                System.out.println("+++ CHUNK: " + WAVConverter.read_string(pHeader, mOffset + 0x00));
                System.out.println("    chunk size : " + mFactChunkSize);
                System.out.println("    data size  : " + WAVConverter.read__int32(pHeader, mOffset + 0x08));
                System.out.println("               : " + WAVConverter.read_string(pHeader, mOffset + 0x0C));
                System.out.println("    peak offset: " + mPeakOffset);
            }
            // Chunk ID + Chunk Data Size + Peak ID + Peak Size
            mOffset += 0x14 + mPeakOffset;
        }
        final String mDataChunkName = WAVConverter.read_string(pHeader, mOffset + 0x00);
        if (!mDataChunkName.equalsIgnoreCase(WAV_CHUNK_DATA)) {
            System.err.println("+++ WARNING @" + WAVConverter.class.getSimpleName() + " / expected `" + WAV_CHUNK_DATA + "`" + " in header.");
        }
        final int mDataChunkSize = WAVConverter.read__int32(pHeader, mOffset + 0x04);
        byte[] mInterlacedByteBuffer = WAVConverter.read__bytes(pHeader, mOffset + 0x08, mDataChunkSize);
        int mDataSize = mInterlacedByteBuffer.length / mWAVStruct.channels / (mWAVStruct.bits_per_sample / 8);
        if (VERBOSE) {
            System.out.println("+++ CHUNK: " + mDataChunkName);
            System.out.println("    chunk size       : " + mDataChunkSize);
            System.out.println("    (samples/channel): " + mDataSize);
        }

        mWAVStruct.samples = new float[mWAVStruct.channels][mDataSize];
        final int mBytesPerSample = mWAVStruct.bits_per_sample / 8;
        final int mStride = mWAVStruct.channels * mBytesPerSample;
        for (int j = 0; j < mWAVStruct.channels; j++) {
            byte[] mByteSamples = new byte[mBytesPerSample * mDataSize];
            int c = 0;
            for (int i = 0; i < mInterlacedByteBuffer.length; i += mStride) {
                for (int l = 0; l < mBytesPerSample; l++) {
                    byte b = mInterlacedByteBuffer[i + j * mBytesPerSample + l];
                    mByteSamples[c] = b;
                    c++;
                }
            }
            float[] mFloatSamples = mWAVStruct.samples[j];
            if (mWAVStruct.format == Wellen.WAV_FORMAT_PCM) {
                Wellen.bytes_to_floats(mByteSamples, mFloatSamples, mWAVStruct.bits_per_sample);
            } else if (mWAVStruct.format == Wellen.WAV_FORMAT_IEEE_FLOAT_32BIT) {
                Wellen.bytes_to_floatIEEEs(mByteSamples, mFloatSamples, true);
            }
        }
        return mWAVStruct;
    }

    private static byte[] read__bytes(byte[] pBuffer, int pStart, int pLength) {
        return PApplet.subset(pBuffer, pStart, pLength);
    }

    private static String read_string(byte[] pBuffer, int pStart) {
        final int mStringLength = 4;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mStringLength; i++) {
            sb.append((char) pBuffer[pStart + i]);
        }
        return sb.toString();
    }

    private static int read__int32(byte[] pBuffer, int pStart) {
        return read____int(pBuffer, pStart, 4);
    }

    private static int read__int16(byte[] pBuffer, int pStart) {
        return read____int(pBuffer, pStart, 2);
    }

    private static int read____int(byte[] pBuffer, int pStart, int pBytes) {
        int v = 0;
        for (int i = 0; i < pBytes; i++) {
            v |= (pBuffer[i + pStart] & 0xFF) << (i * 8);
        }
        return v;
    }

    private static int findSingleBufferLength(float[][] pBuffer) {
        if (pBuffer == null || pBuffer.length == 0) {
            return -1;
        } else if (pBuffer.length == 1) {
            return pBuffer[0].length;
        } else {
            int mBufferLength = pBuffer[0].length;
            for (int i = 1; i < pBuffer.length; i++) {
                if (mBufferLength != pBuffer[i].length) {
                    System.err.println("+++ ERROR @" + WAVConverter.class.getSimpleName() + " / sample buffers have " + "different length.");
                    return mBufferLength;
                }
            }
            return mBufferLength;
        }
    }

    private static void write___byte(ArrayList<Byte> pBuffer, int b) {
        pBuffer.add((byte) b);
    }

    private static void write__bytes(ArrayList<Byte> pBuffer, byte[] b) {
        for (byte value : b) {
            pBuffer.add(value);
        }
    }

    private static void write__int16(ArrayList<Byte> pBuffer, int s) {
        int b0, b1;
        b0 = (s >>> 0) & 0xff;
        b1 = (s >>> 8) & 0xff;
        write_bytes2(pBuffer, b0, b1);
    }

    private static void write__int32(ArrayList<Byte> pBuffer, int i) {
        int b0, b1, b2, b3;
        b0 = (i >>> 0) & 0xff;
        b1 = (i >>> 8) & 0xff;
        b2 = (i >>> 16) & 0xff;
        b3 = (i >>> 24) & 0xff;
        write_bytes4(pBuffer, b0, b1, b2, b3);
    }

    private static void write_bytes2(ArrayList<Byte> pBuffer, int b0, int b1) {
        write___byte(pBuffer, b0);
        write___byte(pBuffer, b1);
    }

    private static void write_bytes4(ArrayList<Byte> pBuffer, int b0, int b1, int b2, int b3) {
        write_bytes2(pBuffer, b0, b1);
        write_bytes2(pBuffer, b2, b3);
    }

    private static void write_string(ArrayList<Byte> pBuffer, String s) {
        final byte[] b = s.getBytes();
        for (byte value : b) {
            pBuffer.add(value);
        }
    }

    public void appendData(float[][] pFloatBuffer) {
        int mNumberOfFrames = findSingleBufferLength(pFloatBuffer);
        float[] mInterleavedFloatBuffer = new float[mNumberOfFrames * mChannels];
        for (int i = 0; i < mNumberOfFrames; i++) {
            for (int mChannel = 0; mChannel < mChannels; mChannel++) {
                mInterleavedFloatBuffer[i * mChannels + mChannel] = pFloatBuffer[mChannel][i];
            }
        }
        byte[] mByteBuffer;
        if (mCompressionFormat == Wellen.WAV_FORMAT_PCM) {
            mByteBuffer = new byte[mNumberOfFrames * mChannels * mBitsPerSample / 8];
            Wellen.floats_to_bytes(mByteBuffer, mInterleavedFloatBuffer, mBitsPerSample);
        } else if (mCompressionFormat == Wellen.WAV_FORMAT_IEEE_FLOAT_32BIT) {
            mByteBuffer = Wellen.floatIEEEs_to_bytes(mInterleavedFloatBuffer);
        } else {
            System.err.println("+++ ERROR @" + WAVConverter.class.getSimpleName() + " / data format not supported.");
            mByteBuffer = null;
        }
        write__bytes(mData, mByteBuffer);
    }

    public void writeHeader() {
        mHeader.clear();
        /* RIFF Chunk */
        write_string(mHeader, WAV_CHUNK_RIFF);
        write__int32(mHeader, mData.size()); // file length ( without header )
        write_string(mHeader, WAV_CHUNK_WAVE);
        /* format chunk */
        write_string(mHeader, WAV_CHUNK_FMT_);
        write__int32(mHeader, 16); // chunk length
        write__int16(mHeader, mCompressionFormat);
        write__int16(mHeader, mChannels);
        write__int32(mHeader, mSampleRate);
        write__int32(mHeader, (mSampleRate * mChannels * mBitsPerSample / 8)); // bytes per second
        write__int16(mHeader, (mChannels * mBitsPerSample / 8)); // block align
        write__int16(mHeader, mBitsPerSample);
        /* data chunk */
        write_string(mHeader, WAV_CHUNK_DATA);
        write__int32(mHeader, mData.size()); // data length
    }

    public byte[] getByteData() {
        byte[] mBuffer = new byte[mHeader.size() + mData.size()];
        for (int i = 0; i < mHeader.size(); i++) {
            mBuffer[i] = mHeader.get(i);
        }
        for (int i = 0; i < mData.size(); i++) {
            mBuffer[i + mHeader.size()] = mData.get(i);
        }
        return mBuffer;
    }

    public static class Info {
        public int channels;
        public int bits_per_sample;
        public int sample_rate;
        public byte[] data;
        public float[][] samples;
        public int format;
    }
}
