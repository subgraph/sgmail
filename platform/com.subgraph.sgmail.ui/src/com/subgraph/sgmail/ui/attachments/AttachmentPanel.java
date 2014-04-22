package com.subgraph.sgmail.ui.attachments;

import com.google.common.io.Files;
import com.subgraph.sgmail.AttachmentExtractionException;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.ui.Resources;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AttachmentPanel extends Composite {
    private final static Logger logger = Logger.getLogger(AttachmentPanel.class.getName());
    private final static int ATTACHMENT_ITEM_WIDTH = 100;

    private final JavamailUtils javamailUtils;
    private final ExecutorService executor;
    private final StoredMessage message;
    private final List<AttachmentPanelItem> items = new ArrayList<>();
    private final Button saveButton;
    private final Label saveSelectedLabel;
    private final Color defaultLabelForeground;

    private AttachmentPanelItem selectedItem;

    public AttachmentPanel(ExecutorService executor, JavamailUtils javamailUtils, StoredMessage message, Composite parent) {
        super(parent, SWT.NONE);
        this.executor = executor;
        this.javamailUtils = javamailUtils;
        this.message = message;
        setLayout(createLayout());

        final Color white = JFaceResources.getColorRegistry().get(Resources.COLOR_WHITE);
        setBackground(white);
        final Group group = createGroup(this, message.getAttachments().size());
        createIconPanel(group, message);
        createSeparator(group);
        saveButton = createSaveButton(group);
        saveSelectedLabel = createSaveSelectedLabel(group);
        defaultLabelForeground = saveSelectedLabel.getForeground();
    }

    private Layout createLayout() {
        final FillLayout layout = new FillLayout();
        layout.marginWidth = 20;
        return layout;
    }

    private Group createGroup(Composite parent, int attachmentCount) {
        final Group group = new Group(parent, SWT.NONE);
        if(attachmentCount == 1) {
            group.setText("1 Attachment");
        } else {
            group.setText(attachmentCount + " Attachments");
        }
        group.setLayout(new GridLayout(2, false));
        return group;
    }

    private Composite createIconPanel(Composite parent, StoredMessage message) {
        final Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new RowLayout());
        panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        for(MessageAttachment attachment: message.getAttachments()) {
            addAttachmentItem(panel, attachment);
        }
        return panel;
    }

    private void addAttachmentItem(Composite parent, MessageAttachment attachment) {
        final AttachmentPanelItem item = new AttachmentPanelItem(parent, this, attachment);
        item.setLayoutData(new RowData(ATTACHMENT_ITEM_WIDTH, SWT.DEFAULT));
        items.add(item);
    }

    private Label createSeparator(Composite parent) {
        Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        sep.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        return sep;
    }

    private Button createSaveButton(Composite parent) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText("Save");
        button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        button.setEnabled(false);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if(selectedItem != null) {
                    runSaveFileDialog(selectedItem);
                }
            }
        });
        return button;
    }

    private void runSaveFileDialog(AttachmentPanelItem currentItem) {
        final FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
        fileDialog.setFileName(currentItem.getAttachment().getFilename());
        fileDialog.setOverwrite(true);
        final String result = fileDialog.open();
        if(result != null) {
            executor.execute(() -> performSaveAttachment(result, currentItem));
        }
    }

    private void performSaveAttachment(String path, AttachmentPanelItem item) {
        try {
            final InputStream inputStream = javamailUtils.extractAttachment(item.getAttachment(), message);
            saveAttachmentStream(path, inputStream);
            item.setSavedToPath(path);
            asyncUpdateSaveSelectedLabel();
        } catch (AttachmentExtractionException e) {
            logger.log(Level.WARNING, "Error extracting attachment: " + e.getMessage(), e);
            item.setErrorMessage("Failed to extract attachment");
            asyncUpdateSaveSelectedLabel();
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException writing attachment file", e);
            if(e.getMessage() != null) {
                item.setErrorMessage(e.getMessage());
            } else {
                item.setErrorMessage("IO Error saving file");
            }
            asyncUpdateSaveSelectedLabel();
        }
    }

    private void asyncUpdateSaveSelectedLabel() {
        getDisplay().asyncExec(() -> {
            if(!isDisposed()) {
                setSaveSelectedLabel(selectedItem);
            }
        });
    }

    private void saveAttachmentStream(String savePath, InputStream attachmentStream) throws IOException {
        final File saveFile = new File(savePath);
        Files.asByteSink(saveFile).writeFrom(attachmentStream);
        attachmentStream.close();
    }

    private Label createSaveSelectedLabel(Composite parent) {
        final Label label = new Label(parent, SWT.WRAP);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        return label;
    }

    public void unselectAll() {
        for(AttachmentPanelItem item: items) {
            item.setUnselected();
        }
        saveButton.setEnabled(false);
        selectedItem = null;
        setSaveSelectedLabel(null);
    }

    public void setItemSelected(AttachmentPanelItem selectedItem) {
        for (AttachmentPanelItem item : items) {
            if(item != selectedItem) {
                item.setUnselected();
            }
        }
        this.selectedItem = selectedItem;
        setSaveSelectedLabel(selectedItem);
        saveButton.setEnabled(true);
    }

    private void setSaveSelectedLabel(AttachmentPanelItem item) {
        if(item == null) {
            setSaveSelectedText("");
        } else if(item.getErrorMessage() != null) {
            setSaveSelectedText(item.getErrorMessage(), JFaceResources.getColorRegistry().get(Resources.COLOR_ERROR_MESSAGE));
        } else if(item.getSavedToPath() != null) {
            setSaveSelectedText("Saved to: "+ item.getSavedToPath());
        } else {
            setSaveSelectedText("Save Attachment: "+ item.getAttachment().getFilename());
        }
    }

    private void setSaveSelectedText(String text) {
        setSaveSelectedText(text, defaultLabelForeground);
    }

    private void setSaveSelectedText(String text, Color foreground) {
        saveSelectedLabel.setForeground(foreground);
        saveSelectedLabel.setText(text);
    }
}
