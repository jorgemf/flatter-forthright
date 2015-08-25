package com.livae.ff.app.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.livae.ff.app.R;

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

public abstract class NotificationMuteDialogFragment extends DialogFragment {

	public void show(FragmentManager fragmentManager) {
		show(fragmentManager, null);
	}

	@Nonnull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.select_notification_mute)
			   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   dismiss();
				   }
			   })
			   .setItems(R.array.notification_mute_array, new DialogInterface.OnClickListener() {
				   @Override
				   public void onClick(DialogInterface dialog, int which) {
					   switch (which) {
						   case 0:
							   onMuteSelected(TimeUnit.HOURS.toMillis(8));
							   break;
						   case 1:
							   onMuteSelected(TimeUnit.DAYS.toMillis(1));
							   break;
						   case 2:
							   onMuteSelected(TimeUnit.DAYS.toMillis(7));
							   break;
						   default:
							   onMuteSelected(-1);
					   }
					   dismiss();
				   }
			   });

		final AlertDialog alertDialog = builder.create();
		setCancelable(true);
		return alertDialog;
	}

	public abstract void onMuteSelected(long mutedTime);

}
