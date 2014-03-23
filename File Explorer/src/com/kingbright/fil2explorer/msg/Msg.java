package com.kingbright.fil2explorer.msg;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class Msg {
	public static void show(Context context, int msg) {
		CustomToast.newToast(context, context.getString(msg)).show();
	}

	static class CustomToast {
		private Toast mToast;
		private TextView mTextView;

		private CustomToast(Context context) {
			init(context);
		}

		public static CustomToast newToast(Context context, String msg) {
			CustomToast toast = new CustomToast(context);
			toast.setText(msg);
			return toast;
		}

		public CustomToast setText(String msg) {
			mTextView.setText(msg);
			return this;
		}

		public CustomToast setDuration(int duration) {
			mToast.setDuration(duration);
			return this;
		}

		public void show() {
			mToast.show();
		}

		public void cancel() {
			mToast.cancel();
		}

		private void init(Context context) {
			mToast = new Toast(context);

			TextView textview = new TextView(context);
			mTextView = textview;
			mToast.setView(textview);
			mTextView.setTextSize(18);
			mTextView.setPadding(35, 24, 35, 24);
			mTextView.setBackgroundColor(Color.argb(153, 00, 00, 00));
			mTextView.setTextColor(Color.WHITE);
			mTextView.setSingleLine(true);

			mToast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 150);
			mToast.setDuration(Toast.LENGTH_SHORT);
		}
	}
}
