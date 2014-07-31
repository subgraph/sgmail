package com.subgraph.sgmail.ui.identity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.nyms.NymsKeyInfo;
import com.subgraph.sgmail.ui.ImageCache;

public class PublicIdentityPane extends Composite {

  private final Label keyInformationLabel;
  private final Label keyImageLabel;
  private final Image defaultImage;
  private final Button editImageButton;
  private final Group group;

  public PublicIdentityPane(Composite composite, Model model, boolean editImage) {
    super(composite, SWT.NONE);
    setLayout(new FillLayout());
    group = new Group(this, SWT.NONE);
    group.setText("Identity");
    group.setLayout(new GridLayout(2, false));
    defaultImage = ImageCache.getInstance().getDisabledImage(ImageCache.USER_IMAGE);

    keyImageLabel = createKeyImageLabel(group);
    keyImageLabel.setImage(defaultImage);
    keyInformationLabel = createKeyInformationLabel(group);
    if (editImage) {
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
      // if(privateIdentity != null) {
      // privateIdentity.addImageData(converted);
      // model.getDatabase().commit();
      // }
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

  public void displayKeyInfo(NymsKeyInfo keyInfo) {
    final Image oldImage = keyImageLabel.getImage();
    if (oldImage != defaultImage) {
      oldImage.dispose();
    }

    keyInformationLabel.setText(keyInfo.getSummary());

    final byte[] imageData = keyInfo.getUserImageData();
    if (imageData == null || imageData.length == 0) {
      keyImageLabel.setImage(defaultImage);
    } else {
      keyImageLabel.setImage(ImageCache.getInstance().createAvatarImage(imageData));
    }
    group.setText(getRealName(keyInfo));
    layout(true, true);
  }

  private String getRealName(NymsKeyInfo keyInfo) {
    if (keyInfo.getUIDs().size() == 0) {
      return "";
    }
    for (String uid : keyInfo.getUIDs()) {
      int idx = uid.indexOf(" <");
      if (idx > 0) {
        return uid.substring(0, idx);
      }
    }
    return keyInfo.getUIDs().get(0);
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
