/**
 * Copyright (C) 2013 - 2016 Johannes Taelman
 *
 * This file is part of Axoloti.
 *
 * Axoloti is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Axoloti is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Axoloti. If not, see <http://www.gnu.org/licenses/>.
 */
package axoloti.attribute;

import axoloti.atom.AtomDefinitionController;
import axoloti.attributedefinition.AxoAttributeComboBox;
import axoloti.object.AxoObjectInstance;
import java.beans.PropertyChangeEvent;
import org.simpleframework.xml.Attribute;

/**
 *
 * @author Johannes Taelman
 */
public class AttributeInstanceComboBox extends AttributeInstanceString<AxoAttributeComboBox> {

    @Attribute(name = "selection", required = false)
    String selection;

    AttributeInstanceComboBox() {
    }

    AttributeInstanceComboBox(AtomDefinitionController controller, AxoObjectInstance axoObj1) {
        super(controller, axoObj1);
    }

    @Override
    public String CValue() {
        if (getModel().getCEntries().isEmpty()) {
            return "";
        }
        String s = getModel().getCEntries().get(getSelectedIndex());
        if (s != null) {
            return s;
        } else {
            return "";
        }
    }

    @Override
    public String getValue() {
        if (selection == null) {
            return "";
        }
        return selection;
    }

    @Override
    public void setValue(String selection) {
        String oldvalue = this.selection;
        if (getModel().getMenuEntries().isEmpty()) {
            // no menu entries present
            this.selection = null;
        } else {
            int selectedIndex = getIndex(selection);
            selection = getModel().getMenuEntries().get(selectedIndex);
            this.selection = selection;
        }
        firePropertyChange(
                ATTR_VALUE,
                oldvalue, this.selection);
    }

    public int getIndex(String selection) {
        int selectedIndex = 0;
        if (selection == null) {
            return 0;
        }
        for (int i = 0; i < getModel().getMenuEntries().size(); i++) {
            if (selection.equals(getModel().getMenuEntries().get(i))) {
                selectedIndex = i;
                break;
            }
        }
        return selectedIndex;
    }

    public int getSelectedIndex() {
        return getIndex(selection);
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt) {
        super.modelPropertyChange(evt);
        if (AxoAttributeComboBox.ATOM_CENTRIES.is(evt)) {
            firePropertyChange(AxoAttributeComboBox.ATOM_CENTRIES, evt.getOldValue(), evt.getNewValue());
        } else if (AxoAttributeComboBox.ATOM_MENUENTRIES.is(evt)) {
            firePropertyChange(AxoAttributeComboBox.ATOM_MENUENTRIES, evt.getOldValue(), evt.getNewValue());
        }
    }

}
