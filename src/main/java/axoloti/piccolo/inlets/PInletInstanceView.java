package axoloti.piccolo.inlets;

import axoloti.INetView;
import axoloti.inlets.IInletInstanceView;
import axoloti.inlets.InletInstance;
import axoloti.inlets.InletInstanceController;
import axoloti.inlets.InletInstancePopupMenu;
import axoloti.objectviews.IAxoObjectInstanceView;
import axoloti.piccolo.iolet.PIoletAbstract;
import components.piccolo.PJackInputComponent;
import components.piccolo.PLabelComponent;
import components.piccolo.PSignalMetaDataIcon;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPopupMenu;
import org.piccolo2d.event.PBasicInputEventHandler;
import org.piccolo2d.event.PInputEvent;

public class PInletInstanceView extends PIoletAbstract implements IInletInstanceView {

    InletInstancePopupMenu popup;
    InletInstance inletInstance;
    InletInstanceController controller;

    public PInletInstanceView(InletInstance inletInstance, IAxoObjectInstanceView axoObjectInstanceView) {
        super(axoObjectInstanceView);
        this.inletInstance = inletInstance;

        popup = new InletInstancePopupMenu(getController());
    }

    private final PBasicInputEventHandler toolTipEventListener = new PBasicInputEventHandler() {
        @Override
        public void mouseEntered(PInputEvent e) {
            if (e.getInputManager().getMouseFocus() == null) {
                axoObjectInstanceView.getCanvas().setToolTipText(inletInstance.getModel().getDescription());
            }
        }

        @Override
        public void mouseExited(PInputEvent e) {
            if (e.getInputManager().getMouseFocus() == null) {
                axoObjectInstanceView.getCanvas().setToolTipText(null);
            }
        }
    };

    @Override
    public void PostConstructor() {
        setLayout(new BoxLayout(getProxyComponent(), BoxLayout.LINE_AXIS));
        setMaximumSize(new Dimension(32767, 14));

        jack = new PJackInputComponent(this);
        ((PJackInputComponent) jack).setForeground(inletInstance.getModel().getDatatype().GetColor());

        addChild(jack);
        addChild(new PSignalMetaDataIcon(inletInstance.getModel().GetSignalMetaData(), axoObjectInstanceView));

        if (axoObjectInstanceView.getModel().getType().getInlets().size() > 1) {
            addToSwingProxy(Box.createHorizontalStrut(3));
            addChild(new PLabelComponent(inletInstance.getModel().getName()));
        }
        addToSwingProxy(Box.createHorizontalGlue());

        addInputEventListener(getInputEventHandler());
        addInputEventListener(toolTipEventListener);
    }

    @Override
    public InletInstance getModel() {
        return inletInstance;
    }

    public void setHighlighted(boolean highlighted) {
        if (axoObjectInstanceView != null
                && axoObjectInstanceView.getPatchView() != null) {
            INetView netView = axoObjectInstanceView.getPatchView().GetNetView((IInletInstanceView) this);
            if (netView != null
                    && netView.getSelected() != highlighted) {
                netView.setSelected(highlighted);
            }
        }
    }

    @Override
    public JPopupMenu getPopup() {
        return popup;
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public InletInstanceController getController() {
        return controller;
    }

    @Override
    public void dispose() {
    }
}
