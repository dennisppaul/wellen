import de.hfkbremen.ton.*; 
import controlP5.*; 
import ddf.minim.*; 
import com.jsyn.unitgen.*; 


final ArrayList<Node> mNodes = new ArrayList();
Node mRoot;
Node mSelectedNode;
void settings() {
    size(1024, 768);
}
void setup() {
    Ton.instrument().osc_type(Instrument.SINE);
    mRoot = node(width / 2.0f, height / 2.0f, mNodes.size());
    Beat mBeat = new Beat(this);
    mBeat.bpm(120 * 2);
}
void draw() {
    background(255);
    if (mSelectedNode != null) {
        stroke(255, 127, 0);
        line(mSelectedNode.x, mSelectedNode.y, mouseX, mouseY);
    }
    for (Node mNode : mNodes) {
        mNode.drawConnections(g);
    }
    for (Node mNode : mNodes) {
        mNode.drawNode(g);
    }
}
void mousePressed() {
    mSelectedNode = within(mouseX, mouseY);
}
void mouseReleased() {
    if (mSelectedNode != null) {
        Node mNodeWithin = within(mouseX, mouseY);
        if (mNodeWithin == null) {
            Node mNewNode = node(mouseX, mouseY, mNodes.size());
            connect(mSelectedNode, mNewNode);
        } else if (mSelectedNode != mNodeWithin) {
            connect(mSelectedNode, mNodeWithin);
        }
        mSelectedNode = null;
    }
}
void keyPressed() {
    mRoot.schedule();
}
void beat(int pBeat) {
    for (Node mNode : mNodes) {
        mNode.update();
    }
    for (Node mNode : mNodes) {
        mNode.scheduled();
    }
}
void connect(Node a, Node b) {
    if (!a.children.contains(b)) {
        a.children.add(b);
    } else {
        a.children.remove(b);
    }
}
Node node(float x, float y, int pNote) {
    int mNote = Scale.note(Scale.MINOR_PENTATONIC, Note.NOTE_C3, pNote);
    Node n = new Node(x, y, mNote);
    mNodes.add(n);
    return n;
}
Node within(float pX, float pY) {
    for (Node mNode : mNodes) {
        float mDistance = dist(pX, pY, mNode.x, mNode.y);
        if (mDistance < mNode.radius) {
            return mNode;
        }
    }
    return null;
}
final class Node {
    float x;
    float y;
    float radius = 10;
    boolean scheduled = false;
    boolean trigger = false;
    boolean playing = false;
    ArrayList<Node> children = new ArrayList();
    int note;
    Node(float pX, float pY, int pNote) {
        xy(pX, pY);
        note = pNote;
    }
    void xy(float pX, float pY) {
        x = pX;
        y = pY;
    }
    void drawNode(PGraphics g) {
        g.stroke(0);
        if (playing) {
            g.fill(0);
        } else if (this == mSelectedNode) {
            g.fill(255, 127, 0);
        } else {
            g.fill(255);
        }
        g.ellipse(x, y, radius * 2, radius * 2);
    }
    void drawConnections(PGraphics g) {
        for (Node mNode : children) {
            g.beginShape(LINES);
            g.stroke(0, 192);
            g.vertex(x, y);
            g.stroke(0, 16);
            g.vertex(mNode.x, mNode.y);
            g.endShape();
        }
    }
    void update() {
        if (trigger) {
            trigger = false;
            playing = true;
            Ton.noteOn(note, 32);
            /* activate child */
            if (children.size() > 0) {
                int mChildID = (int) random(0, children.size());
                children.get(mChildID).scheduled = true;
            }
        } else {
            playing = false;
            Ton.noteOff();
        }
    }
    void scheduled() {
        if (scheduled) {
            scheduled = false;
            trigger = true;
        }
    }
    void schedule() {
        scheduled = true;
    }
}
