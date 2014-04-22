package com.subgraph.sgmail.ui.compose;

import com.subgraph.sgmail.ui.ImageCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class OpenPGPButtons {

    private final MessageCompositionState state;
    private final Button signingButton;
    private final Button encryptionButton;

    OpenPGPButtons(Composite parent, MessageCompositionState state) {
        this.state = state;
        signingButton = createButton(parent);
        encryptionButton = createButton(parent);
        updateButtons();
    }

    public void updateButtons() {
        updateSigningButton();
        updateEncryptionButton();
    }

    private Button createButton(Composite parent) {
        final Button button = new Button(parent, SWT.PUSH);
        final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
        button.setLayoutData(gd);
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                onClicked(button);
            }
        });
        return button;
    }

    private void onClicked(Button button) {
        if(button == signingButton) {
            onSigningButtonClicked();
        } else if(button == encryptionButton) {
            onEncryptionButtonClicked();
        }
    }

    private void onSigningButtonClicked() {
        if(state.isSigningKeyAvailable()) {
            state.setSigningRequested(!state.isSigningRequested());
            updateSigningButton();
        }
    }

    private void onEncryptionButtonClicked() {
        if(state.areRecipientKeysAvailable()) {
            state.setEncryptionRequested(!state.isEncryptionRequested());
            updateEncryptionButton();
        }
    }


    private void updateSigningButton() {
        if(!state.isSigningKeyAvailable()) {
            setSigningButtonImage(false);
            signingButton.setToolTipText("No identity available for signing");
            return;
        }

        if(state.isSigningRequested()) {
            enableSigning();
        } else {
            disableSigning();
        }
    }

    private void enableSigning() {
        setSigningButtonImage(true);
        signingButton.setToolTipText("This message will be signed");
    }

    private void disableSigning() {
        setSigningButtonImage(false);
        signingButton.setToolTipText("Signing disabled for this message");
    }

    private void updateEncryptionButton() {
        if(!state.areRecipientKeysAvailable()) {
            setEncryptionButtonImage(false, state.isEncryptionRequested());
            encryptionButton.setToolTipText("Encryption keys not available for recipients.");
            return;
        }

        if(state.isEncryptionRequested()) {
            enableEncryption();
        } else {
            disableEncryption();
        }
    }

    private void enableEncryption() {
        setEncryptionButtonImage(true, true);
        encryptionButton.setToolTipText("This message will be encrypted");
    }

    private void disableEncryption() {
        setEncryptionButtonImage(false, false);
        encryptionButton.setToolTipText("This message will not be encrypted");
    }

    private void setSigningButtonImage(boolean enabled) {
        setButtonImage(signingButton, ImageCache.SIGNED_IMAGE, enabled);
    }

    private void setEncryptionButtonImage(boolean enabled, boolean isLocked) {
        final String key = (isLocked) ? (ImageCache.LOCKED_IMAGE) : (ImageCache.UNLOCKED_IMAGE);
        setButtonImage(encryptionButton, key, enabled);
    }

    private void setButtonImage(Button button, String key, boolean enabled) {
        final ImageCache cache = ImageCache.getInstance();
        final Image image = (enabled) ? (cache.getImage(key)) : (cache.getDisabledImage(key));
        button.setImage(image);
        button.pack(true);
    }
}
