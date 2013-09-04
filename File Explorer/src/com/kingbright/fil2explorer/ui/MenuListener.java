
package com.kingbright.fil2explorer.ui;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.widget.PopupMenu.OnMenuItemClickListener;

import com.kingbright.fil2explorer.R;
import com.kingbright.fil2explorer.extention.Extentions;
import com.kingbright.fil2explorer.operation.Operations;
import com.kingbright.fil2explorer.operation.Operations.Operation;
import com.kingbright.fil2explorer.operation.Operations.OperationListener;

public class MenuListener implements OnMenuItemClickListener {

    private List<File> mFile;
    private Context mContext;
    private OperationListener mRefreshListener;
    private Handler mHandler;

    public MenuListener(List<File> file, Handler handler, Context mContext,
            OperationListener refreshListener) {
        this.mFile = file;
        this.mContext = mContext;
        this.mRefreshListener = refreshListener;
        this.mHandler = handler;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_share: {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType(Extentions.getIntance(mContext).getMimeType(mFile.get(0)));
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mFile.get(0)));
                    mContext.startActivity(intent);
                } catch (Exception e) {
                    Log.e("MenuListener", "No app could share.");
                }
                return true;
            }
            case R.id.menu_rename: {
                Operations.doOperation(mContext, mHandler, mFile, Operation.RENAME,
                        mRefreshListener);
                return true;
            }
            case R.id.menu_add_favourite: {
                Operations.doOperation(mContext, mHandler, mFile, Operation.MOVE_FAVOURITE);
                return true;
            }
            case R.id.menu_remove_favourite: {
                Operations.doOperation(mContext, mHandler, mFile, Operation.REMOVE_FAVOURITE,
                        mRefreshListener);
                return true;
            }
            case R.id.menu_copy: {
                Operations.doOperation(mContext, mHandler, mFile, Operation.COPY
                        );
                return true;
            }
            case R.id.menu_delete: {
                Operations.doOperation(mContext, mHandler, mFile, Operation.DELETE,
                        mRefreshListener);
                return true;
            }
            case R.id.menu_properties: {
                Operations.doOperation(mContext, mHandler, mFile, Operation.PROPERTY);
                return true;
            }
            case R.id.menu_cut: {
                Operations.doOperation(mContext, mHandler, mFile, Operation.CUT
                        );
                return true;
            }
        }
        return false;
    }

}
