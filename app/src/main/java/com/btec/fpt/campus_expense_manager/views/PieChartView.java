package com.btec.fpt.campus_expense_manager.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PieChartView extends View {
    private Map<String, Double> data;
    private Paint arcPaint;
    private Paint textPaint;
    private Paint linePaint;
    private RectF rectF;
    private List<String> orderedLabels;

    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();
        rectF = new RectF();
        orderedLabels = new ArrayList<>();
    }

    private void initPaints() {
        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.LEFT);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(2f);
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        orderedLabels.clear();
        if (data != null) {
            orderedLabels.addAll(data.keySet());
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.isEmpty()) {
            textPaint.setTextSize(40f);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No expense data available", getWidth() / 2f, getHeight() / 2f, textPaint);
            return;
        }

        float total = 0f;
        for (double value : data.values()) {
            total += value;
        }

        int[] colors = {
                Color.parseColor("#666666"),
                Color.parseColor("#FF6600"),
                Color.parseColor("#FFCC00"),
                Color.parseColor("#CCCCCC"),
                Color.parseColor("#66CCFF"),
                Color.parseColor("#0066CC"),
                Color.parseColor("#663300"),
                Color.parseColor("#003366"),
                Color.parseColor("#CC9900")
        };

        float chartSize = Math.min(getWidth() * 0.6f, getHeight() * 0.8f);
        float legendWidth = getWidth() * 0.3f;
        float padding = 40f;
        float centerX = chartSize / 2 + padding;
        float centerY = getHeight() / 2;
        rectF.set(centerX - chartSize / 2, centerY - chartSize / 2, centerX + chartSize / 2, centerY + chartSize / 2);

        float startAngle = 0f;
        int colorIndex = 0;

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            arcPaint.setColor(colors[colorIndex % colors.length]);
            float sweepAngle = (float) (entry.getValue() / total * 360);
            canvas.drawArc(rectF, startAngle, sweepAngle, true, arcPaint);

            float midAngle = startAngle + sweepAngle / 2;
            float labelRadius = chartSize / 2 + 50f;
            float lineEndRadius = chartSize / 2 + 20f;

            float lineStartX = centerX + (float) Math.cos(Math.toRadians(midAngle)) * chartSize / 2;
            float lineStartY = centerY + (float) Math.sin(Math.toRadians(midAngle)) * chartSize / 2;
            float lineEndX = centerX + (float) Math.cos(Math.toRadians(midAngle)) * lineEndRadius;
            float lineEndY = centerY + (float) Math.sin(Math.toRadians(midAngle)) * lineEndRadius;
            float labelX = centerX + (float) Math.cos(Math.toRadians(midAngle)) * labelRadius;
            float labelY = centerY + (float) Math.sin(Math.toRadians(midAngle)) * labelRadius;

            float textOffsetX = midAngle > 90 && midAngle < 270 ? -textPaint.measureText(entry.getKey() + ": " + entry.getValue()) : 20f;
            canvas.drawLine(lineStartX, lineStartY, lineEndX, lineEndY, linePaint);
            canvas.drawLine(lineEndX, lineEndY, labelX + textOffsetX, labelY, linePaint);

            String label = entry.getKey() + ": " + String.format("%.0f VND", entry.getValue());
            canvas.drawText(label, labelX + textOffsetX, labelY + 10, textPaint);

            startAngle += sweepAngle;
            colorIndex++;
        }

        float legendStartX = getWidth() - legendWidth - padding;
        float legendStartY = centerY - (orderedLabels.size() * 40f) / 2;
        float boxSize = 30f;
        float textOffset = 40f;

        for (int i = 0; i < orderedLabels.size(); i++) {
            String label = orderedLabels.get(i);
            Double value = data.get(label);
            arcPaint.setColor(colors[i % colors.length]);
            float boxY = legendStartY + i * 40f;

            canvas.drawRect(legendStartX, boxY, legendStartX + boxSize, boxY + boxSize, arcPaint);

            String legendText = String.format("%.0f VND", value) + " - " + label;
            canvas.drawText(legendText, legendStartX + textOffset, boxY + boxSize / 2 + 10, textPaint);
        }
    }
}