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
package axoloti.parameters;

import axoloti.datatypes.ValueInt32;
import axoloti.property.Property;
import java.security.MessageDigest;
import java.util.List;
import org.simpleframework.xml.Element;

/**
 *
 * @author Johannes Taelman
 */
public class ParameterInt32Box extends ParameterInt32<ParameterInstanceInt32Box> {

    @Element
    public ValueInt32 MinValue;
    @Element
    public ValueInt32 MaxValue;
    


    public ParameterInt32Box() {
        this.MinValue = new ValueInt32(0);
        this.MaxValue = new ValueInt32(64);
    }

    public ParameterInt32Box(String name, int MinValue, int MaxValue) {
        super(name);
        this.MinValue = new ValueInt32(MinValue);
        this.MaxValue = new ValueInt32(MaxValue);
    }

    @Override
    public ParameterInstanceInt32Box InstanceFactory() {
        ParameterInstanceInt32Box b = new ParameterInstanceInt32Box();
        return b;
    }

    @Override
    public void updateSHA(MessageDigest md) {
        super.updateSHA(md);
        md.update("int32.dial".getBytes());
    }

    static public final String TypeName = "int32";

    @Override
    public String getTypeName() {
        return TypeName;
    }

    @Override
    public List<Property> getEditableFields() {
        List l = super.getEditableFields();
        l.add(VALUE_MIN);
        l.add(VALUE_MAX);
        return l;
    }

    @Override
    public String GetCType() {
        return "param_type_int";
    }

    @Override
    public Integer getMinValue() {
        return MinValue.getInt();
    }

    @Override
    public void setMinValue(Integer minValue) {
        this.MinValue = new ValueInt32(minValue);
        firePropertyChange(VALUE_MIN, null, minValue);
    }

    @Override
    public Integer getMaxValue() {
        return MaxValue.getInt();
    }

    @Override
    public void setMaxValue(Integer max) {
        this.MaxValue = new ValueInt32(max);
        firePropertyChange(VALUE_MAX, null, max);
    }

}
