
package com.kingbright.fil2explorer.msg;

import android.content.Context;
import android.widget.Toast;

public class Msg {
    public static void show(Context context, int msg) {
        Toast.makeText(context, context.getString(msg), Toast.LENGTH_SHORT).show();
    }
}
