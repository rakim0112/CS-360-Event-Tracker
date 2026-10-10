package com.rakim.event47;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.view.View;
import android.view.animation.LinearInterpolator;

/** Quiet motion graphics: charcoal gradients, a nearly invisible "47", and slow red embers. */
public final class SignatureBackdrop extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float phase = 0f;
    private final ValueAnimator motion = ValueAnimator.ofFloat(0f, 1f);

    public SignatureBackdrop(Context context) {
        super(context);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        motion.setDuration(24000L);
        motion.setRepeatCount(ValueAnimator.INFINITE);
        motion.setInterpolator(new LinearInterpolator());
        motion.addUpdateListener(v -> {
            phase = (float) v.getAnimatedValue();
            if (getWindowVisibility() == VISIBLE) invalidate();
        });
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!motion.isStarted()) motion.start();
    }

    @Override protected void onDetachedFromWindow() {
        motion.cancel();
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas canvas) {
        int width = getWidth(), height = getHeight();
        if (width == 0 || height == 0) return;
        paint.setShader(new LinearGradient(0, 0, width, height, 0xFF08090D, 0xFF121014,
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(new RadialGradient(width * 0.94f, height * 0.1f, width * .75f,
                new int[]{0x3BE43943, 0x0AE43943, Color.TRANSPARENT}, null,
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);

        // Brand motif intentionally faint; decoration never obscures input or event text.
        paint.setColor(0x0EFFFFFF);
        paint.setTypeface(android.graphics.Typeface.create("sans-serif-black", 1));
        paint.setTextSize(width * .40f);
        canvas.save();
        canvas.rotate(-18, width / 2f, height / 2f);
        canvas.drawText("47", width * .23f, height * .72f, paint);
        canvas.restore();

        for (int i = 0; i < 12; i++) {
            float x = width * ((i * .227f + .113f) % 1f);
            float y = height * ((i * .179f + .137f + phase * .028f) % 1f);
            int alpha = 22 + (int) (13 * Math.sin((phase + i * .34) * 6.283f));
            paint.setColor(Color.argb(Math.max(5, alpha), 238, 67, 76));
            canvas.drawCircle(x, y, 1.1f + i % 2, paint);
        }
    }
}
