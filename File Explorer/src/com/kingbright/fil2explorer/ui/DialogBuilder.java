package com.kingbright.fil2explorer.ui;

import java.lang.reflect.Field;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

public class DialogBuilder {
	public static Dialog build(Context context, int title, int msg,
			int positive, int negative, OnClickListener pListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(msg);
		builder.setNegativeButton(negative, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				setDismissable(dialog, true);
			}
		});
		builder.setPositiveButton(positive, pListener);
		return builder.create();
	}

	public static Dialog build(Context context, int title, View view,
			int positive, int negative, OnClickListener pListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setView(view);
		builder.setNegativeButton(negative, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				setDismissable(dialog, true);
			}
		});
		builder.setPositiveButton(positive, pListener);

		return builder.create();
	}

	public static void setDismissable(DialogInterface dialog,
			boolean dismissable) {
		if (dialog == null) {
			return;
		}
		try {
			Field field = dialog.getClass().getSuperclass()
					.getDeclaredField("mShowing");
			field.setAccessible(true);
			field.set(dialog, dismissable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
