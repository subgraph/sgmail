package com.subgraph.sgmail.ui.identity;

import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.identity.PublicKeyRenderer;
import com.subgraph.sgmail.ui.ImageCache;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class PublicIdentityPane extends Composite {


    private final Label keyInformationLabel;
    private final Label keyImageLabel;
    private final Image defaultImage;
    private final Button editImageButton;

    public PublicIdentityPane(Composite composite, boolean editImage) {
        super(composite, SWT.NONE);
        setLayout(new FillLayout());
        final Group group = new Group(this, SWT.NONE);
        group.setText("Identity");
        group.setLayout(new GridLayout(2, false));
        defaultImage = ImageCache.getInstance().getDisabledImage(ImageCache.USER_IMAGE);
        keyInformationLabel = createKeyInformationLabel(group);
        keyImageLabel = createKeyImageLabel(group);
        keyImageLabel.setImage(defaultImage);
        if(editImage) {
            editImageButton = createEditImageButton(group);
            editImageButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                onEditButtonClicked();

            }

            });
        } else {
            editImageButton = null;
        }
    }

    private void onEditButtonClicked() {
        final FileDialog fileDialog = new FileDialog(getShell());
        fileDialog.setText("Choose an image file");
        String filename = fileDialog.open();
        Path path = FileSystems.getDefault().getPath(filename);
        try {
            byte[] bs = Files.readAllBytes(path);
            keyImageLabel.setImage(ImageCache.getInstance().createAvatarImage(bs));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Chosen file: "+ path);

    }

    public void displayIdentity(PublicIdentity identity) {
        final Image oldImage = keyImageLabel.getImage();
        if(oldImage != defaultImage) {
            oldImage.dispose();
        }

        final PublicKeyRenderer textRenderer = new PublicKeyRenderer(identity);
        final String text = textRenderer.renderPublicIdentity();
        keyInformationLabel.setText(text);

        final byte[] imageData = identity.getImageData();
        if(imageData == null || imageData.length == 0) {
            keyImageLabel.setImage(defaultImage);
        } else {
            keyImageLabel.setImage(ImageCache.getInstance().createAvatarImage(imageData));
        }
    }

    private Label createKeyInformationLabel(Composite parent) {
        final Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 2));
        label.setFont(JFaceResources.getTextFont());
        return label;
    }

    private Label createKeyImageLabel(Composite parent) {
        final Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        return label;
    }

    private Button createEditImageButton(Composite parent) {
        final Button button = new Button(parent, SWT.PUSH);
        button.setText("Edit");
        button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        return button;
    }
}
