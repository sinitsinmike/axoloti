package components.piccolo.control;

import axoloti.Theme;
import axoloti.objectviews.IAxoObjectInstanceView;
import axoloti.piccolo.PUtils;
import axoloti.utils.KeyUtils;
import components.piccolo.PActionListener;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import org.piccolo2d.event.PInputEvent;
import org.piccolo2d.util.PPaintContext;

public class PCheckboxComponent extends PCtrlComponentAbstract {

    private double value;
    private final int n;
    private final int bsize = 12;
    private int selIndex = -1;

    public PCheckboxComponent(int value, int n, IAxoObjectInstanceView axoObjectInstanceView) {
        super(axoObjectInstanceView);
        this.value = 0;//value;
        this.n = n;

        Dimension d = new Dimension(bsize * n + 2, bsize + 2);
        setMinimumSize(d);
        setMaximumSize(d);
        setPreferredSize(d);
        setSize(d);
    }

    private boolean dragAction = false;
    private boolean dragValue = false;

    private void SetFieldValue(int position, boolean val) {
        if (position >= n) {
            return;
        }
        if (position < 0) {
            return;
        }
        int mask = 1 << position;
        int v = (int) value;
        if (val) {
            v = v | mask;
        } else {
            v = v & ~mask;
        }
        setValue((double) v);
        notifyActionListeners();
    }

    private boolean GetFieldValue(int position) {
        return (((int) value) & (1 << position)) != 0;
    }

    @Override
    protected void mouseDragged(PInputEvent e) {
        if (dragAction) {
            Point localPosition = PUtils.asPoint(e.getPositionRelativeTo(this));
            int i = localPosition.x / bsize;
            if (i < n) {
                SetFieldValue(i, dragValue);
                selIndex = i;
                repaint();
            }
            e.setHandled(true);
        }
    }

    @Override
    protected void mousePressed(PInputEvent e) {
        if (!e.isPopupTrigger()) {

            grabFocus();
            if (e.getButton() == 1) {
                Point localPosition = PUtils.asPoint(e.getPositionRelativeTo(this));
                int i = localPosition.x / bsize;
                if (i < n) {
                    fireEventAdjustmentBegin();
                    dragAction = true;
                    if (e.isShiftDown()) {
                        dragValue = GetFieldValue(i);
                    } else {
                        dragValue = !GetFieldValue(i);
                    }
                    SetFieldValue(i, dragValue);
                    selIndex = i;
                }
            }
            e.setHandled(true);
        }
    }

    @Override
    protected void mouseReleased(PInputEvent e) {
        if (!e.isPopupTrigger()) {
            fireEventAdjustmentFinished();
            e.setHandled(true);
        }
        dragAction = false;
    }

    @Override
    public void keyPressed(PInputEvent ke) {
        if (KeyUtils.isIgnoreModifierDown(ke)) {
            return;
        }
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_LEFT: {
                selIndex = selIndex - 1;
                if (selIndex < 0) {
                    selIndex = n - 1;
                }
                repaint();
                ke.setHandled(true);
                return;
            }
            case KeyEvent.VK_RIGHT: {
                selIndex = selIndex + 1;
                if (selIndex >= n) {
                    selIndex = 0;
                }
                repaint();
                ke.setHandled(dragValue);
                return;
            }
            case KeyEvent.VK_UP: {
                fireEventAdjustmentBegin();
                SetFieldValue(selIndex, true);
                fireEventAdjustmentFinished();
                ke.setHandled(true);
                return;
            }
            case KeyEvent.VK_DOWN: {
                fireEventAdjustmentBegin();
                SetFieldValue(selIndex, false);
                fireEventAdjustmentFinished();
                ke.setHandled(true);
                return;
            }
            case KeyEvent.VK_PAGE_UP: {
                fireEventAdjustmentBegin();
                SetFieldValue(selIndex, true);
                fireEventAdjustmentFinished();
                ke.setHandled(true);
                return;
            }
            case KeyEvent.VK_PAGE_DOWN: {
                fireEventAdjustmentBegin();
                SetFieldValue(selIndex, false);
                fireEventAdjustmentFinished();
                ke.setHandled(true);
                return;
            }
        }
        switch (ke.getKeyChar()) {
            case '0':
                fireEventAdjustmentBegin();
                SetFieldValue(selIndex, false);
                fireEventAdjustmentFinished();
                ke.setHandled(true);
                break;
            case '1':
                fireEventAdjustmentBegin();
                SetFieldValue(selIndex, true);
                fireEventAdjustmentFinished();
                ke.setHandled(true);
                break;
            case ' ':
                fireEventAdjustmentBegin();
                SetFieldValue(selIndex, !GetFieldValue(selIndex));
                fireEventAdjustmentFinished();
                ke.setHandled(true);
                break;
        }
    }

    private static final Stroke strokeThin = new BasicStroke(1);
    private static final Stroke strokeThick = new BasicStroke(2);

    @Override
    protected void paint(PPaintContext paintContext) {
        Graphics2D g2 = paintContext.getGraphics();
        g2.setStroke(strokeThin);
        if (isEnabled()) {
            g2.setColor(Theme.getCurrentTheme().Component_Secondary);
        } else {
            g2.setColor(Theme.getCurrentTheme().Object_Default_Background);
        }
        g2.fillRect(0, 0, bsize * n, bsize + 1);
        g2.setPaint(getForeground());

        if (isFocusOwner()) {
            g2.setStroke(strokeThick);
            g2.drawRect(0, 0, bsize * n + 0, bsize + 1);
            g2.drawRect(selIndex * bsize, 0, bsize, bsize + 1);
        } else {
            g2.drawRect(0, 0, bsize * n, bsize + 1);
        }

        g2.setStroke(strokeThin);
        for (int i = 1; i < n; i++) {
            g2.drawLine(bsize * i, 0, bsize * i, bsize + 1);
        }

        if (isEnabled()) {
            int v = (int) value;
            int inset = 2;
            for (int i = 0; i < n; i++) {
                if ((v & 1) != 0) {
                    g2.fillRect(i * bsize + inset, inset, bsize - inset - 1, bsize - inset);
                }
                v = v >> 1;
            }
        }
    }

    @Override
    public void setValue(double value) {
        if (this.value != value) {
            this.value = value;
            getParent().repaint();
        }
        fireEvent();
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    void keyReleased(PInputEvent ke) {
    }

    private String callbackData;

    public void setCallbackData(String data) {
        callbackData = data;
    }

    private final List<PActionListener> actionListeners = new ArrayList<PActionListener>();

    public void addPActionListener(PActionListener actionListener) {
        actionListeners.add(actionListener);
    }

    private void notifyActionListeners() {
        for (PActionListener al : actionListeners) {
            al.actionPerformed(callbackData + " " + (this.value == 1));
        }
    }
}
