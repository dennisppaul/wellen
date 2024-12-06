package wellen.examples.basics;

import com.sun.jna.Native;
import processing.core.PApplet;
import wellen.Note;
import wellen.Scale;
import wellen.Tone;
import wellen.Wellen;

import javax.swing.JFrame;

public class ExampleBasics02Scales extends PApplet {

    /*
     * this example demonstrates how to use *musical scales*. a selection of predefined scales is available in `Scale`,
     * however custom scales can also be created.
     */

    private int   mNote;
    private int[] mScale;
    private int   mStep;

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        Tone.preset(Wellen.INSTRUMENT_PRESET_SUB_SINE);

        mScale = Scale.CHORD_MINOR_7TH;
        mNote  = Note.NOTE_C4;
        fill(0);
    }

    public void draw() {
        background(255);
        float mDiameter = map(mNote, Note.NOTE_C3, Note.NOTE_C4, height * 0.1f, height * 0.8f);
        ellipse(width * 0.5f, height * 0.5f, mDiameter, mDiameter);
    }

    public void keyPressed() {
        if (key == ' ') {
            mStep++;
            mStep %= mScale.length + 1;
            mNote = Scale.get_note(mScale, Note.NOTE_C3, mStep);
            /*
             * note that this variant of `note_on` takes three parameters where the third parameter defines the
             * duration of the note in seconds. a `note_off` is automatically triggered after the duration.
             */
            Tone.note_on(mNote, 100, 0.25f);
        }
        if (key == '1') {
            mScale = Scale.HALF_TONE;
        }
        if (key == '2') {
            mScale = Scale.CHORD_MINOR_7TH;
        }
        if (key == '3') {
            mScale = Scale.MINOR_PENTATONIC;
        }
        if (key == '4') {
            mScale = new int[]{0, 2, 3, 6, 7, 8, 11}; // Nawa Athar
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setUndecorated(true); // Remove title bar
        frame.setSize(400, 400); // Set the size of the JFrame

        // Force the frame to initialize
        frame.setVisible(true);
        frame.validate(); // Ensure components are properly laid out
        frame.repaint(); // Trigger rendering

        // Debug: Check frame bounds
        System.out.println("Frame is displayable: " + frame.isDisplayable());
        System.out.println("Frame is visible: " + frame.isVisible());
        System.out.println("Frame bounds: " + frame.getBounds());

        // Wait briefly to ensure rendering completes
        try {
            Thread.sleep(100); // 100ms delay to allow the frame to render
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Try retrieving the native window ID
        long windowID = Native.getWindowID(frame);
        System.out.println("Native Window ID: " + windowID);

        if (windowID == 0) {
            throw new IllegalStateException("Failed to retrieve native window ID.");
        }

        PApplet.main(ExampleBasics02Scales.class.getName());
    }
}
