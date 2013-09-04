package com.kingbright.fil2explorer.clipboard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.kingbright.fil2explorer.operation.Operations.Operation;

public class ClipBoard {

	private List<File> mFile;
	private Operation mOperation;

	private static ClipBoard mBoard;

	private List<File> mNeedRefresh;

	private ClipBoard() {
		mFile = new ArrayList<File>();
		mNeedRefresh = new ArrayList<File>();
	}

	public static ClipBoard getInstance() {
		if (mBoard == null) {
			mBoard = new ClipBoard();
		}
		return mBoard;
	}

	public void addClipBoard(List<File> files, Operation operation) {
		this.mFile.clear();
		this.mFile.addAll(files);
		this.mOperation = operation;
	}

	public void clear() {
		mNeedRefresh.add(mFile.get(0).getParentFile());
		this.mFile.clear();
		this.mOperation = null;
	}

	public List<File> getClipBoardData() {
		return mFile;
	}

	public Operation getOperation() {
		return mOperation;
	}

	public boolean needRefresh(File file) {
		boolean needRefresh = mNeedRefresh.contains(file);
		if (needRefresh) {
			mNeedRefresh.remove(file);
		}
		return needRefresh;
	}
}
