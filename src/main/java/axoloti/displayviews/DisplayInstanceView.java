package axoloti.displayviews;

import axoloti.ModelChangedListener;
import axoloti.displays.DisplayInstance;
import components.LabelComponent;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

public abstract class DisplayInstanceView extends JPanel implements ModelChangedListener {

    DisplayInstance displayInstance;

    DisplayInstanceView(DisplayInstance displayInstance) {
        this.displayInstance = displayInstance;
    }

    public void PostConstructor() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        if ((displayInstance.getDefinition().noLabel == null) || (displayInstance.getDefinition().noLabel == false)) {
            add(new LabelComponent(displayInstance.getDefinition().getName()));
        }
        setSize(getPreferredSize());
    }

    public abstract void updateV();

    public void modelChanged() {
        updateV();
    }
}
