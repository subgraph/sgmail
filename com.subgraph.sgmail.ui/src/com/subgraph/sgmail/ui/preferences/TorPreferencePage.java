package com.subgraph.sgmail.ui.preferences;

import com.subgraph.sgmail.model.Preferences;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Composite;

public class TorPreferencePage extends FieldEditorPreferencePage {

    private BooleanFieldEditor enableTorEditor;
    private IntegerFieldEditor torPortEditor;

    TorPreferencePage() {
        super(GRID);
        setTitle("Tor");
    }

    @Override
    protected void createFieldEditors() {
        final Composite fieldEditorParent = getFieldEditorParent();
        enableTorEditor = new BooleanFieldEditor(Preferences.TOR_ENABLED, "Use Tor for network access", fieldEditorParent);
        torPortEditor = new IntegerFieldEditor(Preferences.TOR_SOCKS_PORT, "Tor SOCKS Port", fieldEditorParent);
        addField(enableTorEditor);
        addField(torPortEditor);
        synchronizeControls();
    }

    private void synchronizeControls() {
        final boolean isEnabled = enableTorEditor.getBooleanValue();
        torPortEditor.setEnabled(isEnabled, getFieldEditorParent());
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getSource() == enableTorEditor) {
            synchronizeControls();
        }
        super.propertyChange(event);
    }
}
