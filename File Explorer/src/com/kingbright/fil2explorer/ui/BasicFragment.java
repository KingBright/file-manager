package com.kingbright.fil2explorer.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.kingbright.fil2explorer.R;
import com.kingbright.fil2explorer.file.FileList;
import com.kingbright.fil2explorer.operation.Operations.OperationListener;

public abstract class BasicFragment extends Fragment implements
		OnItemClickListener, OnItemLongClickListener {
	public static final int REFRESH_FINISH = 1;
	public static final int SAVE_POSITION = 2;
	public static final int REFRESH = 3;
	public static final int QUERY_RESULT = 4;
	public static final int EXIT_QUERY = 5;

	private LayoutInflater mLayoutInflater;
	private FileAdapter mAdapter;
	private ListView mList;
	private View mEmptyView;
	private SearchView mSearchView;
	private Thread mQueryThread;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			handleMsg(msg);
		}
	};

	public Handler getHandler() {
		return mHandler;
	}

	@SuppressWarnings("unchecked")
	protected void handleMsg(Message msg) {
		switch (msg.what) {
		case SAVE_POSITION: {
			break;
		}
		case QUERY_RESULT: {
			mAdapter.setQueryResult((List<File>) msg.obj);
			break;
		}
		case EXIT_QUERY: {
			mAdapter.exitQueryMode(true);
		}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mLayoutInflater = inflater;
		View view = mLayoutInflater.inflate(R.layout.fragment_file_list, null);
		mList = (ListView) view.findViewById(R.id.listview);
		mEmptyView = (TextView) view.findViewById(R.id.emptyview);

		mAdapter = new FileAdapter();
		mList.setAdapter(mAdapter);
		mList.setOnItemClickListener(this);
		mList.setOnItemLongClickListener(this);

		mSearchView = (SearchView) view.findViewById(R.id.search_view);
		mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String newText) {
				if (mQueryThread != null) {
					mQueryThread.interrupt();
				}
				mQueryThread = new Thread(new QueryRunnable(newText));
				mQueryThread.start();
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}
		});
		mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
			@Override
			public boolean onClose() {
				exitQueryMode(true);
				return false;
			}
		});
		return view;
	}

	protected abstract boolean onBackPressed();

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		setMenuItemVisibility(menu, mAdapter.isMultiSelectionMode());
	}

	protected abstract void setMenuItemVisibility(Menu menu,
			boolean multiSelectionMode);

	protected void setMultiMode(boolean multi) {
		mAdapter.setMultiSelectionMode(multi);
		getActivity().invalidateOptionsMenu();
		mAdapter.notifyDataSetChanged();
	}

	protected void performSelect(int position) {
		mAdapter.performSelect(position);
	}

	protected File getItem(int position) {
		return mAdapter.getItem(position);
	}

	protected void exitQueryMode(boolean restore) {
		mSearchView.setVisibility(View.GONE);
		mAdapter.exitQueryMode(restore);
		getActivity().invalidateOptionsMenu();
	}

	public PopupMenu getPopupMenu(View view, Handler handler, List<File> file,
			OperationListener refreshListener) {
		PopupMenu menu = new PopupMenu(getActivity(), view);
		menu.inflate(R.menu.context_menu);
		menu.setOnMenuItemClickListener(new MenuListener(file, handler,
				getActivity(), refreshListener));
		return menu;
	}

	public PopupMenu getPopupMenu(View view, Handler handler, File file,
			OperationListener refreshListener) {
		List<File> list = new ArrayList<File>();
		list.add(file);
		return getPopupMenu(view, handler, list, refreshListener);
	}

	protected void setFileListVisibility(boolean flag) {
		mList.setVisibility(flag ? View.GONE : View.VISIBLE);
	}

	protected void setEmptyView(boolean flag) {
		mEmptyView.setVisibility(flag ? View.VISIBLE : View.GONE);
	}

	protected void setFiles(FileList list) {
		mAdapter.setFiles(list);
	}

	protected void setFileListSelection(int position) {
		mList.setSelection(position);
	}

	protected int getFirstVisiblePosition() {
		return mList.getFirstVisiblePosition();
	}

	protected List<File> getFileList() {
		return mAdapter.getFiles().getFileList();
	}

	protected int getSearchViewVisibility() {
		return mSearchView.getVisibility();
	}

	protected void setSearchViewVisibility(int visibility) {
		mSearchView.setVisibility(visibility);
	}

	protected void clearSelectedFiles() {
		mAdapter.getSelected().clear();
	}

	protected List<Integer> getSelectedFiles() {
		return mAdapter.getSelected();
	}

	protected boolean isMultiSelectionMode() {
		return mAdapter.isMultiSelectionMode();
	}

	/**
	 * File Adapter
	 * 
	 * @author jin
	 */
	class FileAdapter extends BasicAdapter {
		private FileList mList;
		private FileList mBackup;
		private boolean mQueryMode;

		public FileAdapter() {
		}

		public FileList getFiles() {
			if (mQueryMode && mBackup != null) {
				return mBackup;
			}
			return mList;
		}

		@Override
		public int getCount() {
			return mList == null ? 0 : mList.getFileList() == null ? 0 : mList
					.getFileList().size();
		}

		@Override
		public File getItem(int position) {
			return mList == null ? null : mList.getFileList() == null ? null
					: mList.getFileList().get(position);

		}

		public boolean isInQueryMode() {
			return mQueryMode;
		}

		public void setQueryResult(List<File> file) {
			if (!mQueryMode) {
				mBackup = mList;
			}
			FileList list = new FileList(file);
			setFiles(list);
			mQueryMode = true;
		}

		public void exitQueryMode(boolean restore) {
			if (!mQueryMode) {
				return;
			}
			if (restore) {
				setFiles(mBackup);
			}
			mQueryMode = false;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View view, ViewGroup parent) {
			View fileView = null;
			if (isSelected(position)) {
				fileView = mLayoutInflater.inflate(R.layout.file_item_selected,
						null);
			} else {
				fileView = mLayoutInflater.inflate(R.layout.file_item, null);
			}
			TextView fileName = (TextView) fileView
					.findViewById(R.id.file_name);
			TextView fileInfo = (TextView) fileView
					.findViewById(R.id.file_info);
			ImageView fileIcon = (ImageView) fileView.findViewById(R.id.icon);

			File file = getItem(position);
			fileName.setText(file.getName());
			fileInfo.setText(file.getParent());
			fileIcon.setImageResource(file.isDirectory() ? R.drawable.folder
					: R.drawable.file);

			return fileView;
		}

		public void setFiles(FileList list) {
			this.mList = list;
			notifyDataSetChanged();
		}
	}

	/**
	 * Query Thread
	 * 
	 * @author jin
	 */
	class QueryRunnable implements Runnable {

		String queryStr;

		public QueryRunnable(String queryString) {
			queryStr = queryString.toLowerCase();
		}

		@Override
		public void run() {
			try {
				List<File> result = new ArrayList<File>();
				List<File> target = mAdapter.getFiles().getFileList();
				if (target == null) {
					return;
				}
				Pattern pattern = Pattern.compile(queryStr);
				for (File file : target) {
					Matcher m = pattern.matcher(file.getName().toLowerCase());
					if (m.find()) {
						result.add(file);
					}
				}

				Message msg = mHandler.obtainMessage();
				msg.obj = result;
				msg.what = QUERY_RESULT;
				msg.sendToTarget();
			} catch (Exception e) {
				Log.e("QueryRunnable", "Query Error");
			}
		}

	}

	public void onSelected() {
	}
}
