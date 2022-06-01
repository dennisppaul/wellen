import wellen.*; 

/*
 * this example demonstrates how to use a sampler with loop in- and out-points. the loop-points specify a region
 * in the sample buffer that loops once the sample playback is started with `start()` until `stop()` is called.
 * after this call the remaining region between loop out-point and the rest of the sample buffer is processed.
 *
 * note that *global* in- and out-points can be defined with `set_in()` + `set_out()`.
 */

Sampler mSampler;

static final float BORDER = 32;

void settings() {
    size(640, 480);
}

void setup() {
    byte[] mData = loadBytes("../../../resources/hello.raw");
    mSampler = new Sampler();
    mSampler.load(mData);
    mSampler.forward();
    DSP.start(this);
}

void draw() {
    background(255);
    translate(BORDER, BORDER);
    scale((width - BORDER * 2.0f) / width, (height - BORDER * 2.0f) / height);
    /* backdrop */
    noStroke();
    fill(255, 127, 0, 63);
    rect(0, 0, width, height);
    /* selection */
    float x0 = map(mSampler.get_loop_in(), 0, mSampler.data().length, 0, width);
    float x1 = map(mSampler.get_loop_out(), 0, mSampler.data().length, 0, width);
    if (mSampler.get_loop_in() >= 0 && mSampler.get_loop_out() >= 0) {
        if (mSampler.get_loop_in() < mSampler.get_loop_out()) {
            fill(255, 127);
            noStroke();
            beginShape();
            vertex(x0, 0);
            vertex(x1, 0);
            vertex(x1, height);
            vertex(x0, height);
            endShape();
        }
    }
    stroke(255);
    line(x0, 0, x0, height);
    line(x1, 0, x1, height);
    /* samples */
    noFill();
    stroke(255, 127, 0);
    beginShape();
    for (int i = 0; i < mSampler.data().length; i++) {
        float x = map(i, 0, mSampler.data().length, 0, width);
        float y = map(mSampler.data()[i], -1.0f, 1.0f, 0, height);
        vertex(x, y);
    }
    endShape();
    /* draw audio buffer */
    fill(255, 127, 0, 63);
    noStroke();
    scale(0.1f);
    rect(0, 0, width, height);
    stroke(0);
    noFill();
    DSP.draw_buffer(g, width, height);
}

void mousePressed() {
    mSampler.rewind();
    mSampler.start();
}

void mouseReleased() {
    mSampler.stop();
}

void keyPressed() {
    switch (key) {
        case '1':
            mSampler.set_loop_in((int) map(mouseX, BORDER, width - BORDER, 0, mSampler.data().length - 1));
            break;
        case '2':
            mSampler.set_loop_out((int) map(mouseX, BORDER, width - BORDER, 0, mSampler.data().length - 1));
            break;
        case 'z':
            int[] mLoopPoints = Wellen.find_zero_crossings(mSampler.data(),
                                                           mSampler.get_loop_in(),
                                                           mSampler.get_loop_out());
            if (mLoopPoints[0] > 0 && mLoopPoints[1] > 0) {
                mSampler.set_loop_in(mLoopPoints[0]);
                mSampler.set_loop_out(mLoopPoints[1]);
            }
            break;
        case ' ':
            mSampler.set_loop_in(Sampler.NO_LOOP_POINT);
            mSampler.set_loop_out(Sampler.NO_LOOP_POINT);
            break;
        case 'd':
            mSampler.set_speed(mSampler.get_speed() * -1);
            break;
    }
}

void audioblock(float[] pOutputSignal) {
    for (int i = 0; i < pOutputSignal.length; i++) {
        pOutputSignal[i] = mSampler.output();
    }
}
