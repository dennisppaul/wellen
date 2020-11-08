package de.hfkbremen.ton.examples_ext;

import de.hfkbremen.ton.Instrument;
import de.hfkbremen.ton.Note;
import de.hfkbremen.ton.Scale;
import de.hfkbremen.ton.Ton;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

/**
 * this examples shows how to use an instrument with an amplitude envelope ( ADSR ). the envelope controls the amplitude
 * of a tone over time. it is started by calling the `note_on()` method and released by calling the `note_off()`
 * method.
 */
/*
 * diagram of an (A)ttack, (D)ecay, (S)ustain and (R)elease envelope:
 *
 *     ^    /\
 *     |   /  \
 *     |  /    \______
 *     | /            \
 *     |/              \
 *     +---------------------->
 *     [A   ][D][S   ][R]
 */
public class SketchExampleInstruments01ADSR extends PApplet {

    private Parameter mParameterAttack;
    private Parameter mParameterDecay;
    private Parameter mParameterSustain;
    private Parameter mParameterRelease;
    private int mNote;

    public void settings() {
        size(640, 480, P2D);
    }

    public void setup() {
        hint(DISABLE_KEY_REPEAT);

        mParameterAttack = new Parameter();
        mParameterDecay = new Parameter();
        mParameterSustain = new Parameter();
        mParameterSustain.horziontal = false;
        mParameterRelease = new Parameter();
        updateADSR();
        println(Instrument.ADSR_DIAGRAM);
    }

    public void draw() {
        if (Ton.isPlaying()) {
            int mColor = (mNote - Note.NOTE_A2) * 5 + 50;
            background(mColor);
        } else {
            background(255);
        }
        final float mXOffset = (width - Parameter.size * 4) * 0.5f;
        final float mYOffset = height * 0.5f;
        translate(mXOffset, mYOffset);
        scale(1, -1);

        updateDiagram(mXOffset, mYOffset);
        drawDiagram();
    }

    public void mouseDragged() {
        updateADSR();
    }

    public void keyPressed() {
        mNote = Scale.note(Scale.MAJOR_CHORD_7, Note.NOTE_A2, (int) random(0, 10));
        Ton.note_on(mNote, 100);
    }

    public void keyReleased() {
        Ton.note_off();
    }

    private void drawDiagram() {
        stroke(0, 63);
        line(0, 0, Parameter.size * 4, 0);

        stroke(0);
        line(0, 0,
             mParameterAttack.current_position_x(),
             mParameterAttack.current_position_y());
        line(mParameterAttack.current_position_x(),
             mParameterAttack.current_position_y(),
             mParameterDecay.current_position_x(),
             mParameterDecay.current_position_y());
        line(mParameterDecay.current_position_x(),
             mParameterDecay.current_position_y(),
             mParameterSustain.current_position_x(),
             mParameterSustain.current_position_y());
        line(mParameterSustain.current_position_x(),
             mParameterSustain.current_position_y(),
             mParameterRelease.current_position_x(),
             mParameterRelease.current_position_y());

        noStroke();
        fill(0);
        ellipse(0, 0, Parameter.radius * 2, Parameter.radius * 2);

        mParameterAttack.draw(g);
        mParameterDecay.draw(g);
        mParameterSustain.draw(g);
        mParameterRelease.draw(g);
    }

    private void updateDiagram(float mXOffset, float mYOffset) {
        float mX = mouseX - mXOffset;
        float mY = -mouseY + mYOffset;

        mParameterAttack.update(mX, mY, mousePressed);
        mParameterAttack.x = 0;
        mParameterAttack.y = Parameter.size;

        mParameterDecay.update(mX, mY, mousePressed);
        mParameterDecay.x = mParameterAttack.current_position_x();
        mParameterDecay.y = mParameterSustain.current_position_y();

        mParameterSustain.update(mX, mY, mousePressed);
        mParameterSustain.x = mParameterDecay.current_position_x() + Parameter.size;
        mParameterSustain.y = 0;

        mParameterRelease.update(mX, mY, mousePressed);
        mParameterRelease.x = mParameterSustain.x;
        mParameterRelease.y = 0;
    }

    private void updateADSR() {
        Ton.instrument().attack(mParameterAttack.value);
        Ton.instrument().decay(mParameterDecay.value);
        Ton.instrument().sustain(mParameterSustain.value);
        Ton.instrument().release(mParameterRelease.value);
    }

    private static class Parameter {

        private static final float size = 120;
        private static final float radius = 6;
        float x;
        float y;
        float value;
        boolean horziontal;
        boolean hoover;
        boolean drag;

        public Parameter() {
            x = 0;
            y = 0;
            value = 0.5f;
            horziontal = true;
            hoover = false;
            drag = false;
        }

        void draw(PGraphics g) {
//            g.stroke(0, 63);
//            g.line(x, y, x + (horziontal ? size : 0), y + (horziontal ? 0 : size));

            g.noStroke();
            g.fill(hoover ? (drag ? 223 : 127) : 0);
            g.ellipse(current_position_x(), current_position_y(), radius * 2, radius * 2);
        }

        void update_value(float pX, float pY) {
            value = (horziontal ? (pX - x) : (pY - y)) / size;
            value = min(1, max(0, value));
        }

        boolean hit(float pX, float pY) {
            final float mDistance = PVector.dist(new PVector().set(pX, pY),
                                                 new PVector().set(current_position_x(), current_position_y()));
            return mDistance < radius * 2;
        }

        float current_position_x() {
            return horziontal ? x + size * value : x;
        }

        float current_position_y() {
            return horziontal ? y : y + size * value;
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
    }

    public static void main(String[] args) {
        PApplet.main(SketchExampleInstruments01ADSR.class.getName());
    }
}
