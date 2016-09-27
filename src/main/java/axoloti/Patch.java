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
package axoloti;

import axoloti.displays.DisplayInstance;
import axoloti.inlets.InletInstance;
import axoloti.iolet.IoletAbstract;
import axoloti.object.AxoObject;
import axoloti.object.AxoObjectAbstract;
import axoloti.object.AxoObjectInstance;
import axoloti.object.AxoObjectInstanceAbstract;
import axoloti.object.AxoObjectInstanceComment;
import axoloti.object.AxoObjectInstanceHyperlink;
import axoloti.object.AxoObjectInstancePatcher;
import axoloti.object.AxoObjectInstancePatcherObject;
import axoloti.object.AxoObjectInstanceZombie;
import axoloti.object.AxoObjectPatcher;
import axoloti.object.AxoObjectPatcherObject;
import axoloti.object.AxoObjectZombie;
import axoloti.object.AxoObjects;
import axoloti.outlets.OutletInstance;
import axoloti.parameters.ParameterInstance;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.xml.*;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Complete;
import org.simpleframework.xml.core.Persist;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.core.Validate;
import org.simpleframework.xml.strategy.Strategy;
import qcmds.QCmdProcessor;
import qcmds.QCmdRecallPreset;

/**
 *
 * @author Johannes Taelman
 */
@Root
public class Patch {

    @Attribute(required = false)
    String appVersion;
    private @ElementListUnion({
        @ElementList(entry = "obj", type = AxoObjectInstance.class, inline = true, required = false),
        @ElementList(entry = "patcher", type = AxoObjectInstancePatcher.class, inline = true, required = false),
        @ElementList(entry = "patchobj", type = AxoObjectInstancePatcherObject.class, inline = true, required = false),
        @ElementList(entry = "comment", type = AxoObjectInstanceComment.class, inline = true, required = false),
        @ElementList(entry = "hyperlink", type = AxoObjectInstanceHyperlink.class, inline = true, required = false),
        @ElementList(entry = "zombie", type = AxoObjectInstanceZombie.class, inline = true, required = false)})
    ArrayList<AxoObjectInstanceAbstract> objectinstances = new ArrayList<AxoObjectInstanceAbstract>();
    @ElementList(name = "nets")
    protected ArrayList<Net> nets = new ArrayList<Net>();
    @Element(required = false)
    private PatchSettings settings;
    @Element(required = false, data = true)
    protected String notes = "";
    @Element(required = false)
    Rectangle windowPos;
    private String FileNamePath;
    PatchFrame patchframe;
    private ArrayList<ParameterInstance> ParameterInstances = new ArrayList<ParameterInstance>();
    private ArrayList<DisplayInstance> DisplayInstances = new ArrayList<DisplayInstance>();
    protected ArrayList<Modulator> Modulators = new ArrayList<Modulator>();
    public int presetNo = 0;
    boolean locked = false;
    private boolean dirty = false;
    @Element(required = false)
    private String helpPatch;

    // patch this patch is contained in
    private Patch container = null;
    private AxoObjectInstanceAbstract controllerinstance;

    public boolean presetUpdatePending = false;

    private List<String> previousStates = new ArrayList<String>();
    private int currentState = 0;

    private IPlatform platform;

    public ArrayList<DisplayInstance> getDisplayInstances() {
        return DisplayInstances;
    }

    public ArrayList<Modulator> getModulators() {
        return Modulators;
    }

    public AxoObjectInstanceAbstract getControllerInstance() {
        return controllerinstance;
    }

    public int getDisplayDataLength() {
        return displayDataLength;
    }

    public ArrayList<AxoObjectInstanceAbstract> getObjectInstances() {
        return objectinstances;
    }

    public void setControllerInstance(AxoObjectInstanceAbstract controllerinstance) {
        this.controllerinstance = controllerinstance;
    }

    public ArrayList<Net> getNets() {
        return nets;
    }

    public String getHelpPatch() {
        return helpPatch;
    }

    public void setSettings(PatchSettings settings) {
        this.settings = settings;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    static public class PatchVersionException
            extends RuntimeException {

        PatchVersionException(String msg) {
            super(msg);
        }
    }

    private static final int AVX = getVersionX(Version.AXOLOTI_SHORT_VERSION),
            AVY = getVersionY(Version.AXOLOTI_SHORT_VERSION),
            AVZ = getVersionZ(Version.AXOLOTI_SHORT_VERSION);

    private static int getVersionX(String vS) {
        if (vS != null) {
            int i = vS.indexOf('.');
            if (i > 0) {
                String v = vS.substring(0, i);
                try {
                    return Integer.valueOf(v);
                } catch (NumberFormatException e) {
                }
            }
        }
        return -1;
    }

    private static int getVersionY(String vS) {
        if (vS != null) {
            int i = vS.indexOf('.');
            if (i > 0) {
                int j = vS.indexOf('.', i + 1);
                if (j > 0) {
                    String v = vS.substring(i + 1, j);
                    try {
                        return Integer.valueOf(v);
                    } catch (NumberFormatException e) {

                    }
                }
            }
        }
        return -1;
    }

    private static int getVersionZ(String vS) {
        if (vS != null) {
            int i = vS.indexOf('.');
            if (i > 0) {
                int j = vS.indexOf('.', i + 1);
                if (j > 0) {
                    String v = vS.substring(j + 1);
                    try {
                        return Integer.valueOf(v);
                    } catch (NumberFormatException e) {

                    }
                }
            }
        }
        return -1;
    }

    @Validate
    public void Validate() {
        // called after deserialializtion, stops validation
        if (appVersion != null
                && !appVersion.equals(Version.AXOLOTI_SHORT_VERSION)) {
            int vX = getVersionX(appVersion);
            int vY = getVersionY(appVersion);
            int vZ = getVersionZ(appVersion);

            if (AVX > vX) {
                return;
            }
            if (AVX == vX) {
                if (AVY > vY) {
                    return;
                }
                if (AVY == vY) {
                    if (AVZ > vZ) {
                        return;
                    }
                    if (AVZ == vZ) {
                        return;
                    }
                }
            }

            throw new PatchVersionException(appVersion);
        }
    }

    @Complete
    public void Complete() {
        // called after deserialializtion
    }

    @Persist
    public void Persist() {
        // called prior to serialization
        appVersion = Version.AXOLOTI_SHORT_VERSION;
    }

    MainFrame GetMainFrame() {
        return MainFrame.mainframe;
    }

    public QCmdProcessor GetQCmdProcessor() {
        if (patchframe == null) {
            return null;
        }
        return patchframe.qcmdprocessor;
    }

    public PatchSettings getSettings() {
        return settings;
    }

    // IPatchTarget - start
    public void GoLive() {
        platform.GoLive();
    }

    public void WriteCode() {
        platform.WriteCode();
    }

    public AxoObject GenerateAxoObj(AxoObject template) {
        return platform.GenerateAxoObj(template);
    }

    public void UploadDependentFiles() {
        platform.UploadDependentFiles();
    }

    public void Compile() {
        platform.Compile();
    }

    // IPatchTarget - end
    public int[] DistillPreset(int i) {
        int[] pdata;
        pdata = new int[getSettings().GetNPresetEntries() * 2];
        for (int j = 0; j < getSettings().GetNPresetEntries(); j++) {
            pdata[j * 2] = -1;
        }
        int index = 0;
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            for (ParameterInstance param : o.getParameterInstances()) {
                ParameterInstance p7 = (ParameterInstance) param;
                Preset p = p7.GetPreset(i);
                if (p != null) {
                    pdata[index * 2] = p7.getIndex();
                    pdata[index * 2 + 1] = p.value.getRaw();
                    index++;
                    if (index == getSettings().GetNPresetEntries()) {
                        Logger.getLogger(Patch.class.getName()).log(Level.SEVERE, "more than {0}entries in preset, skipping...", getSettings().GetNPresetEntries());
                        return pdata;
                    }
                }
            }
        }
        /*
         System.out.format("preset data : %d\n",i);
         for(int j=0;j<pdata.length/2;j++){
         System.out.format("  %d : %d\n",pdata[j*2],pdata[j*2+1] );
         }
         */
        return pdata;
    }

    public void ShowCompileFail() {
        Unlock();
    }

    void ShowDisconnect() {
        if (patchframe != null) {
            patchframe.ShowDisconnect();
        }
    }

    void ShowConnect() {
        if (patchframe != null) {
            patchframe.ShowConnect();
        }
    }

    public void setFileNamePath(String FileNamePath) {
        this.FileNamePath = FileNamePath;
    }

    public String getFileNamePath() {
        return FileNamePath;
    }

    public Patch() {
        super();
        platform = MainFrame.prefs.getPlatform(this);
    }

    public void PostContructor() {
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            o.patch = this;
            AxoObjectAbstract t = o.resolveType();
            if ((t != null) && (t.providesModulationSource())) {
                o.PostConstructor();

                Modulator[] m = t.getModulators();
                if (getModulators() == null) {
                    Modulators = new ArrayList<Modulator>();
                }
                for (Modulator mm : m) {
                    mm.objinst = o;
                    getModulators().add(mm);
                }
            }
        }

        ArrayList<AxoObjectInstanceAbstract> obj2 = (ArrayList<AxoObjectInstanceAbstract>) getObjectInstances().clone();
        for (AxoObjectInstanceAbstract o : obj2) {
            AxoObjectAbstract t = o.getType();
            if ((t != null) && (!t.providesModulationSource())) {
                o.patch = this;
                o.PostConstructor();
                //System.out.println("Obj added " + o.getInstanceName());
            } else if (t == null) {
                //o.patch = this;
                getObjectInstances().remove(o);
                AxoObjectInstanceZombie zombie = new AxoObjectInstanceZombie(new AxoObjectZombie(), this, o.getInstanceName(), new Point(o.getX(), o.getY()));
                zombie.patch = this;
                zombie.typeName = o.typeName;
                zombie.PostConstructor();
                getObjectInstances().add(zombie);
            }
        }
        ArrayList<Net> nets2 = (ArrayList<Net>) getNets().clone();
        for (Net n : nets2) {
            n.patch = this;
            n.PostConstructor();
        }
        PromoteOverloading(true);
        ShowPreset(0);
        if (getSettings() == null) {
            setSettings(new PatchSettings());
        }
        ClearDirty();
        saveState();
    }

    public ArrayList<ParameterInstance> getParameterInstances() {
        return ParameterInstances;
    }

    public AxoObjectInstanceAbstract GetObjectInstance(String n) {
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            if (n.equals(o.getInstanceName())) {
                return o;
            }
        }
        return null;
    }

    public void ClearDirty() {
        setDirty(false);
    }

    public void SetDirty() {
        SetDirty(true);
    }

    public void SetDirty(boolean shouldSaveState) {
        setDirty(true);

        if (container != null) {
            container.SetDirty(shouldSaveState);
        }
        if (shouldSaveState) {
            currentState += 1;
            saveState();
        }
        if (patchframe != null) {
            patchframe.updateUndoRedoEnabled();
        }
    }

    public boolean isDirty() {
        return dirty;
    }

    public Patch container() {
        return container;
    }

    public void container(Patch c) {
        container = c;
    }

    public AxoObjectInstanceAbstract AddObjectInstance(AxoObjectAbstract obj, Point loc) {
        if (!IsLocked()) {
            if (obj == null) {
                Logger.getLogger(Patch.class.getName()).log(Level.SEVERE, "AddObjectInstance NULL");
                return null;
            }
            int i = 1;
            String n = obj.getDefaultInstanceName() + "_";
            while (GetObjectInstance(n + i) != null) {
                i++;
            }
            AxoObjectInstanceAbstract objinst = obj.CreateInstance(this, n + i, loc);
            SetDirty();

            Modulator[] m = obj.getModulators();
            if (m != null) {
                if (getModulators() == null) {
                    Modulators = new ArrayList<Modulator>();
                }
                for (Modulator mm : m) {
                    mm.objinst = objinst;
                    getModulators().add(mm);
                }
            }

            return objinst;
        } else {
            Logger.getLogger(Patch.class.getName()).log(Level.INFO, "Can't add instance: locked!");
        }
        return null;
    }

    public Net GetNet(IoletAbstract io) {
        for (Net net : getNets()) {
            for (InletInstance d : net.getDest()) {
                if (d == io) {
                    return net;
                }
            }

            for (OutletInstance d : net.getSource()) {
                if (d == io) {
                    return net;
                }
            }
        }
        return null;
    }

    /*
     private boolean CompatType(DataType source, DataType d2){
     if (d1 == d2) return true;
     if ((d1 == DataType.bool32)&&(d2 == DataType.frac32)) return true;
     if ((d1 == DataType.frac32)&&(d2 == DataType.bool32)) return true;
     return false;
     }*/
    public Net AddConnection(InletInstance il, OutletInstance ol) {
        if (!IsLocked()) {
            if (il.GetObjectInstance().patch != this) {
                Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't connect: different patch");
                return null;
            }
            if (ol.GetObjectInstance().patch != this) {
                Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't connect: different patch");
                return null;
            }
            Net n1, n2;
            n1 = GetNet(il);
            n2 = GetNet(ol);
            if ((n1 == null) && (n2 == null)) {
                Net n = new Net(this);
                getNets().add(n);
                n.connectInlet(il);
                n.connectOutlet(ol);
                Logger.getLogger(Patch.class.getName()).log(Level.FINE, "connect: new net added");
                return n;
            } else if (n1 == n2) {
                Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't connect: already connected");
                return null;
            } else if ((n1 != null) && (n2 == null)) {
                if (n1.getSource().isEmpty()) {
                    Logger.getLogger(Patch.class.getName()).log(Level.FINE, "connect: adding outlet to inlet net");
                    n1.connectOutlet(ol);
                    return n1;
                } else {
                    disconnect(il);
                    Net n = new Net(this);
                    getNets().add(n);
                    n.connectInlet(il);
                    n.connectOutlet(ol);
                    Logger.getLogger(Patch.class.getName()).log(Level.FINE, "connect: replace inlet with new net");
                    return n;
                }
            } else if ((n1 == null) && (n2 != null)) {
                n2.connectInlet(il);
                Logger.getLogger(Patch.class.getName()).log(Level.FINE, "connect: add additional outlet");
                return n2;
            } else if ((n1 != null) && (n2 != null)) {
                // inlet already has connect, and outlet has another
                // replace 
                disconnect(il);
                n2.connectInlet(il);
                Logger.getLogger(Patch.class.getName()).log(Level.FINE, "connect: replace inlet with existing net");
                return n2;
            }
        } else {
            Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't add connection: locked");
        }
        return null;
    }

    public Net AddConnection(InletInstance il, InletInstance ol) {
        if (!IsLocked()) {
            if (il == ol) {
                Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't connect: same inlet");
                return null;
            }
            if (il.GetObjectInstance().patch != this) {
                Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't connect: different patch");
                return null;
            }
            if (ol.GetObjectInstance().patch != this) {
                Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't connect: different patch");
                return null;
            }
            Net n1, n2;
            n1 = GetNet(il);
            n2 = GetNet(ol);
            if ((n1 == null) && (n2 == null)) {
                Net n = new Net(this);
                getNets().add(n);
                n.connectInlet(il);
                n.connectInlet(ol);
                Logger.getLogger(Patch.class.getName()).log(Level.FINE, "connect: new net added");
                return n;
            } else if (n1 == n2) {
                Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't connect: already connected");
            } else if ((n1 != null) && (n2 == null)) {
                n1.connectInlet(ol);
                Logger.getLogger(Patch.class.getName()).log(Level.FINE, "connect: inlet added");
                return n1;
            } else if ((n1 == null) && (n2 != null)) {
                n2.connectInlet(il);
                Logger.getLogger(Patch.class.getName()).log(Level.FINE, "connect: inlet added");
                return n2;
            } else if ((n1 != null) && (n2 != null)) {
                Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't connect: both inlets included in net");
                return null;
            }
        } else {
            Logger.getLogger(Patch.class.getName()).log(Level.INFO, "can't add connection: locked!");
        }
        return null;
    }

    public Net disconnect(IoletAbstract io) {
        if (!IsLocked()) {
            Net n = GetNet(io);
            if (n != null) {
                if (io instanceof OutletInstance) {
                    n.getSource().remove((OutletInstance) io);
                } else if (io instanceof InletInstance) {
                    n.getDest().remove((InletInstance) io);
                }
                if (n.getSource().size() + n.getDest().size() <= 1) {
                    delete(n);
                }
                return n;
            }
        } else {
            Logger.getLogger(Patch.class.getName()).log(Level.INFO, "Can''t disconnect: locked!");
        }
        return null;
    }

    public Net delete(Net n) {
        if (!IsLocked()) {
            getNets().remove(n);
            return n;
        } else {
            Logger.getLogger(Patch.class.getName()).log(Level.INFO, "Can''t disconnect: locked!");
        }
        return null;
    }

    public void delete(AxoObjectInstanceAbstract o) {
        if (o == null) {
            return;
        }
        for (InletInstance ii : o.GetInletInstances()) {
            disconnect(ii);
        }
        for (OutletInstance oi : o.GetOutletInstances()) {
            disconnect(oi);
        }
        int i;
        for (i = getModulators().size() - 1; i >= 0; i--) {
            Modulator m1 = getModulators().get(i);
            if (m1.objinst == o) {
                getModulators().remove(m1);
                for (Modulation mt : m1.Modulations) {
                    mt.getDestination().removeModulation(mt);
                }
            }
        }
        getObjectInstances().remove(o);
        AxoObjectAbstract t = o.getType();
        if (o != null) {
            o.Close();
            t.DeleteInstance(o);
            t.removeObjectModifiedListener(o);
        }
    }

    public void updateModulation(Modulation n) {
        // find modulator
        Modulator m = null;
        for (Modulator m1 : getModulators()) {
            if (m1.objinst == n.getSource()) {
                if ((m1.name == null) || (m1.name.isEmpty())) {
                    m = m1;
                    break;
                } else if (m1.name.equals(n.modName)) {
                    m = m1;
                    break;
                }
            }
        }
        if (m == null) {
            throw new UnsupportedOperationException("Modulator not found");
        }
        if (!m.Modulations.contains(n)) {
            m.Modulations.add(n);
            System.out.println("modulation added to Modulator " + getModulators().indexOf(m));
        }
    }

    void deleteSelectedAxoObjInstances() {
        Logger.getLogger(Patch.class.getName()).log(Level.FINE, "deleteSelectedAxoObjInstances()");
        if (!IsLocked()) {
            boolean cont = true;
            while (cont) {
                cont = false;
                for (AxoObjectInstanceAbstract o : getObjectInstances()) {
                    if (o.IsSelected()) {
                        this.delete(o);
                        cont = true;
                        break;
                    }
                }
            }
            SetDirty();
        } else {
            Logger.getLogger(Patch.class.getName()).log(Level.INFO, "Can't delete: locked!");
        }
    }

    void PreSerialize() {
    }

    void saveState() {
        SortByPosition();
        PreSerialize();
        Serializer serializer = new Persister();
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        try {
            serializer.write(this, b);
            try {
                previousStates.set(currentState, b.toString());
                if (cleanDanglingStates) {
                    try {
                        // if we've saved a new edit
                        // after some undoing,
                        // cleanup dangling states
                        previousStates.subList(currentState + 1, previousStates.size()).clear();
                    } catch (IndexOutOfBoundsException e) {
                    }
                }
                this.cleanDanglingStates = true;
            } catch (IndexOutOfBoundsException e) {
                previousStates.add(b.toString());
            }
        } catch (Exception ex) {
            Logger.getLogger(AxoObjects.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cleanUpIntermediateChangeStates(int n) {
        int length = previousStates.size();
        if (length >= n) {
            previousStates.subList(length - n, length).clear();
            this.currentState -= n - 1;
            saveState();
        }
    }

    private boolean cleanDanglingStates = true;

    void loadState() {
        Serializer serializer = new Persister();
        ByteArrayInputStream b = new ByteArrayInputStream(previousStates.get(currentState).getBytes());
        try {
            Patch p = serializer.read(Patch.class, b);
            // prevent detached sub-windows
            Close();
            this.objectinstances = p.getObjectInstances();
            this.nets = p.nets;
            this.Modulators = p.Modulators;
            this.cleanDanglingStates = false;
            this.PostContructor();
            AdjustSize();
        } catch (Exception ex) {
            Logger.getLogger(AxoObjects.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    boolean save(File f) {
        SortByPosition();
        PreSerialize();
        Strategy strategy = new AnnotationStrategy();
        Serializer serializer = new Persister(strategy);
        try {
            serializer.write(this, f);
            MainFrame.prefs.addRecentFile(f.getAbsolutePath());
            setDirty(false);
        } catch (Exception ex) {
            Logger.getLogger(AxoObjects.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
//        if (settings == null) {
//            return;
//        }
//        if (settings.subpatchmode == SubPatchMode.no) {
//            return;
//        }
        /*
         String axoObjPath = getFileNamePath();
         int i = axoObjPath.lastIndexOf(".axp");
         axoObjPath = axoObjPath.substring(0, i) + ".axo";
         Logger.getLogger(Patch.class.getName()).log(Level.INFO, "exporting axo to " + axoObjPath);
         File f2 = new File(axoObjPath);
         ExportAxoObj(f2);
         MainFrame.axoObjects.LoadAxoObjects();
         */
    }

    private int displayDataLength = 0;

    void refreshIndexes() {
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            o.refreshIndex();
        }
        int i = 0;
        ParameterInstances = new ArrayList<ParameterInstance>();
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            for (ParameterInstance p : o.getParameterInstances()) {
                p.setIndex(i);
                i++;
                getParameterInstances().add(p);
            }
        }
        int offset = 0;
        // 0 : header
        // 1 : patchref
        // 2 : length

        DisplayInstances = new ArrayList<DisplayInstance>();
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            for (DisplayInstance p : o.GetDisplayInstances()) {
                p.setOffset(offset + 3);
                int l = p.getLength();
                offset += l;
                getDisplayInstances().add(p);
            }
        }
        displayDataLength = offset;
    }

    Dimension GetSize() {
        int nx = 0;
        int ny = 0;
        // negative coordinates?
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            Point p = o.getLocation();
            if (p.x < nx) {
                nx = p.x;
            }
            if (p.y < ny) {
                ny = p.y;
            }
        }
        if ((nx < 0) || (ny < 0)) { // move all to positive coordinates
            for (AxoObjectInstanceAbstract o : getObjectInstances()) {
                Point p = o.getLocation();
                o.SetLocation(p.x - nx, p.y - ny);
            }
        }

        int mx = 0;
        int my = 0;
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            Point p = o.getLocation();
            Dimension s = o.getSize();
            int px = p.x + s.width;
            int py = p.y + s.height;
            if (px > mx) {
                mx = px;
            }
            if (py > my) {
                my = py;
            }
        }
        return new Dimension(mx, my);
    }

    public void SortByPosition() {
        Collections.sort(this.getObjectInstances());
        refreshIndexes();
    }

    public Modulator GetModulatorOfModulation(Modulation modulation) {
        if (getModulators() == null) {
            return null;
        }
        for (Modulator m : getModulators()) {
            if (m.Modulations.contains(modulation)) {
                return m;
            }
        }
        return null;
    }

    public int GetModulatorIndexOfModulation(Modulation modulation) {
        if (getModulators() == null) {
            return -1;
        }
        for (Modulator m : getModulators()) {
            int i = m.Modulations.indexOf(modulation);
            if (i >= 0) {
                return i;
            }
        }
        return -1;
    }

    List<AxoObjectAbstract> GetUsedAxoObjects() {
        ArrayList<AxoObjectAbstract> aos = new ArrayList<AxoObjectAbstract>();
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            if (!aos.contains(o.getType())) {
                aos.add(o.getType());
            }
        }
        return aos;
    }

    public void AdjustSize() {
    }

    public HashSet<String> getIncludes() {
        HashSet<String> includes = new HashSet<String>();
        if (getControllerInstance() != null) {
            Set<String> i = getControllerInstance().getType().GetIncludes();
            if (i != null) {
                includes.addAll(i);
            }
        }
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            Set<String> i = o.getType().GetIncludes();
            if (i != null) {
                includes.addAll(i);
            }
        }

        return includes;
    }

    public HashSet<String> getDepends() {
        HashSet<String> depends = new HashSet<String>();
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            Set<String> i = o.getType().GetDepends();
            if (i != null) {
                depends.addAll(i);
            }
        }
        return depends;
    }

    int IID = -1; // iid identifies the patch

    public int GetIID() {
        return IID;
    }

    public void CreateIID() {
        java.util.Random r = new java.util.Random();
        IID = r.nextInt();
    }

    public void ShowPreset(int i) {
        presetNo = i;

        Collection<AxoObjectInstanceAbstract> c = (Collection<AxoObjectInstanceAbstract>) getObjectInstances().clone();
        for (AxoObjectInstanceAbstract o : c) {
            for (ParameterInstance p : o.getParameterInstances()) {
                p.ShowPreset(i);
            }
        }
        repaint();
    }

    /*
     void ApplyPreset(int i) { // OBSOLETE
     presetNo = i;
     if (presets == null) {
     presets = new ArrayList<>();
     }
     while (presets.size()<8) presets.add(new PresetObsolete());
     if (i>0) {
     PresetObsolete p = presets.get(i-1);
     for(AxoObjectInstance o:objectinstances){
     for(ParameterInstance a:o.parameterInstances){
     a.SetPresetState(false);
     a.ppc = null;
     }
     }
     for(PresetParameterChange ppc:p.paramchanges){
     ppc.ref.ppc = ppc;
     ppc.ref.SetValueRaw(ppc.newValue);
     ppc.ref.SetPresetState(true);
     }
     } else if (i==0) {
     if (initPreset == null){
     initPreset = new InitPreset();
     initPreset.patch = this;
     }
     for(AxoObjectInstance o:objectinstances){
     for(ParameterInstance a:o.parameterInstances){
     a.SetPresetState(false);
     a.ppc = null;
     }
     }
     for(PresetParameterChange ppc:initPreset.paramchanges){
     ppc.ref.ppc = ppc;
     ppc.ref.SetValueRaw(ppc.newValue);
     ppc.ref.SetPresetState(true);
     }
     SaveToInitPreset();
     } else {
     for(AxoObjectInstance o:objectinstances){
     for(ParameterInstance a:o.parameterInstances){
     a.SetPresetState(false);
     a.ppc = null;
     }
     }            
     }
     }
     */
    void ClearCurrentPreset() {
    }

    void CopyCurrentToInit() {
    }

    void DifferenceToPreset() {
        /*
         for(AxoObjectInstance o:objectinstances){
         for(ParameterInstance param:o.parameterInstances){
         // find corresponding in init
         PresetParameterChange ppc = null;
         for (PresetParameterChange ppc1:initPreset.paramchanges){
         if (ppc1.ref == param) {
         ppc=ppc1;
         break;
         }
         }
         if (ppc!=null) { // ppc = param in preset
         if (ppc.newValue != param.GetValueRaw()) {
         IncludeParameterInPreset(param);
         }
         }
         }
         }*/
    }

    /*
     PresetParameterChange IncludeParameterInPreset(ParameterInstance param) {
     if (presetNo>0){
     for (PresetParameterChange ppc:presets.get(presetNo-1).paramchanges){
     if (ppc.ref == param) return ppc;
     }
     PresetParameterChange ppc = new PresetParameterChange();
     ppc.newValue = param.GetValueRaw();
     ppc.paramName = param.getName();
     ppc.ref = param;
     presets.get(presetNo-1).paramchanges.add(ppc);
     param.SetPresetState(true);
     return ppc;
     }
     return null;
     }

     void ExcludeParameterFromPreset(ParameterInstance param) {
     if (presetNo>0)
     presets.get(presetNo-1).ExcludeParameter(param);
     }
    
     void SaveToInitPreset(){
     for(AxoObjectInstance o:objectinstances){
     for(ParameterInstance param:o.parameterInstances){
     PresetParameterChange ppc = null;
     for (PresetParameterChange ppc1:initPreset.paramchanges){
     if (ppc1.ref == param) {
     ppc=ppc1;
     break;
     }
     }
     if (ppc == null) {
     ppc = new PresetParameterChange();
     ppc.paramName = param.getName();
     ppc.ref = param;                    
     initPreset.paramchanges.add(ppc);
     }
     ppc.newValue = param.GetValueRaw();
     }
     }        
     }
     */
    //final int NPRESETS = 8;
    //final int NPRESET_ENTRIES = 32;
    void Upload() {
        platform.Upload();
    }

    public void Lock() {
        locked = true;
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            o.Lock();
        }
    }

    public void Unlock() {
        locked = false;
        ArrayList<AxoObjectInstanceAbstract> objInstsClone = (ArrayList<AxoObjectInstanceAbstract>) getObjectInstances().clone();
        for (AxoObjectInstanceAbstract o : objInstsClone) {
            o.Unlock();
        }
    }

    public boolean IsLocked() {
        return locked;
    }

    public AxoObjectInstanceAbstract ChangeObjectInstanceType1(AxoObjectInstanceAbstract obj, AxoObjectAbstract objType) {
        if ((obj instanceof AxoObjectInstancePatcher) && (objType instanceof AxoObjectPatcher)) {
            return obj;
        } else if ((obj instanceof AxoObjectInstancePatcherObject) && (objType instanceof AxoObjectPatcherObject)) {
            return obj;
        } else if (obj instanceof AxoObjectInstance) {
            String n = obj.getInstanceName();
            obj.setInstanceName(n + "____tmp");
            AxoObjectInstanceAbstract obj1 = AddObjectInstance(objType, obj.getLocation());
            if ((obj1 instanceof AxoObjectInstance)) {
                AxoObjectInstance new_obj = (AxoObjectInstance) obj1;
                AxoObjectInstance old_obj = (AxoObjectInstance) obj;
                new_obj.outletInstances = old_obj.outletInstances;
                new_obj.inletInstances = old_obj.inletInstances;
                new_obj.parameterInstances = old_obj.parameterInstances;
                new_obj.attributeInstances = old_obj.attributeInstances;
            }
            obj1.setName(n);
            return obj1;
        } else if (obj instanceof AxoObjectInstanceZombie) {
            String n = obj.getInstanceName();
            obj.setInstanceName(n + "____tmp");
            AxoObjectInstanceAbstract obj1 = AddObjectInstance(objType, obj.getLocation());
            if ((obj1 instanceof AxoObjectInstance)) {
                AxoObjectInstance new_obj = (AxoObjectInstance) obj1;
                AxoObjectInstanceZombie old_obj = (AxoObjectInstanceZombie) obj;
                new_obj.outletInstances = old_obj.outletInstances;
                new_obj.inletInstances = old_obj.inletInstances;
            }
            obj1.setName(n);
            return obj1;
        }
        return obj;
    }

    public AxoObjectInstanceAbstract ChangeObjectInstanceType(AxoObjectInstanceAbstract obj, AxoObjectAbstract objType) {
        AxoObjectInstanceAbstract obj1 = ChangeObjectInstanceType1(obj, objType);
        if (obj1 != obj) {
            obj1.PostConstructor();
            delete(obj);
            SetDirty();
        }
        return obj1;
    }

    void invalidate() {
    }

    void SetDSPLoad(int pct) {
    }

    public void repaint() {
    }

    public void RecallPreset(int i) {
        GetQCmdProcessor().AppendToQueue(new QCmdRecallPreset(i));
    }

    /**
     *
     * @param initial If true, only objects restored from object name reference
     * (not UUID) will promote to a variant with the same name.
     */
    public void PromoteOverloading(boolean initial) {
        refreshIndexes();
        Set<String> ProcessedInstances = new HashSet<String>();
        boolean p = true;
        while (p && !(ProcessedInstances.size() == objectinstances.size())) {
            p = false;
            for (AxoObjectInstanceAbstract o : getObjectInstances()) {
                if (!ProcessedInstances.contains(o.getInstanceName())) {
                    ProcessedInstances.add(o.getInstanceName());
                    if (!initial || o.isTypeWasAmbiguous()) {
                        o.PromoteToOverloadedObj();
                    }
                    p = true;
                    break;
                }
            }
        }
        if (!(ProcessedInstances.size() == objectinstances.size())) {
            for (AxoObjectInstanceAbstract o : getObjectInstances()) {
                if (!ProcessedInstances.contains(o.getInstanceName())) {
                    Logger.getLogger(Patch.class.getName()).log(Level.SEVERE, "PromoteOverloading : fault in {0}", o.getInstanceName());
                }
            }
        }
    }

    public InletInstance getInletByReference(String objname, String inletname) {
        if (objname == null) {
            return null;
        }
        if (inletname == null) {
            return null;
        }
        AxoObjectInstanceAbstract o = GetObjectInstance(objname);
        if (o == null) {
            return null;
        }
        return o.GetInletInstance(inletname);
    }

    public OutletInstance getOutletByReference(String objname, String outletname) {
        if (objname == null) {
            return null;
        }
        if (outletname == null) {
            return null;
        }
        AxoObjectInstanceAbstract o = GetObjectInstance(objname);
        if (o == null) {
            return null;
        }
        return o.GetOutletInstance(outletname);
    }

    public String GetCurrentWorkingDirectory() {
        if (getFileNamePath() == null) {
            return null;
        }
        int i = getFileNamePath().lastIndexOf(File.separatorChar);
        if (i < 0) {
            return null;
        }
        return getFileNamePath().substring(0, i);
    }

    public Rectangle getWindowPos() {
        return windowPos;
    }

    public PatchFrame getPatchframe() {
        return patchframe;
    }

    public String getNotes() {
        return notes;
    }

    public ArrayList<SDFileReference> GetDependendSDFiles() {
        ArrayList<SDFileReference> files = new ArrayList<SDFileReference>();
        for (AxoObjectInstanceAbstract o : getObjectInstances()) {
            ArrayList<SDFileReference> f2 = o.GetDependendSDFiles();
            if (f2 != null) {
                files.addAll(f2);
            }
        }
        return files;
    }

    public void UploadToSDCard() {
        platform.UploadToSDCard();
    }

    public void UploadToSDCard(String sdfilename) {
        platform.UploadToSDCard(sdfilename);
    }

    public String getSDCardPath() {
        String FileNameNoPath = getFileNamePath();
        String separator = System.getProperty("file.separator");
        int lastSeparatorIndex = FileNameNoPath.lastIndexOf(separator);
        if (lastSeparatorIndex > 0) {
            FileNameNoPath = FileNameNoPath.substring(lastSeparatorIndex + 1);
        }
        String FileNameNoExt = FileNameNoPath;
        if (FileNameNoExt.endsWith(".axp") || FileNameNoExt.endsWith(".axs") || FileNameNoExt.endsWith(".axh")) {
            FileNameNoExt = FileNameNoExt.substring(0, FileNameNoExt.length() - 4);
        }
        return FileNameNoExt;
    }

    public void Close() {
        Unlock();
        Collection<AxoObjectInstanceAbstract> c = (Collection<AxoObjectInstanceAbstract>) getObjectInstances().clone();
        for (AxoObjectInstanceAbstract o : c) {
            o.Close();
        }
    }

    public boolean canUndo() {
        return !this.IsLocked() && (currentState > 0);
    }

    public boolean canRedo() {
        return !this.IsLocked() && (currentState < previousStates.size() - 1);
    }

    public void undo() {
        if (canUndo()) {
            currentState -= 1;
            loadState();
            SetDirty(false);
            patchframe.updateUndoRedoEnabled();
        }
    }

    public void redo() {
        if (canRedo()) {
            currentState += 1;
            loadState();
            SetDirty(false);
            patchframe.updateUndoRedoEnabled();
        }
    }
}
