package app.visuals;

import java.awt.Graphics;

public interface IVisual {
    int process(double[] fftResults, Graphics g); // returns particle/element count
}
