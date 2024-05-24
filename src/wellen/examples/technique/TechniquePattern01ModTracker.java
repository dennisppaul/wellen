package wellen.examples.technique;

import processing.core.PApplet;
import processing.core.PGraphics;
import wellen.Beat;
import wellen.Tone;

public class TechniquePattern01ModTracker extends PApplet {

    /*
     * this example demonstrates how to implement a mod tracker.
     */

    public void settings() {
        size(640, 480);
    }

    static final int   TRACKS       = 4;
    static final int   TRACK_LENGTH = 16;
    static final float TRACK_WIDTH  = 100;
    static final float TRACK_HEIGHT = 25;

    Track[]      fTracks;
    StepSelected fSelected;

    public void setup() {
        textFont(createFont("JetBrains Mono", 11));

        fTracks = new Track[TRACKS];
        for (int i = 0; i < TRACKS; i++) {
            fTracks[i] = new Track(i, TRACK_LENGTH);
        }
        fSelected = new StepSelected(fTracks);

        Beat.start(this, 120 * 4);
    }

    public void draw() {
        background(255);

        translate((width - TRACKS * TRACK_WIDTH) * 0.5f, (height - TRACK_LENGTH * TRACK_HEIGHT) * 0.5f);
        pushMatrix();
        for (int i = 0; i < fTracks.length; i++) {
            fTracks[i].draw(g);
            translate(fTracks[i].width, 0);
        }
        popMatrix();

        fSelected.draw(g);
    }

    public void beat(int beat) {
        for (int i = 0; i < TRACKS; i++) {
            fTracks[i].play(beat);
        }
    }

    static class StepSelected {
        int x;
        int y;

        Step copied_step;

        private final Track[] fTracks;

        StepSelected(Track[] pTracks) {
            fTracks     = pTracks;
            x           = 0;
            y           = 0;
            copied_step = new Step();
        }

        void copy() {
            copied_step.note     = fTracks[x].steps[y].note;
            copied_step.velocity = fTracks[x].steps[y].velocity;
        }

        void paste() {
            fTracks[x].steps[y].note     = copied_step.note;
            fTracks[x].steps[y].velocity = copied_step.velocity;
        }

        void left() {
            x--;
            if (x < 0) {
                x = fTracks.length - 1;
            }
        }

        void right() {
            x++;
            if (x >= fTracks.length) {
                x = 0;
            }
        }

        void up() {
            y--;
            if (y < 0) {
                y = fTracks[x].length - 1;
            }
        }

        void down() {
            y++;
            if (y >= fTracks[x].length) {
                y = 0;
            }
        }

        void draw(PGraphics g) {
            g.strokeWeight(4);
            g.stroke(0, 127, 255);
            g.noFill();
            g.rect(x * TRACK_WIDTH, y * TRACK_HEIGHT, TRACK_WIDTH, TRACK_HEIGHT);
            g.strokeWeight(1);
        }
    }

    public static class Step {
        int note     = 0;
        int velocity = 100;
    }

    public static class Track {
        Step[] steps;
        final int length;
        final int ID;
        int current_step = 0;

        float width  = TRACK_WIDTH;
        float height = TRACK_HEIGHT;

        Track(int pID, int pLength) {
            ID     = pID;
            length = pLength;
            steps  = new Step[length];
            for (int i = 0; i < length; i++) {
                steps[i] = new Step();
            }
        }

        void draw(PGraphics g) {
            for (int i = 0; i < length; i++) {
                /* frame */
                if (i == current_step) {
                    g.fill(91);
                } else {
                    g.fill(0);
                }
                g.stroke(255);
                g.rect(0, i * height, width, height);
                /* value */
                g.fill(255);
                final String mNoteName     = nf(steps[i].note, 3);
                final String mVelocityName = nf(steps[i].velocity, 3);
                final String mName         = mNoteName + " : " + mVelocityName;
                g.textAlign(CENTER, CENTER);
                g.text(mName, width * 0.5f, (i + 0.5f) * height);
            }
        }

        public void play(int beat) {
            current_step = beat % length;
            int mNote = steps[current_step].note;
            if (mNote != 0) {
                Tone.instrument(ID).note_on(mNote, steps[current_step].velocity);
            } else {
                Tone.instrument(ID).note_off();
            }
        }
    }

    public Step getSelectedStep() {
        return fTracks[fSelected.x].steps[fSelected.y];
    }

    private boolean fIsShiftPressed = false;

    public void keyPressed() {
        switch (keyCode) {
            case LEFT:
                if (fIsShiftPressed) {
                    getSelectedStep().velocity--;
                } else {
                    fSelected.left();
                }
                break;
            case RIGHT:
                if (fIsShiftPressed) {
                    getSelectedStep().velocity++;
                } else {
                    fSelected.right();
                }
                break;
            case UP:
                if (fIsShiftPressed) {
                    getSelectedStep().note++;
                } else {
                    fSelected.up();
                }
                break;
            case DOWN:
                if (fIsShiftPressed) {
                    getSelectedStep().note--;
                } else {
                    fSelected.down();
                }
                break;
            case BACKSPACE:
            case DELETE:
                getSelectedStep().note = 0;
                break;
            case SHIFT:
                fIsShiftPressed = true;
                break;
        }
        switch (key) {
            case ' ':
                getSelectedStep().note = 0;
                break;
            case 'c':
                fSelected.copy();
                break;
            case 'v':
                fSelected.paste();
                break;
            case 'r':
                for (Track mTrack : fTracks) {
                    for (Step mStep : mTrack.steps) {
                        mStep.note = random(1) > 0.75 ? 36 + (int) random(0, 24) : 0;
                    }
                }
                break;
            case 'R':
                for (Track mTrack : fTracks) {
                    for (Step mStep : mTrack.steps) {
                        mStep.velocity = (int) random(0, 120);
                    }
                }
                break;
            case 'q':
                for (Track mTrack : fTracks) {
                    for (Step mStep : mTrack.steps) {
                        mStep.note     = 0;
                        mStep.velocity = 100;
                    }
                }
                break;
        }
    }

    public void keyReleased() {
        if (keyCode == SHIFT) {
            fIsShiftPressed = false;
        }
    }

    public static void main(String[] args) {
        PApplet.main(TechniquePattern01ModTracker.class.getName());
    }
}
