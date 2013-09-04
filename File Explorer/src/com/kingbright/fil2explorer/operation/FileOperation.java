
package com.kingbright.fil2explorer.operation;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;

import com.kingbright.fil2explorer.R;
import com.kingbright.fil2explorer.msg.Msg;
import com.kingbright.fil2explorer.operation.Operations.OperationListener;

public abstract class FileOperation implements Runnable {
    Thread mThread;

    List<File> files;
    OperationListener opListener;
    Activity mContext;
    DialogInterface mDialog;
    Handler mHandler;

    private boolean mShowMsg = true;

    public FileOperation(Context context, Handler handler, DialogInterface d, List<File> files,
            OperationListener opListener) {
        this.files = files;
        this.opListener = opListener;
        this.mContext = (Activity) context;
        this.mHandler = handler;
        this.mDialog = d;
    }

    public FileOperation(Context context, Handler handler, List<File> files,
            OperationListener opListener) {
        this.mHandler = handler;
        this.files = files;
        this.opListener = opListener;
        this.mContext = (Activity) context;
    }

    public FileOperation setMsgShow(boolean b) {
        mShowMsg = b;
        return this;
    }

    public void post(Runnable runnable) {
        if (mShowMsg) {
            mHandler.post(runnable);
        }
    }

    public void post(Runnable runnable, long delayed) {
        if (mShowMsg) {
            mHandler.postDelayed(runnable, 1000);
        }
    }

    public void start() {
        mThread = new Thread(this);
        mThread.start();
    }

    public void run() {
        try {
            if (perform()) {
                if (opListener != null) {
                    post(new Runnable() {
                        public void run() {
                            opListener.onOperationDone();
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e("FileOperation", "" + e);
            final int msg = (e instanceof InterruptedException) ? R.string.canceled
                    : R.string.unknown_error;
            post(new Runnable() {
                public void run() {
                    Msg.show(mContext, msg);
                }
            });
        }
    }

    public void cancel() {
        mThread.interrupt();
    }

    public abstract boolean perform();

}
