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
import axoloti.displays.DisplayInstance;
import axoloti.inlets.InletBool32;
import axoloti.inlets.InletCharPtr32;
import axoloti.inlets.InletFrac32;
import axoloti.inlets.InletFrac32Buffer;
import axoloti.inlets.InletInstance;
import axoloti.inlets.InletInt32;
import axoloti.object.AxoObject;
import axoloti.object.AxoObjectAbstract;
import axoloti.object.AxoObjectFile;
import axoloti.object.AxoObjectInstanceAbstract;
import axoloti.outlets.OutletBool32;
import axoloti.outlets.OutletCharPtr32;
import axoloti.outlets.OutletFrac32;
import axoloti.outlets.OutletFrac32Buffer;
import axoloti.outlets.OutletInstance;
import axoloti.outlets.OutletInt32;
import axoloti.parameters.ParameterInstance;
import axoloti.utils.Preferences;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import qcmds.QCmdChangeWorkingDirectory;
import qcmds.QCmdCompilePatch;
import qcmds.QCmdCreateDirectory;
import qcmds.QCmdLock;
import qcmds.QCmdProcessor;
import qcmds.QCmdStart;
import qcmds.QCmdStop;
import qcmds.QCmdUploadFile;
import qcmds.QCmdUploadPatch;

public class PlatformAxoloti extends PlatformBase implements IPlatform {

////////////////////////////////////////////////////
// IPatchTarget
////////////////////////////////////////////////////
    @Override
    public void GoLive() {
        patch.GetQCmdProcessor().AppendToQueue(new QCmdStop());
        String f = "/" + patch.getSDCardPath();
        System.out.println("pathf" + f);
        patch.GetQCmdProcessor().AppendToQueue(new QCmdCreateDirectory(f));
        patch.GetQCmdProcessor().AppendToQueue(new QCmdChangeWorkingDirectory(f));
//        GetQCmdProcessor().AppendToQueue(new QCmdStop());
        patch.UploadDependentFiles();
        patch.ShowPreset(0);
        WriteCode();
        patch.presetUpdatePending = false;
        patch.GetQCmdProcessor().SetPatch(null);
        patch.GetQCmdProcessor().AppendToQueue(new QCmdCompilePatch(patch));
        patch.GetQCmdProcessor().AppendToQueue(new QCmdUploadPatch());
        patch.GetQCmdProcessor().AppendToQueue(new QCmdStart(patch));
        patch.GetQCmdProcessor().AppendToQueue(new QCmdLock(patch));
    }

    @Override
    public void WriteCode() {
        String c = GenerateCode3();

        try {
            String buildDir = System.getProperty(Axoloti.HOME_DIR) + "/build";
            FileOutputStream f = new FileOutputStream(buildDir + "/xpatch.cpp");
            f.write(c.getBytes());
            f.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.SEVERE, ex.toString());
        } catch (IOException ex) {
            Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.SEVERE, ex.toString());
        }
        Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.INFO, "Generate code complete");
    }

    @Override
    public void UploadDependentFiles() {
        String sdpath = patch.getSDCardPath();
        ArrayList<SDFileReference> files = patch.GetDependendSDFiles();
        for (SDFileReference fref : files) {
            File f = fref.getLocalfile();
            if (!f.exists()) {
                Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.SEVERE, "File reference unresolved: {0}", f.getName());
                continue;
            }
            if (!f.canRead()) {
                Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.SEVERE, "Can't read file {0}", f.getName());
                continue;
            }
            if (!SDCardInfo.getInstance().exists("/" + sdpath + "/" + fref.getTargetPath(), f.lastModified(), f.length())) {
                if (f.length() > 8 * 1024 * 1024) {
                    Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.INFO, "file {0} is larger than 8MB, skip uploading", f.getName());
                    continue;
                }
                patch.GetQCmdProcessor().AppendToQueue(new QCmdUploadFile(f, "/" + sdpath + "/" + fref.getTargetPath()));
            } else {
                Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.INFO, "file {0} matches timestamp and size, skip uploading", f.getName());
            }
        }
    }

    @Override
    public void Compile() {
        patch.GetQCmdProcessor().AppendToQueue(new QCmdCompilePatch(patch));
    }

    @Override
    public void UploadToSDCard() {
        UploadToSDCard("/" + patch.getSDCardPath() + "/patch.bin");
    }

    @Override
    public void Upload() {
        patch.GetQCmdProcessor().AppendToQueue(new QCmdUploadPatch());
    }

    @Override
    public void UploadToSDCard(String sdfilename) {
        WriteCode();
        Logger.getLogger(PatchFrame.class.getName()).log(Level.INFO, "sdcard filename:{0}", sdfilename);
        QCmdProcessor qcmdprocessor = QCmdProcessor.getQCmdProcessor();
        qcmdprocessor.AppendToQueue(new qcmds.QCmdStop());
        qcmdprocessor.AppendToQueue(new qcmds.QCmdCompilePatch(patch));
        // create subdirs...

        for (int i = 1; i < sdfilename.length(); i++) {
            if (sdfilename.charAt(i) == '/') {
                qcmdprocessor.AppendToQueue(new qcmds.QCmdCreateDirectory(sdfilename.substring(0, i)));
                qcmdprocessor.WaitQueueFinished();
            }
        }
        qcmdprocessor.WaitQueueFinished();
        Calendar cal;
        if (patch.isDirty()) {
            cal = Calendar.getInstance();
        } else {
            cal = Calendar.getInstance();
            if (patch.getFileNamePath() != null && !patch.getFileNamePath().isEmpty()) {
                File f = new File(patch.getFileNamePath());
                if (f.exists()) {
                    cal.setTimeInMillis(f.lastModified());
                }
            }
        }
        qcmdprocessor.AppendToQueue(new qcmds.QCmdUploadFile(getBinFile(), sdfilename, cal));
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

////////////////////////////////////////////////////
// Implementation
////////////////////////////////////////////////////
    // UNUSED?
    void ExportAxoObj(File f1) {
        String fnNoExtension = f1.getName().substring(0, f1.getName().lastIndexOf(".axo"));
        AxoObject ao = GenerateAxoObj();
        ao.sDescription = patch.getFileNamePath();
        ao.id = fnNoExtension;

        AxoObjectFile aof = new AxoObjectFile();
        aof.objs.add(ao);
        Serializer serializer = new Persister();
        try {
            serializer.write(aof, f1);
        } catch (Exception ex) {
            Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.INFO, "Export obj complete");
    }

    public File getBinFile() {
        String buildDir = System.getProperty(Axoloti.HOME_DIR) + "/build";;
        return new File(buildDir + "/xpatch.bin");
//            Logger.getLogger(QCmdWriteFile.class.getName()).log(Level.INFO, "bin path: {0}", f.getAbsolutePath());        
    }

    public String generateIncludes() {
        String inc = "";
        Set<String> includes = patch.getIncludes();
        for (String s : includes) {
            if (s.startsWith("\"")) {
                inc += "#include " + s + "\n";
            } else {
                inc += "#include \"" + s + "\"\n";
            }
        }
        return inc;
    }

    String GenerateCode3() {
        Preferences prefs = MainFrame.prefs;
        patch.setControllerInstance(null);
        String cobjstr = prefs.getControllerObject();
        if (prefs.isControllerEnabled() && cobjstr != null && !cobjstr.isEmpty()) {
            Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.INFO, "Using controller object: {0}", cobjstr);
            AxoObjectAbstract x = null;
            ArrayList<AxoObjectAbstract> objs = MainFrame.axoObjects.GetAxoObjectFromName(cobjstr, patch.GetCurrentWorkingDirectory());
            if ((objs != null) && (!objs.isEmpty())) {
                x = objs.get(0);
            }
            if (x != null) {
                patch.setControllerInstance(x.CreateInstance(null, "ctrl0x123", new Point(0, 0)));
            } else {
                Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.INFO, "Unable to created controller for : {0}", cobjstr);
            }
        }

        patch.CreateIID();
        patch.SortByPosition();
        String c = "extern \"C\" { \n";
        c += generateIncludes();
        c += "}\n"
                + "#pragma GCC diagnostic ignored \"-Wunused-variable\"\n"
                + "#pragma GCC diagnostic ignored \"-Wunused-parameter\"\n";
        if (patch.getSettings() == null) {
            c += "#define MIDICHANNEL 0 // DEPRECATED!\n";
        } else {
            c += "#define MIDICHANNEL " + (patch.getSettings().GetMidiChannel() - 1) + " // DEPRECATED!\n";
        }
        c += "void xpatch_init2(int fwid);\n"
                + "extern \"C\" __attribute__ ((section(\".boot\"))) void xpatch_init(int fwid){\n"
                + "  xpatch_init2(fwid);\n"
                + "}\n\n";

        c += "void PatchMidiInHandler(midi_device_t dev, uint8_t port, uint8_t status, uint8_t data1, uint8_t data2);\n\n";

        c += "     int32buffer AudioInputLeft;\n";
        c += "     int32buffer AudioInputRight;\n";
        c += "     int32buffer AudioOutputLeft;\n";
        c += "     int32buffer AudioOutputRight;\n";
        c += "     typedef enum { A_STEREO, A_MONO, A_BALANCED } AudioModeType;\n";
        c += "     AudioModeType AudioOutputMode = A_STEREO;\n";
        c += "     AudioModeType AudioInputMode = A_STEREO;\n";

        c += "static void PropagateToSub(ParameterExchange_t *origin) {\n"
                + "      ParameterExchange_t *pex = (ParameterExchange_t *)origin->finalvalue;\n"
                + "      PExParameterChange(pex,origin->modvalue,0xFFFFFFEE);\n"
                + "}\n";

        c += GenerateStructCodePlusPlus("rootc", false, "rootc")
                + "static const int polyIndex = 0;\n"
                + GenerateParamInitCode3("rootc")
                + GeneratePresetCode3("rootc")
                + GenerateModulationCode3()
                + GenerateInitCodePlusPlus("rootc")
                + GenerateDisposeCodePlusPlus("rootc")
                + GenerateDSPCodePlusPlus("rootc", false)
                + GenerateMidiCodePlusPlus("rootc")
                + GeneratePatchCodePlusPlus("rootc");

        c = c.replace("attr_poly", "1");

        if (patch.getSettings() == null) {
            c = c.replace("attr_midichannel", "0");
        } else {
            c = c.replace("attr_midichannel", Integer.toString(patch.getSettings().GetMidiChannel() - 1));
        }
        if (patch.getSettings() == null || !patch.getSettings().GetMidiSelector()) {
            c = c.replace("attr_mididevice", "0");
            c = c.replace("attr_midiport", "0");
        }
        return c;
    }

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
        /* object structures */
//         ao.sCName = fnNoExtension;
        ao.sLocalData = GenerateStructCodePlusPlusSub("attr_parent", true)
                + "static const int polyIndex = 0;\n";
        ao.sLocalData += GenerateParamInitCode3("");
        ao.sLocalData += GeneratePresetCode3("");
        ao.sLocalData += GenerateModulationCode3();
        ao.sLocalData = ao.sLocalData.replaceAll("attr_poly", "1");
        ao.sInitCode = GenerateParamInitCodePlusPlusSub("attr_parent", "this");
        ao.sInitCode += GenerateObjInitCodePlusPlusSub("attr_parent", "this");
        ao.sDisposeCode = GenerateDisposeCodePlusPlusSub("attr_parent");
        ao.includes = patch.getIncludes();
        ao.depends = patch.getDepends();
        if ((patch.getNotes() != null) && (!patch.getNotes().isEmpty())) {
            ao.sDescription = patch.getNotes();
        } else {
            ao.sDescription = "no description";
        }
        ao.sKRateCode = "int i; /*...*/\n";
        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            if (o.typeName.equals("patch/inlet f") || o.typeName.equals("patch/inlet i") || o.typeName.equals("patch/inlet b")) {
                ao.sKRateCode += "   " + o.getCInstanceName() + "_i._inlet = inlet_" + o.getLegalName() + ";\n";
            } else if (o.typeName.equals("patch/inlet string")) {
                ao.sKRateCode += "   " + o.getCInstanceName() + "_i._inlet = (char *)inlet_" + o.getLegalName() + ";\n";
            } else if (o.typeName.equals("patch/inlet a")) {
                ao.sKRateCode += "   for(i=0;i<BUFSIZE;i++) " + o.getCInstanceName() + "_i._inlet[i] = inlet_" + o.getLegalName() + "[i];\n";
            }

        }
        ao.sKRateCode += GenerateDSPCodePlusPlusSub("attr_parent", true);
        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            if (o.typeName.equals("patch/outlet f") || o.typeName.equals("patch/outlet i") || o.typeName.equals("patch/outlet b")) {
                ao.sKRateCode += "   outlet_" + o.getLegalName() + " = " + o.getCInstanceName() + "_i._outlet;\n";
            } else if (o.typeName.equals("patch/outlet string")) {
                ao.sKRateCode += "   outlet_" + o.getLegalName() + " = (char *)" + o.getCInstanceName() + "_i._outlet;\n";
            } else if (o.typeName.equals("patch/outlet a")) {
                ao.sKRateCode += "      for(i=0;i<BUFSIZE;i++) outlet_" + o.getLegalName() + "[i] = " + o.getCInstanceName() + "_i._outlet[i];\n";
            }
        }

        ao.sMidiCode = ""
                + "if ( attr_mididevice > 0 && dev > 0 && attr_mididevice != dev) return;\n"
                + "if ( attr_midiport > 0 && port > 0 && attr_midiport != port) return;\n"
                + GenerateMidiInCodePlusPlus();

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
        return ao;
    }

    // Poly voices from one (or omni) midi channel
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

        ao.sLocalData = GenerateParamInitCode3("");
        ao.sLocalData += GeneratePexchAndDisplayCode();
        ao.sLocalData += "/* parameter instance indices */\n";
        int k = 0;
        for (ParameterInstance p : patch.getParameterInstances()) {
            ao.sLocalData += "static const int PARAM_INDEX_" + p.GetObjectInstance().getLegalName() + "_" + p.getLegalName() + " = " + k + ";\n";
            k++;
        }

        ao.sLocalData += GeneratePresetCode3("");
        ao.sLocalData += GenerateModulationCode3();
        ao.sLocalData += "class voice {\n";
        ao.sLocalData += "   public:\n";
        ao.sLocalData += "   int polyIndex;\n";
        ao.sLocalData += GeneratePexchAndDisplayCodeV();
        ao.sLocalData += GenerateObjectCode("voice", true, "parent->common->");
        ao.sLocalData += "attr_parent *common;\n";
        ao.sLocalData += "void Init(voice *parent) {\n";
        ao.sLocalData += "        int i;\n"
                + "        for(i=0;i<NPEXCH;i++){\n"
                + "          PExch[i].pfunction = 0;\n"
                + "        }\n";
        ao.sLocalData += GenerateObjInitCodePlusPlusSub("voice", "parent");
        ao.sLocalData += "}\n\n";
        ao.sLocalData += "void dsp(void) {\n int i;\n";
        ao.sLocalData += GenerateDSPCodePlusPlusSub("", true);
        ao.sLocalData += "}\n";
        ao.sLocalData += "void dispose(void) {\n int i;\n";
        ao.sLocalData += GenerateDisposeCodePlusPlusSub("");
        ao.sLocalData += "}\n";
        ao.sLocalData += GenerateMidiCodePlusPlus("attr_parent");
        ao.sLocalData += "};\n";
        ao.sLocalData += "static voice * getVoices(void){\n"
                + "     static voice v[attr_poly];\n"
                + "    return v;\n"
                + "}\n";

        ao.sLocalData += "static void PropagateToVoices(ParameterExchange_t *origin) {\n"
                + "      ParameterExchange_t *pex = (ParameterExchange_t *)origin->finalvalue;\n"
                + "      int vi;\n"
                + "      for (vi = 0; vi < attr_poly; vi++) {\n"
                + "        PExParameterChange(pex,origin->modvalue,0xFFFFFFEE);\n"
                + "          pex = (ParameterExchange_t *)((int)pex + sizeof(voice)); // dirty trick...\n"
                + "      }"
                + "}\n";

        ao.sLocalData += "int8_t notePlaying[attr_poly];\n";
        ao.sLocalData += "int32_t voicePriority[attr_poly];\n";
        ao.sLocalData += "int32_t priority;\n";
        ao.sLocalData += "int32_t sustain;\n";
        ao.sLocalData += "int8_t pressed[attr_poly];\n";

        ao.sLocalData = ao.sLocalData.replaceAll("parent->PExModulationSources", "parent->common->PExModulationSources");
        ao.sLocalData = ao.sLocalData.replaceAll("parent->PExModulationPrevVal", "parent->common->PExModulationPrevVal");
        ao.sLocalData = ao.sLocalData.replaceAll("parent->GetModulationTable", "parent->common->GetModulationTable");

        ao.sInitCode = GenerateParamInitCodePlusPlusSub("", "parent");
        ao.sInitCode += "int k;\n"
                + "   for(k=0;k<NPEXCH;k++){\n"
                + "      PExch[k].pfunction = PropagateToVoices;\n"
                + "      PExch[k].finalvalue = (int32_t) (&(getVoices()[0].PExch[k]));\n"
                + "   }\n";
        ao.sInitCode += "int vi; for(vi=0;vi<attr_poly;vi++) {\n"
                + "   voice *v = &getVoices()[vi];\n"
                + "   v->polyIndex = vi;\n"
                + "   v->common = this;\n"
                + "   v->Init(&getVoices()[vi]);\n"
                + "   notePlaying[vi]=0;\n"
                + "   voicePriority[vi]=0;\n"
                + "   for (j = 0; j < v->NPEXCH; j++) {\n"
                + "      v->PExch[j].value = 0;\n"
                + "      v->PExch[j].modvalue = 0;\n"
                + "   }\n"
                + "}\n"
                + "      for (k = 0; k < NPEXCH; k++) {\n"
                + "        if (PExch[k].pfunction){\n"
                + "          (PExch[k].pfunction)(&PExch[k]);\n"
                + "        } else {\n"
                + "          PExch[k].finalvalue = PExch[k].value;\n"
                + "        }\n"
                + "      }\n"
                + "priority=0;\n"
                + "sustain=0;\n";
        ao.sDisposeCode = "int vi; for(vi=0;vi<attr_poly;vi++) {\n"
                + "  voice *v = &getVoices()[vi];\n"
                + "  v->dispose();\n"
                + "}\n";
        ao.sKRateCode = "";
        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            if (o.typeName.equals("patch/outlet f") || o.typeName.equals("patch/outlet i")
                    || o.typeName.equals("patch/outlet b") || o.typeName.equals("patch/outlet string")) {
                ao.sKRateCode += "   outlet_" + o.getLegalName() + " = 0;\n";
            } else if (o.typeName.equals("patch/outlet a")) {
                ao.sKRateCode += "{\n"
                        + "      int j;\n"
                        + "      for(j=0;j<BUFSIZE;j++) outlet_" + o.getLegalName() + "[j] = 0;\n"
                        + "}\n";
            }
        }
        ao.sKRateCode += "int vi; for(vi=0;vi<attr_poly;vi++) {";

        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            if (o.typeName.equals("inlet") || o.typeName.equals("inlet_i") || o.typeName.equals("inlet_b") || o.typeName.equals("inlet_")
                    || o.typeName.equals("patch/inlet f") || o.typeName.equals("patch/inlet i") || o.typeName.equals("patch/inlet b")) {
                ao.sKRateCode += "   getVoices()[vi]." + o.getCInstanceName() + "_i._inlet = inlet_" + o.getLegalName() + ";\n";
            } else if (o.typeName.equals("inlet_string") || o.typeName.equals("patch/inlet string")) {
                ao.sKRateCode += "   getVoices()[vi]." + o.getCInstanceName() + "_i._inlet = (char *)inlet_" + o.getLegalName() + ";\n";
            } else if (o.typeName.equals("inlet~") || o.typeName.equals("patch/inlet a")) {
                ao.sKRateCode += "{int j; for(j=0;j<BUFSIZE;j++) getVoices()[vi]." + o.getCInstanceName() + "_i._inlet[j] = inlet_" + o.getLegalName() + "[j];}\n";
            }
        }
        ao.sKRateCode += "getVoices()[vi].dsp();\n";
        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            if (o.typeName.equals("outlet") || o.typeName.equals("patch/outlet f")
                    || o.typeName.equals("patch/outlet i")
                    || o.typeName.equals("patch/outlet b")) {
                ao.sKRateCode += "   outlet_" + o.getLegalName() + " += getVoices()[vi]." + o.getCInstanceName() + "_i._outlet;\n";
            } else if (o.typeName.equals("patch/outlet string")) {
                ao.sKRateCode += "   outlet_" + o.getLegalName() + " = (char *)getVoices()[vi]." + o.getCInstanceName() + "_i._outlet;\n";
            } else if (o.typeName.equals("patch/outlet a")) {
                ao.sKRateCode += "{\n"
                        + "      int j;\n"
                        + "      for(j=0;j<BUFSIZE;j++) outlet_" + o.getLegalName() + "[j] += getVoices()[vi]." + o.getCInstanceName() + "_i._outlet[j];\n"
                        + "}\n";
            }
        }
        ao.sKRateCode += "}\n";
        ao.sMidiCode = ""
                + "if ( attr_mididevice > 0 && dev > 0 && attr_mididevice != dev) return;\n"
                + "if ( attr_midiport > 0 && port > 0 && attr_midiport != port) return;\n"
                + "if ((status == MIDI_NOTE_ON + attr_midichannel) && (data2)) {\n"
                + "  int min = 1<<30;\n"
                + "  int mini = 0;\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++){\n"
                + "    if (voicePriority[i] < min){\n"
                + "      min = voicePriority[i];\n"
                + "      mini = i;\n"
                + "    }\n"
                + "  }\n"
                + "  voicePriority[mini] = 100000+priority++;\n"
                + "  notePlaying[mini] = data1;\n"
                + "  pressed[mini] = 1;\n"
                + "  getVoices()[mini].MidiInHandler(dev, port, status, data1, data2);\n"
                + "} else if (((status == MIDI_NOTE_ON + attr_midichannel) && (!data2))||\n"
                + "          (status == MIDI_NOTE_OFF + attr_midichannel)) {\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++){\n"
                + "    if ((notePlaying[i] == data1) && pressed[i]){\n"
                + "      voicePriority[i] = priority++;\n"
                + "      pressed[i] = 0;\n"
                + "      if (!sustain)\n"
                + "        getVoices()[i].MidiInHandler(dev, port, status, data1, data2);\n"
                + "      }\n"
                + "  }\n"
                + "} else if (status == attr_midichannel + MIDI_CONTROL_CHANGE) {\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++) getVoices()[i].MidiInHandler(dev, port, status, data1, data2);\n"
                + "  if (data1 == 64) {\n"
                + "    if (data2>0) {\n"
                + "      sustain = 1;\n"
                + "    } else if (sustain == 1) {\n"
                + "      sustain = 0;\n"
                + "      for(i=0;i<attr_poly;i++){\n"
                + "        if (pressed[i] == 0) {\n"
                + "          getVoices()[i].MidiInHandler(dev, port, MIDI_NOTE_ON + attr_midichannel, notePlaying[i], 0);\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "} else {"
                + "  int i;   for(i=0;i<attr_poly;i++) getVoices()[i].MidiInHandler(dev, port, status, data1, data2);\n"
                + "}\n";
        return ao;
    }

    // Poly (Multi) Channel supports per Channel CC/Touch
    // all channels are independent
    AxoObject GenerateAxoObjPolyChannel() {
        AxoObject o = GenerateAxoObjPoly();
        o.sLocalData
                += "int8_t voiceChannel[attr_poly];\n";
        o.sInitCode
                += "int vc;\n"
                + "for (vc=0;vc<attr_poly;vc++) {\n"
                + "   voiceChannel[vc]=0xFF;\n"
                + "}\n";
        o.sMidiCode = ""
                + "if ( attr_mididevice > 0 && dev > 0 && attr_mididevice != dev) return;\n"
                + "if ( attr_midiport > 0 && port > 0 && attr_midiport != port) return;\n"
                + "int msg = (status & 0xF0);\n"
                + "int channel = (status & 0x0F);\n"
                + "if ((msg == MIDI_NOTE_ON) && (data2)) {\n"
                + "  int min = 1<<30;\n"
                + "  int mini = 0;\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++){\n"
                + "    if (voicePriority[i] < min){\n"
                + "      min = voicePriority[i];\n"
                + "      mini = i;\n"
                + "    }\n"
                + "  }\n"
                + "  voicePriority[mini] = 100000 + priority++;\n"
                + "  notePlaying[mini] = data1;\n"
                + "  pressed[mini] = 1;\n"
                + "  voiceChannel[mini] = status & 0x0F;\n"
                + "  getVoices()[mini].MidiInHandler(dev, port, status & 0xF0, data1, data2);\n"
                + "} else if (((msg == MIDI_NOTE_ON) && (!data2))||\n"
                + "            (msg == MIDI_NOTE_OFF)) {\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++){\n"
                + "    if (notePlaying[i] == data1){\n"
                + "      voicePriority[i] = priority++;\n"
                + "      voiceChannel[i] = 0xFF;\n"
                + "      pressed[i] = 0;\n"
                + "      if (!sustain)\n"
                + "         getVoices()[i].MidiInHandler(dev, port, msg + attr_midichannel, data1, data2);\n"
                + "      }\n"
                + "  }\n"
                + "} else if (msg == MIDI_CONTROL_CHANGE) {\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++) {\n"
                + "    if (voiceChannel[i] == channel) {\n"
                + "      getVoices()[i].MidiInHandler(dev, port, MIDI_CONTROL_CHANGE + attr_midichannel, data1, data2);\n"
                + "    }\n"
                + "  }\n"
                + "  if (data1 == 64) {\n"
                + "    if (data2>0) {\n"
                + "      sustain = 1;\n"
                + "    } else if (sustain == 1) {\n"
                + "      sustain = 0;\n"
                + "      for(i=0;i<attr_poly;i++){\n"
                + "        if (pressed[i] == 0) {\n"
                + "          getVoices()[i].MidiInHandler(dev, port, MIDI_NOTE_ON + attr_midichannel, notePlaying[i], 0);\n"
                + "        }\n"
                + "      }\n"
                + "    }\n"
                + "  }\n"
                + "} else if (msg == MIDI_PITCH_BEND) {\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++){\n"
                + "    if (voiceChannel[i] == channel) {\n"
                + "      getVoices()[i].MidiInHandler(dev, port, MIDI_PITCH_BEND + attr_midichannel, data1, data2);\n"
                + "    }\n"
                + "  }\n"
                + "} else {"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++) {\n"
                + "    if (voiceChannel[i] == channel) {\n"
                + "         getVoices()[i].MidiInHandler(dev, port,msg + attr_midichannel, data1, data2);\n"
                + "    }\n"
                + "  }\n"
                + "}\n";
        return o;
    }

    // Poly Expression supports the Midi Polyphonic Expression (MPE) Spec
    // Can be used with (or without) the MPE objects
    // the midi channel of the patch is the 'main/global channel'
    AxoObject GenerateAxoObjPolyExpression() {
        AxoObject o = GenerateAxoObjPoly();
        o.sLocalData
                += "int8_t voiceChannel[attr_poly];\n"
                + "int8_t pitchbendRange;\n"
                + "int8_t lowChannel;\n"
                + "int8_t highChannel;\n"
                + "int8_t lastRPN_LSB;\n"
                + "int8_t lastRPN_MSB;\n";
        o.sInitCode
                += "int vc;\n"
                + "for (vc=0;vc<attr_poly;vc++) {\n"
                + "   voiceChannel[vc]=0xFF;\n"
                + "}\n"
                + "lowChannel = attr_midichannel + 1;\n"
                + "highChannel = attr_midichannel + ( 15 - attr_midichannel);\n"
                + "pitchbendRange = 48;\n"
                + "lastRPN_LSB=0xFF;\n"
                + "lastRPN_MSB=0xFF;\n";
        o.sMidiCode = ""
                + "if ( attr_mididevice > 0 && dev > 0 && attr_mididevice != dev) return;\n"
                + "if ( attr_midiport > 0 && port > 0 && attr_midiport != port) return;\n"
                + "int msg = (status & 0xF0);\n"
                + "int channel = (status & 0x0F);\n"
                + "if ((msg == MIDI_NOTE_ON) && (data2)) {\n"
                + "  if (channel == attr_midichannel \n"
                + "   || channel < lowChannel || channel > highChannel)\n"
                + "    return;\n"
                + "  int min = 1<<30;\n"
                + "  int mini = 0;\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++){\n"
                + "    if (voicePriority[i] < min){\n"
                + "      min = voicePriority[i];\n"
                + "      mini = i;\n"
                + "    }\n"
                + "  }\n"
                + "  voicePriority[mini] = 100000 + priority++;\n"
                + "  notePlaying[mini] = data1;\n"
                + "  pressed[mini] = 1;\n"
                + "  voiceChannel[mini] = status & 0x0F;\n"
                + "  getVoices()[mini].MidiInHandler(dev, port, status & 0xF0, data1, data2);\n"
                + "} else if (((msg == MIDI_NOTE_ON) && (!data2))||\n"
                + "            (msg == MIDI_NOTE_OFF)) {\n"
                + "  if (channel == attr_midichannel\n "
                + "   || channel < lowChannel || channel > highChannel)\n"
                + "    return;\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++){\n"
                + "    if (notePlaying[i] == data1 && voiceChannel[i] == channel){\n"
                + "      voicePriority[i] = priority++;\n"
                + "      voiceChannel[i] = 0xFF;\n"
                + "      pressed[i] = 0;\n"
                + "      if (!sustain)\n"
                + "         getVoices()[i].MidiInHandler(dev, port, msg + attr_midichannel, data1, data2);\n"
                + "      }\n"
                + "  }\n"
                + "} else if (msg == MIDI_CONTROL_CHANGE) {\n"
                + "  if (data1 == MIDI_C_POLY) {\n" // MPE enable mode
                + "     if (data2 > 0) {\n "
                + "       if (channel == attr_midichannel) {\n"
                + "         if (channel != 15) {\n" // e.g ch 1 (g), we use 2-N notes
                + "           lowChannel = channel + 1;\n"
                + "           highChannel = lowChannel + data2 - 1;\n"
                + "         } else {\n" // ch 16, we use 16(g) 15-N notes
                + "           highChannel = channel - 1;\n"
                + "           lowChannel = highChannel + 1 - data2;\n"
                + "         }\n"
                + "         for(int i=0;i<attr_poly;i++) {\n"
                + "           getVoices()[i].MidiInHandler(dev, port, MIDI_CONTROL_CHANGE + attr_midichannel, 100, lastRPN_LSB);\n"
                + "           getVoices()[i].MidiInHandler(dev, port, MIDI_CONTROL_CHANGE + attr_midichannel, 101, lastRPN_MSB);\n"
                + "           getVoices()[i].MidiInHandler(dev, port, MIDI_CONTROL_CHANGE + attr_midichannel, 6, pitchbendRange);\n"
                + "         }\n" //for
                + "      }\n" //if mainchannel
                + "    } else {\n" // enable/disable
                + "      lowChannel = 0;\n" //disable, we may in the future want to turn this in to normal poly mode
                + "      highChannel = 0;\n"
                + "    }\n"
                + "  }\n"// cc127
                + "  if (channel != attr_midichannel\n"
                + "    && (channel < lowChannel || channel > highChannel))\n"
                + "    return;\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++) {\n"
                + "    if (voiceChannel[i] == channel || channel == attr_midichannel) {\n"
                + "      getVoices()[i].MidiInHandler(dev, port, MIDI_CONTROL_CHANGE + attr_midichannel, data1, data2);\n"
                + "    }\n"
                + "  }\n"
                + "  if (data1 == MIDI_C_RPN_MSB || data1 == MIDI_C_RPN_LSB || data1 == MIDI_C_DATA_ENTRY) {\n"
                + "     switch(data1) {\n"
                + "         case MIDI_C_RPN_LSB: lastRPN_LSB = data2; break;\n"
                + "         case MIDI_C_RPN_MSB: lastRPN_MSB = data2; break;\n"
                + "         case MIDI_C_DATA_ENTRY: {\n"
                + "             if (lastRPN_LSB == 0 && lastRPN_MSB == 0) {\n"
                + "               for(i=0;i<attr_poly;i++) {\n"
                + "                 if (voiceChannel[i] != channel) {\n" // because already sent above
                + "                   pitchbendRange = data2;\n"
                + "                   getVoices()[i].MidiInHandler(dev, port, MIDI_CONTROL_CHANGE + attr_midichannel, 100, lastRPN_LSB);\n"
                + "                   getVoices()[i].MidiInHandler(dev, port, MIDI_CONTROL_CHANGE + attr_midichannel, 101, lastRPN_MSB);\n"
                + "                   getVoices()[i].MidiInHandler(dev, port, MIDI_CONTROL_CHANGE + attr_midichannel, 6, pitchbendRange);\n"
                + "                 }\n" // if
                + "               }\n" //for
                + "             }\n" // if lsb/msb=0
                + "           }\n" // case 6
                + "           break;\n"
                + "         default: break;\n"
                + "     }\n" //switch
                + "  } else if (data1 == 64) {\n" //end //cc 101,100,6, cc64
                + "    if (data2>0) {\n"
                + "      sustain = 1;\n"
                + "    } else if (sustain == 1) {\n"
                + "      sustain = 0;\n"
                + "      for(i=0;i<attr_poly;i++){\n"
                + "        if (pressed[i] == 0) {\n"
                + "          getVoices()[i].MidiInHandler(dev, port, MIDI_NOTE_ON + attr_midichannel, notePlaying[i], 0);\n"
                + "        }\n"
                + "      }\n"
                + "    }\n" //sus=1
                + "  }\n" //cc64
                + "} else if (msg == MIDI_PITCH_BEND) {\n"
                + "  if (channel != attr_midichannel\n"
                + "    && (channel < lowChannel || channel > highChannel))\n"
                + "    return;\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++) {\n"
                + "    if (voiceChannel[i] == channel || channel == attr_midichannel) {\n"
                + "      getVoices()[i].MidiInHandler(dev, port, MIDI_PITCH_BEND + attr_midichannel, data1, data2);\n"
                + "    }\n"
                + "  }\n"
                + "} else {" // end pb, other midi
                + "  if (channel != attr_midichannel\n"
                + "    && (channel < lowChannel || channel > highChannel))\n"
                + "    return;\n"
                + "  int i;\n"
                + "  for(i=0;i<attr_poly;i++) {\n"
                + "    if (voiceChannel[i] == channel || channel == attr_midichannel) {\n"
                + "         getVoices()[i].MidiInHandler(dev, port, msg + attr_midichannel, data1, data2);\n"
                + "    }\n"
                + "  }\n"
                + "}\n"; // other midi
        return o;
    }

    /* the c++ code generator */
    String GeneratePexchAndDisplayCode() {
        String c = GeneratePexchAndDisplayCodeV();
        c += "    int32_t PExModulationPrevVal[attr_poly][NMODULATIONSOURCES];\n";
        return c;
    }

    String GeneratePexchAndDisplayCodeV() {
        String c = "";
        c += "    static const uint32_t NPEXCH = " + patch.getParameterInstances().size() + ";\n";
        c += "    ParameterExchange_t PExch[NPEXCH];\n";
        c += "    int32_t displayVector[" + (patch.getDisplayDataLength() + 3) + "];\n";
        c += "    static const uint32_t NPRESETS = " + patch.getSettings().GetNPresets() + ";\n";
        c += "    static const uint32_t NPRESET_ENTRIES = " + patch.getSettings().GetNPresetEntries() + ";\n";
        c += "    static const uint32_t NMODULATIONSOURCES = " + patch.getSettings().GetNModulationSources() + ";\n";
        c += "    static const uint32_t NMODULATIONTARGETS = " + patch.getSettings().GetNModulationTargetsPerSource() + ";\n";
        return c;
    }

    String GenerateObjectCode(String classname, boolean enableOnParent, String OnParentAccess) {
        String c = "";
        {
            c += "/* modsource defines */\n";
            int k = 0;
            for (Modulator m : patch.getModulators()) {
                c += "static const int " + m.getCName() + " = " + k + ";\n";
                k++;
            }
        }
        {
            c += "/* parameter instance indices */\n";
            int k = 0;
            for (ParameterInstance p : patch.getParameterInstances()) {
                c += "static const int PARAM_INDEX_" + p.GetObjectInstance().getLegalName() + "_" + p.getLegalName() + " = " + k + ";\n";
                k++;
            }
        }
        c += "/* controller classes */\n";
        if (patch.getControllerInstance() != null) {
            c += patch.getControllerInstance().GenerateClass(classname, OnParentAccess, enableOnParent);
        }
        c += "/* object classes */\n";
        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            c += o.GenerateClass(classname, OnParentAccess, enableOnParent);
        }
        c += "/* controller instances */\n";
        if (patch.getControllerInstance() != null) {
            String s = patch.getControllerInstance().getCInstanceName();
            if (!s.isEmpty()) {
                c += "     " + s + " " + s + "_i;\n";
            }
        }

        c += "/* object instances */\n";
        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            String s = o.getCInstanceName();
            if (!s.isEmpty()) {
                c += "     " + s + " " + s + "_i;\n";
            }
        }
        c += "/* net latches */\n";
        for (Net n : patch.getNets()) {
            // check if net has multiple sources
            if ((n.CType() != null) && n.NeedsLatch()) {
                c += "    " + n.CType() + " " + n.CName() + "Latch" + ";\n";
            }
        }
        return c;
    }

    String GenerateStructCodePlusPlusSub(String classname, boolean enableOnParent) {
        String c = "";
        c += GeneratePexchAndDisplayCode();
        c += GenerateObjectCode(classname, enableOnParent, "parent->");
        return c;
    }

    String GenerateStructCodePlusPlus(String classname, boolean enableOnParent, String parentclassname) {
        String c = "";
        c += "class " + classname + "{\n";
        c += "   public:\n";
        c += GenerateStructCodePlusPlusSub(parentclassname, enableOnParent);
        return c;
    }

    String GeneratePresetCode3(String ClassName) {
        String c = "   static const int32_t * GetPresets(void){\n";
        c += "      static const int32_t p[NPRESETS][NPRESET_ENTRIES][2] = {\n";
        for (int i = 0; i < patch.getSettings().GetNPresets(); i++) {
//            c += "// preset " + i + "\n";
//            c += "pp = (int*)(&Presets[" + i + "]);\n";
            int[] dp = patch.DistillPreset(i + 1);
            c += "         {\n";
            for (int j = 0; j < patch.getSettings().GetNPresetEntries(); j++) {
                c += "           {" + dp[j * 2] + "," + dp[j * 2 + 1] + "}";
                if (j != patch.getSettings().GetNPresetEntries() - 1) {
                    c += ",\n";
                } else {
                    c += "\n";
                }
            }
            if (i != patch.getSettings().GetNPresets() - 1) {
                c += "         },\n";
            } else {
                c += "         }\n";
            }
        }
        c += "      };\n";
        c += "   return &p[0][0][0];\n";
        c += "   };\n";

        c += "void ApplyPreset(int index){\n"
                + "   if (!index) {\n"
                + "     int i;\n"
                + "     int32_t *p = GetInitParams();\n"
                + "     for(i=0;i<NPEXCH;i++){\n"
                + "        PExParameterChange(&PExch[i],p[i],0xFFEF);\n"
                + "     }\n"
                + "   }\n"
                + "   index--;\n"
                + "   if (index < NPRESETS) {\n"
                + "     PresetParamChange_t *pa = (PresetParamChange_t *)(GetPresets());\n"
                + "     PresetParamChange_t *p = &pa[index*NPRESET_ENTRIES];\n"
                + "       int i;\n"
                + "       for(i=0;i<NPRESET_ENTRIES;i++){\n"
                + "         PresetParamChange_t *pp = &p[i];\n"
                + "         if ((pp->pexIndex>=0)&&(pp->pexIndex<NPEXCH)) {\n"
                + "           PExParameterChange(&PExch[pp->pexIndex],pp->value,0xFFEF);"
                + "         }\n"
                + "         else break;\n"
                + "       }\n"
                + "   }\n"
                + "}\n";
        return c;
    }

    String GenerateModulationCode3() {
        String s = "   static PExModulationTarget_t * GetModulationTable(void){\n";
        s += "    static const PExModulationTarget_t PExModulationSources[NMODULATIONSOURCES][NMODULATIONTARGETS] = \n";
        s += "{";
        for (int i = 0; i < patch.getSettings().GetNModulationSources(); i++) {
            s += "{";
            if (i < patch.getModulators().size()) {
                Modulator m = patch.getModulators().get(i);
                for (int j = 0; j < patch.getSettings().GetNModulationTargetsPerSource(); j++) {
                    if (j < m.Modulations.size()) {
                        Modulation n = m.Modulations.get(j);
                        s += "{" + n.getDestination().indexName() + ", " + n.getValue().getRaw() + "}";
                    } else {
                        s += "{-1,0}";
                    }
                    if (j != patch.getSettings().GetNModulationTargetsPerSource() - 1) {
                        s += ",";
                    } else {
                        s += "}";
                    }
                }
            } else {
                for (int j = 0; j < patch.getSettings().GetNModulationTargetsPerSource() - 1; j++) {
                    s += "{-1,0},";
                }
                s += "{-1,0}}";
            }
            if (i != patch.getSettings().GetNModulationSources() - 1) {
                s += ",\n";
            }
        }
        s += "};\n";
        s += "   return (PExModulationTarget_t *)&PExModulationSources[0][0];\n";
        s += "   };\n";

        return s;
    }

    String GenerateParamInitCode3(String ClassName) {
        int s = patch.getParameterInstances().size();
        String c = "   static int32_t * GetInitParams(void){\n"
                + "      static const int32_t p[" + s + "]= {\n";
        for (int i = 0; i < s; i++) {
            c += "      " + patch.getParameterInstances().get(i).GetValueRaw();
            if (i != s - 1) {
                c += ",\n";
            } else {
                c += "\n";
            }
        }
        c += "      };\n"
                + "      return (int32_t *)&p[0];\n"
                + "   }";
        return c;
    }

    String GenerateObjInitCodePlusPlusSub(String className, String parentReference) {
        String c = "";
        if (patch.getControllerInstance() != null) {
            String s = patch.getControllerInstance().getCInstanceName();
            if (!s.isEmpty()) {
                c += "   " + s + "_i.Init(" + parentReference;
                for (DisplayInstance i : patch.getControllerInstance().GetDisplayInstances()) {
                    if (i.display.getLength() > 0) {
                        c += ", ";
                        c += i.valueName("");
                    }
                }
                c += " );\n";
            }
        }

        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            String s = o.getCInstanceName();
            if (!s.isEmpty()) {
                c += "   " + o.getCInstanceName() + "_i.Init(" + parentReference;
                for (DisplayInstance i : o.GetDisplayInstances()) {
                    if (i.display.getLength() > 0) {
                        c += ", ";
                        c += i.valueName("");
                    }
                }
                c += " );\n";
            }
        }
        c += "      int k;\n"
                + "      for (k = 0; k < NPEXCH; k++) {\n"
                + "        if (PExch[k].pfunction){\n"
                + "          (PExch[k].pfunction)(&PExch[k]);\n"
                + "        } else {\n"
                + "          PExch[k].finalvalue = PExch[k].value;\n"
                + "        }\n"
                + "      }\n";
        return c;
    }

    String GenerateParamInitCodePlusPlusSub(String className, String parentReference) {
        String c = "";
        c += "   int i;\n";
        c += "   int j;\n";
        c += "   const int32_t *p;\n";
        c += "   p = GetInitParams();\n";
        c += "   for(j=0;j<" + patch.getParameterInstances().size() + ";j++){\n";
        c += "      PExch[j].value = p[j];\n";
        c += "      PExch[j].modvalue = p[j];\n";
        c += "      PExch[j].signals = 0;\n";
        c += "      PExch[j].pfunction = 0;\n";
//        c += "      PExch[j].finalvalue = p[j];\n"; /*TBC*/
        c += "   }\n";
        c += "   int32_t *pp = &PExModulationPrevVal[0][0];\n";
        c += "   for(j=0;j<attr_poly*NMODULATIONSOURCES;j++){\n";
        c += "      *pp = 0; pp++;\n";
        c += "   }\n";
        c += "     displayVector[0] = 0x446F7841;\n"; // "AxoD"
        c += "     displayVector[1] = 0;\n";
        c += "     displayVector[2] = " + patch.getDisplayDataLength() + ";\n";
        return c;
    }

    String GenerateInitCodePlusPlus(String className) {
        String c = "";
        c += "/* init */\n";
        c += "void Init() {\n";
        c += GenerateParamInitCodePlusPlusSub("", "this");
        c += GenerateObjInitCodePlusPlusSub("", "this");
        c += "}\n\n";
        return c;
    }

    String GenerateDisposeCodePlusPlusSub(String className) {
        // reverse order
        String c = "";
        int l = patch.getObjectInstances().size();
        for (int i = l - 1; i >= 0; i--) {
            AxoObjectInstanceAbstract o = patch.getObjectInstances().get(i);
            String s = o.getCInstanceName();
            if (!s.isEmpty()) {
                c += "   " + o.getCInstanceName() + "_i.Dispose();\n";
            }
        }
        if (patch.getControllerInstance() != null) {
            String s = patch.getControllerInstance().getCInstanceName();
            if (!s.isEmpty()) {
                c += "   " + patch.getControllerInstance().getCInstanceName() + "_i.Dispose();\n";
            }
        }

        return c;
    }

    String GenerateDisposeCodePlusPlus(String className) {
        String c = "";
        c += "/* dispose */\n";
        c += "void Dispose() {\n";
        c += GenerateDisposeCodePlusPlusSub(className);
        c += "}\n\n";
        return c;
    }

    String GenerateDSPCodePlusPlusSub(String ClassName, boolean enableOnParent) {
        String c = "";
        c += "//--------- <nets> -----------//\n";
        for (Net n : patch.getNets()) {
            if (n.CType() != null) {
                c += "    " + n.CType() + " " + n.CName() + ";\n";
            } else {
                Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.INFO, "Net has no data type!");
            }
        }
        c += "//--------- </nets> ----------//\n";
        c += "//--------- <zero> ----------//\n";
        c += "  int32_t UNCONNECTED_OUTPUT;\n";
        c += "  static const int32_t UNCONNECTED_INPUT=0;\n";
        c += "  static const int32buffer zerobuffer = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};\n";
        c += "  int32buffer UNCONNECTED_OUTPUT_BUFFER;\n";
        c += "//--------- </zero> ----------//\n";

        c += "//--------- <controller calls> ----------//\n";
        if (patch.getControllerInstance() != null) {
            c += GenerateDSPCodePlusPlusSubObj(patch.getControllerInstance(), ClassName, enableOnParent);
        }
        c += "//--------- <object calls> ----------//\n";
        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            c += GenerateDSPCodePlusPlusSubObj(o, ClassName, enableOnParent);
        }
        c += "//--------- </object calls> ----------//\n";

        c += "//--------- <net latch copy> ----------//\n";
        for (Net n : patch.getNets()) {
            // check if net has multiple sources
            if (n.NeedsLatch()) {
                if (n.GetDataType() != null) {
                    c += n.GetDataType().GenerateCopyCode(n.CName() + "Latch", n.CName());
                } else {
                    Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.SEVERE, "Only inlets connected on net!");
                }
            }
        }
        c += "//--------- </net latch copy> ----------//\n";
        return c;
    }

    String GenerateDSPCodePlusPlusSubObj(AxoObjectInstanceAbstract o, String ClassName, boolean enableOnParent) {
        String c = "";
        String s = o.getCInstanceName();
        if (s.isEmpty()) {
            return c;
        }
        c += "  " + o.getCInstanceName() + "_i.dsp(";
//            c += "  " + o.GenerateDoFunctionName() + "(this";
        boolean needsComma = false;
        for (InletInstance i : o.GetInletInstances()) {
            if (needsComma) {
                c += ", ";
            }
            Net n = patch.GetNet(i);
            if ((n != null) && (n.isValidNet())) {
                if (i.GetDataType().equals(n.GetDataType())) {
                    if (n.NeedsLatch()
                            && (patch.getObjectInstances().indexOf(n.getSource().get(0).GetObjectInstance()) >= patch.getObjectInstances().indexOf(o))) {
                        c += n.CName() + "Latch";
                    } else {
                        c += n.CName();
                    }
                } else if (n.NeedsLatch()
                        && (patch.getObjectInstances().indexOf(n.getSource().get(0).GetObjectInstance()) >= patch.getObjectInstances().indexOf(o))) {
                    c += n.GetDataType().GenerateConversionToType(i.GetDataType(), n.CName() + "Latch");
                } else {
                    c += n.GetDataType().GenerateConversionToType(i.GetDataType(), n.CName());
                }
            } else if (n == null) { // unconnected input
                c += i.GetDataType().GenerateSetDefaultValueCode();
            } else if (!n.isValidNet()) {
                c += i.GetDataType().GenerateSetDefaultValueCode();
                Logger.getLogger(PlatformAxoloti.class.getName()).log(Level.SEVERE, "Patch contains invalid net! {0}", i.objname + ":" + i.getInletname());
            }
            needsComma = true;
        }
        for (OutletInstance i : o.GetOutletInstances()) {
            if (needsComma) {
                c += ", ";
            }
            Net n = patch.GetNet(i);
            if ((n != null) && n.isValidNet()) {
                if (n.IsFirstOutlet(i)) {
                    c += n.CName();
                } else {
                    c += n.CName() + "+";
                }
            } else {
                c += i.GetDataType().UnconnectedSink();
            }
            needsComma = true;
        }
        for (ParameterInstance i : o.getParameterInstances()) {
            if (i.parameter.PropagateToChild == null) {
                if (needsComma) {
                    c += ", ";
                }
                c += i.variableName("", false);
                needsComma = true;
            }
        }
        for (DisplayInstance i : o.GetDisplayInstances()) {
            if (i.display.getLength() > 0) {
                if (needsComma) {
                    c += ", ";
                }
                c += i.valueName("");
                needsComma = true;
            }
        }
        c += ");\n";
        return c;
    }

    String GenerateMidiInCodePlusPlus() {
        String c = "";
        if (patch.getControllerInstance() != null) {
            c += patch.getControllerInstance().GenerateCallMidiHandler();
        }
        for (AxoObjectInstanceAbstract o : patch.getObjectInstances()) {
            c += o.GenerateCallMidiHandler();
        }
        return c;
    }

    String GenerateDSPCodePlusPlus(String ClassName, boolean enableOnParent) {
        String c;
        c = "/* krate */\n";
        c += "void dsp (void) {\n";
        c += "  int i;\n";
        c += "  for(i=0;i<BUFSIZE;i++) AudioOutputLeft[i]=0;\n";
        c += "  for(i=0;i<BUFSIZE;i++) AudioOutputRight[i]=0;\n";
        c += GenerateDSPCodePlusPlusSub(ClassName, enableOnParent);
        c += "}\n\n";
        return c;
    }

    String GenerateMidiCodePlusPlus(String ClassName) {
        String c = "";
        c += "void MidiInHandler(midi_device_t dev, uint8_t port,uint8_t status, uint8_t data1, uint8_t data2){\n";
        c += GenerateMidiInCodePlusPlus();
        c += "}\n\n";
        return c;
    }

    String GeneratePatchCodePlusPlus(String ClassName) {
        String c = "";
        c += "};\n\n";
        c += "static rootc root;\n";

        c += "void PatchProcess( int32_t * inbuf, int32_t * outbuf) {\n"
                + "  int i;\n"
                + "  for(i=0;i<BUFSIZE;i++){\n"
                + "    AudioInputLeft[i] = inbuf[i*2]>>4;\n"
                + "    switch(AudioInputMode) {\n"
                + "       case A_MONO:\n"
                + "             AudioInputRight[i] = AudioInputLeft[i];break;\n"
                + "       case A_BALANCED:\n"
                + "             AudioInputLeft[i] = (AudioInputLeft[i] - (inbuf[i*2+1]>>4) ) >> 1;\n"
                + "             AudioInputRight[i] = AudioInputLeft[i];"
                + "             break;\n"
                + "       case A_STEREO:\n"
                + "       default:\n"
                + "             AudioInputRight[i] = inbuf[i*2+1]>>4;\n"
                + "     }\n"
                + "  }\n"
                + "  root.dsp();\n";
        if (patch.getSettings().getSaturate()) {
            c += "  for(i=0;i<BUFSIZE;i++){\n"
                    + "    outbuf[i*2] = __SSAT(AudioOutputLeft[i],28)<<4;\n"
                    + "    switch(AudioOutputMode) {\n"
                    + "       case A_MONO:\n"
                    + "             outbuf[i*2+1] = 0;break;\n"
                    + "       case A_BALANCED:\n"
                    + "             outbuf[i*2+1] = ~ outbuf[i*2];break;\n"
                    + "       case A_STEREO:\n"
                    + "       default:\n"
                    + "             outbuf[i*2+1] = __SSAT(AudioOutputRight[i],28)<<4;\n"
                    + "     }\n"
                    + "  }\n";
        } else {
            c += "  for(i=0;i<BUFSIZE;i++){\n"
                    + "    outbuf[i*2] = AudioOutputLeft[i];\n"
                    + "    switch(AudioOutputMode) {\n"
                    + "       case A_MONO:\n"
                    + "             outbuf[i*2+1] = 0;break;\n"
                    + "       case A_BALANCED:\n"
                    + "             outbuf[i*2+1] = ~ outbuf[i*2];break;\n"
                    + "       case A_STEREO:\n"
                    + "       default:\n"
                    + "             outbuf[i*2+1] = AudioOutputRight[i];\n"
                    + "     }\n"
                    + "  }\n";
        }
        c += "}\n\n";

        c += "void ApplyPreset(int32_t i) {\n"
                + "   root.ApplyPreset(i);\n"
                + "}\n\n";

        c += "void PatchMidiInHandler(midi_device_t dev, uint8_t port, uint8_t status, uint8_t data1, uint8_t data2){\n"
                + "  root.MidiInHandler(dev, port, status, data1, data2);\n"
                + "}\n\n";

        c += "typedef void (*funcp_t)(void);\n"
                + "typedef funcp_t * funcpp_t;\n"
                + "extern funcp_t __ctor_array_start;\n"
                + "extern funcp_t __ctor_array_end;"
                + "extern funcp_t __dtor_array_start;\n"
                + "extern funcp_t __dtor_array_end;";

        c += "void PatchDispose( ) {\n"
                + "  root.Dispose();\n"
                + "  {\n"
                + "    funcpp_t fpp = &__dtor_array_start;\n"
                + "    while (fpp < &__dtor_array_end) {\n"
                + "      (*fpp)();\n"
                + "      fpp++;\n"
                + "    }\n"
                + "  }\n"
                + "}\n\n";

        c += "void xpatch_init2(int fwid)\n"
                + "{\n"
                + "  if (fwid != 0x" + MainFrame.mainframe.LinkFirmwareID + ") {\n"
                + "    return;"
                + "  }\n"
                + "  extern uint32_t _pbss_start;\n"
                + "  extern uint32_t _pbss_end;\n"
                + "  volatile uint32_t *p;\n"
                + "  for(p=&_pbss_start;p<&_pbss_end;p++) *p=0;\n"
                + "  {\n"
                + "    funcpp_t fpp = &__ctor_array_start;\n"
                + "    while (fpp < &__ctor_array_end) {\n"
                + "      (*fpp)();\n"
                + "      fpp++;\n"
                + "    }\n"
                + "  }\n"
                + "  patchMeta.npresets = " + patch.getSettings().GetNPresets() + ";\n"
                + "  patchMeta.npreset_entries = " + patch.getSettings().GetNPresetEntries() + ";\n"
                + "  patchMeta.pPresets = (PresetParamChange_t*) root.GetPresets();\n"
                + "  patchMeta.pPExch = &root.PExch[0];\n"
                + "  patchMeta.pDisplayVector = &root.displayVector[0];\n"
                + "  patchMeta.numPEx = " + patch.getParameterInstances().size() + ";\n"
                + "  patchMeta.patchID = " + patch.GetIID() + ";\n"
                + "  root.Init();\n"
                + "  patchMeta.fptr_applyPreset = ApplyPreset;\n"
                + "  patchMeta.fptr_patch_dispose = PatchDispose;\n"
                + "  patchMeta.fptr_MidiInHandler = PatchMidiInHandler;\n"
                + "  patchMeta.fptr_dsp_process = PatchProcess;\n"
                + "}\n";
        return c;
    }

}
