package com.imbryk.text;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.LeadingMarginSpan;
import android.widget.TextView;

public class NumberSpan implements LeadingMarginSpan {
	private final int mGapWidth;
	private final boolean mWantColor;
	private final int mColor;
	private final float mEms;

	private TextView mTv;
	private String mNum;

	public static final int STANDARD_GAP_WIDTH = 2;

	public NumberSpan(TextView tv, int num) {
		mTv = tv;
		mNum = String.valueOf(num);
		mGapWidth = STANDARD_GAP_WIDTH;
		mWantColor = false;
		mColor = 0;
		mEms = 2;
	}

	public NumberSpan(TextView tv, int num, int gapWidth) {
		mTv = tv;
		mNum = String.valueOf(num);
		mGapWidth = gapWidth;
		mWantColor = false;
		mColor = 0;
		mEms = 2;
	}

	public NumberSpan(TextView tv, int num, int gapWidth, float ems) {
		mTv = tv;
		mNum = String.valueOf(num);
		mGapWidth = gapWidth;
		mEms = ems;
		mColor = 0;
		mWantColor = false;
	}

	public NumberSpan(TextView tv, int num, int gapWidth, float ems, int color) {
		mTv = tv;
		mNum = String.valueOf(num);
		mGapWidth = gapWidth;
		mEms = ems;
		mColor = color;
		mWantColor = true;
	}

	public int describeContents() {
		return 0;
	}

	public int getLeadingMargin(boolean first) {

		if (mTv != null) {
			TextPaint textPaint = mTv.getPaint();
			float em = textPaint.measureText("M");
			return (int) (mEms * em + mGapWidth);
		} else {
			return mGapWidth;
		}
	}

	public void drawLeadingMargin(Canvas c, Paint paint, int x, int dir, int top, int baseline, int bottom,
			CharSequence text, int start, int end, boolean first, Layout l) {
		if (((Spanned) text).getSpanStart(this) == start) {

			if (mTv != null) {
				TextPaint textPaint = mTv.getPaint();
				int oldcolor = 0;

				if (mWantColor) {
					oldcolor = textPaint.getColor();
					textPaint.setColor(mColor);
				}

				String txt = mNum + ".";
				float em = textPaint.measureText("M");
				float txtW = textPaint.measureText(txt);

				if (textPaint != null) {
					c.drawText(txt, x + mEms * em - txtW, baseline, textPaint);
				}
				if (mWantColor) {
					textPaint.setColor(oldcolor);
				}
			}
		}
	}
}
