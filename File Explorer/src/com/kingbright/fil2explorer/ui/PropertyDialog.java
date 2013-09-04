
package com.kingbright.fil2explorer.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kingbright.fil2explorer.R;

public class PropertyDialog {
    private Dialog dialog;
    private TextView name;
    private TextView location;
    private TextView items;
    private TextView size;
    private TextView lastModified;

    public PropertyDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(
                R.layout.property_view, null);

        builder.setTitle(R.string.menu_properties);
        name = (TextView) view.findViewById(R.id.name);
        location = (TextView) view.findViewById(R.id.location);
        items = (TextView) view.findViewById(R.id.item);
        size = (TextView) view.findViewById(R.id.size);
        lastModified = (TextView) view.findViewById(R.id.last_modified);

        builder.setPositiveButton(R.string.ok, null);
        builder.setView(view);

        dialog = builder.create();
    }

    public void setDismissListener(OnDismissListener listener) {
        dialog.setOnDismissListener(listener);
    }

    public void show() {
        dialog.show();
    }

    public void setName(String name) {
        this.name.setText(name);
    }

    public void setLocation(String l) {
        this.location.setText(l);
    }

    public void setItems(int count) {
        items.setText("" + count);
    }

    public void setSize(String size) {
        this.size.setText(size);
    }

    public void setLastModified(String time) {
        this.lastModified.setText(time);
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }

}
