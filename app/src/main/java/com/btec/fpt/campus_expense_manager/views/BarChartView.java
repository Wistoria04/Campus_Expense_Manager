package com.btec.fpt.campus_expense_manager.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Map;

public class BarChartView extends View {
    private Map<String, Double> data;
    private Paint paint;
    private Paint textPaint;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        invalidate(); // Redraw the chart
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.isEmpty()) {
            paint.setTextSize(40f);
            paint.setColor(Color.BLACK);
            canvas.drawText("No data available", getWidth() / 2f - 100, getHeight() / 2f, paint);
            return;
        }

        float barWidth = getWidth() / (data.size() * 2f);
        float max = 0f;

        for (double value : data.values()) {
            if (value > max) max = (float) value;
        }

        int colorIndex = 0;
        int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA};

        float x = barWidth;

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            paint.setColor(colors[colorIndex % colors.length]);
            float barHeight = (float) (entry.getValue() / max * (getHeight() - 100));

            // Draw bar
            canvas.drawRect(x, getHeight() - barHeight, x + barWidth, getHeight(), paint);

            // Draw label
            textPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(entry.getKey(), x + barWidth / 2, getHeight() - barHeight - 10, textPaint);

            textPaint.setTextAlign(Paint.Align.CENTER);
            String valueText = String.valueOf(entry.getValue());
            canvas.drawText(valueText, x + barWidth / 2, getHeight() - barHeight - 40, textPaint);

            x += barWidth * 2;
            colorIndex++;
        }
    }
}
