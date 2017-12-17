package axoloti;

import axoloti.datatypes.ValueInt32;
import org.simpleframework.xml.Element;

/**
 *
 * @author jtaelman
 */
public class PresetInt extends Preset<Integer> {


    @Element(name = "i", required = false)
    public ValueInt32 getValuexy() {
        return new ValueInt32(v);
    }

    public PresetInt() {
    }

    public PresetInt(@Element(name = "i") ValueInt32 x) {
        v = x.getInt();
    }
    
    public PresetInt(int index, Integer value) {
        super(index, value);
    }


}
