package axoloti;

import axoloti.inlets.IInletInstanceView;
import axoloti.inlets.InletInstance;
import axoloti.mvc.AbstractController;
import axoloti.object.IAxoObjectInstance;
import axoloti.objectviews.IAxoObjectInstanceView;
import axoloti.outlets.IOutletInstanceView;
import axoloti.outlets.OutletInstance;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.QuadCurve2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class NetView extends JComponent implements INetView {

    protected ArrayList<IOutletInstanceView> source = new ArrayList<>();
    protected ArrayList<IInletInstanceView> dest = new ArrayList<>();
    protected Net net;
    protected boolean selected = false;

    NetController controller;
    PatchViewSwing patchView;

    NetView(Net net, NetController controller, PatchViewSwing patchView) {
        this.net = net;
        this.patchView = patchView;
        this.controller = controller;

        setOpaque(false);
        PostConstructor();
        updateBounds();
    }

    @Override
    public void PostConstructor() {
        source.clear();
        dest.clear();
        // resolve inlet/outlet views
        for (OutletInstance i : net.getSources()) {
            IAxoObjectInstance o = i.getObjectInstance();
            IAxoObjectInstanceView ov = patchView.getObjectInstanceView(o);
            if (ov == null) {
                throw new Error("no corresponding outlet instance view found");
            }
            for (IOutletInstanceView o2 : ov.getOutletInstanceViews()) {
                if (o2.getController().getModel() == i) {
                    source.add(o2);
                    break;
                }
            }
        }
        for (InletInstance i : net.getDestinations()) {
            IAxoObjectInstance o = i.getObjectInstance();
            IAxoObjectInstanceView ov = patchView.getObjectInstanceView(o);
            if (ov == null) {
                throw new Error("no corresponding inlet instance view found");
            }
            for (IInletInstanceView o2 : ov.getInletInstanceViews()) {
                if (o2.getController().getModel() == i) {
                    dest.add(o2);
                    break;
                }
            }
        }
    }

    @Override
    public void setSelected(boolean selected) {
        if (this.selected == selected) {
            return;
        }
        this.selected = selected;
        for (IOutletInstanceView i : source) {
            i.setHighlighted(selected);
        }
        for (IInletInstanceView i : dest) {
            i.setHighlighted(selected);
        }
        repaint();
    }

    @Override
    public boolean getSelected() {
        return selected;
    }

    final static float[] dash = {2.f, 4.f};
    final static Stroke strokeValidSelected = new BasicStroke(1.75f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    final static Stroke strokeValidDeselected = new BasicStroke(0.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    final static Stroke strokeBrokenSelected = new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0.f);
    final static Stroke strokeBrokenDeselected = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dash, 0.f);
    final QuadCurve2D.Float curve = new QuadCurve2D.Float();

    float CtrlPointY(float x1, float y1, float x2, float y2) {
        return Math.max(y1, y2) + Math.abs(y2 - y1) * 0.1f + Math.abs(x2 - x1) * 0.1f;
    }

    void DrawWire(Graphics2D g2, float x1, float y1, float x2, float y2) {
        curve.setCurve(x1, y1, (x1 + x2) / 2, CtrlPointY(x1, y1, x2, y2), x2, y2);
        g2.draw(curve);
    }

    @Override
    public void updateBounds() {
        int min_y = Integer.MAX_VALUE;
        int min_x = Integer.MAX_VALUE;
        int max_y = Integer.MIN_VALUE;
        int max_x = Integer.MIN_VALUE;

        for (IInletInstanceView i : dest) {
            if (i == null) {
                System.out.println("null");
            }
            Point p1 = i.getJackLocInCanvas();
            min_x = Math.min(min_x, p1.x);
            min_y = Math.min(min_y, p1.y);
            max_x = Math.max(max_x, p1.x);
            max_y = Math.max(max_y, p1.y);
        }
        for (IOutletInstanceView i : source) {
            if (i == null) {
                System.out.println("null");
            }
            Point p1 = i.getJackLocInCanvas();
            min_x = Math.min(min_x, p1.x);
            min_y = Math.min(min_y, p1.y);
            max_x = Math.max(max_x, p1.x);
            max_y = Math.max(max_y, p1.y);
        }
        int padding = 8;
        Rectangle pbounds = getBounds();
        setBounds(min_x - padding, min_y - padding,
                Math.max(1, max_x - min_x + (2 * padding)),
                (int) CtrlPointY(min_x, min_y, max_x, max_y) - min_y + (2 * padding));
        Rectangle b2 = pbounds.union(getBounds());
        if (getParent() != null) {
            // schedule repaint in 5ms
            getParent().repaint(5, b2.x, b2.y, b2.width, b2.height);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        paint1(g);
    }

    void paint1(Graphics g) {
        float shadowOffset = 0.5f;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        Point p0;
        Color c;
        if (net.isValidNet()) {
            if (selected) {
                g2.setStroke(strokeValidSelected);
            } else {
                g2.setStroke(strokeValidDeselected);
            }

            c = net.getDataType().GetColor();
            if (source.isEmpty()) {
                p0 = new Point(10, 10);
            } else {
                p0 = source.get(0).getJackLocInCanvas();
            }
        } else {
            if (selected) {
                g2.setStroke(strokeBrokenSelected);
            } else {
                g2.setStroke(strokeBrokenDeselected);
            }

            if (net.getDataType() != null) {
                c = net.getDataType().GetColor();
            } else {
                c = Theme.getCurrentTheme().Cable_Shadow;
            }

            if (!source.isEmpty()) {
                p0 = source.get(0).getJackLocInCanvas();
            } else if (!dest.isEmpty()) {
                p0 = dest.get(0).getJackLocInCanvas();
            } else {
                //throw new Error("empty nets should not exist");
                p0 = new Point(0, 0);
            }
        }

        Point from = SwingUtilities.convertPoint(patchView.Layers, p0, this);
        for (IInletInstanceView i : dest) {
            Point p1 = i.getJackLocInCanvas();

            Point to = SwingUtilities.convertPoint(patchView.Layers, p1, this);
            g2.setColor(Theme.getCurrentTheme().Cable_Shadow);
            DrawWire(g2, from.x + shadowOffset, from.y + shadowOffset, to.x + shadowOffset, to.y + shadowOffset);
            g2.setColor(c);
            DrawWire(g2, from.x, from.y, to.x, to.y);
        }
        for (IOutletInstanceView i : source) {
            Point p1 = i.getJackLocInCanvas();

            Point to = SwingUtilities.convertPoint(patchView.Layers, p1, this);
            g2.setColor(Theme.getCurrentTheme().Cable_Shadow);
            DrawWire(g2, from.x + shadowOffset, from.y + shadowOffset, to.x + shadowOffset, to.y + shadowOffset);
            g2.setColor(c);
            DrawWire(g2, from.x, from.y, to.x, to.y);

        }
    }

    @Override
    public Net getNet() {
        return net;
    }

    @Override
    public ArrayList<IOutletInstanceView> getSourceViews() {
        return source;
    }

    @Override
    public ArrayList<IInletInstanceView> getDestinationViews() {
        return dest;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt) {
        if (Net.NET_SOURCES.is(evt)
                || Net.NET_DESTINATIONS.is(evt)) {
            PostConstructor();
            updateBounds();
            repaint();
        }
    }

    @Override
    public AbstractController getController() {
        return controller;
    }

    @Override
    public void dispose() {
    }
}
