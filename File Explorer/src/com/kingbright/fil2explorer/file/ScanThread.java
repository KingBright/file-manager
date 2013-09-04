package com.kingbright.fil2explorer.file;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

public class ScanThread implements Runnable {

	private File mFolder;
	private FileScanListener mListener;
	private FileScanner mScanner;
	private ScanState mState;
	private ScanOption mOption;
	private boolean mRefresh;

	private Thread mThread;

	public enum ScanState {
		RUNNING, STOP
	};

	public ScanThread(File base, ScanOption option) {
		this.mFolder = base;
		this.mOption = option;
		this.mState = ScanState.STOP;
	}

	@Override
	public void run() {
		mState = ScanState.RUNNING;
		FileList fileList = null;
		try {
			File[] files = mFolder.listFiles(mOption.filter);
			Arrays.sort(files, mOption.sorter);
			List<File> list = Arrays.asList(files);

			fileList = new FileList(list);
		} catch (Exception e) {
			Log.e("ScanThread", e.toString());
		}

		if (fileList == null || fileList.getFileList() == null) {
			mListener.onScanFailed();
			mState = ScanState.STOP;
			return;
		}

		if (mRefresh && !mScanner.isEmpty()) {
			FileList list = mScanner.pop();
			fileList.setPosition(list.getPosition());
		}

		mScanner.push(fileList);
		mListener.onScanSuccessed();

		mState = ScanState.STOP;
	}

	public void startScanning(FileScanListener listener,
			FileScanner fileScanner, boolean refresh) {
		mListener = listener;
		mScanner = fileScanner;
		mRefresh = refresh;

		mThread = new Thread(this);
		mListener.onScanStart();
		mThread.start();
	}

	public ScanState getState() {
		return mState;
	}

	public void stop() {
		mThread.interrupt();
	}

}
