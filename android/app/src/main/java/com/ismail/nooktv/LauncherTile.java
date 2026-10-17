package com.ismail.nooktv;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.view.animation.DecelerateInterpolator;
import android.widget.*;

/**
 * Focusable card with restrained Apple TV-inspired geometry, high-contrast focus outline,
 * clean typography, and proportional artwork presentation.
 */
final class LauncherTile extends LinearLayout {
    private final ImageView artwork;
    private final TextView title, subtitle;
    private final int color;
    private final boolean motion;

    LauncherTile(Context context, String label, String detail, String symbol, int color, int width, boolean motion, Runnable open, Runnable more) {
        super(context);
        this.color = color;
        this.motion = motion;
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setPadding(dp(10), dp(12), dp(10), dp(10));
        setFocusable(true);
        setClickable(true);
        setClipChildren(false);
        setContentDescription(label + (detail.isEmpty() ? "" : ", " + detail));

        artwork = new ImageView(context);
        artwork.setScaleType(ImageView.ScaleType.FIT_CENTER);
        artwork.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        artwork.setImageDrawable(new Symbol(symbol));
        addView(artwork, new LayoutParams(-1, dp(60)));

        title = new TextView(context);
        title.setText(label);
        title.setTextColor(0xfff8fafc);
        title.setTextSize(15);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setGravity(Gravity.CENTER);
        title.setMaxLines(1);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        title.setPadding(0, dp(8), 0, 0);
        addView(title, new LayoutParams(-1, dp(28)));

        subtitle = new TextView(context);
        subtitle.setText(detail);
        subtitle.setTextColor(0xff94a3b8);
        subtitle.setTextSize(12);
        subtitle.setMaxLines(1);
        subtitle.setEllipsize(android.text.TextUtils.TruncateAt.END);
        subtitle.setGravity(Gravity.CENTER);
        addView(subtitle, new LayoutParams(-1, dp(18)));

        setBackground(background(false));
        setOnClickListener(v -> open.run());
        if (more != null) {
            setOnLongClickListener(v -> {
                more.run();
                return true;
            });
        }

        setOnFocusChangeListener((v, focused) -> {
            setBackground(background(focused));
            setElevation(focused ? dp(10) : dp(1));
            if (this.motion && android.animation.ValueAnimator.areAnimatorsEnabled()) {
                animate()
                    .scaleX(focused ? 1.04f : 1.0f)
                    .scaleY(focused ? 1.04f : 1.0f)
                    .setDuration(140)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .start();
            } else {
                setScaleX(1.0f);
                setScaleY(1.0f);
            }
        });

        setLayoutParams(new ViewGroup.LayoutParams(width, dp(140)));
    }

    void artwork(Drawable image) {
        if (image != null) artwork.setImageDrawable(image);
    }

    private int dp(int n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }

    private Drawable background(boolean focus) {
        GradientDrawable d = new GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            new int[]{color, 0xff161a29}
        );
        d.setCornerRadius(dp(16));
        d.setStroke(dp(focus ? 3 : 1), focus ? 0xffffffff : 0x24ffffff);
        return d;
    }

    private static final class Symbol extends Drawable {
        final String glyph;
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        Symbol(String glyph) {
            this.glyph = glyph;
        }

        @Override
        public void draw(Canvas c) {
            Rect b = getBounds();
            float x = b.exactCenterX(), y = b.exactCenterY(), s = Math.min(b.width(), b.height()) * .34f;
            p.setColor(Color.WHITE);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(s * .12f);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);

            if (glyph.equals("phone")) {
                c.drawRoundRect(x - s * .58f, y - s, x + s * .58f, y + s, s * .18f, s * .18f, p);
                c.drawLine(x - s * .15f, y + s * .75f, x + s * .15f, y + s * .75f, p);
            } else if (glyph.equals("apps")) {
                for (int row = 0; row < 2; row++) {
                    for (int col = 0; col < 2; col++) {
                        float left = x - s + col * s * 1.15f, top = y - s + row * s * 1.15f;
                        c.drawRoundRect(left, top, left + s * .8f, top + s * .8f, s * .12f, s * .12f, p);
                    }
                }
            } else if (glyph.equals("drive")) {
                Path t = new Path();
                t.moveTo(x, y - s);
                t.lineTo(x + s, y + s * .75f);
                t.lineTo(x - s, y + s * .75f);
                t.close();
                c.drawPath(t, p);
            } else if (glyph.equals("recent")) {
                c.drawCircle(x, y, s, p);
                c.drawLine(x, y, x, y - s * .55f, p);
                c.drawLine(x, y, x + s * .45f, y + s * .2f, p);
            } else if (glyph.equals("play") || glyph.equals("youtube")) {
                p.setStyle(Paint.Style.FILL);
                Path t = new Path();
                t.moveTo(x - s * .55f, y - s * .9f);
                t.lineTo(x + s * .9f, y);
                t.lineTo(x - s * .55f, y + s * .9f);
                t.close();
                c.drawPath(t, p);
            } else if (glyph.equals("rss")) {
                c.drawCircle(x - s * .6f, y + s * .6f, s * .18f, p);
                RectF r1 = new RectF(x - s * .6f - s * .8f, y + s * .6f - s * .8f, x - s * .6f + s * .8f, y + s * .6f + s * .8f);
                c.drawArc(r1, -90, 90, false, p);
                RectF r2 = new RectF(x - s * .6f - s * 1.4f, y + s * .6f - s * 1.4f, x - s * .6f + s * 1.4f, y + s * .6f + s * 1.4f);
                c.drawArc(r2, -90, 90, false, p);
            } else {
                p.setStyle(Paint.Style.FILL);
                p.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                p.setTextSize(s * 1.5f);
                p.setTextAlign(Paint.Align.CENTER);
                c.drawText(glyph.isEmpty() ? "N" : glyph.substring(0, 1).toUpperCase(java.util.Locale.ROOT), x, y - (p.ascent() + p.descent()) / 2, p);
            }
        }

        @Override
        public void setAlpha(int a) {
            p.setAlpha(a);
        }

        @Override
        public void setColorFilter(ColorFilter f) {
            p.setColorFilter(f);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
