
package com.kingbright.fil2explorer.operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.kingbright.fil2explorer.R;
import com.kingbright.fil2explorer.clipboard.ClipBoard;
import com.kingbright.fil2explorer.db.DBHelper;
import com.kingbright.fil2explorer.extention.Extentions;
import com.kingbright.fil2explorer.file.FileScanner;
import com.kingbright.fil2explorer.msg.Msg;
import com.kingbright.fil2explorer.ui.DialogBuilder;
import com.kingbright.fil2explorer.ui.PropertyDialog;

public class Operations {
    public enum Operation {
        OPEN, RENAME, DELETE, CUT, COPY, PASTE, PROPERTY, MOVE_FAVOURITE, REMOVE_FAVOURITE, NEW_FOLDER
    };

    public static void doOperation(Context context, Handler handler, File file, Operation op) {
        doOperation(context, handler, file, op, null);
    }

    public static void doOperation(Context context, Handler handler, File file, Operation op,
            OperationListener opListener) {
        List<File> list = new ArrayList<File>();
        list.add(file);
        doOperation(context, handler, list, op, opListener);
    }

    public static void doOperation(Context context, Handler handler, List<File> file,
            Operation op) {
        doOperation(context, handler, file, op, null);
    }

    public static void doOperation(final Context context, final Handler handler,
            final List<File> file, Operation op,
            final OperationListener opListener) {
        if (file == null || file.size() == 0) {
            Activity act = (Activity) context;
            act.runOnUiThread(new Runnable() {
                public void run() {
                    Msg.show(context, R.string.select_at_least_one_file);
                }
            });
            return;
        }
        switch (op) {
            case OPEN: {
                File f = file.get(0);
                String type = Extentions.getIntance(context).getType(f);
                String mimetype = Extentions.getIntance(context).getMimeType(type);
                Intent intent = OpenIntent.get(
                        type, mimetype, f);
                context.startActivity(intent);
                return;
            }
            case REMOVE_FAVOURITE: {
                return;
            }
            case DELETE: {
                Dialog d = DialogBuilder.build(context, R.string.confirm,
                        R.string.delete_confirm, R.string.yes, R.string.no,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteAction(context, handler, file, opListener).start();
                            }
                        });
                d.show();
                return;
            }
            case CUT:
            case COPY: {
                ClipBoard board = ClipBoard.getInstance();
                board.addClipBoard(file, op);
                ((Activity) context).invalidateOptionsMenu();
                Msg.show(context, R.string.copied_to_clipboard);
                return;
            }
            case PASTE: {
                new PasteAction(context, handler, file, opListener).start();
                return;
            }
            case PROPERTY: {
                new PropertyAction(context, handler, file, null).start();
                return;
            }
            case RENAME: {
                final EditText folder = (EditText) LayoutInflater.from(context)
                        .inflate(R.layout.new_folder_name_view, null);
                folder.setText(file.get(0).getName());
                Dialog d = DialogBuilder.build(context, R.string.input_a_name,
                        folder, R.string.ok, R.string.cancel,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                RenameAction fileOperation = new RenameAction(
                                        context, handler, dialog, file, opListener, folder
                                                .getText().toString());
                                DialogBuilder.setDismissable(dialog, false);
                                fileOperation.start();
                            }
                        });
                d.show();
                return;
            }
            case MOVE_FAVOURITE: {

                int result = DBHelper.addFavourite(context, file.get(0));
                if (result == DBHelper.ADD_SUCCESS) {
                    Msg.show(context, R.string.add_success);
                } else if (result == DBHelper.ALREADY_ADD) {
                    Msg.show(context, R.string.already_add);
                } else {
                    Msg.show(context, R.string.error);
                }
                return;
            }
            case NEW_FOLDER: {
                final EditText folder = (EditText) LayoutInflater.from(context)
                        .inflate(R.layout.new_folder_name_view, null);
                Dialog d = DialogBuilder.build(context, R.string.input_a_name,
                        folder, R.string.ok, R.string.cancel,
                        new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                NewFolderAction fileOperation = new NewFolderAction(
                                        context, handler, dialog, file, opListener, folder
                                                .getText().toString());
                                DialogBuilder.setDismissable(dialog, false);
                                fileOperation.start();
                            }
                        });
                d.show();
                return;
            }
            default:
                break;
        }
    }

    static class PropertyAction extends FileOperation {
        private PropertyDialog dialog;
        private int count;
        private long size;

        public PropertyAction(Context context, Handler handler, List<File> files,
                OperationListener opListener) {
            super(context, handler, files, opListener);
        }

        private void refreshInfo() {
            post(new Runnable() {
                public void run() {
                    if (!dialog.isShowing()) {
                        return;
                    }
                    dialog.setItems(count);

                    String suffix = "Bytes";
                    float temp = size;
                    if (temp > 1024) {
                        temp = temp / 1024;
                        suffix = "KB";
                        if (temp > 1024) {
                            temp = temp / 1024;
                            suffix = "MB";
                            if (temp > 1024) {
                                temp = temp / 1024;
                                suffix = "GB";
                            }
                        }
                    }
                    DecimalFormat format = new DecimalFormat("#.00");
                    dialog.setSize(format.format(temp) + " " + suffix);

                    post(this, 1500);
                }
            });
        }

        @Override
        public boolean perform() {
            final File file = files.get(0);
            if (!file.canRead()) {
                // TODO
            }
            post(new Runnable() {
                public void run() {
                    dialog = new PropertyDialog(mContext);
                    dialog.setName(file.getName());
                    dialog.setLocation(file.getParent());
                    SimpleDateFormat formatter = new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss");
                    dialog.setLastModified(formatter.format(new Date(file
                            .lastModified())));
                    dialog.setDismissListener(new OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            PropertyAction.this.cancel();
                        }
                    });
                    dialog.show();
                }
            });
            refreshInfo();

            calculate(file);
            return false;
        }

        private void calculate(File file) {
            count++;
            if (file.canRead()) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    File[] files = file.listFiles();
                    for (File f : files) {
                        calculate(f);
                    }
                }
            }
        }
    }

    static class PasteAction extends FileOperation {
        private CountDownLatch mLock;
        private ProgressDialog mProgressDialog;
        private boolean mIgnore;
        private OnClickListener mNegativeListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mIgnore = true;
                mLock.countDown();
            }
        };
        private OnClickListener mPositiveListener = new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mLock.countDown();
            }
        };

        public PasteAction(final Context context, Handler handler, List<File> files,
                OperationListener opListener) {
            super(context, handler, files, opListener);
            post(new Runnable() {
                public void run() {
                    mProgressDialog = new ProgressDialog(mContext);
                    mProgressDialog.setTitle(R.string.copy);
                    mProgressDialog.setMessage(mContext
                            .getText(R.string.preparing));
                    mProgressDialog.setOnCancelListener(new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            PasteAction.this.cancel();
                        }
                    });
                }
            });
        }

        @Override
        public boolean perform() {
            File current = FileScanner.getInstance().getCurrentFile();
            ClipBoard board = ClipBoard.getInstance();
            Operation operation = board.getOperation();

            if (!current.canWrite()) {
                post(new Runnable() {
                    public void run() {
                        Msg.show(mContext, R.string.cant_write);
                    }
                });
                return false;
            } else if (files.get(0).getParentFile().equals(current)) {
                if (operation == Operation.CUT) {
                    post(new Runnable() {
                        public void run() {
                            Msg.show(mContext, R.string.meaningless_cut);
                        }
                    });
                    return false;
                } else {
                    post(new Runnable() {
                        public void run() {
                            mProgressDialog.show();
                        }
                    });

                    for (final File file : files) {
                        post(new Runnable() {
                            public void run() {
                                mProgressDialog.setMessage(mContext.getString(
                                        R.string.copy_file,
                                        (file.isFile() ? "file" : "folder")
                                                + " " + file.getName()));
                            }
                        });

                        try {
                            autoRenameCopy(file, current);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    post(new Runnable() {
                        public void run() {
                            mProgressDialog.dismiss();
                            Msg.show(mContext, R.string.copy_done);
                        }
                    });
                    return true;
                }
            } else {
                post(new Runnable() {
                    public void run() {
                        mProgressDialog.show();
                    }
                });

                for (File file : files) {
                    String name = file.getName();
                    File destination = new File(current, name);
                    try {
                        if (destination.exists()) {
                            doMergeOrReplace(file, destination);
                        } else {
                            doCopy(file, destination);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (operation == Operation.CUT) {
                    DeleteAction deleteAction = new DeleteAction(mContext, mHandler,
                            files, null);
                    deleteAction.setMsgShow(false).run();
                    board.clear();
                }

                post(new Runnable() {
                    public void run() {
                        mProgressDialog.dismiss();
                        Msg.show(mContext, R.string.copy_done);
                    }
                });

                return true;
            }
        }

        private void doMergeOrReplace(final File file, final File destination)
                throws IOException {
            mLock = new CountDownLatch(1);
            mIgnore = false;
            post(new Runnable() {
                public void run() {
                    AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                    b.setTitle(R.string.file_exist);
                    b.setNegativeButton(R.string.ignore, mNegativeListener);
                    b.setPositiveButton(R.string.yes, mPositiveListener);
                    b.setCancelable(false);
                    if (file.isDirectory() & destination.isDirectory()) {
                        b.setMessage(mContext.getString(R.string.merge_or_not,
                                "'" + destination.getName() + "'"));
                    } else {
                        b.setMessage(mContext.getString(
                                R.string.replace_or_not,
                                destination.isFile() ? "file" : "folder", "'"
                                        + file.getName() + "'"));
                    }
                    b.create().show();
                }
            });
            try {
                mLock.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (mIgnore) {
                return;
            }

            if (file.isDirectory() & destination.isDirectory()) {
                mergeCopy(file, destination);
            } else {
                replace(file, destination);
            }

        }

        private void replace(File file, File destination) throws IOException {
            List<File> files = new ArrayList<File>();
            files.add(destination);
            DeleteAction deleteAction = new DeleteAction(mContext, mHandler, files, null);
            deleteAction.setMsgShow(false).run();
            if (!destination.exists()) {
                doCopy(file, destination);
            }
        }

        private void mergeCopy(File file, File destination) throws IOException {
            File[] files = file.listFiles();
            for (File f : files) {
                File dest = new File(destination, f.getName());
                if (!dest.exists()) {
                    doCopy(f, dest);
                } else {
                    doMergeOrReplace(f, dest);
                }
            }
        }

        private void autoRenameCopy(File src, File folder) throws IOException {
            int time = 1;
            File dest = new File(folder, src.getName());
            while (dest.exists()) {
                if (src.isDirectory()) {
                    dest = new File(folder, src.getName()
                            + " ("
                            + (time == 1 ? "copy" : time == 2 ? "another copy"
                                    : time == 3 ? "3rd copy" : time + "th copy")
                            + ")");
                } else {
                    String name = src.getName();
                    int index = name.lastIndexOf(".");
                    if (index != -1 || index < (name.length() - 1)) {
                        dest = new File(folder, name.substring(0, index) + " ("
                                + (time == 1 ? "copy" : time == 2 ? "another copy"
                                        : time == 3 ? "3rd copy" : time + "th copy")
                                + ")" + name.substring(index));
                    }
                }
                time++;
            }

            doCopy(src, dest);
        }

        private void doCopy(File src, File dest) throws IOException {
            if (src.isFile()) {
                dest.createNewFile();
            } else {
                dest.mkdir();
                File[] files = src.listFiles();
                for (File file : files) {
                    doCopy(file, new File(dest, file.getName()));
                }
                return;
            }
            FileOutputStream fos = new FileOutputStream(dest);
            FileInputStream fis = new FileInputStream(src);
            byte[] buffer = new byte[1444];
            while (fis.read(buffer) != -1) {
                fos.write(buffer);
            }
            fos.close();
            fis.close();
        }
    }

    static class DeleteAction extends FileOperation {
        private boolean mExceptionHappened;
        private boolean mOnlyOneFile;
        private ProgressDialog mProgressDialog;

        public DeleteAction(final Context context, Handler handler, List<File> files,
                OperationListener opListener) {
            super(context, handler, files, opListener);
        }

        private void deleteFiles(List<File> files) {
            mOnlyOneFile = (files.size() == 1);
            for (File file : files) {
                if (file.canWrite()) {
                    deleteFile(file);
                } else {
                    mExceptionHappened = true;
                    continue;
                }
            }

            int msg = R.string.delete_success;
            if (mExceptionHappened) {
                if (mOnlyOneFile) {
                    msg = R.string.need_root;
                } else {
                    msg = R.string.some_cant_delete;
                }
            }
            final int msgId = msg;
            post(new Runnable() {
                public void run() {
                    mProgressDialog.dismiss();
                    Msg.show(mContext, msgId);
                }
            });
        }

        private void deleteFile(final File file) {
            if (file.exists()) {
                if (file.isFile()) {
                    file.delete();
                    post(new Runnable() {
                        public void run() {
                            mProgressDialog.setMessage(mContext.getString(
                                    R.string.deleting_file, file.getName()));
                        }
                    });
                } else {
                    deleteFolder(file);
                }
            }
        }

        private void deleteFolder(final File folder) {
            if (!folder.canRead()) {
                mExceptionHappened = true;
                return;
            }
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.canWrite()) {
                    deleteFile(file);
                } else {
                    mExceptionHappened = true;
                    continue;
                }
            }
            if (folder.canWrite()) {
                folder.delete();
                post(new Runnable() {
                    public void run() {
                        mProgressDialog.setMessage(mContext.getString(
                                R.string.deleting_file, folder.getName()));
                    }
                });
            } else {
                mExceptionHappened = true;
            }
        }

        @Override
        public boolean perform() {
            post(new Runnable() {
                public void run() {
                    mProgressDialog = new ProgressDialog(mContext);
                    mProgressDialog.setTitle(R.string.delete);
                    mProgressDialog.setMessage(mContext
                            .getText(R.string.preparing));
                    mProgressDialog.setOnCancelListener(new OnCancelListener() {
                        public void onCancel(DialogInterface dialog) {
                            DeleteAction.this.cancel();
                        }
                    });
                    mProgressDialog.show();
                }
            });
            deleteFiles(files);
            return true;
        }
    }

    static class NewFolderAction extends FileOperation {
        private String mName;

        public NewFolderAction(Context context, Handler handler, List<File> file,
                OperationListener opListener, String name) {
            super(context, handler, file, opListener);
            this.mName = name;
        }

        public NewFolderAction(Context context, Handler handler, DialogInterface d,
                List<File> file, OperationListener opListener, String name) {
            super(context, handler, d, file, opListener);
            this.mName = name;
        }

        @Override
        public boolean perform() {
            int bytes = mName.getBytes().length;
            if (bytes < 1 || bytes > 254) {
                post(new Runnable() {
                    public void run() {
                        Msg.show(mContext, R.string.input_a_valid_name);
                    }
                });
                return false;
            }

            FileScanner scanner = FileScanner.getInstance();
            File currentFolder = scanner.getCurrentFile();

            if (!currentFolder.canWrite()) {
                post(new Runnable() {
                    public void run() {
                        Msg.show(mContext, R.string.cant_write);
                    }
                });
                return false;
            }

            File newFolder = new File(currentFolder, mName);
            if (newFolder.exists()) {
                post(new Runnable() {
                    public void run() {
                        Msg.show(mContext, R.string.duplicated_name);
                    }
                });
                return false;
            }

            final boolean success = newFolder.mkdir();

            DialogBuilder.setDismissable(mDialog, true);
            post(new Runnable() {
                public void run() {
                    mDialog.dismiss();
                    Msg.show(mContext, success ? R.string.folder_created
                            : R.string.unknown_error);
                }
            });

            return true;
        }
    };

    static class RenameAction extends FileOperation {
        private String mName;

        public RenameAction(Context context, Handler handler, DialogInterface d,
                List<File> files, OperationListener opListener) {
            super(context, handler, d, files, opListener);
        }

        public RenameAction(Context context, Handler handler, DialogInterface dialog,
                List<File> file, OperationListener opListener, String name) {
            super(context, handler, dialog, file, opListener);
            mName = name;
        }

        @Override
        public boolean perform() {
            File file = files.get(0);

            int bytes = mName.getBytes().length;
            if (bytes < 1 || bytes > 254) {
                post(new Runnable() {
                    public void run() {
                        Msg.show(mContext, R.string.input_a_valid_name);
                    }
                });
                return false;
            }

            if (file.getName().equals(mName)) {
                DialogBuilder.setDismissable(mDialog, true);
                return false;
            }

            File newFile = new File(FileScanner.getInstance().getCurrentFile(),
                    mName);
            if (newFile.exists()) {
                post(new Runnable() {
                    public void run() {
                        Msg.show(mContext, R.string.duplicated_name);
                    }
                });
                return false;
            }

            final boolean success = file.renameTo(newFile);

            DialogBuilder.setDismissable(mDialog, true);
            post(new Runnable() {
                public void run() {
                    mDialog.dismiss();
                    Msg.show(mContext, success ? R.string.rename_success
                            : R.string.unknown_error);
                }
            });
            return true;
        }
    }

    public interface OperationListener {
        public void onOperationDone();
    }
}
