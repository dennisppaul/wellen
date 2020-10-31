package de.hfkbremen.ton.tools;

import processing.core.PApplet;

/*
@TODO
- write to text file
- supply some default functions ( sine, triangle, … )
- adjust samples + range + signedness
- visualize
 */

public class ToolWaveTableGenerator extends PApplet {

    public void settings() {
    }

    public void setup() {
        final int SAMPLES = 2048;
        final int RANGE = 512;

        for (int i=0; i <= SAMPLES; i++) {
            print(floor(sin((float)i / SAMPLES * 2 * PI) * RANGE) + ", ");
        }
    }

    public void draw() {
    }

    public static void main(String[] args) {
        PApplet.main(ToolWaveTableGenerator.class.getName());
    }
}
