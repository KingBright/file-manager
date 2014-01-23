package com.kingbright.fil2explorer.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;

import com.kingbright.fil2explorer.R;
import com.kingbright.fil2explorer.clipboard.ClipBoard;
import com.kingbright.fil2explorer.file.FileList;
import com.kingbright.fil2explorer.file.FileScanListener;
import com.kingbright.fil2explorer.file.FileScanner;
import com.kingbright.fil2explorer.operation.Operations;
import com.kingbright.fil2explorer.operation.Operations.Operation;
import com.kingbright.fil2explorer.operation.Operations.OperationListener;

public class FileListFragment extends BasicFragment {
	private FileScanner mScanner;
	private int mLastPosition;
	private Operations.OperationListener mRefreshOperation = new Operations.OperationListener() {
		public void onOperationDone() {
			refresh();
		}
	};

	private Operations.OperationListener mInvalidateOptionsMenuListener = new OperationListener() {
		@Override
		public void onOperationDone() {
			getActivity().invalidateOptionsMenu();
		}
	};

	protected void handleMsg(Message msg) {
		switch (msg.what) {
		case REFRESH_FINISH: {
			FileList list = mScanner.peek();
			if (list == null) {
				return;
			}
			boolean flag = list.getFileList().size() == 0;

			setFileListVisibility(flag);

			setEmptyView(flag);

			setFiles(list);

			setFileListSelection(mLastPosition);
			if (ClipBoard.getInstance().needRefresh(mScanner.getCurrentFile())) {
				Log.e("refresh", "position " + mLastPosition);
				getHandler().sendEmptyMessage(REFRESH);
			}
			return;
		}
		case REFRESH: {
			refresh();
			return;
		}
		}
		super.handleMsg(msg);
	}

	private FileScanListener mListener = new FileScanListener() {

		public void onScanStart() {
			mLastPosition = getFirstVisiblePosition();
			mScanner.saveLastPosition(mLastPosition);
		}

		@Override
		public void onScanSuccessed() {
			mLastPosition = mScanner.getLastPosition();
			getHandler().sendEmptyMessage(REFRESH_FINISH);
		}

		@Override
		public void onScanCanceled() {
		}

		@Override
		public void onScanFailed() {
		}

		@Override
		public void onScanRunning() {
		}

	};

	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);
		mScanner = FileScanner.getInstance();
		mScanner.startScanning(null, mListener);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.file_list_menu, menu);
	}

	private void refresh() {
		mScanner.startScanning(mScanner.getCurrentFile(), mListener);
	}

	protected void setMenuItemVisibility(Menu menu, boolean visibility) {
		menu.findItem(R.id.menu_add_favourite).setVisible(false);
		menu.findItem(R.id.menu_copy).setVisible(visibility);
		menu.findItem(R.id.menu_delete).setVisible(visibility);
		menu.findItem(R.id.menu_cut).setVisible(visibility);
		menu.findItem(R.id.menu_folder).setVisible(!visibility);
		menu.findItem(R.id.menu_refresh).setVisible(!visibility);
		menu.findItem(R.id.menu_settings).setVisible(true);
		menu.findItem(R.id.menu_exit).setVisible(true);
		menu.findItem(R.id.menu_search).setVisible(true);
		menu.findItem(R.id.menu_multi_select).setVisible(!visibility);
		menu.findItem(R.id.menu_single_select).setVisible(visibility);

		List<File> clipBoard = ClipBoard.getInstance().getClipBoardData();
		menu.findItem(R.id.menu_paste).setVisible(
				clipBoard == null ? false : clipBoard.size() == 0 ? false
						: true);

		if (getSearchViewVisibility() == View.VISIBLE) {
			handleQueryModeMenu(menu);
			return;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_search: {
			setSearchViewVisibility(View.VISIBLE);
			getActivity().invalidateOptionsMenu();
			break;
		}
		case R.id.menu_add_favourite: {
			Operations.doOperation(getActivity(), getHandler(), getSelected(),
					Operation.MOVE_FAVOURITE);
			break;
		}
		case R.id.menu_copy: {
			Operations.doOperation(getActivity(), getHandler(), getSelected(),
					Operation.COPY, mInvalidateOptionsMenuListener);
			break;
		}
		case R.id.menu_cut: {
			Operations.doOperation(getActivity(), getHandler(), getSelected(),
					Operation.CUT, mInvalidateOptionsMenuListener);
			break;
		}
		case R.id.menu_delete: {
			Operations.doOperation(getActivity(), getHandler(), getSelected(),
					Operation.DELETE, mRefreshOperation);
			break;
		}
		case R.id.menu_paste: {
			Operations.doOperation(getActivity(), getHandler(), ClipBoard
					.getInstance().getClipBoardData(), Operation.PASTE,
					new OperationListener() {
						@Override
						public void onOperationDone() {
							getActivity().invalidateOptionsMenu();
							mRefreshOperation.onOperationDone();
						}
					});
			break;
		}
		case R.id.menu_folder: {
			Operations.doOperation(getActivity(), getHandler(),
					mScanner.getCurrentFile(), Operation.NEW_FOLDER,
					mRefreshOperation);
			break;
		}
		case R.id.menu_refresh: {
			refresh();
			break;
		}
		case R.id.menu_multi_select: {
			setMultiMode(true);
			break;
		}
		case R.id.menu_single_select: {
			setMultiMode(false);
			break;
		}
		default:
			super.onOptionsItemSelected(item);
		}
		return true;
	}

	private List<File> getSelected() {
		List<File> list = getFileList();
		List<File> selectedFiles = new ArrayList<File>();
		for (int index : getSelectedFiles()) {
			selectedFiles.add(list.get(index));
		}
		clearSelectedFiles();
		return selectedFiles;
	}

	private void handleQueryModeMenu(Menu menu) {
		menu.findItem(R.id.menu_search).setVisible(false);
		menu.findItem(R.id.menu_folder).setVisible(false);
		menu.findItem(R.id.menu_refresh).setVisible(false);
		menu.findItem(R.id.menu_paste).setVisible(false);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		File file = (File) parent.getItemAtPosition(position);
		if (isMultiSelectionMode()) {
			performSelect(position);
			if (!isMultiSelectionMode()) {
				setMultiMode(false);
			}
		} else {
			if (file.isDirectory()) {
				exitQueryMode(false);
				mScanner.startScanning(file, mListener);
			} else {
				Operations.doOperation(getActivity(), getHandler(), file,
						Operation.OPEN);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		File file = getItem(position);
		PopupMenu menu = getPopupMenu(view, getHandler(), file,
				mRefreshOperation);
		menu.getMenu().findItem(R.id.menu_remove_favourite).setVisible(false);
		menu.getMenu().findItem(R.id.menu_share)
				.setVisible(file.isFile() & file.canRead());
		menu.show();
		return true;
	}

	@Override
	public boolean onBackPressed() {
		if (getSearchViewVisibility() == View.VISIBLE) {
			exitQueryMode(true);
			return true;
		}
		return mScanner.handleBack(mListener);
	}

}
