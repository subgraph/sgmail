package com.subgraph.sgmail.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ImageCache {
	public final static String USER_IMAGE = "user_64x64.png";
	public final static String INBOX_IMAGE = "inbox_16x16.png";
	public final static String FOLDER_IMAGE = "folder_16x16.png";
	public final static String STARRED_IMAGE = "starred_16x16.png";
	public final static String UNSTARRED_IMAGE = "unstarred_16x16.png";
	
	public final static String FORWARD_IMAGE = "forward_16x16.png";
	public final static String REPLY_IMAGE = "reply_16x16.png";
	public final static String REPLY_ALL_IMAGE = "reply-all_16x16.png";
	public final static String TAG_IMAGE = "tag_16x16.png";
	
	public final static String BLUE_DOT_IMAGE = "bullet_blue_16x16.png";
	
	public final static String COMPOSE_IMAGE = "compose_24x24.png";
	
	
	public static ImageCache _instance;
	
	public static ImageCache getInstance() {
		if(_instance == null) {
			_instance = new ImageCache();
		}
		return _instance;
	}
	
	
	private final Map<String, Image> imageMap = new HashMap<>();
	
	public Image getImage(String key) {
		
		if(!imageMap.containsKey(key)) {
			if(key.equals(USER_IMAGE)) {
				imageMap.put(key, createImage(key, true));
			} else {
				imageMap.put(key, createImage(key, false));
			}
		}
		return imageMap.get(key);
	}

	private Image createImage(String key, boolean greyed) {
		final Image img = loadImage("/icons/" + key);
		if(!greyed) {
			return img;
		}
		Image greyedImage = new Image(Display.getDefault(), img, SWT.IMAGE_DISABLE);
		img.dispose();
		return greyedImage;
	}
	
	private Image loadImage(String path) {
		final Image image = tryResourceLoad(path);
		if(image != null) {
			return image;
		}
		return new Image(Display.getDefault(), System.getProperty("user.dir") + path);
	}
	
	private Image tryResourceLoad(String path) {
		final InputStream in = getClass().getResourceAsStream(path);
		if(in == null) {
			return null;
		}
		try {
			return new Image(Display.getDefault(), in);
		} finally {
			try {
				in.close();
			} catch (IOException e) { }
		}
	}
}
