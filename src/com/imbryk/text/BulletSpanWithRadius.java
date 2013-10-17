package com.imbryk.text;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.os.Build;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.BulletSpan;
import android.text.style.LeadingMarginSpan;

/**
 * Custom Bullet Span implementation (based on {@link BulletSpan})
 * Default implementation doesn't allow for radius modification
 */
public class BulletSpanWithRadius implements LeadingMarginSpan {
	private final int mGapWidth;
	private final int mBulletRadius;
	private final boolean mWantColor;
	private final int mColor;

	private static Path sBulletPath = null;
	public static final int STANDARD_GAP_WIDTH = 2;
	public static final int STANDARD_BULLET_RADIUS = 4;

	public BulletSpanWithRadius() {
		mGapWidth = STANDARD_GAP_WIDTH;
		mBulletRadius = STANDARD_BULLET_RADIUS;
		mWantColor = false;
		mColor = 0;
	}

	public BulletSpanWithRadius(int gapWidth) {
		mGapWidth = gapWidth;
		mBulletRadius = STANDARD_BULLET_RADIUS;
		mWantColor = false;
		mColor = 0;
	}

	public BulletSpanWithRadius(int bulletRadius, int gapWidth) {
		mGapWidth = gapWidth;
		mBulletRadius = bulletRadius;
		mWantColor = false;
		mColor = 0;
	}

	public BulletSpanWithRadius(int bulletRadius, int gapWidth, int color) {
		mGapWidth = gapWidth;
		mBulletRadius = bulletRadius;
		mWantColor = true;
		mColor = color;
	}

	public int getLeadingMargin(boolean first) {
		return 2 * mBulletRadius + mGapWidth;
	}

	@SuppressLint("NewApi")
	public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom,
			CharSequence text, int start, int end, boolean first, Layout l) {
		if (((Spanned) text).getSpanStart(this) == start) {
			Paint.Style style = p.getStyle();
			int oldcolor = 0;

			if (mWantColor) {
				oldcolor = p.getColor();
				p.setColor(mColor);
			}

			p.setStyle(Paint.Style.FILL);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && c.isHardwareAccelerated()) {
				if (sBulletPath == null) {
					sBulletPath = new Path();
					// Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
					sBulletPath.addCircle(0.0f, 0.0f, 1.2f * mBulletRadius, Direction.CW);
				}

				c.save();
				c.translate(x + dir * (mBulletRadius * 1.2f + 1), (top + bottom) / 2.0f);
				c.drawPath(sBulletPath, p);
				c.restore();
			} else {
				c.drawCircle(x + dir * (mBulletRadius + 1), (top + bottom) / 2.0f, mBulletRadius, p);
			}

			if (mWantColor) {
				p.setColor(oldcolor);
			}
			p.setStyle(style);
		}
	}

}
