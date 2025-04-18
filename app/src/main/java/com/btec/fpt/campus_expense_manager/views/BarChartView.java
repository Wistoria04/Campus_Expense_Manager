package com.btec.fpt.campus_expense_manager.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.btec.fpt.campus_expense_manager.R;

import java.util.Map;

public class BarChartView extends View {
    private Map<String, Double> data;
    private Paint barPaint;
    private Paint gridPaint;
    private Paint textPaint;
    private Paint noDataPaint;
    private float scaleFactor = 0f;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaints();

        Animation scaleAnim = AnimationUtils.loadAnimation(context, R.anim.scale_up);
        startAnimation(scaleAnim);
    }

    private void initPaints() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);
        barPaint.setShadowLayer(8f, 0f, 4f, Color.argb(100, 0, 0, 0));

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setColor(Color.parseColor("#E0E0E0"));
        gridPaint.setStrokeWidth(1f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(26f);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setShadowLayer(4f, 0, 2f, Color.argb(150, 0, 0, 0));

        noDataPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        noDataPaint.setColor(Color.argb(200, 255, 255, 255));
        noDataPaint.setTextSize(40f);
        noDataPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.ITALIC));
        noDataPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(Map<String, Double> data) {
        this.data = data;
        scaleFactor = 0f;
        Log.d("BarChartView", "Data set: " + (data != null ? data.toString() : "null"));
        postInvalidateDelayed(50);
        animateChart();
    }

    private void animateChart() {
        if (scaleFactor < 1f) {
            scaleFactor += 0.05f;
            postInvalidateDelayed(16);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (data == null || data.isEmpty()) {
            canvas.drawColor(Color.argb(50, 200, 200, 200));
            canvas.drawText("No Expense Data", getWidth() / 2f, getHeight() / 2f - 20, noDataPaint);
            canvas.drawText("Add expenses to see insights", getWidth() / 2f, getHeight() / 2f + 40, noDataPaint);
            return;
        }

        float margin = 40f;
        float barSpacing = 20f;
        float barWidth = (getWidth() - 2 * margin - (data.size() - 1) * barSpacing) / data.size();
        float maxValue = 0f;

        // Tìm giá trị lớn nhất
        for (double value : data.values()) {
            if (value > maxValue) maxValue = (float) value;
        }

        // Tránh trường hợp maxValue = 0
        if (maxValue == 0) maxValue = 1f; // Giá trị mặc định để tránh chia cho 0

        // Vẽ lưới nền
        int gridLines = 5;
        float chartHeight = getHeight() - 100f;
        for (int i = 0; i <= gridLines; i++) {
            float y = getHeight() - (i * chartHeight / gridLines);
            canvas.drawLine(margin, y, getWidth() - margin, y, gridPaint);
            textPaint.setColor(Color.parseColor("#B0BEC5"));
            textPaint.setTextSize(20f);
            canvas.drawText(String.format("%.0f VND", maxValue * i / gridLines), margin / 2, y + 10, textPaint);
        }

        // Vẽ cột
        float x = margin;
        int[] colorPairs = {
                Color.parseColor("#2196F3"), Color.parseColor("#1976D2"), // Blue
                Color.parseColor("#FF9800"), Color.parseColor("#F57C00"), // Orange
                Color.parseColor("#4CAF50"), Color.parseColor("#388E3C"), // Green
                Color.parseColor("#9C27B0"), Color.parseColor("#7B1FA2"), // Purple
                Color.parseColor("#F44336"), Color.parseColor("#D32F2F")  // Red
        };
        int colorIndex = 0;

        for (Map.Entry<String, Double> entry : data.entrySet()) {
            float barHeight = (float) (entry.getValue() / maxValue * chartHeight) * scaleFactor;
            float top = getHeight() - barHeight;

            // Gradient cho cột
            @SuppressLint("DrawAllocation") LinearGradient gradient = new LinearGradient(
                    x, top, x, getHeight(),
                    colorPairs[colorIndex % colorPairs.length * 2],
                    colorPairs[colorIndex % colorPairs.length * 2 + 1],
                    Shader.TileMode.CLAMP
            );
            barPaint.setShader(gradient);

            // Vẽ cột bo góc
            RectF barRect = new RectF(x, top, x + barWidth, getHeight());
            canvas.drawRoundRect(barRect, 10f, 10f, barPaint);

            // Vẽ nền cho nhãn
            barPaint.setShader(null);
            barPaint.setColor(Color.argb(180, 0, 0, 0));
            float labelTop = top - 80;
            if (labelTop < 0) labelTop = 0; // Đảm bảo nhãn không bị cắt
            canvas.drawRoundRect(new RectF(x, labelTop, x + barWidth, labelTop + 60), 8f, 8f, barPaint);

            // Vẽ nhãn và giá trị
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(26f);
            canvas.drawText(entry.getKey(), x + barWidth / 2, labelTop + 30, textPaint);
            String valueText = String.format("%.0f VND", entry.getValue());
            canvas.drawText(valueText, x + barWidth / 2, labelTop + 60, textPaint);

            x += barWidth + barSpacing;
            colorIndex++;
        }
    }
}