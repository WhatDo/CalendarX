package dk.appdo.calendarx;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dk.appdo.calendarx.view.WeekView;

public class WeekFragment extends Fragment {

	private static final long ANIMATION_DURATION = 250;

	public static final int VIEW_WIDTH = -1;

	private WeekView mWeekView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mWeekView = new WeekView(getActivity());
		return mWeekView;
	}

	public void setShownWeek(int week) {
		mWeekView.setFocusedWeek(week);
	}

	public void onPageScrolled(boolean focusWeek, int pixels, int focusedTopOffset) {
		int width = mWeekView.getWidth();
		if (pixels != VIEW_WIDTH) {
			mWeekView.slideFocusedView(-width + pixels);
		} else {
			mWeekView.slideFocusedView(0);
		}
		mWeekView.setWeekIsFocused(focusWeek);
		mWeekView.translateFocusedViewY(focusedTopOffset - mWeekView.getFocusedViewDisplayedTopOffset());
	}

	public void onAnimationStarted() {
		mWeekView.scaleFocusedView(1.0f / 6.0f);
	}

	public void onAnimationComplete() {
		mWeekView.animateFocusedViewY(1, 0, ANIMATION_DURATION);
	}
}
