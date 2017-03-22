package axoloti;

import axoloti.inlets.IInletInstanceView;
import axoloti.object.AxoObjects;
import axoloti.objectviews.IAxoObjectInstanceView;
import axoloti.outlets.IOutletInstanceView;
import axoloti.piccolo.PObjectSearchFrame;
import axoloti.piccolo.PPatchBorder;
import static axoloti.piccolo.PUtils.asPoint;
import axoloti.piccolo.PatchPCanvas;
import axoloti.piccolo.PatchPNode;
import axoloti.utils.Constants;
import axoloti.utils.KeyUtils;
import components.piccolo.PFocusable;
import components.piccolo.control.PCtrlComponentAbstract;
import components.piccolo.control.PDropDownComponent;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import static javax.swing.TransferHandler.COPY_OR_MOVE;
import static javax.swing.TransferHandler.MOVE;
import org.piccolo2d.PCamera;
import org.piccolo2d.PNode;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class PatchViewPiccolo extends PatchView {

    private PatchPCanvas canvas;

    public PatchViewPiccolo(PatchController patchController) {
        super(patchController);
    }

    private PCtrlComponentAbstract focusedCtrl;

    public void setFocusedCtrl(PCtrlComponentAbstract ctrl) {
        focusedCtrl = ctrl;
        canvas.getScrollPane().setKeyActionsDisabled(ctrl != null);
    }

    public PBasicInputEventHandler inputEventHandler = new PBasicInputEventHandler() {
        @Override
        public void mousePressed(PInputEvent e) {
            e.getInputManager().setKeyboardFocus(this);
            setFocusedCtrl(null);
        }

        @Override
        public void mouseClicked(PInputEvent e) {
            if (e.isLeftMouseButton()) {
                if (e.getPickedNode() instanceof PCamera) {
                    if (e.getClickCount() == 2) {
                        ShowClassSelector(e, null, null);
                    } else if ((osf != null) && osf.isVisible()) {
                        osf.Accept();
                    } //                Layers.requestFocusInWindow();
                }
            } else if ((osf != null) && osf.isVisible()) {
                osf.Cancel();
            } //          Layers.requestFocusInWindow();
            e.setHandled(true);
        }

        @Override
        public void mouseWheelRotated(PInputEvent e) {
            if (e.isControlDown() || e.isMetaDown()) {
                canvas.getScrollPane().setWheelScrollingEnabled(false);
            }
        }

        @Override
        public void keyReleased(PInputEvent e) {
            if (KeyUtils.isControlOrCommand(e.getKeyCode())
                    && MainFrame.prefs.getMouseWheelPan()) {
                canvas.getScrollPane().setWheelScrollingEnabled(true);
            }
        }

        @Override
        public void keyPressed(PInputEvent e) {
            int xsteps = 1;
            int ysteps = 1;
            if (!e.isShiftDown()) {
                xsteps = Constants.X_GRID;
                ysteps = Constants.Y_GRID;
            }
            if ((e.getKeyCode() == KeyEvent.VK_SPACE)
                    || ((e.getKeyCode() == KeyEvent.VK_N) && !KeyUtils.isControlOrCommandDown(e))
                    || ((e.getKeyCode() == KeyEvent.VK_1) && KeyUtils.isControlOrCommandDown(e))) {
                e.setHandled(true);
                ShowClassSelector(e, null, null);
            } else if (((e.getKeyCode() == KeyEvent.VK_C) && !KeyUtils.isControlOrCommandDown(e))
                    || ((e.getKeyCode() == KeyEvent.VK_5) && KeyUtils.isControlOrCommandDown(e))) {
                Point patchPosition = asPoint(e.getInputManager().getCurrentCanvasPosition());
                getCanvas().getCamera().getViewTransform().inverseTransform(patchPosition, patchPosition);
                getPatchController().AddObjectInstance(
                        MainFrame.axoObjects.GetAxoObjectFromName(patchComment, null).get(0), patchPosition);
                e.setHandled(true);
            } else if ((e.getKeyCode() == KeyEvent.VK_I) && !KeyUtils.isControlOrCommandDown(e)) {
                e.setHandled(true);
                ShowClassSelector(e, null, patchInlet);
            } else if ((e.getKeyCode() == KeyEvent.VK_O) && !KeyUtils.isControlOrCommandDown(e)) {
                e.setHandled(true);
                ShowClassSelector(e, null, patchOutlet);
            } else if ((e.getKeyCode() == KeyEvent.VK_D) && !KeyUtils.isControlOrCommandDown(e)) {
                e.setHandled(true);
                ShowClassSelector(e, null, patchDisplay);
            } else if ((e.getKeyCode() == KeyEvent.VK_M) && !KeyUtils.isControlOrCommandDown(e)) {
                e.setHandled(true);
                if (e.isShiftDown()) {
                    ShowClassSelector(e, null, patchMidiKey);
                } else {
                    ShowClassSelector(e, null, patchMidi);
                }
            } else if ((e.getKeyCode() == KeyEvent.VK_A) && !KeyUtils.isControlOrCommandDown(e)) {
                e.setHandled(true);
                if (e.isShiftDown()) {
                    ShowClassSelector(e, null, patchAudioOut);
                } else {
                    ShowClassSelector(e, null, patchAudio);
                }
            } else if ((e.getKeyCode() == KeyEvent.VK_DELETE) || (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
                deleteSelectedAxoObjectInstanceViews();

                e.setHandled(true);
            } else if (e.getKeyCode() == KeyEvent.VK_UP) {
                MoveSelectedAxoObjInstances(Direction.UP, xsteps, ysteps);
                e.setHandled(true);
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                MoveSelectedAxoObjInstances(Direction.DOWN, xsteps, ysteps);
                e.setHandled(true);
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                MoveSelectedAxoObjInstances(Direction.RIGHT, xsteps, ysteps);
                e.setHandled(true);
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                MoveSelectedAxoObjInstances(Direction.LEFT, xsteps, ysteps);
                e.setHandled(true);
            }
        }
    };

    @Override
    public PatchViewportView getViewportView() {
        if (canvas == null) {
            canvas = new PatchPCanvas(this);

            canvas.addInputEventListener(inputEventHandler);
            canvas.setTransferHandler(TH);

            InputMap inputMap = canvas.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                    KeyUtils.CONTROL_OR_CMD_MASK), "cut");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                    KeyUtils.CONTROL_OR_CMD_MASK), "copy");
            inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                    KeyUtils.CONTROL_OR_CMD_MASK), "paste");

            ActionMap map = canvas.getActionMap();
            map.put(TransferHandler.getCutAction().getValue(Action.NAME),
                    TransferHandler.getCutAction());
            map.put(TransferHandler.getCopyAction().getValue(Action.NAME),
                    TransferHandler.getCopyAction());
            map.put(TransferHandler.getPasteAction().getValue(Action.NAME),
                    TransferHandler.getPasteAction());
            canvas.setEnabled(true);
            canvas.setFocusable(true);
            canvas.setFocusCycleRoot(true);
            canvas.setDropTarget(dt);
            canvas.getRoot().getDefaultInputManager().setKeyboardFocus(inputEventHandler);
        }
        return canvas;
    }

    TransferHandler TH = new TransferHandler() {
        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }

        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
            PatchModel p = getSelectedObjects();
            if (p.getObjectInstances().isEmpty()) {
                if (focusedCtrl != null) {
                    clip.setContents(new StringSelection(Double.toString(focusedCtrl.getValue())), (ClipboardOwner) null);
                } else {
                    clip.setContents(new StringSelection(""), null);
                }
                return;
            }
            Serializer serializer = new Persister();
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                serializer.write(p, baos);
                StringSelection s = new StringSelection(baos.toString());
                clip.setContents(s, (ClipboardOwner) null);
            } catch (Exception ex) {
                Logger.getLogger(AxoObjects.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (action == MOVE) {
                deleteSelectedAxoObjectInstanceViews();
            }
        }

        @Override
        public boolean importData(TransferHandler.TransferSupport support) {
            return super.importData(support);
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                if (!isLocked()) {
                    if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                        String transferData = (String) t.getTransferData(DataFlavor.stringFlavor);
                        if (focusedCtrl != null) {
                            try {
                                focusedCtrl.setValue(Double.parseDouble(transferData));
                                return true;
                            } catch (NumberFormatException e) {
                                // fall through to paste
                            }
                        }
                        paste((String) t.getTransferData(DataFlavor.stringFlavor), comp.getMousePosition(), false);
                    }
                }
            } catch (UnsupportedFlavorException ex) {
                Logger.getLogger(PatchViewSwing.class.getName()).log(Level.SEVERE, "paste", ex);
            } catch (IOException ex) {
                Logger.getLogger(PatchViewSwing.class.getName()).log(Level.SEVERE, "paste", ex);
            }
            return true;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return new StringSelection("copy");
        }

        @Override
        public boolean canImport(TransferHandler.TransferSupport support) {
            boolean r = super.canImport(support);
            return r;
        }

    };

    @Override
    public void repaint() {
        // no need to explicitly repaint on model change
    }

    @Override
    public Point getLocationOnScreen() {
        return getCanvas().getLocationOnScreen();
    }

    @Override
    public void PostConstructor() {
        getPatchController().patchModel.PostContructor();
        modelChanged(false);
        getPatchController().patchModel.PromoteOverloading(true);
        ShowPreset(0);
        SelectNone();
        getPatchController().patchModel.pushUndoState(false);
    }

    @Override
    public void SelectNone() {
        canvas.getSelectionEventHandler().unselectAll();
    }

    @Override
    public void requestFocus() {

    }

    private PPatchBorder patchBorder;

    @Override
    public void AdjustSize() {

    }

    @Override
    public void Close() {
        super.Close();
    }

    private boolean cordsInBackground = false;

    public void updateNetZPosition() {
        if (cordsInBackground) {
            lowerNetsToBottom();
        } else {
            raiseNetsToTop();
        }
    }

    @Override
    void setCordsInBackground(boolean cordsInBackground) {
        this.cordsInBackground = cordsInBackground;
        updateNetZPosition();
    }

    private void raiseNetsToTop() {
        for (INetView netView : netViews) {
            ((PNode) netView).raiseToTop();
        }
    }

    private void lowerNetsToBottom() {
        for (INetView netView : netViews) {
            ((PNode) netView).lowerToBottom();
        }
    }

    public void addFocusables(ListIterator<PNode> childrenIterator) {
        while (childrenIterator.hasNext()) {
            PNode child = childrenIterator.next();
            addFocusables(child.getChildrenIterator());
            if (child instanceof PFocusable
                    && !(child instanceof PDropDownComponent)) {
                addFocusable((PFocusable) child);
            }
        }
    }

    public void removeFocusables(ListIterator<PNode> childrenIterator) {
        while (childrenIterator.hasNext()) {
            PNode child = childrenIterator.next();
            removeFocusables(child.getChildrenIterator());
            if (child instanceof PFocusable) {
                removeFocusable((PFocusable) child);
            }
        }
    }

    public void add(IAxoObjectInstanceView v) {
        PatchPNode node = (PatchPNode) v;
        getCanvas().getLayer().addChild(node);
        objectInstanceViews.add(v);
        addFocusables(node.getChildrenIterator());
    }

    public void remove(IAxoObjectInstanceView v) {
        PatchPNode node = (PatchPNode) v;
        getCanvas().getLayer().removeChild(node);
        objectInstanceViews.remove(v);
        removeFocusables(node.getChildrenIterator());
    }

    public void removeAllObjectViews() {
        for (IAxoObjectInstanceView objectView : objectInstanceViews) {
            getCanvas().getLayer().removeChild((PatchPNode) objectView);
        }
        objectInstanceViews.clear();
    }

    public void removeAllNetViews() {
        for (INetView netView : netViews) {
            getCanvas().getLayer().removeChild((PatchPNode) netView);
            for (IInletInstanceView iiv : netView.getDestinationViews()) {
                iiv.repaint();
            }
            for (IOutletInstanceView oiv : netView.getSourceViews()) {
                oiv.repaint();
            }
        }
        netViews.clear();
    }

    public void add(INetView v) {
        netViews.add(v);
        PatchPNode node = (PatchPNode) v;
        getCanvas().getLayer().addChild(node);
//        node.setVisible(false);
        if (cordsInBackground) {
            node.lowerToBottom();
        } else {
            node.raiseToTop();
        }
    }

    public void validate() {
        getCanvas().validate();
    }

    public void validateObjects() {
        for (IAxoObjectInstanceView objectInstanceView : objectInstanceViews) {
            objectInstanceView.validate();
        }
    }

    public void validateNets() {
        for (INetView netView : netViews) {
            netView.validate();
        }
    }

    public PatchPCanvas getCanvas() {
        return (PatchPCanvas) getViewportView();
    }

    public void ShowClassSelector(PInputEvent e, IAxoObjectInstanceView o, String searchString) {
        try {
            Point2D p = e.getPosition();
            Point2D q = e.getCanvasPosition();
            ShowClassSelector(asPoint(e.getPosition()), asPoint(e.getCanvasPosition()), o, searchString);
        } catch (RuntimeException ex) {
            // if this is from a keyboard event
            Point canvasPosition = asPoint(e.getInputManager().getCurrentCanvasPosition());
            Point patchPosition = (Point) canvasPosition.clone();
            getCanvas().getCamera().getViewTransform().inverseTransform(patchPosition, patchPosition);
            ShowClassSelector(patchPosition, canvasPosition, o, searchString);
        }
    }

    public void ShowClassSelector(Point patchPosition, Point canvasPosition, IAxoObjectInstanceView o, String searchString) {
        if (isLocked()) {
            return;
        }
        if (canvasPosition == null) {
            canvasPosition = asPoint(getCanvas().getRoot().getDefaultInputManager().getCurrentCanvasPosition());
        }
        if (osf == null) {
            osf = new PObjectSearchFrame(getPatchController());
        }
        osf.Launch(patchPosition, o, searchString);
        Point ps = getPatchController().getViewLocationOnScreen();
        Point patchLocClipped = osf.clipToStayWithinScreen(canvasPosition);
        osf.setLocation(patchLocClipped.x + ps.x, patchLocClipped.y + ps.y);
        osf.setVisible(true);
    }

    @Override
    void paste(String v, Point pos, boolean restoreConnectionsToExternalOutlets) {
        SelectNone();
        getCanvas().getCamera().getViewTransform().inverseTransform(pos, pos);
        getPatchController().paste(v,
                pos,
                restoreConnectionsToExternalOutlets);
    }

    private final List<PFocusable> focusables = new ArrayList<PFocusable>();
    private int focusableIndex = 0;

    public void addFocusable(PFocusable focusable) {
        focusables.add(focusable);
        focusable.setFocusableIndex(focusableIndex);
        focusableIndex += 1;
    }

    public void removeFocusable(PFocusable focusable) {
        try {
            focusables.remove(focusable.getFocusableIndex());
        } catch (IndexOutOfBoundsException e) {

        }
        int focusableCount = focusables.size();
        for (int i = focusable.getFocusableIndex(); i < focusableCount; i++) {
            focusables.get(i).setFocusableIndex(i);
        }
        focusableIndex = focusableCount;
    }

    public void transferFocus(PFocusable focusable) {
        focusables.get((focusable.getFocusableIndex() + 1) % focusables.size()).grabFocus();
    }

    @Override
    public void Lock() {
        super.Lock();
        canvas.setBackground(Theme.getCurrentTheme().Patch_Locked_Background);
    }

    @Override
    public void Unlock() {
        super.Unlock();
        canvas.setBackground(Theme.getCurrentTheme().Patch_Unlocked_Background);
    }
}
