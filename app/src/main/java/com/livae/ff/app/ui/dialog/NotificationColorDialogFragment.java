package com.livae.ff.app.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;

import com.livae.ff.app.R;

import javax.annotation.Nonnull;

public abstract class NotificationColorDialogFragment extends DialogFragment
  implements View.OnClickListener {

	private static final String ARGUMENT_COLOR = "ARGUMENT_COLOR";

	public void show(FragmentManager fragmentManager, Integer selectedColor) {
		Bundle arguments = new Bundle();
		if (selectedColor != null) {
			arguments.putInt(ARGUMENT_COLOR, selectedColor);
		}
		setArguments(arguments);
		show(fragmentManager, (String) null);
	}

	@Nonnull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(R.layout.dialog_colors)
			   .setTitle(R.string.select_notification_color)
			   .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
									  @Override
									  public void onClick(DialogInterface dialog, int which) {
										  dismiss();
									  }
								  })
			   .setNeutralButton(R.string.default_color, new DialogInterface.OnClickListener() {
									 @Override
									 public void onClick(DialogInterface dialog, int which) {
										 onColorSelected(null);
										 dismiss();
									 }
								 });

		final AlertDialog alertDialog = builder.create();
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				View view = getView();
				assert view != null;
				ImageView[] colorButtons = new ImageView[16];
				colorButtons[0] = (ImageView) view.findViewById(R.id.color_yellow_500);
				colorButtons[1] = (ImageView) view.findViewById(R.id.color_amber_500);
				colorButtons[2] = (ImageView) view.findViewById(R.id.color_orange_500);
				colorButtons[3] = (ImageView) view.findViewById(R.id.color_deep_orange_500);
				colorButtons[4] = (ImageView) view.findViewById(R.id.color_red_500);
				colorButtons[5] = (ImageView) view.findViewById(R.id.color_pink_500);
				colorButtons[6] = (ImageView) view.findViewById(R.id.color_purple_500);
				colorButtons[7] = (ImageView) view.findViewById(R.id.color_deep_purple_500);
				colorButtons[8] = (ImageView) view.findViewById(R.id.color_indigo_500);
				colorButtons[9] = (ImageView) view.findViewById(R.id.color_blue_500);
				colorButtons[10] = (ImageView) view.findViewById(R.id.color_ligth_blue_500);
				colorButtons[11] = (ImageView) view.findViewById(R.id.color_cyan_500);
				colorButtons[12] = (ImageView) view.findViewById(R.id.color_teal_500);
				colorButtons[13] = (ImageView) view.findViewById(R.id.color_green_500);
				colorButtons[14] = (ImageView) view.findViewById(R.id.color_light_green_500);
				colorButtons[15] = (ImageView) view.findViewById(R.id.color_lime_500);
				for (ImageView imageView : colorButtons) {
					imageView.setOnClickListener(NotificationColorDialogFragment.this);
				}
				final Bundle arguments = getArguments();
				if (arguments.containsKey(ARGUMENT_COLOR)) {
					int selectedColor = arguments.getInt(ARGUMENT_COLOR);
					switch (selectedColor) {
						case 0xFFEB3B:
							colorButtons[0].setSelected(true);
							break;
						case 0xFFC107:
							colorButtons[1].setSelected(true);
							break;
						case 0xFF9800:
							colorButtons[2].setSelected(true);
							break;
						case 0xFF5722:
							colorButtons[3].setSelected(true);
							break;
						case 0xF44336:
							colorButtons[4].setSelected(true);
							break;
						case 0xE91E63:
							colorButtons[5].setSelected(true);
							break;
						case 0x9C27B0:
							colorButtons[6].setSelected(true);
							break;
						case 0x673AB7:
							colorButtons[7].setSelected(true);
							break;
						case 0x3F51B5:
							colorButtons[8].setSelected(true);
							break;
						case 0x2196F3:
							colorButtons[9].setSelected(true);
							break;
						case 0x03A9F4:
							colorButtons[10].setSelected(true);
							break;
						case 0x00BCD4:
							colorButtons[11].setSelected(true);
							break;
						case 0x009688:
							colorButtons[12].setSelected(true);
							break;
						case 0x4CAF50:
							colorButtons[13].setSelected(true);
							break;
						case 0x8BC34A:
							colorButtons[14].setSelected(true);
							break;
						case 0xCDDC39:
							colorButtons[15].setSelected(true);
							break;
					}
				}
			}
		});
		setCancelable(true);
		return alertDialog;
	}

	@Override
	public void onClick(View v) {
		Resources.Theme theme = getContext().getTheme();
		Resources resources = getResources();
		switch (v.getId()) {
			case R.id.color_yellow_500:
				onColorSelected(resources.getColor(R.color.yellow_500, theme));
				break;
			case R.id.color_amber_500:
				onColorSelected(resources.getColor(R.color.amber_500, theme));
				break;
			case R.id.color_orange_500:
				onColorSelected(resources.getColor(R.color.orange_500, theme));
				break;
			case R.id.color_deep_orange_500:
				onColorSelected(resources.getColor(R.color.deep_orange_500, theme));
				break;
			case R.id.color_red_500:
				onColorSelected(resources.getColor(R.color.red_500, theme));
				break;
			case R.id.color_pink_500:
				onColorSelected(resources.getColor(R.color.pink_500, theme));
				break;
			case R.id.color_purple_500:
				onColorSelected(resources.getColor(R.color.purple_500, theme));
				break;
			case R.id.color_deep_purple_500:
				onColorSelected(resources.getColor(R.color.deep_purple_500, theme));
				break;
			case R.id.color_indigo_500:
				onColorSelected(resources.getColor(R.color.indigo_500, theme));
				break;
			case R.id.color_blue_500:
				onColorSelected(resources.getColor(R.color.blue_500, theme));
				break;
			case R.id.color_ligth_blue_500:
				onColorSelected(resources.getColor(R.color.light_blue_500, theme));
				break;
			case R.id.color_cyan_500:
				onColorSelected(resources.getColor(R.color.cyan_500, theme));
				break;
			case R.id.color_teal_500:
				onColorSelected(resources.getColor(R.color.teal_500, theme));
				break;
			case R.id.color_green_500:
				onColorSelected(resources.getColor(R.color.green_500, theme));
				break;
			case R.id.color_light_green_500:
				onColorSelected(resources.getColor(R.color.light_green_500, theme));
				break;
			case R.id.color_lime_500:
				onColorSelected(resources.getColor(R.color.lime_500, theme));
				break;
		}
		dismiss();
	}

	public abstract void onColorSelected(Integer color);

}
