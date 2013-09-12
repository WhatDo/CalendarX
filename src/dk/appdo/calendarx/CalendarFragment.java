package dk.appdo.calendarx;

import android.support.v4.app.Fragment;

public abstract class CalendarFragment extends Fragment {

	public interface OnTitleChangedListener {
		void onTitleChanged(String newTitle, int page);
	}

	public static final int PAGE_MONTH = 0;
	public static final int PAGE_WEEK = 1;
	public static final int PAGE_DAY = 2;

	protected OnTitleChangedListener mOnTitleChangedListener;

	public void setOnTitleChangedListener(OnTitleChangedListener onTitleChangedListener) {
		mOnTitleChangedListener = onTitleChangedListener;
	}

}
