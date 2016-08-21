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
 *
 * @author Mark Harris
 */
package axoloti;

import axoloti.attributedefinition.AxoAttributeComboBox;
import axoloti.inlets.InletBool32;
import axoloti.inlets.InletCharPtr32;
import axoloti.inlets.InletFrac32;
import axoloti.inlets.InletFrac32Buffer;
import axoloti.inlets.InletInt32;
import axoloti.object.AxoObject;
import axoloti.object.AxoObjectInstanceAbstract;
import axoloti.outlets.OutletBool32;
import axoloti.outlets.OutletCharPtr32;
import axoloti.outlets.OutletFrac32;
import axoloti.outlets.OutletFrac32Buffer;
import axoloti.outlets.OutletInt32;
import axoloti.parameters.ParameterInstance;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlatformNull extends PlatformBase implements IPlatform {

////////////////////////////////////////////////////
// IPatchTarget
////////////////////////////////////////////////////
    @Override
    public void GoLive() {
        Logger.getLogger(PlatformNull.class.getName()).log(Level.INFO, "NullPatch, GoLive() , do nothing");
    }

    @Override
    public void WriteCode() {
        Logger.getLogger(PlatformNull.class.getName()).log(Level.INFO, "NullPatch, WriteCode() , do nothing");
    }

    @Override
    public void UploadDependentFiles() {
        Logger.getLogger(PlatformNull.class.getName()).log(Level.INFO, "NullPatch, UploadDependentFiles() , do nothing");
    }

    @Override
    public void Compile() {
        Logger.getLogger(PlatformNull.class.getName()).log(Level.INFO, "NullPatch, Compile() , do nothing");
    }

    @Override
    public void UploadToSDCard() {
        Logger.getLogger(PlatformNull.class.getName()).log(Level.INFO, "NullPatch, UploadToSDCard() , do nothing");
    }

    @Override
    public void Upload() {
        Logger.getLogger(PlatformNull.class.getName()).log(Level.INFO, "NullPatch, Upload() , do nothing");
    }

    @Override
    public void UploadToSDCard(String sdfilename) {
        Logger.getLogger(PlatformNull.class.getName()).log(Level.INFO, "NullPatch, UploadToSDCard() , do nothing");
    }

    @Override
    public AxoObject GenerateAxoObj() {
        AxoObject ao;
        if (patch.getSettings() == null) {
            ao = GenerateAxoObjNormal();
        } else {
            switch (patch.getSettings().subpatchmode) {
                case no:
                case normal:
                    ao = GenerateAxoObjNormal();
                    break;
                case polyphonic:
                    ao = GenerateAxoObjPoly();
                    break;
                case polychannel:
                    ao = GenerateAxoObjPolyChannel();
                    break;
                case polyexpression:
                    ao = GenerateAxoObjPolyExpression();
                    break;
                default:
                    return null;
            }
        }
        if (patch.getSettings() != null) {
            ao.sAuthor = patch.getSettings().getAuthor();
            ao.sLicense = patch.getSettings().getLicense();
            ao.sDescription = patch.getNotes();
            ao.helpPatch = patch.getHelpPatch();
        }
        return ao;
    }

    /// implementation
    public AxoObject GenerateAxoObjNormal() {
        patch.SortByPosition();
        AxoObject ao = new AxoObject();
        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            if (o.typeName.equals("patch/inlet f")) {
                ao.inlets.add(new InletFrac32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/inlet i")) {
                ao.inlets.add(new InletInt32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/inlet b")) {
                ao.inlets.add(new InletBool32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/inlet a")) {
                ao.inlets.add(new InletFrac32Buffer(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/inlet string")) {
                ao.inlets.add(new InletCharPtr32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet f")) {
                ao.outlets.add(new OutletFrac32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet i")) {
                ao.outlets.add(new OutletInt32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet b")) {
                ao.outlets.add(new OutletBool32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet a")) {
                ao.outlets.add(new OutletFrac32Buffer(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet string")) {
                ao.outlets.add(new OutletCharPtr32(o.getInstanceName(), o.getInstanceName()));
            }
            for (ParameterInstance p : o.getParameterInstances()) {
                if (p.isOnParent()) {
                    ao.params.add(p.getParameterForParent());
                }
            }
        }
        ao.includes = patch.getIncludes();
        ao.depends = patch.getDepends();
        if ((patch.getNotes() != null) && (!patch.getNotes().isEmpty())) {
            ao.sDescription = patch.getNotes();
        } else {
            ao.sDescription = "no description";
        }

        if (patch.getSettings() != null
                && (patch.getSettings().GetMidiSelector())) {
            String cch[] = {"attr_midichannel", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"};
            String uch[] = {"inherit", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
            ao.attributes.add(new AxoAttributeComboBox("midichannel", uch, cch));
            // use a cut down list of those currently supported
            String cdev[] = {"0", "1", "2", "3", "15"};
            String udev[] = {"omni", "din", "usb device", "usb host", "internal"};
            ao.attributes.add(new AxoAttributeComboBox("mididevice", udev, cdev));
            String cport[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
            String uport[] = {"omni", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
            ao.attributes.add(new AxoAttributeComboBox("midiport", uport, cport));
        }

        ao.sLocalData = "";
        ao.sInitCode = "";
        ao.sDisposeCode = "";
        ao.sKRateCode = "";
        ao.sMidiCode = "";

        return ao;
    }

    AxoObject GenerateAxoObjPoly() {
        patch.SortByPosition();
        AxoObject ao = new AxoObject("unnamedobject", patch.getFileNamePath());
        ao.includes = patch.getIncludes();
        ao.depends = patch.getDepends();
        if ((patch.getNotes() != null) && (!patch.getNotes().isEmpty())) {
            ao.sDescription = patch.getNotes();
        } else {
            ao.sDescription = "no description";
        }
        String centries[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
        ao.attributes.add(new AxoAttributeComboBox("poly", centries, centries));
        if ((patch.getSettings() != null) && (patch.getSettings().GetMidiSelector())) {
            String cch[] = {"attr_midichannel", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15"};
            String uch[] = {"inherit", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
            ao.attributes.add(new AxoAttributeComboBox("midichannel", uch, cch));
            // use a cut down list of those currently supported
            String cdev[] = {"0", "1", "2", "3", "15"};
            String udev[] = {"omni", "din", "usb device", "usb host", "internal"};
            ao.attributes.add(new AxoAttributeComboBox("mididevice", udev, cdev));
            String cport[] = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
            String uport[] = {"omni", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
            ao.attributes.add(new AxoAttributeComboBox("midiport", uport, cport));
        }

        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            if (o.typeName.equals("patch/inlet f")) {
                ao.inlets.add(new InletFrac32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/inlet i")) {
                ao.inlets.add(new InletInt32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/inlet b")) {
                ao.inlets.add(new InletBool32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/inlet a")) {
                ao.inlets.add(new InletFrac32Buffer(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/inlet string")) {
                ao.inlets.add(new InletCharPtr32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet f")) {
                ao.outlets.add(new OutletFrac32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet i")) {
                ao.outlets.add(new OutletInt32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet b")) {
                ao.outlets.add(new OutletBool32(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet a")) {
                ao.outlets.add(new OutletFrac32Buffer(o.getInstanceName(), o.getInstanceName()));
            } else if (o.typeName.equals("patch/outlet string")) {
                Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.SEVERE, "string outlet impossible in poly subpatches!");
                // ao.outlets.add(new OutletCharPtr32(o.getInstanceName(), o.getInstanceName()));                
            }
            for (ParameterInstance p : o.getParameterInstances()) {
                if (p.isOnParent()) {
                    ao.params.add(p.getParameterForParent());
                }
            }
        }

        ao.sLocalData = "";
        ao.sInitCode = "";
        ao.sDisposeCode = "";
        ao.sKRateCode = "";
        ao.sMidiCode = "";
        return ao;
    }

    // Poly (Multi) Channel supports per Channel CC/Touch
    // all channels are independent
    AxoObject GenerateAxoObjPolyChannel() {
        AxoObject o = GenerateAxoObjPoly();
        return o;
    }

    // Poly Expression supports the Midi Polyphonic Expression (MPE) Spec
    // Can be used with (or without) the MPE objects
    // the midi channel of the patch is the 'main/global channel'
    AxoObject GenerateAxoObjPolyExpression() {
        AxoObject o = GenerateAxoObjPoly();
        return o;
    }
}
