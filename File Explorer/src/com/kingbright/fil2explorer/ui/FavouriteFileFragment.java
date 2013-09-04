
package com.kingbright.fil2explorer.ui;

import java.io.File;
import java.util.List;

import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;

import com.kingbright.fil2explorer.db.DBHelper;
import com.kingbright.fil2explorer.file.FileList;

public class FavouriteFileFragment extends BasicFragment {
    private static final int LOAD_FINISH = 6;

    Runnable mLoader = new Runnable() {
        public void run() {
            List<File> list = DBHelper.getFavourites(getActivity());
            Message msg = getHandler().obtainMessage();
            msg.obj = list;
            msg.what = LOAD_FINISH;
            msg.sendToTarget();
        }
    };

    @SuppressWarnings("unchecked")
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case LOAD_FINISH: {
                mAdapter.setFiles(new FileList((List<File>) msg.obj));
                return;
            }
        }
        super.handleMsg(msg);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View view, int position, long id) {
        return false;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        new Thread(mLoader).start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void setMenuItemVisibility(Menu menu, boolean multiSelectionMode) {
    }
}
