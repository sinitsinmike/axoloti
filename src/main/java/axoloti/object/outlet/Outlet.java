/**
 * Copyright (C) 2013, 2014 Johannes Taelman
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
package axoloti.object.outlet;

import axoloti.object.atom.AtomDefinition;
import axoloti.datatypes.DataType;
import axoloti.datatypes.SignalMetaData;
import axoloti.property.Property;
import axoloti.utils.CharEscape;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import org.simpleframework.xml.Attribute;

/**
 *
 * @author Johannes Taelman
 */
public abstract class Outlet extends AtomDefinition implements Cloneable {

    @Deprecated
    @Attribute(required = false)
    Boolean SumBuffer;

    public DataType getDatatype() {
        return null;
    }

    public Outlet() {
    }

    public Outlet(String name, String description) {
        super(name, description);
    }

    @Override
    public String toString() {
        return getTypeName();
    }

    public String GetCName() {
        return "outlet_" + CharEscape.CharEscape(getName());
    }

    public SignalMetaData GetSignalMetaData() {
        return SignalMetaData.none;
    }

    public void updateSHA(MessageDigest md) {
        md.update(getName().getBytes());
        md.update((byte) getDatatype().hashCode());
    }

    @Override
    public Outlet clone() throws CloneNotSupportedException {
        return (Outlet) super.clone();
    }

    @Override
    public List<Property> getEditableFields() {
        return new ArrayList<>();
    }
}