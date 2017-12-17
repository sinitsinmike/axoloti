package axoloti.property;

import axoloti.datatypes.ValueInt32;

/**
 *
 * @author jtaelman
 */
public class Int32Property extends PropertyReadWrite<ValueInt32> {

    public Int32Property(String name, Class containerClass, String friendlyName) {
        super(name, ValueInt32.class, containerClass, friendlyName);
    }

    @Override
    public Class getType() {
        return ValueInt32.class;
    }

    @Override
    public boolean allowNull() {
        return true;
    }

    @Override
    public String getAsString(Object o) {
        ValueInt32 v = get(o);
        if (v == null) {
            return "";
        } else {
            return v.toString();
        }
    }

    @Override
    public ValueInt32 StringToObj(String v) {
        if (allowNull() && ((v == null) || v.isEmpty())) {
            return null;
        }
        return new ValueInt32(Integer.valueOf(v));
    }

}
