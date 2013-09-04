
package com.kingbright.fil2explorer.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;

public class ClassifiedFileFragment extends BasicFragment {

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        return false;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
    }

    @Override
    protected void setMenuItemVisibility(Menu menu, boolean multiSelectionMode) {
    }

}
