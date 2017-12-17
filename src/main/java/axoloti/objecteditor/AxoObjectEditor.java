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
package axoloti.objecteditor;

import axoloti.DocumentWindow;
import axoloti.DocumentWindowList;
import axoloti.MainFrame;
import axoloti.mvc.IView;
import axoloti.mvc.UndoUI;
import axoloti.object.AxoObject;
import axoloti.object.IAxoObject;
import axoloti.object.ObjectController;
import axoloti.property.Property;
import axoloti.utils.AxolotiLibrary;
import axoloti.utils.OSDetect;
import axoloti.utils.Preferences;
import java.awt.BorderLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 *
 * @author Johannes Taelman
 */
public final class AxoObjectEditor extends JFrame implements DocumentWindow, IView {

    final ObjectController controller;
    private String origXML;
    private final RSyntaxTextArea jTextAreaLocalData;
    private final RSyntaxTextArea jTextAreaInitCode;
    private final RSyntaxTextArea jTextAreaKRateCode;
    private final RSyntaxTextArea jTextAreaSRateCode;
    private final RSyntaxTextArea jTextAreaDisposeCode;
    private final RSyntaxTextArea jTextAreaMidiCode;

    private boolean readonly = false;

    UndoUI undoUi;

    static RSyntaxTextArea initCodeEditor(JPanel p) {
        RSyntaxTextArea rsta = new RSyntaxTextArea(20, 60);
        rsta.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
        rsta.setCodeFoldingEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(rsta);
        p.setLayout(new BorderLayout());
        p.add(sp);
        rsta.setVisible(true);
        return rsta;
    }

    @Override
    public ObjectController getController() {
        return controller;
    }

    AxoObject getModel() {
        return (AxoObject) (getController().getModel());
    }

    @Override
    public void modelPropertyChange(PropertyChangeEvent evt) {
        if (AxoObject.OBJ_AUTHOR.is(evt)) {
            jTextFieldAuthor.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_LICENSE.is(evt)) {
            jTextFieldLicense.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_DESCRIPTION.is(evt)) {
            jTextDesc.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_HELPPATCH.is(evt)) {
            jTextFieldHelp.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_LOCAL_DATA.is(evt)) {
            jTextAreaLocalData.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_INIT_CODE.is(evt)) {
            jTextAreaInitCode.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_KRATE_CODE.is(evt)) {
            jTextAreaKRateCode.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_SRATE_CODE.is(evt)) {
            jTextAreaSRateCode.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_DISPOSE_CODE.is(evt)) {
            jTextAreaDisposeCode.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_MIDI_CODE.is(evt)) {
            jTextAreaMidiCode.setText((String) evt.getNewValue());
        } else if (AxoObject.OBJ_ID.is(evt)) {
            setTitle((String) evt.getNewValue());
        }
        updateReferenceXML();
    }

    String CleanString(String s) {
        if (s == null) {
            return "";
        }
        s = s.trim();
        if (s.isEmpty()) {
            return "";
        }
        return s;
    }

    public void updateReferenceXML() {
        Serializer serializer = new Persister();
        ByteArrayOutputStream origOS = new ByteArrayOutputStream(2048);
        try {
            serializer.write(controller.getModel(), origOS);
        } catch (Exception ex) {
            Logger.getLogger(AxoObjectEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        origXML = origOS.toString();
        rSyntaxTextAreaXML.setText(origXML);
    }

    void Revert() {
        // needs review
        /*
        try {
            Serializer serializer = new Persister();
            AxoObject objrev = serializer.read(AxoObject.class, origXML);
            editObj.copy(objrev);
            Close();

        } catch (Exception ex) {
            Logger.getLogger(AxoObjectEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
    }

    void SetUndoablePropString(Property prop, String s) {
        String orig = (String) getController().getModelProperty(prop);
        if (s.equals(orig)) {
            return;
        }
        getController().addMetaUndo("edit " + prop.getFriendlyName());
        getController().setModelUndoableProperty(prop, s);
    }

    public AxoObjectEditor(ObjectController ctrl) {
        initComponents();
        this.controller = ctrl;
        if (OSDetect.getOS() == OSDetect.OS.MAC) {
            jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.TOP);
        }

        fileMenu1.initComponents();
        DocumentWindowList.RegisterWindow(this);
        jTextAreaLocalData = initCodeEditor(jPanelLocalData);
        jTextAreaInitCode = initCodeEditor(jPanelInitCode);
        jTextAreaKRateCode = initCodeEditor(jPanelKRateCode2);
        jTextAreaSRateCode = initCodeEditor(jPanelSRateCode);
        jTextAreaDisposeCode = initCodeEditor(jPanelDisposeCode);
        jTextAreaMidiCode = initCodeEditor(jPanelMidiCode2);
        setIconImage(new ImageIcon(getClass().getResource("/resources/axoloti_icon.png")).getImage());

        undoUi = new UndoUI(ctrl.getUndoManager());
        if (ctrl.getDocumentRoot() != null) {
            ctrl.getDocumentRoot().addUndoListener(undoUi);
        }
        jMenuEdit.add(undoUi.createMenuItemUndo());
        jMenuEdit.add(undoUi.createMenuItemRedo());

        initEditFromOrig();
        InletDefinitionsEditorPanel inlets = new InletDefinitionsEditorPanel(ctrl);
        OutletDefinitionsEditorPanel outlets = new OutletDefinitionsEditorPanel(ctrl);
        AttributeDefinitionsEditorPanel attrs = new AttributeDefinitionsEditorPanel(ctrl);
        ParamDefinitionsEditorPanel params = new ParamDefinitionsEditorPanel(ctrl);
        DisplayDefinitionsEditorPanel disps = new DisplayDefinitionsEditorPanel(ctrl);

        ctrl.addView(inlets);
        ctrl.addView(outlets);
        ctrl.addView(attrs);
        ctrl.addView(params);
        ctrl.addView(disps);

        inlets.initComponents(inletDefinitionsEditor1);
        outlets.initComponents(outletDefinitionsEditor1);
        attrs.initComponents(attributeDefinitionsEditorPanel1);
        params.initComponents(paramDefinitionsEditorPanel1);
        disps.initComponents(displayDefinitionsEditorPanel1);

        jTextFieldAuthor.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_AUTHOR, jTextFieldAuthor.getText());
            }
        });

        jTextFieldLicense.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_LICENSE, jTextFieldLicense.getText());
            }
        });

        jTextFieldHelp.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_HELPPATCH, jTextFieldHelp.getText().trim());
            }
        });

        jTextDesc.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_DESCRIPTION, jTextDesc.getText());
            }
        });

//        jLabelMidiPrototype.setText(AxoObjectInstance.MidiHandlerFunctionHeader);
        jTextAreaLocalData.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_LOCAL_DATA, CleanString(jTextAreaLocalData.getText()));
            }
        });
        jTextAreaInitCode.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_INIT_CODE, CleanString(jTextAreaInitCode.getText()));
            }
        });
        jTextAreaKRateCode.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_KRATE_CODE, CleanString(jTextAreaKRateCode.getText()));
            }
        });
        jTextAreaSRateCode.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_SRATE_CODE, CleanString(jTextAreaSRateCode.getText()));
            }
        });
        jTextAreaDisposeCode.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_DISPOSE_CODE, CleanString(jTextAreaDisposeCode.getText()));
            }
        });
        jTextAreaMidiCode.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                SetUndoablePropString(AxoObject.OBJ_MIDI_CODE, CleanString(jTextAreaMidiCode.getText()));
            }
        });
        rSyntaxTextAreaXML.setEditable(false);

        // is it from the factory?
        AxolotiLibrary sellib = null;
        for (AxolotiLibrary lib : Preferences.getPreferences().getLibraries()) {
            if (ctrl.getModel().getPath() != null && ctrl.getModel().getPath().startsWith(lib.getLocalLocation())) {

                if (sellib == null || sellib.getLocalLocation().length() < lib.getLocalLocation().length()) {
                    sellib = lib;
                }
            }
        }
        if (IsEmbeddedObj()) {
            jMenuItemSave.setEnabled(false);
            jLabelLibrary.setText("embedded");
            setTitle("embedded");
            // embedded objects have no use for help patches
            jTextFieldHelp.setVisible(false);
            jLabelHelp.setVisible(false);
        } else // normal objects
        if (sellib != null) {
            jMenuItemSave.setEnabled(!sellib.isReadOnly());
            if (sellib.isReadOnly()) {
                SetReadOnly(true);
                jLabelLibrary.setText(sellib.getId() + " (readonly)");
                setTitle(sellib.getId() + ":" + ctrl.getModel().getId() + " (readonly)");
            } else {
                jLabelLibrary.setText(sellib.getId());
                setTitle(sellib.getId() + ":" + ctrl.getModel().getId());
            }
        }

        jTextDesc.requestFocus();
        controller.addView(this);
    }

    boolean IsEmbeddedObj() {
        return controller.getModel().getPath().isEmpty();
    }

    void SetReadOnly(boolean readonly) {
        this.readonly = readonly;
        jTextDesc.setEditable(!readonly);
        jTextFieldAuthor.setEditable(!readonly);
        jTextFieldLicense.setEditable(!readonly);
        jTextFieldHelp.setEditable(!readonly);
        jTextAreaLocalData.setEditable(!readonly);
        jTextAreaInitCode.setEditable(!readonly);
        jTextAreaKRateCode.setEditable(!readonly);
        jTextAreaSRateCode.setEditable(!readonly);
        jTextAreaDisposeCode.setEditable(!readonly);
        jTextAreaMidiCode.setEditable(!readonly);
// FIXME: readonly
//        inletDefinitionsEditor1.setEditable(!readonly);
//        outletDefinitionsEditorPanel1.setEditable(!readonly);
//        paramDefinitionsEditorPanel1.setEditable(!readonly);
//        attributeDefinitionsEditorPanel1.setEditable(!readonly);
//        displayDefinitionsEditorPanel1.setEditable(!readonly);
    }

    void initFields() {
        jLabelName.setText(getModel().getCName());

        ((DefaultListModel) jListIncludes.getModel()).removeAllElements();
        if (getModel().includes != null) {
            for (String i : getModel().includes) {
                ((DefaultListModel) jListIncludes.getModel()).addElement(i);
            }
        }

        ((DefaultListModel) jListIncludes.getModel()).removeAllElements();
        if (getModel().depends != null) {
            for (String i : getModel().depends) {
                ((DefaultListModel) jListDepends.getModel()).addElement(i);
            }
        }

        if (getModel().modules != null) {
            for (String i : getModel().modules) {
                ((DefaultListModel) jListModules.getModel()).addElement(i);
            }
        }

    }

    boolean compareField(String oVal, String nVal) {
        String ov = oVal, nv = nVal;
        if (ov == null) {
            ov = "";
        }
        return ov.equals(nv);
    }

    boolean hasChanged() {
        Serializer serializer = new Persister();

        ByteArrayOutputStream editOS = new ByteArrayOutputStream(2048);
        try {
            serializer.write(getModel(), editOS);
        } catch (Exception ex) {
            Logger.getLogger(AxoObjectEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return !(origXML.equals(editOS.toString()));
    }

    public void initEditFromOrig() {
        initFields();
    }

    @Override
    public boolean askClose() {
        // if it's an embedded object ("patch/object"), assume the parent patch is saving
        if (IsEmbeddedObj()) {
            Close();
            return false;
        }
        // warn if changes, and its not an embedded object
        if (hasChanged()) {
            if (!readonly) {
                Object[] options = {"Yes", "Revert changes", "Cancel"};
                int n = JOptionPane.showOptionDialog(this,
                        "Unsaved changes, do you want to save?",
                        "Axoloti asks:",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                switch (n) {
                    case 0: // yes
                        jMenuItemSaveActionPerformed(null);
                        Close();
                        return false;
                    case 1: // revert
                        Revert();
                        Close();
                        return false;
                    case 2: // cancel
                    default: // closed
                        return true;
                }
            } else {
                Logger.getLogger(AxoObjectEditor.class.getName()).log(Level.SEVERE, null, "changed but readonly: should not happen");
                return true;
            }
        } else {
            // no changes
            Close();
            return false;
        }
    }

    public void Close() {
        DocumentWindowList.UnregisterWindow(this);
        dispose();
        getModel().CloseEditor();
    }

    public int getActiveTabIndex() {
        return this.jTabbedPane1.getSelectedIndex();
    }

    public void setActiveTabIndex(int n) {
        this.jTabbedPane1.setSelectedIndex(n);
    }

    boolean isCompositeObject() {
        if (getModel().getPath() == null) {
            return false;
        }
        int count = 0;
        for (IAxoObject o : MainFrame.axoObjects.ObjectList) {
            if (getModel().getPath().equalsIgnoreCase(o.getPath())) {
                count++;
            }
        }
        return (count > 1);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jInternalFrame1 = new javax.swing.JInternalFrame();
        jLabel4 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelOverview = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelLibrary = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabelName = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jTextFieldAuthor = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jTextFieldLicense = new javax.swing.JTextField();
        jLabelHelp = new javax.swing.JLabel();
        jTextFieldHelp = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        jTextDesc = new javax.swing.JTextArea();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListIncludes = new javax.swing.JList();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane12 = new javax.swing.JScrollPane();
        jListDepends = new javax.swing.JList();
        jLabel11 = new javax.swing.JLabel();
        jScrollPane14 = new javax.swing.JScrollPane();
        jListModules = new javax.swing.JList();
        inletDefinitionsEditor1 = new javax.swing.JPanel();
        outletDefinitionsEditor1 = new javax.swing.JPanel();
        attributeDefinitionsEditorPanel1 = new javax.swing.JPanel();
        paramDefinitionsEditorPanel1 = new javax.swing.JPanel();
        displayDefinitionsEditorPanel1 = new javax.swing.JPanel();
        jPanelLocalData = new javax.swing.JPanel();
        jPanelInitCode = new javax.swing.JPanel();
        jPanelKRateCode = new javax.swing.JPanel();
        jPanelKRateCode2 = new javax.swing.JPanel();
        jPanelSRateCode = new javax.swing.JPanel();
        jPanelDisposeCode = new javax.swing.JPanel();
        jPanelMidiCode = new javax.swing.JPanel();
        jLabelMidiPrototype = new javax.swing.JLabel();
        jPanelMidiCode2 = new javax.swing.JPanel();
        jPanelXML = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        rSyntaxTextAreaXML = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea();
        jLabel2 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu1 = new axoloti.menus.FileMenu();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItemSave = new javax.swing.JMenuItem();
        jMenuItemCopyToLibrary = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        windowMenu1 = new axoloti.menus.WindowMenu();
        helpMenu1 = new axoloti.menus.HelpMenu();

        jInternalFrame1.setVisible(true);

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(540, 400));
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                formWindowLostFocus(evt);
            }
        });
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.PAGE_AXIS));
        getContentPane().add(jLabel4);

        jPanel1.setPreferredSize(new java.awt.Dimension(640, 100));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.PAGE_AXIS));

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(640, 100));

        jPanelOverview.setLayout(new javax.swing.BoxLayout(jPanelOverview, javax.swing.BoxLayout.Y_AXIS));

        jPanel2.setLayout(new java.awt.GridLayout(5, 2));

        jLabel1.setText("Library:");
        jPanel2.add(jLabel1);

        jLabelLibrary.setText("library");
        jPanel2.add(jLabelLibrary);

        jLabel7.setText("Name:");
        jPanel2.add(jLabel7);

        jLabelName.setText("object name");
        jPanel2.add(jLabelName);

        jLabel8.setText("Author:");
        jPanel2.add(jLabel8);

        jTextFieldAuthor.setText("author");
        jPanel2.add(jTextFieldAuthor);

        jLabel9.setText("License:");
        jPanel2.add(jLabel9);

        jTextFieldLicense.setText("license");
        jPanel2.add(jTextFieldLicense);

        jLabelHelp.setText("Help patch");
        jPanel2.add(jLabelHelp);

        jTextFieldHelp.setText("help");
        jPanel2.add(jTextFieldHelp);

        jPanelOverview.add(jPanel2);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.Y_AXIS));

        jLabel10.setText("Description:");
        jPanel3.add(jLabel10);

        jTextDesc.setColumns(20);
        jTextDesc.setLineWrap(true);
        jTextDesc.setRows(5);
        jTextDesc.setWrapStyleWord(true);
        jScrollPane13.setViewportView(jTextDesc);

        jPanel3.add(jScrollPane13);

        jLabel5.setText("Includes");
        jPanel3.add(jLabel5);

        jListIncludes.setModel(new DefaultListModel());
        jScrollPane4.setViewportView(jListIncludes);

        jPanel3.add(jScrollPane4);

        jLabel6.setText("Dependencies");
        jPanel3.add(jLabel6);

        jListDepends.setModel(new DefaultListModel());
        jScrollPane12.setViewportView(jListDepends);

        jPanel3.add(jScrollPane12);

        jLabel11.setText("Modules");
        jPanel3.add(jLabel11);

        jListModules.setModel(new DefaultListModel());
        jScrollPane14.setViewportView(jListModules);

        jPanel3.add(jScrollPane14);

        jPanelOverview.add(jPanel3);

        jTabbedPane1.addTab("Overview", jPanelOverview);

        javax.swing.GroupLayout inletDefinitionsEditor1Layout = new javax.swing.GroupLayout(inletDefinitionsEditor1);
        inletDefinitionsEditor1.setLayout(inletDefinitionsEditor1Layout);
        inletDefinitionsEditor1Layout.setHorizontalGroup(
            inletDefinitionsEditor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 481, Short.MAX_VALUE)
        );
        inletDefinitionsEditor1Layout.setVerticalGroup(
            inletDefinitionsEditor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 433, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Inlets", inletDefinitionsEditor1);

        javax.swing.GroupLayout outletDefinitionsEditor1Layout = new javax.swing.GroupLayout(outletDefinitionsEditor1);
        outletDefinitionsEditor1.setLayout(outletDefinitionsEditor1Layout);
        outletDefinitionsEditor1Layout.setHorizontalGroup(
            outletDefinitionsEditor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 481, Short.MAX_VALUE)
        );
        outletDefinitionsEditor1Layout.setVerticalGroup(
            outletDefinitionsEditor1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 433, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Outlets", outletDefinitionsEditor1);

        javax.swing.GroupLayout attributeDefinitionsEditorPanel1Layout = new javax.swing.GroupLayout(attributeDefinitionsEditorPanel1);
        attributeDefinitionsEditorPanel1.setLayout(attributeDefinitionsEditorPanel1Layout);
        attributeDefinitionsEditorPanel1Layout.setHorizontalGroup(
            attributeDefinitionsEditorPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 481, Short.MAX_VALUE)
        );
        attributeDefinitionsEditorPanel1Layout.setVerticalGroup(
            attributeDefinitionsEditorPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 433, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Attributes", attributeDefinitionsEditorPanel1);

        javax.swing.GroupLayout paramDefinitionsEditorPanel1Layout = new javax.swing.GroupLayout(paramDefinitionsEditorPanel1);
        paramDefinitionsEditorPanel1.setLayout(paramDefinitionsEditorPanel1Layout);
        paramDefinitionsEditorPanel1Layout.setHorizontalGroup(
            paramDefinitionsEditorPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 481, Short.MAX_VALUE)
        );
        paramDefinitionsEditorPanel1Layout.setVerticalGroup(
            paramDefinitionsEditorPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 433, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Parameters", paramDefinitionsEditorPanel1);

        javax.swing.GroupLayout displayDefinitionsEditorPanel1Layout = new javax.swing.GroupLayout(displayDefinitionsEditorPanel1);
        displayDefinitionsEditorPanel1.setLayout(displayDefinitionsEditorPanel1Layout);
        displayDefinitionsEditorPanel1Layout.setHorizontalGroup(
            displayDefinitionsEditorPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 481, Short.MAX_VALUE)
        );
        displayDefinitionsEditorPanel1Layout.setVerticalGroup(
            displayDefinitionsEditorPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 433, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Displays", displayDefinitionsEditorPanel1);

        javax.swing.GroupLayout jPanelLocalDataLayout = new javax.swing.GroupLayout(jPanelLocalData);
        jPanelLocalData.setLayout(jPanelLocalDataLayout);
        jPanelLocalDataLayout.setHorizontalGroup(
            jPanelLocalDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 521, Short.MAX_VALUE)
        );
        jPanelLocalDataLayout.setVerticalGroup(
            jPanelLocalDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 416, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Local Data", jPanelLocalData);

        javax.swing.GroupLayout jPanelInitCodeLayout = new javax.swing.GroupLayout(jPanelInitCode);
        jPanelInitCode.setLayout(jPanelInitCodeLayout);
        jPanelInitCodeLayout.setHorizontalGroup(
            jPanelInitCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 521, Short.MAX_VALUE)
        );
        jPanelInitCodeLayout.setVerticalGroup(
            jPanelInitCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 416, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Init Code", jPanelInitCode);

        jPanelKRateCode.setLayout(new javax.swing.BoxLayout(jPanelKRateCode, javax.swing.BoxLayout.Y_AXIS));

        javax.swing.GroupLayout jPanelKRateCode2Layout = new javax.swing.GroupLayout(jPanelKRateCode2);
        jPanelKRateCode2.setLayout(jPanelKRateCode2Layout);
        jPanelKRateCode2Layout.setHorizontalGroup(
            jPanelKRateCode2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 521, Short.MAX_VALUE)
        );
        jPanelKRateCode2Layout.setVerticalGroup(
            jPanelKRateCode2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 416, Short.MAX_VALUE)
        );

        jPanelKRateCode.add(jPanelKRateCode2);

        jTabbedPane1.addTab("K-rate Code", jPanelKRateCode);

        javax.swing.GroupLayout jPanelSRateCodeLayout = new javax.swing.GroupLayout(jPanelSRateCode);
        jPanelSRateCode.setLayout(jPanelSRateCodeLayout);
        jPanelSRateCodeLayout.setHorizontalGroup(
            jPanelSRateCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 521, Short.MAX_VALUE)
        );
        jPanelSRateCodeLayout.setVerticalGroup(
            jPanelSRateCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 416, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("S-rate Code", jPanelSRateCode);

        javax.swing.GroupLayout jPanelDisposeCodeLayout = new javax.swing.GroupLayout(jPanelDisposeCode);
        jPanelDisposeCode.setLayout(jPanelDisposeCodeLayout);
        jPanelDisposeCodeLayout.setHorizontalGroup(
            jPanelDisposeCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 521, Short.MAX_VALUE)
        );
        jPanelDisposeCodeLayout.setVerticalGroup(
            jPanelDisposeCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 416, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Dispose Code", jPanelDisposeCode);

        jPanelMidiCode.setLayout(new javax.swing.BoxLayout(jPanelMidiCode, javax.swing.BoxLayout.Y_AXIS));

        jLabelMidiPrototype.setText("jLabel11");
        jPanelMidiCode.add(jLabelMidiPrototype);

        javax.swing.GroupLayout jPanelMidiCode2Layout = new javax.swing.GroupLayout(jPanelMidiCode2);
        jPanelMidiCode2.setLayout(jPanelMidiCode2Layout);
        jPanelMidiCode2Layout.setHorizontalGroup(
            jPanelMidiCode2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 521, Short.MAX_VALUE)
        );
        jPanelMidiCode2Layout.setVerticalGroup(
            jPanelMidiCode2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );

        jPanelMidiCode.add(jPanelMidiCode2);

        jTabbedPane1.addTab("MIDI Code", jPanelMidiCode);

        rSyntaxTextAreaXML.setColumns(20);
        rSyntaxTextAreaXML.setRows(5);
        jScrollPane6.setViewportView(rSyntaxTextAreaXML);

        javax.swing.GroupLayout jPanelXMLLayout = new javax.swing.GroupLayout(jPanelXML);
        jPanelXML.setLayout(jPanelXMLLayout);
        jPanelXMLLayout.setHorizontalGroup(
            jPanelXMLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
        );
        jPanelXMLLayout.setVerticalGroup(
            jPanelXMLLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("XML", jPanelXML);

        jPanel1.add(jTabbedPane1);

        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jPanel1.add(jLabel2);

        getContentPane().add(jPanel1);

        fileMenu1.setText("File");
        fileMenu1.add(jSeparator1);

        jMenuItemSave.setText("Save");
        jMenuItemSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveActionPerformed(evt);
            }
        });
        fileMenu1.add(jMenuItemSave);

        jMenuItemCopyToLibrary.setText("Copy to Library...");
        jMenuItemCopyToLibrary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyToLibraryActionPerformed(evt);
            }
        });
        fileMenu1.add(jMenuItemCopyToLibrary);

        jMenuBar1.add(fileMenu1);

        jMenuEdit.setText("Edit");
        jMenuBar1.add(jMenuEdit);
        jMenuBar1.add(windowMenu1);

        helpMenu1.setText("Help");
        jMenuBar1.add(helpMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveActionPerformed
        if (!isCompositeObject()) {
            MainFrame.axoObjects.WriteAxoObject(getModel().getPath(), getModel());
            updateReferenceXML();
            MainFrame.axoObjects.LoadAxoObjects();
        } else {
            JOptionPane.showMessageDialog(null, "The original object file " + getModel().getPath() + " contains multiple objects, the object editor does not support this.\n"
                    + "Your changes are NOT saved!");
        }
    }//GEN-LAST:event_jMenuItemSaveActionPerformed

    private void jMenuItemCopyToLibraryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyToLibraryActionPerformed
        AddToLibraryDlg dlg = new AddToLibraryDlg(this, true, getModel());
        dlg.setVisible(true);
        Close();
    }//GEN-LAST:event_jMenuItemCopyToLibraryActionPerformed

    private void formWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowLostFocus
        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowLostFocus

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel attributeDefinitionsEditorPanel1;
    private javax.swing.JPanel displayDefinitionsEditorPanel1;
    private axoloti.menus.FileMenu fileMenu1;
    private axoloti.menus.HelpMenu helpMenu1;
    private javax.swing.JPanel inletDefinitionsEditor1;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelHelp;
    private javax.swing.JLabel jLabelLibrary;
    private javax.swing.JLabel jLabelMidiPrototype;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JList jListDepends;
    private javax.swing.JList jListIncludes;
    private javax.swing.JList jListModules;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenuItem jMenuItemCopyToLibrary;
    private javax.swing.JMenuItem jMenuItemSave;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelDisposeCode;
    private javax.swing.JPanel jPanelInitCode;
    private javax.swing.JPanel jPanelKRateCode;
    private javax.swing.JPanel jPanelKRateCode2;
    private javax.swing.JPanel jPanelLocalData;
    private javax.swing.JPanel jPanelMidiCode;
    private javax.swing.JPanel jPanelMidiCode2;
    private javax.swing.JPanel jPanelOverview;
    private javax.swing.JPanel jPanelSRateCode;
    private javax.swing.JPanel jPanelXML;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextDesc;
    private javax.swing.JTextField jTextFieldAuthor;
    private javax.swing.JTextField jTextFieldHelp;
    private javax.swing.JTextField jTextFieldLicense;
    private javax.swing.JPanel outletDefinitionsEditor1;
    private javax.swing.JPanel paramDefinitionsEditorPanel1;
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea rSyntaxTextAreaXML;
    private axoloti.menus.WindowMenu windowMenu1;
    // End of variables declaration//GEN-END:variables

    @Override
    public JFrame getFrame() {
        return this;
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public ArrayList<DocumentWindow> getChildDocuments() {
        return null;
    }
}
