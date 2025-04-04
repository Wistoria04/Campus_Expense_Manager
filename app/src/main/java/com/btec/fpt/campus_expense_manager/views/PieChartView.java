package com.btec.fpt.campus_expense_manager.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Map;

public class PieChartView extends View {
    private Map<String, Double> data;
    private Paint paint;
    private Paint textPaint;
    private RectF rectF;

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        rectF = new RectF();
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

        float total = 0f;
        for (double value : data.values()) {
            total += value;
        }

        float startAngle = 0f;
        rectF.set(100, 100, getWidth() - 100, getHeight() - 100);

        int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA};
        int colorIndex = 0;

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            paint.setColor(colors[colorIndex % colors.length]);
            float sweepAngle = (float) (entry.getValue() / total * 360);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint);

            // Calculate text position
            float midAngle = startAngle + sweepAngle / 2;
            float x = (float) (getWidth() / 2 + Math.cos(Math.toRadians(midAngle)) * rectF.width() / 2.5);
            float y = (float) (getHeight() / 2 + Math.sin(Math.toRadians(midAngle)) * rectF.height() / 2.5);

            // Draw percentage text
            String label = entry.getKey() + " (" + Math.round((entry.getValue() / total) * 100) + "%)";
            canvas.drawText(label, x, y, textPaint);

            startAngle += sweepAngle;
            colorIndex++;
        }
    }
}
