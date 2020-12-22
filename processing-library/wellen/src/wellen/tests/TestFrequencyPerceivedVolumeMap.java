package wellen.tests;

import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;
import wellen.Tone;
import wellen.Wellen;

public class TestFrequencyPerceivedVolumeMap extends PApplet {

    private final float[] mOffsetSINE_AND_TRIANGLE = new float[]{1.0f, 0.82472223f, 0.67277783f, 0.5425f, 0.43222225f,
                                                                 0.34027773f, 0.26500002f, 0.2047222f, 0.15777776f,
                                                                 0.12249999f, 0.09722222f, 0.08027777f, 0.07f,
                                                                 0.064722225f, 0.06277778f, 0.0625f};
    private final float[] mOffsetSAWTOOTH_AND_SQUARE = new float[]{0.25f, 0.21494445f, 0.18455556f, 0.1585f,
                                                                   0.13644445f, 0.118055545f, 0.103f, 0.09094444f,
                                                                   0.08155555f, 0.074499995f, 0.06944444f, 0.06605555f,
                                                                   0.064f, 0.06294444f, 0.06255556f, 0.0625f};
    private Slider[] mSlider;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        float[] mOffset = mOffsetSINE_AND_TRIANGLE;
        mSlider = new Slider[Wellen.DEFAULT_NUMBER_OF_INSTRUMENTS];
        for (int i = 0; i < mSlider.length; i++) {
            Tone.instrument(i).set_oscillator_type(Wellen.WAVESHAPE_SAWTOOTH);
            mSlider[i] = new Slider(height * 0.5f);
            mSlider[i].x = map(i, 0, mSlider.length - 1, width * 0.25f, width * 0.75f);
            mSlider[i].y = height * 0.25f;
            mSlider[i].value = mOffset[i];
        }
//        for (int i = 0; i < mSlider.length; i++) {
//            final float mPadding = 32.0f / 128.0f; // SAWTOOTH_AND_SQUARE
////            final float mPadding = 8.0f / 128.0f; // SINE_AND_TRIANGLE
//            float mValue = map(i, 0, 15, 1, 0);
//            mValue = pow(mValue, 3);
//            mValue *= 1.0f - mPadding;
//            mValue += mPadding;
//            mSlider[i].value = mValue;
//        }
        for (int i = 0; i < mSlider.length; i++) {
            final float mPadding = 32.0f / 128.0f; // SAWTOOTH_AND_SQUARE
            float mValue = map(i, 0, 15, 1, 0);
            mValue = pow(mValue, 3);
            mValue *= 1.0f - mPadding;
            mValue += mPadding;
            mValue *= 0.25f;
            mSlider[i].value = mValue;
        }
//        for (int i = 0; i < mSlider.length; i += 2) {
//            Tone.instrument(i).set_oscillator_type(Wellen.OSC_SINE);
//            final float mPadding = 8.0f / 128.0f; // SINE_AND_TRIANGLE
//            float mValue = map(i, 0, 15, 1, 0);
//            mValue = pow(mValue, 3);
//            mValue *= 1.0f - mPadding;
//            mValue += mPadding;
//            mSlider[i].value = mValue;
//        }
    }

    public void draw() {
        background(255);
        for (int i = 0; i < mSlider.length; i++) {
            final Slider s = mSlider[i];
            s.draw(g, Tone.instrument(i).is_playing() ? 0 : 127);
            s.update(mouseX, mouseY, mousePressed);
        }
        stroke(0);
        Wellen.draw_buffer(g, width, height, Tone.get_buffer());
    }

    public void keyPressed() {
        final char M_CHAR = 'a';
        if (key >= M_CHAR && key < M_CHAR + mSlider.length) {
            final int mInstrument = key - M_CHAR;
            final int mNote = 24 + 4 * mInstrument;
            final int mVelocity = (int) (127 * mSlider[mInstrument].value);
            Tone.instrument(mInstrument);
            Tone.note_on(mNote, mVelocity, 1.0f);
            System.out.println(mInstrument + ", " + mNote + ", " + mVelocity);
        }
        switch (key) {
            case 'C':
                for (final Slider s : mSlider) {
                    s.value = 0.5f;
                }
                break;
            case ' ':
                printSlidersAsArray();
                break;
        }
    }

    private void printSlidersAsArray() {
        final String M_ARRAY_NAME = "mOffset";
        System.out.println(M_ARRAY_NAME + " = new float[" + mSlider.length + "];");
        for (int i = 0; i < mSlider.length; i++) {
            System.out.print(M_ARRAY_NAME + "[" + i + "] = ");
            System.out.print(mSlider[i].value + "f");
            System.out.println(";");
        }
        System.out.print("float[] " + M_ARRAY_NAME + " = new float[] {");
        for (int i = 0; i < mSlider.length; i++) {
            System.out.print(mSlider[i].value + "f" + (i == mSlider.length - 1 ? "" : ", "));
        }
        System.out.println("};");
    }

    private static class Slider {

        private static final float radius = 8;
        private static float size;
        float x;
        float y;
        float value;
        boolean horizontal;
        boolean hoover;
        boolean drag;

        public Slider(float pSize) {
            size = pSize;
            x = 0;
            y = 0;
            value = 0.5f;
            horizontal = false;
            hoover = false;
            drag = false;
        }

        float current_position_x() {
            return horizontal ? x + size * value : x;
        }

        float current_position_y() {
            return horizontal ? y : y + size * value;
        }

        void draw(PGraphics g, int pColor) {
            g.stroke(pColor);
            g.line(x, y, x + (horizontal ? size : 0), y + (horizontal ? 0 : size));

            final float mEdgeDiameter = 5;
            g.noStroke();
            g.fill(pColor);
            g.ellipse(x, y, mEdgeDiameter, mEdgeDiameter);
            g.ellipse(x + (horizontal ? size : 0), y + (horizontal ? 0 : size), mEdgeDiameter, mEdgeDiameter);

            g.noStroke();
            g.fill(pColor);
            g.ellipse(current_position_x(), current_position_y(), radius * (hoover ? 2 : 1), radius * (hoover ? 2 : 1));
        }

        boolean hit(float pX, float pY) {
            final float mDistance = PVector.dist(new PVector().set(pX, pY),
                                                 new PVector().set(current_position_x(), current_position_y()));
            return mDistance < radius * 2;
        }

        void update(float mouse_x, float mouse_y, boolean mouse_pressed) {
            if (hit(mouse_x, mouse_y)) {
                if (mouse_pressed) {
                    update_value(mouse_x, mouse_y);
                    drag = true;
                } else {
                    drag = false;
                }
                hoover = true;
            } else {
                hoover = false;
                drag = false;
            }
        }

        void update_value(float pX, float pY) {
            value = (horizontal ? (pX - x) : (pY - y)) / size;
            value = min(1, max(0, value));
        }
    }

    public static void main(String[] args) {
        PApplet.main(TestFrequencyPerceivedVolumeMap.class.getName());
    }
}