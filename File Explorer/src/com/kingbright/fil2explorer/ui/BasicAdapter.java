
package com.kingbright.fil2explorer.ui;

import java.util.ArrayList;
import java.util.List;

import android.widget.BaseAdapter;

public abstract class BasicAdapter extends BaseAdapter {
    private boolean mIsMultiSelectionMode;
    private List<Integer> list = new ArrayList<Integer>();

    public boolean isMultiSelectionMode() {
        return mIsMultiSelectionMode;
    }

    public void setMultiSelectionMode(boolean isMultiSelection) {
        if (isMultiSelection != mIsMultiSelectionMode) {
            mIsMultiSelectionMode = isMultiSelection;
            notifyDataSetChanged();
        }
    }

    public void performSelect(int index) {
        if (list.contains(index)) {
            list.remove(Integer.valueOf(index));
            if (list.size() == 0) {
                setMultiSelectionMode(false);
                return;
            }
            notifyDataSetChanged();
        } else {
            list.add(index);
            if (!isMultiSelectionMode()) {
                setMultiSelectionMode(true);
                return;
            }
            notifyDataSetChanged();
        }
    }

    public List<Integer> getSelected() {
        return list;
    }

    public void clearSelected() {
        list.clear();
        notifyDataSetChanged();
    }

    public boolean isSelected(int index) {
        return list.contains(index);
    }
}
