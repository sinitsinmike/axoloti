package axoloti.attributeviews;

import axoloti.PatchView;
import axoloti.Theme;
import axoloti.atom.AtomInstanceView;
import axoloti.attribute.AttributeInstance;
import axoloti.objectviews.AxoObjectInstanceView;
import components.LabelComponent;
import javax.swing.BoxLayout;

public abstract class AttributeInstanceView extends AtomInstanceView {

    AxoObjectInstanceView axoObjectInstanceView;
    PatchView patchView;

    AttributeInstance attributeInstance;

    AttributeInstanceView(AttributeInstance attributeInstance, AxoObjectInstanceView axoObjectInstanceView) {
        this.attributeInstance = attributeInstance;
        this.axoObjectInstanceView = axoObjectInstanceView;

    }

    public abstract void Lock();

    public abstract void UnLock();

    public void PostConstructor() {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        setBackground(Theme.getCurrentTheme().Object_Default_Background);
        add(new LabelComponent(attributeInstance.getDefinition().getName()));
        setSize(getPreferredSize());
    }

    @Override
    public String getName() {
        if (attributeInstance != null) {
            return attributeInstance.getAttributeName();
        } else {
            return super.getName();
        }
    }

    public PatchView getPatchView() {
        return axoObjectInstanceView.getPatchView();
    }

    public AttributeInstance getAttributeInstance() {
        return this.attributeInstance;
    }
}
