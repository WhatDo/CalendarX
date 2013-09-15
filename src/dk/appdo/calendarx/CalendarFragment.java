package dk.appdo.calendarx;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dk.appdo.calendarx.view.CalendarView;

public class CalendarFragment extends Fragment {

	public interface OnTitleChangedListener {
		void onTitleChanged(String newTitle, int page);
	}

	private static final int WEEKVIEW_ROW_COUNT = 1;

	private OnTitleChangedListener mOnTitleChangedListener;
	private CalendarView mCalendarView;

	private int mType;

	public static CalendarFragment newInstance(int type) {
		CalendarFragment frag = new CalendarFragment();
		frag.mType = type;
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mCalendarView = new CalendarView(getActivity());

		switch (mType) {
			case MainActivity.PAGE_MONTH:
				setUpMonth();
				break;

			case MainActivity.PAGE_WEEK:
				setUpWeek();
				break;
		}

		return mCalendarView;
	}

	private void setUpMonth() {

	}

	private void setUpWeek() {
		mCalendarView.setRowCount(WEEKVIEW_ROW_COUNT);
	}

	public void setOnTitleChangedListener(OnTitleChangedListener onTitleChangedListener) {
		mOnTitleChangedListener = onTitleChangedListener;
	}

}
