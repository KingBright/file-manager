
package com.kingbright.fil2explorer.file;

import java.io.File;
import java.util.List;

public class FileList {

    private List<File> mList;

    private int mCurrentPosition;

    public void setPosition(int currentPosition) {
        this.mCurrentPosition = currentPosition;
    }

    public int getPosition() {
        return mCurrentPosition;
    }

    public FileList(List<File> list) {
        this.mList = list;
    }

    public List<File> getFileList() {
        return mList;
    }

    public void setFileList(List<File> list) {
        this.mList = list;
    }

}
