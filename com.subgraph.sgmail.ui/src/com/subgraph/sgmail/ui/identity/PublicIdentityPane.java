package com.subgraph.sgmail.ui.identity;

import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.identity.PublicKeyRenderer;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.ImageCache;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class PublicIdentityPane extends Composite {


    private final Label keyInformationLabel;
    private final Label keyImageLabel;
    private final Image defaultImage;
    private final Button editImageButton;
    private final Model model;
    private PublicIdentity publicIdentity;
    private PrivateIdentity privateIdentity;

    public PublicIdentityPane(Composite composite, Model model, boolean editImage) {
        super(composite, SWT.NONE);
        setLayout(new FillLayout());
        this.model = model;
        final Group group = new Group(this, SWT.NONE);
        group.setText("Identity");
        group.setLayout(new GridLayout(2, false));
        defaultImage = ImageCache.getInstance().getDisabledImage(ImageCache.USER_IMAGE);

        keyImageLabel = createKeyImageLabel(group);
        keyImageLabel.setImage(defaultImage);
        keyInformationLabel = createKeyInformationLabel(group);
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
            byte[] converted = convertImage(bs);
            if(privateIdentity != null) {
                privateIdentity.addImageData(converted);
                model.commit();
            }
            keyImageLabel.setImage(ImageCache.getInstance().createAvatarImage(converted));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] convertImage(byte[] imageBytes) {
        final ByteArrayInputStream input = new ByteArrayInputStream(imageBytes);
        final ImageData data = new ImageData(input);
        final Image image = new Image(Display.getDefault(), data);
        final Image cropped = cropImage(image);
        Rectangle b = cropped.getBounds();
        int dim = Math.min(b.width, 128);
        final Image resized = resizeImage(cropped, dim, dim);
        final ImageLoader loader = new ImageLoader();
        loader.data = new ImageData[] { resized.getImageData() };
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        loader.save(out, SWT.IMAGE_JPEG);
        return out.toByteArray();
    }

    private Image cropImage(Image image) {
        final Rectangle bounds = image.getBounds();
        final int sz = Math.min(bounds.width, bounds.height);
        final Image outputImage = new Image(image.getDevice(), sz, sz);
        final GC gc = new GC(outputImage);
        final int srcX = (bounds.width - sz) / 2;
        final int srcY = (bounds.height - sz) / 2;
        gc.drawImage(image, srcX, srcY, sz, sz, 0, 0, sz, sz);
        image.dispose();
        return outputImage;
    }

    private Image resizeImage(Image image, int width, int height) {
        final Image scaled = new Image(image.getDevice(), width, height);
        final GC gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        final Rectangle b = image.getBounds();
        gc.drawImage(image, 0, 0, b.width, b.height, 0, 0, width, height);
        gc.dispose();
        image.dispose();
        return scaled;
    }



    public void displayIdentity(PublicIdentity publicIdentity, PrivateIdentity privateIdentity) {
        final Image oldImage = keyImageLabel.getImage();
        if(oldImage != defaultImage) {
            oldImage.dispose();
        }

        final PublicKeyRenderer textRenderer = new PublicKeyRenderer(publicIdentity);
        final String text = textRenderer.renderPublicIdentity();
        keyInformationLabel.setText(text);

        final byte[] imageData = publicIdentity.getImageData();
        if(imageData == null || imageData.length == 0) {
            keyImageLabel.setImage(defaultImage);
        } else {
            keyImageLabel.setImage(ImageCache.getInstance().createAvatarImage(imageData));
        }
        this.publicIdentity = publicIdentity;
        this.privateIdentity = privateIdentity;
        layout(true, true);
    }

    private Label createKeyInformationLabel(Composite parent) {
        final Label label = new Label(parent, SWT.NONE);
        final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 2);
        gd.horizontalIndent = 20;
        label.setLayoutData(gd);
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
