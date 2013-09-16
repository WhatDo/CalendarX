package dk.appdo.calendarx;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dk.appdo.calendarx.view.CalendarView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarFragment extends Fragment implements CalendarView.OnFocusMonthChangeListener {

	public interface OnTitleChangedListener {
		void onTitleChanged(String newTitle, int page);
	}

	private static final int WEEKVIEW_ROW_COUNT = 1;

	private static final String LOG_TAG = "CalendarFragment";

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
		mCalendarView.setOnFocusMonthChangeListener(this);

		switch (mType) {
			case MainActivity.PAGE_MONTH:
				setUpMonth();
				break;

			case MainActivity.PAGE_WEEK:
				setUpWeek();
				break;
		}

		mCalendarView.init();
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

	@Override
	public void onFocusMonthChanged(long newTimeInMillis) {
		Log.d(LOG_TAG, "CHANGING TITLE TO " + newTimeInMillis);
		if (mOnTitleChangedListener != null) {
			final int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_MONTH_DAY
					| DateUtils.FORMAT_SHOW_YEAR;

			String newMonthName = DateUtils.formatDateRange(getActivity(), newTimeInMillis, newTimeInMillis, flags);

			Log.d(LOG_TAG, "CHANGING TITLE TO " + newMonthName);

			mOnTitleChangedListener.onTitleChanged(newMonthName, mType);
		}
	}
}
