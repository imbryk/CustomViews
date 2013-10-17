package com.imbryk.text;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.net.Uri;
import android.provider.Browser;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.imbryk.demo.R;

/**
 *  Container for a {@link TextView} converting {@link URLSpan} from text to clickable links,
 * while still allowing to catch the touch events behind the TextView.
 * <p>
 * Standard implementation of {@link TextView} with {@link Html} intercepts all the clicks.
 * <p> 
 * Additionally a custom highlight color can be applied 
 *
 */
public class TextWithLinksContainer extends FrameLayout {

	private static final float TOUCH_RECT_SIZE = 8;//dp
	private int mLayoutWidth = 0;
	private TextView mTv;

	private int mSelectionColor = 0x55000099;

	private int mTop = 0;
	private int mLeft = 0;
	private Paint mPaintSelected;
	private ArrayList<Region> mRegions;
	private ArrayList<Path> mPaths;
	private HashMap<Region, Path> mRegion2Path;
	private HashMap<Region, String> mRegion2Url;

	private int mTouchX;
	private int mTouchY;
	private Rect mTouchRect;
	private Region mSelectedRegion;

	private boolean mIsDown = false;

	private TextWatcher mTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			findUrls();
		}
	};

	public TextWithLinksContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public TextWithLinksContainer(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TextWithLinksContainer(Context context) {
		this(context, null);
	}

	private void init(Context context, AttributeSet attrs) {
		setWillNotDraw(false);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TextWithLinksContainer);
		mSelectionColor = a.getColor(R.styleable.TextWithLinksContainer_selected_color, mSelectionColor);
		a.recycle();

		int d = (int) (context.getResources().getDisplayMetrics().density * TOUCH_RECT_SIZE);
		mTouchRect = new Rect(-d, -d, d, d);

		mPaintSelected = new Paint();
		mPaintSelected.setColor(mSelectionColor);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mTv = (TextView) getChildAt(0);
		if (mTv != null) {
			mTv.addTextChangedListener(mTextWatcher);
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			findUrls();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mIsDown) {
			if (mSelectedRegion != null) {
				canvas.drawPath(mRegion2Path.get(mSelectedRegion), mPaintSelected);
			}
		}
	}

	protected void findUrls() {

		Layout layout = mTv.getLayout();
		if (layout != null) {
			int w = layout.getWidth();
			if (w == mLayoutWidth) {
				return;
			}
			mTop = mTv.getTop() + mTv.getPaddingTop();
			mLeft = mTv.getLeft() + mTv.getPaddingLeft();
			mLayoutWidth = w;
			CharSequence txt = mTv.getText();
			SpannableString s = new SpannableString(txt);
			URLSpan[] spans = s.getSpans(0, txt.length(), URLSpan.class);

			mPaths = new ArrayList<Path>();
			mRegions = new ArrayList<Region>();
			mRegion2Path = new HashMap<Region, Path>();
			mRegion2Url = new HashMap<Region, String>();

			for (URLSpan span : spans) {
				int start = s.getSpanStart(span);
				int end = s.getSpanEnd(span);

				Path path = new Path();
				layout.getSelectionPath(start, end, path);
				path.offset(mLeft, mTop);
				mPaths.add(path);

				RectF rectF = new RectF();
				path.computeBounds(rectF, true);

				Region region = new Region();
				region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right,
						(int) rectF.bottom));
				mRegions.add(region);

				mRegion2Path.put(region, path);
				mRegion2Url.put(region, span.getURL());
			}
		}
	}

	private boolean testTouch() {
		mTouchRect.offset(mTouchX, mTouchY);
		Iterator<Region> ir = mRegions.iterator();
		mSelectedRegion = null;
		boolean hit = false;
		while (ir.hasNext()) {
			Region original = ir.next();
			Region region = new Region(original);
			if (region.op(mTouchRect, Op.INTERSECT)) {
				mSelectedRegion = original;
				hit = true;
			}
		}
		mTouchRect.offset(-mTouchX, -mTouchY);
		return hit;
	}

	private void preformUrlClick() {
		String url = mRegion2Url.get(mSelectedRegion).trim();

		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.putExtra(Browser.EXTRA_APPLICATION_ID, getContext().getPackageName());
		getContext().startActivity(intent);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		int action = MotionEventCompat.getActionMasked(event);
		boolean shouldInterceptTouch = false;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mIsDown = true;
			mTouchX = (int) event.getX();
			mTouchY = (int) event.getY();
			shouldInterceptTouch = testTouch();
			if (shouldInterceptTouch) {
				ViewCompat.postInvalidateOnAnimation(this);
			}
			break;
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (mIsDown) {
				mIsDown = false;
				ViewCompat.postInvalidateOnAnimation(this);
			}
			break;
		}

		return shouldInterceptTouch;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = MotionEventCompat.getActionMasked(event);

		switch (action) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:

			if (mIsDown) {
				mTouchX = (int) event.getX();
				mTouchY = (int) event.getY();
				if (testTouch()) {
					ViewCompat.postInvalidateOnAnimation(this);
					return true;
				} else {
					mIsDown = false;
				}
				ViewCompat.postInvalidateOnAnimation(this);
			} else {

			}
		case MotionEvent.ACTION_UP:
			if (mIsDown) {
				mTouchX = (int) event.getX();
				mTouchY = (int) event.getY();
				mIsDown = false;
				ViewCompat.postInvalidateOnAnimation(this);
				if (testTouch()) {
					preformUrlClick();
					return true;
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			if (mIsDown) {
				mIsDown = false;
				ViewCompat.postInvalidateOnAnimation(this);
			}
			break;
		}

		return super.onTouchEvent(event);
	}
}
