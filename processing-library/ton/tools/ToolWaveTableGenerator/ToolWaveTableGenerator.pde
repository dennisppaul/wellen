import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


void settings() {
}
void setup() {
    final int SAMPLES = 2048;
    final int RANGE = 512;
    for (int i=0; i <= SAMPLES; i++) {
        print(floor(sin((float)i / SAMPLES * 2 * PI) * RANGE) + ", ");
    }
}
void draw() {
}
