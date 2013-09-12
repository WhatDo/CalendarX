package dk.appdo.calendarx.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import dk.appdo.calendarx.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by Jonas on 20-08-13.
 */
public class MonthView extends FrameLayout {

	public interface OnFocusMonthChangeListener {
		void onFocusMonthChanged(long newTimeInMillis);
	}

	public interface OnDateSelectedListener {
		void OnDateSelected(long timeInMillis);
	}

	private static final int DAYS_PER_WEEK = 7;

	/**
	 * The default minimal date.
	 */
	private static final String DEFAULT_MIN_DATE = "01/01/1900";

	/**
	 * The default maximal date.
	 */
	private static final String DEFAULT_MAX_DATE = "01/01/2100";

	/**
	 * String for parsing dates.
	 */
	private static final String DATE_FORMAT = "dd/MM/yyyy";

	private static final int DEFAULT_FIRST_DAY_OF_WEEK = Calendar.MONDAY;

	private static final String LOG_TAG = "calendarx";

	private static final long MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

	private static final long MILLIS_IN_WEEK = MILLIS_IN_DAY * 7;

	private Paint mDatePaint = new Paint();

	private Paint mFocusPaint = new Paint();

	private Paint mTempPaint = new Paint();

	private Calendar mTempDate = Calendar.getInstance();

	private Calendar mFocus = Calendar.getInstance();

	private ListView mListView;

	private WeekAdapter mAdapter;

	private ViewGroup mDayNamesHeader;

	private Drawable mDividerDrawable;

	private boolean mShowWeekNumbers = false;

	private Context mContext;

	private int mHeight;

	private int mShownWeekCount = 6;

	private int mDateTextSize;

	private DateFormat mDateFormat = new SimpleDateFormat(DATE_FORMAT);

	private Calendar mMinDate = Calendar.getInstance();

	private Calendar mMaxDate = Calendar.getInstance();

	private Calendar mTodayDate = Calendar.getInstance();

	private int mFirstDayOfWeek;

	private int mFocusMonth;

	private int mFocusMonthColor;

	private int mUnfocusMonthColor;

	private int mFirstWeekOfFocusedMonth;

	private float mFriction = .05f;

	private float mVelocityScale = .333f;

	private int mSelectedColor;

	private int mFocusedWeekColor;

	private WeekView mFocusedWeekView;

	private OnFocusMonthChangeListener mOnFocusMonthChangeListener;

	private OnDateSelectedListener mOnDateSelectedListener;

	private boolean mOverrideHeightMeassure = false;

	public MonthView(Context context) {
		this(context, null);
	}

	public MonthView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MonthView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, 0);

		mContext = context;

		TypedArray t = context.obtainStyledAttributes(android.R.style.TextAppearance_Small, new int[]{android.R.attr.textSize});

		mDateTextSize = t.getDimensionPixelSize(0, 1);

		t.recycle();

		mSelectedColor = getResources().getColor(android.R.color.holo_green_dark);

		mFocusedWeekColor = getResources().getColor(android.R.color.holo_blue_dark);
		mFocusMonthColor = getResources().getColor(android.R.color.holo_blue_light);
		mUnfocusMonthColor = getResources().getColor(android.R.drawable.screen_background_dark_transparent);

		mDividerDrawable = getResources().getDrawable(android.R.drawable.divider_horizontal_dark);

		parseDate(DEFAULT_MAX_DATE, mMaxDate);
		parseDate(DEFAULT_MIN_DATE, mMinDate);

		mTodayDate.setTimeInMillis(System.currentTimeMillis());

		mTempDate.setTimeInMillis(System.currentTimeMillis());

		mFirstDayOfWeek = DEFAULT_FIRST_DAY_OF_WEEK;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = inflater.inflate(R.layout.calendar_view, null, false);

		addView(content);

		mAdapter = new WeekAdapter();

		mListView = (ListView) content.findViewById(R.id.calendar_list);
		mListView.setAdapter(mAdapter);
		mListView.setItemsCanFocus(true);
		mListView.setVerticalScrollBarEnabled(false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mListView.setFriction(mFriction);
			mListView.setVelocityScale(mVelocityScale);
		}

		mListView.setOnScrollListener(mAdapter);

		mDayNamesHeader = (ViewGroup) content.findViewById(R.id.day_names_header);

		setUpHeader();

		initPaints();

		goTo(getWeeksSinceMinDate(mTodayDate), true);
	}

	private void initPaints() {
		mDatePaint.setColor(0xFFFFFFFF);
		mDatePaint.setTextAlign(Paint.Align.CENTER);
		mDatePaint.setFakeBoldText(true);
		mDatePaint.setTextSize(mDateTextSize);
		mDatePaint.setAntiAlias(true);

		mFocusPaint.setColor(getResources().getColor(android.R.color.holo_orange_dark));
		mFocusPaint.setAntiAlias(true);
		mFocusPaint.setStyle(Paint.Style.STROKE);
		mFocusPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));

		mTempPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	}

	private boolean isToday(Calendar date) {
		mTodayDate.setTimeInMillis(System.currentTimeMillis());
		return
				date.get(Calendar.DAY_OF_MONTH) == mTodayDate.get(Calendar.DAY_OF_MONTH)
						&& date.get(Calendar.MONTH) == mTodayDate.get(Calendar.MONTH)
						&& date.get(Calendar.YEAR) == mTodayDate.get(Calendar.YEAR);
	}

	private void setUpHeader() {

		String[] dayLabels = new String[DAYS_PER_WEEK];

		for (int i = DEFAULT_FIRST_DAY_OF_WEEK, j = 0; i < DAYS_PER_WEEK + DEFAULT_FIRST_DAY_OF_WEEK; i++, j++) {
			int calendarDay = (i > Calendar.SATURDAY) ? i - Calendar.SATURDAY : i;
			dayLabels[j] = DateUtils.getDayOfWeekString(calendarDay,
					DateUtils.LENGTH_MEDIUM);
		}

		TextView v = (TextView) mDayNamesHeader.getChildAt(0);

		if (mShowWeekNumbers) {
			v.setVisibility(VISIBLE);
		} else {
			v.setVisibility(GONE);
		}

		for (int i = 1; i < mDayNamesHeader.getChildCount(); i++) {
			v = (TextView) mDayNamesHeader.getChildAt(i);
			v.setText(dayLabels[i - 1]);
			v.setGravity(Gravity.CENTER_HORIZONTAL);
		}
	}

	/**
	 * Parses the given <code>date</code> and in case of success sets
	 * the result to the <code>outDate</code>.
	 *
	 * @return True if the date was parsed.
	 */
	private boolean parseDate(String date, Calendar outDate) {
		try {
			outDate.setTime(mDateFormat.parse(date));
			return true;
		} catch (ParseException e) {
			Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
			return false;
		}
	}

	/**
	 * @return Returns the number of weeks between the current <code>date</code>
	 *         and the <code>mMinDate</code>.
	 */
	private int getWeeksSinceMinDate(Calendar date) {
		if (date.before(mMinDate)) {
			throw new IllegalArgumentException("fromDate: " + mMinDate.getTime()
					+ " does not precede toDate: " + date.getTime());
		}
		long endTimeMillis = date.getTimeInMillis()
				+ date.getTimeZone().getOffset(date.getTimeInMillis());
		long startTimeMillis = mMinDate.getTimeInMillis()
				+ mMinDate.getTimeZone().getOffset(mMinDate.getTimeInMillis());
		long dayOffsetMillis = (mMinDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek)
				* MILLIS_IN_DAY;
		return (int) ((endTimeMillis - startTimeMillis + dayOffsetMillis) / MILLIS_IN_WEEK);
	}

	private void goTo(int week, boolean focusMonth) {

		if (focusMonth) {
			mTempDate.setTimeInMillis(mMinDate.getTimeInMillis());
			mTempDate.add(Calendar.WEEK_OF_YEAR, week);

			mTempDate.set(Calendar.DAY_OF_MONTH, 1);

			week = getWeeksSinceMinDate(mTempDate);

			mAdapter.setFocusedMonth(mTempDate.get(Calendar.MONTH));

			mFirstWeekOfFocusedMonth = week;
		}
		mFocus.setTimeInMillis(mTempDate.getTimeInMillis());
		onFocusMonthChange(mTempDate.getTimeInMillis());
		mListView.setSelection(week);
	}

	private void onDateSelected(long timeInMillis) {
		if (mOnDateSelectedListener != null) {
			mOnDateSelectedListener.OnDateSelected(timeInMillis);
		}
	}

	private void onFocusMonthChange(long timeInMillis) {
		if (mOnFocusMonthChangeListener != null) {
			mOnFocusMonthChangeListener.onFocusMonthChanged(timeInMillis);
		}
	}

	public void setOnFocusMonthChangedListener(OnFocusMonthChangeListener onFocusMonthChangeListener) {
		this.mOnFocusMonthChangeListener = onFocusMonthChangeListener;
	}

	public void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
		this.mOnDateSelectedListener = onDateSelectedListener;
	}

	public int getFocusedWeekNumber() {
		if (mFocusedWeekView != null) {
			return mFocusedWeekView.getWeekNumber();
		}

		return -1;
	}

	public void setFocusedWeek(int week) {
		mListView.setSelection(week);
		mFocusedWeekView = (WeekView) mListView.getChildAt(0);
	}

	public void setShownItem(int item) {
		mListView.setSelection(item);
	}

	public void setWeekIsFocused(boolean focus) {
		if (focus) {
			if (mFocusedWeekView != null) {
				mFocusedWeekView.setIsFocus(true);
				mFocusedWeekView.invalidate();
			}
		} else {
			if (mFocusedWeekView != null) {
				mFocusedWeekView.setIsFocus(false);
				mFocusedWeekView.invalidate();
			}
		}
	}

	public void slideFocusedView(int x) {
		if (mFocusedWeekView != null) {
			//mFocusedWeekView.animate().translationX(x).setDuration(0).setStartDelay(0).start();
			mFocusedWeekView.setTranslationX(x);
		}
	}

	protected void setDayViewHeight(int height) {
		mHeight = height;
		mOverrideHeightMeassure = true;
		invalidate();
	}

	public void scaleFocusedView(float scale) {
		//mFocusedWeekView.setScaleY(scale);
		if (mFocusedWeekView != null) {
			mFocusedWeekView.setDisplayedHeight((int) (mFocusedWeekView.getHeight() * scale));
		}
	}

	public void animateFocusedViewY(float yScale, int yTranslate, long duration) {
		if (mFocusedWeekView != null) {
			//mFocusedWeekView.animate().setDuration(duration).scaleY(i).setStartDelay(0).start();
			new ScaleViewTask(mFocusedWeekView, (int) (mHeight * yScale), yTranslate, duration).execute();
		}
	}

	public void translateFocusedViewY(int y) {
		if (mFocusedWeekView != null) {
			mFocusedWeekView.setDisplayedY(y);
		}
	}

	public int getFocusedViewYPos() {
		if (mFocusedWeekView != null) {
			return mFocusedWeekView.getTop();
		}
		return -1;
	}

	public int getFocusedViewDisplayedTopOffset() {
		return mFocusedWeekView.getTop() + mFocusedWeekView.getHeight() / 2 - mFocusedWeekView.getDisplayedHeight() / 2;
	}

	private class ScaleViewTask extends AsyncTask<Void, Void, Void> {

		private int mTargetHeight;

		private int mHeight;

		private long mDuration;

		private int mTargetYTranslate;

		private int mYTranslate;

		private WeekView mView;

		private int mHeightSteps;

		private int mYSteps;

		private int mSteps = 60;

		private Runnable mRun = new Runnable() {
			@Override
			public void run() {
				mView.setDisplayedY(mYTranslate);
				mView.setDisplayedHeight(mHeight);
			}
		};

		public ScaleViewTask(WeekView v, int targetHeight, int targetYTranslate, long duration) {
			mView = v;
			mHeight = v.getDisplayedHeight();
			mTargetHeight = targetHeight;
			mDuration = duration;
			mTargetYTranslate = targetYTranslate;
			mYTranslate = v.getDisplayedY();
			mHeightSteps = (targetHeight - mHeight) / mSteps;
			mYSteps = (mTargetYTranslate - mYTranslate) / mSteps;
		}

		@Override
		protected Void doInBackground(Void... params) {

			while (mHeight < mTargetHeight) {
				mHeight += mHeightSteps;
				mYTranslate += mYSteps;

				mView.post(mRun);

				try {
					Thread.sleep(mDuration / mSteps);
				} catch (InterruptedException e) {
				}
			}

			mHeight = MonthView.this.mHeight;
			mYTranslate = mTargetYTranslate;
			mView.post(mRun);
			return null;
		}
	}

	private class WeekAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

		@Override
		public int getCount() {
			return getWeeksSinceMinDate(mMaxDate);
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			WeekView weekView = (WeekView) convertView;
			if (weekView == null) {
				weekView = new WeekView(mContext);
				AbsListView.LayoutParams params =
						new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT);
				weekView.setLayoutParams(params);
				weekView.setClickable(true);
			}

			weekView.init(position);

			return weekView;
		}

		public void setFocusedMonth(int month) {
			mFocusMonth = month;

			notifyDataSetChanged();
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (firstVisibleItem - mFirstWeekOfFocusedMonth > 2) {
				mFocus.add(Calendar.MONTH, 1);
				notifyDataSetChanged();
				onFocusMonthChange(mFocus.getTimeInMillis());
			} else if (firstVisibleItem - mFirstWeekOfFocusedMonth < -2) {
				mFocus.add(Calendar.MONTH, -1);
				notifyDataSetChanged();
				onFocusMonthChange(mFocus.getTimeInMillis());
			}
			mFocus.set(Calendar.DAY_OF_MONTH, 1);
			mFirstWeekOfFocusedMonth = getWeeksSinceMinDate(mFocus);
		}
	}

	public ListView getCalendarListView() {
		return mListView;
	}

	private class WeekView extends LinearLayout implements OnTouchListener, OnClickListener {

		private int mWeekN;

		private boolean mIsFocusWeek;

		private int mDisplayedHeight;

		private int mY;

		private DayView[] mDayViews = new DayView[DAYS_PER_WEEK];


		public WeekView(Context context) {
			this(context, null);
		}

		public WeekView(Context context, AttributeSet attrs) {
			super(context, attrs);

			for (int i = 0; i < DAYS_PER_WEEK; i++) {

				if (i != 0) {
					ImageView divider = new ImageView(mContext);
					divider.setImageDrawable(mDividerDrawable);
					divider.setScaleType(ImageView.ScaleType.FIT_XY);

					LayoutParams params = new LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()), ViewGroup.LayoutParams.MATCH_PARENT);
					addView(divider, params);
				}

				DayView d = new DayView(mContext);
				mDayViews[i] = d;

				d.setOnTouchListener(this);
				d.setOnClickListener(this);

				LayoutParams params = new LayoutParams(0, 0, 1);
				addView(d, params);

			}
		}

		public void setIsFocus(boolean isFocus) {
			mIsFocusWeek = isFocus;
			for (DayView dv : mDayViews) {
				dv.invalidate();
			}
		}

		public void init(int week) {
			mTempDate.setTimeInMillis(mMinDate.getTimeInMillis());

			mTempDate.add(Calendar.WEEK_OF_YEAR, week);

			mWeekN = week;

			for (DayView d : mDayViews) {
				d.init(mTempDate.get(Calendar.DAY_OF_MONTH), mTempDate.get(Calendar.MONTH), mTempDate.getTimeInMillis(), isToday(mTempDate));
				mTempDate.add(Calendar.DAY_OF_MONTH, 1);
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			mHeight = mOverrideHeightMeassure ? mHeight : (mListView.getHeight() - mListView.getPaddingTop() - mListView
					.getPaddingBottom()) / mShownWeekCount;
			mDisplayedHeight = mHeight;
			setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			mFocusedWeekView = this;
			DayView w = (DayView) v;
			int action = event.getAction();

			switch (action) {
				case MotionEvent.ACTION_DOWN:
					w.setIsSelected(true);
					break;

				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					w.setIsSelected(false);
					break;

				default:
					return false;
			}
			w.invalidate();
			return true;
		}

		@Override
		public void onClick(View v) {
			DayView d = (DayView) v;
			onDateSelected(d.getDate());
		}

		public int getWeekNumber() {
			return mWeekN;
		}

		public void setDisplayedHeight(int height) {
			mDisplayedHeight = height;
			for (DayView dv : mDayViews) {
				dv.invalidate();
			}
		}

		public void setDisplayedY(int y) {
			mY = y;
			for (DayView dv : mDayViews) {
				dv.invalidate();
			}
		}

		public int getDisplayedHeight() {
			return mDisplayedHeight;
		}

		public int getDisplayedY() {
			return mY;
		}

		private class DayView extends View {

			private int mDayOfMonth;

			private boolean mIsFocusMonth;

			private boolean mIsToday;

			private boolean mIsSelected;

			private long mTimeInMillis;

			private RectF mTempRect = new RectF();

			public DayView(Context context) {
				this(context, null);
			}

			public DayView(Context context, AttributeSet attrs) {
				this(context, attrs, 0);
			}

			public DayView(Context context, AttributeSet attrs, int defStyle) {
				super(context, attrs, 0);

			}

			public void init(int dayOfMonth, int month, long timeInMillis, boolean isToday) {
				mTimeInMillis = timeInMillis;

				mDayOfMonth = dayOfMonth;

				mIsFocusMonth = month == mFocus.get(Calendar.MONTH);

				mIsToday = isToday;
			}

			public void setIsSelected(boolean selected) {
				mIsSelected = selected;
			}

			@Override
			protected void onDraw(Canvas canvas) {
				if (mIsFocusWeek) {
					//canvas.drawColor(mFocusedWeekColor);
					mTempPaint.setColor(mFocusedWeekColor);
				} else if (mIsSelected) {
					//canvas.drawColor(mSelectedColor);
					mTempPaint.setColor(mSelectedColor);
				} else if (mIsFocusMonth) {
					//canvas.drawColor(mFocusMonthColor);
					mTempPaint.setColor(mFocusMonthColor);
				} else {
					//canvas.drawColor(mUnfocusMonthColor);
					mTempPaint.setColor(mUnfocusMonthColor);
				}

				int y = canvas.getHeight() / 2 + mY;
				int dispY = mDisplayedHeight / 2;

				mTempRect.set(0, y - dispY, canvas.getWidth(), y + dispY);

				canvas.drawRect(mTempRect, mTempPaint);

				if (mIsToday) {
					canvas.drawRect(mTempRect, mFocusPaint);
				}

				canvas.drawText(mDayOfMonth + "", canvas.getWidth() / 2.0f, y, mDatePaint);
			}

			@Override
			protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
			}

			public long getDate() {
				return mTimeInMillis;
			}
		}
	}
}
