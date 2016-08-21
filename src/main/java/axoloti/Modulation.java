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
package axoloti;

import axoloti.datatypes.ValueFrac32;
import axoloti.object.AxoObjectInstanceAbstract;
import axoloti.parameters.ParameterInstanceFrac32;
import java.util.ArrayList;
import org.simpleframework.xml.Attribute;

/**
 *
 * @author Johannes Taelman
 */
public class Modulation {

    @Attribute
    public String sourceName; // object instance name

    @Attribute(required = false)
    public String modName; // name of modulation (can be null or empty)

    @Attribute(name = "value", required = false)
    public double getValuex() {
        return getValue().getDouble();
    }

    private ValueFrac32 value = new ValueFrac32();

    public Modulation(@Attribute(name = "value") double v) {
        value.setDouble(v);
    }

    public Modulation() {
    }

    public ValueFrac32 getValue() {
        return value;
    }

    public void PostConstructor(ParameterInstanceFrac32 p) {
        System.out.println("Modulation postconstructor");
        setDestination(p);
        setSource(p.GetObjectInstance().patch.GetObjectInstance(sourceName));
        if (getSource() == null) {
            System.out.println("modulation source missing!");
        } else {
            System.out.println("modulation source found " + getSource().getInstanceName());
        }
        Modulator m = null;
        for (Modulator m1 : p.GetObjectInstance().patch.getModulators()) {
            System.out.println("modulator match? " + m1.objinst.getInstanceName());
            if (m1.objinst == getSource()) {
                if ((m1.name != null) && (!m1.name.isEmpty())) {
                    if (m1.name.equals(modName)) {
                        m = m1;
                    }
                } else {
                    m = m1;
                }
            }
        }
        if (m == null) {
            System.out.println("Modulation source missing");
        } else {
            if (m.Modulations == null) {
                m.Modulations = new ArrayList<Modulation>();
            }
            if (!m.Modulations.contains(this)) {
                m.Modulations.add(this);
            }
        }
    }
    private AxoObjectInstanceAbstract source;
    private ParameterInstanceFrac32 destination;

    public AxoObjectInstanceAbstract getSource() {
        return source;
    }

    public ParameterInstanceFrac32 getDestination() {
        return destination;
    }

    public void setValue(ValueFrac32 value) {
        this.value = value;
    }

    public void setSource(AxoObjectInstanceAbstract source) {
        this.source = source;
    }

    public void setDestination(ParameterInstanceFrac32 destination) {
        this.destination = destination;
    }
}
