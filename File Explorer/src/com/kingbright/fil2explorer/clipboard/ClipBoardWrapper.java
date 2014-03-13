package com.kingbright.fil2explorer.clipboard;

import java.io.File;
import java.util.List;

import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;

import com.kingbright.fil2explorer.extention.Extentions;

/**
 * A wrapper class for ClipboardManager
 * 
 * @author jinliang01
 * 
 */
public class ClipBoardWrapper {
	private static ClipBoardWrapper clipBoard;
	private ClipboardManager mClipboradManager;
	private Context mContext;

	private ClipBoardWrapper(Context context) {
		mClipboradManager = (ClipboardManager) context
				.getSystemService(Context.CLIPBOARD_SERVICE);
		mContext = context;
	}

	public static ClipBoardWrapper getClipboradManager(Context context) {
		if (clipBoard == null) {
			clipBoard = new ClipBoardWrapper(context);

		}
		return clipBoard;
	}

	public void addItem(File file) {
		Item item = new Item(file.getAbsolutePath());
		String[] mimetype = new String[] { file.isDirectory() ? "directory"
				: Extentions.getIntance(mContext).getMimeType(file) };
		ClipDescription des = new ClipDescription(file.getName(), mimetype);
		ClipData cd = new ClipData(des, item);

		mClipboradManager.setPrimaryClip(cd);
	}

	public void addItem(List<File> filelist) {

	}
}
