package com.jellyape.slidedashview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * 恒温面板
 * TODO: 颜色值的可配置化
 * Created by terry on 3/14/17.
 */

public class SlideDashView extends View {

	private static final String TAG = "SlideDashView";

	// 边缘画笔
	private Paint edgePaint;
	// 内环画笔
	private Paint ringPaint;
	// 刻度画笔(同时是刻度数字画笔)
	private Paint scalePaint;
	// 中刻度画笔
	private Paint midScalePaint;
	// 高亮刻度画笔
	private Paint hightLightScalePaint;
	// 高亮刻度数字画笔
	private Paint hightLightScaleNumPaint;
	// 高亮区域画笔
	private Paint hightLightAreaPaint;
	// 擦除
	private Paint eraser;
	// 默认刻度宽度
	private static final int DEFAULT_SCALE_STROKE = 4;
	// 大刻度默认长度
	private static final int DEFAULT_BIG_SCALE = 90;
	// 大刻度默认字体大小
	private static final int DEFAULT_BIG_SCALE_TEXTSIZE = 42;
	// 中刻度默认长度
	private static final int DEFAULT_MID_SCALE = 80;
	// 中刻度默认字体大小
	private static final int DEFAULT_MID_SCALE_TEXTSIZE = 32;
	// 小刻度默认长度
	private static final int DEFAULT_SMALL_SCALE = 70;
	// 记录当前的旋转度数
	private float degree = 0;
	// 手势检测辅助
	private GestureDetector gestureDetector;
	// 高亮区域角度
	private static final int DEFAULT_HIGHTLIGHT_ANGEL = 36;
	// 每个刻度的角度值
	private static final float SCALE_ANGEL = 1.2f;
	// 大刻度角度值
	private static final float BIG_SCALE_ANGEL = SCALE_ANGEL * 10;
	// 中刻度角度
	private static final float MID_SCALE_ANGEL = SCALE_ANGEL * 5;
	// 默认低至/高至 圆形半径
	private static final float DEFAULT_RADIUS_HIGHLIGHT_CIRCLE = 14f;
	// 默认最低刻度值
	private static final int DEFAULT_LOW_SCALE = 14;
	// 默认刻度个数
	private static final int DEFAULT_SCALE_NUMBER = 22;
	// 默认指向刻度
	private static final int DEFAULT_SCALE = 30;
	// 默认最低温度值
	private static final int DEFAULT_MIN_TEM = 17;
	// 默认最高温度值
	private static final int DEFAULT_MAX_TEM = 30;

	private float width;
	private float height;
	private float centerX;
	private float centerY;
	private float radius = dpToPix(DEFAULT_RADIUS);
	// 边缘宽度
	private float edgeWidth = dpToPix(DEFAULT_EDGE_WIDTH);
	// 圆环宽度
	private float ringWidth = dpToPix(DEFAULT_RING_WIDTH);
	// 刻度宽
	private float scaleWidth;
	// 大刻度长
	private float bigScaleLong;
	// 中刻度长
	private float midScaleLong;
	// 小刻度长
	private float scaleLong;
	// 大刻度文字大小
	private float bigScaleTextSize;
	// 中刻度文字大小
	private float midScaleTextSize;
	// 单个刻度角度值
	private float scaleAngel;
	// 底部边缘和刻度间距
	private float scaleEdgeWidth = dpToPix(DEFAULT_SCALEEDGE_WIDTH);

	// 默认边缘宽度13.5dp
	private static final float DEFAULT_EDGE_WIDTH = 12f;
	// 默认圆环宽度170dp
	private static final float DEFAULT_RING_WIDTH = 85;
	// 默认半径
	private static final float DEFAULT_RADIUS = 387.5f;
	// 默认底部边缘和刻度间距差
	private static final float DEFAULT_SCALEEDGE_WIDTH = 8;

	// 记录滑动角度，用来在滑动完成的时候判断是否需要做微调整
	// 具体调整是，如果停止角度刚好是半个数字是高亮区，就移动多一格，保证不会出现半个数字是高亮的情况
	private float scrollRatio;
	private boolean isLeftScroll = false;
	// 当前刻度
	private int currentScale = DEFAULT_SCALE;
	// 边缘颜色值
	private int edgeColor = DEFAULT_EDGE_COLOR;
	// 圆盘颜色值
	private int ringColor = DEFAULT_RING_COLOR;
	// 刻度颜色
	private int scaleColor = DEFAULT_SCALE_COLOR;
	// 高亮渐变颜色初值
	private int hightLightStartColor = DEFAULT_HIGHLIGHT_START_COLOR;
	// 高亮渐变颜色终值
	private int hightLightEndColor = DEFAULT_HIGHLIGHT_END_COLOR;
	// 高亮刻度颜色值
	private int hightLightScaleColor = DEFAULT_HIGHLIGHT_SCALE_COLOR;
	// 文字颜色
	private int textColor = DEFAULT_TEXT_COLOR;
	// 高亮文字颜色
	private int highLightTextColor = DEFAULT_HIGHLIGHT_TEXT_COLOR;
	// 低至/高至颜色
	private int circleColor = DEFAULT_CIRCLE_COLOR;

	// 默认边缘颜色值
	private static final int DEFAULT_EDGE_COLOR = 0x2f141a20;
	// 默认圆盘颜色值
	private static final int DEFAULT_RING_COLOR = 0xff212932;
	// 默认高亮渐变颜色初值
	private static final int DEFAULT_HIGHLIGHT_START_COLOR = 0xff21303f;
	// 默认高亮渐变颜色初值
	private static final int DEFAULT_HIGHLIGHT_END_COLOR = 0x00000000;
	// 默认高亮刻度颜色值
	private static final int DEFAULT_HIGHLIGHT_SCALE_COLOR = 0xff4cb549;
	// 默认文字颜色
	private static final int DEFAULT_TEXT_COLOR = 0xff4e5156;
	// 默认高亮文字颜色
	private static final int DEFAULT_HIGHLIGHT_TEXT_COLOR = 0xffffffff;
	// 默认低至/高至颜色
	private static final int DEFAULT_CIRCLE_COLOR = 0xff52575a;
	// 默认刻度颜色
	private static final int DEFAULT_SCALE_COLOR = 0xff4e5156;

	private Canvas tmpCanvas;
	private Bitmap bitmap;

	private OnScaleChangeListener onScaleChangeListener;

	private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.OnGestureListener() {
		@Override
		public boolean onDown(MotionEvent e) {//手指轻触屏幕的一瞬间，由一个ACTION_DOWN触发
			log("轻触一下");
			return true;
		}

		@Override
		public void onShowPress(MotionEvent e) {//手指轻触屏幕，尚未松开或拖动，由一个ACTION_DOWN触发
			log("轻触未松开");
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {//手指离开屏幕，伴随一个ACTION_UP触发，单击行为
			log("单击");
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {//手指按下屏幕并拖动
			// 由一个由一个ACTION_DOWN，多个ACTION_MOVE触发，是拖动行为
			if (e1.getX() > e2.getX()) {
				if (getCurrentTem() == DEFAULT_MAX_TEM) {
					return false;
				}
				float ratio = Math.round((Math.round(degree) - 36) / 1.2f % 10);
				scrollRatio = ratio;
				isLeftScroll = true;
				log("左滑:" + degree);
				++currentScale;
				setCurrentScale(currentScale);
			} else if (e1.getX() < e2.getX()) {
				if (getCurrentTem() == DEFAULT_MIN_TEM) {
					return false;
				}
				float ratio = Math.round((Math.round(degree) - 36) / 1.2f % 10);
				scrollRatio = ratio;
				isLeftScroll = false;
				log("右滑:" + degree);
				--currentScale;
				setCurrentScale(currentScale);
			}
			return false;
		}

		@Override
		public void onLongPress(MotionEvent e) {//长按
			log("长按");
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			//按下屏幕，快速滑动后松开，由一个由一个ACTION_DOWN，多个ACTION_MOVE，一个ACTION_UP触发
			log("快速滑动");
			return false;
		}
	};

	public SlideDashView(Context context) {
		super(context);
		init(context);
	}

	public SlideDashView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public SlideDashView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = getMeasuredWidth();
		height = getMeasuredHeight();
		centerX = width / 2;
		centerY = radius;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
			tmpCanvas = new Canvas(bitmap);
		}
		tmpCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		drawDash(canvas);
	}

	private void init(Context context) {
		gestureDetector = new GestureDetector(context, onGestureListener);
		//解决屏幕长按后无法拖动
		gestureDetector.setIsLongpressEnabled(false);

		hightLightAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		ColorMatrix colorMatrix = new ColorMatrix(new float[]{
				0, 0, 0, 0, 33,
				0, 0, 0, 0, 64,
				0, 0, 0, 0, 63,
				0, 0, 0, 1.0f, 0
		});
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
		hightLightAreaPaint.setColorFilter(filter);
		hightLightAreaPaint.setAntiAlias(true);
		hightLightAreaPaint.setStyle(Paint.Style.FILL);
//		hightLightAreaPaint.setStrokeWidth(30);

		hightLightScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		hightLightScalePaint.setAntiAlias(true);
		hightLightScalePaint.setStyle(Paint.Style.FILL);
		hightLightScalePaint.setColor(hightLightScaleColor);
		hightLightScalePaint.setStrokeWidth(DEFAULT_SCALE_STROKE);
		hightLightScalePaint.setTextSize(DEFAULT_BIG_SCALE_TEXTSIZE);
		hightLightScalePaint.setTextAlign(Paint.Align.CENTER);

		hightLightScaleNumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		hightLightScaleNumPaint.setAntiAlias(true);
		hightLightScaleNumPaint.setStyle(Paint.Style.FILL);
		hightLightScaleNumPaint.setColor(highLightTextColor);
		hightLightScaleNumPaint.setStrokeWidth(DEFAULT_SCALE_STROKE);
		hightLightScaleNumPaint.setTextSize(DEFAULT_BIG_SCALE_TEXTSIZE);
		hightLightScaleNumPaint.setTextAlign(Paint.Align.CENTER);

		edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		edgePaint.setAntiAlias(true);
		edgePaint.setStyle(Paint.Style.FILL);
		edgePaint.setColor(edgeColor);

		scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		scalePaint.setAntiAlias(true);
		scalePaint.setStyle(Paint.Style.FILL);
		scalePaint.setColor(scaleColor);
		scalePaint.setStrokeWidth(DEFAULT_SCALE_STROKE);
		// 初始化文字情况
		scalePaint.setTextSize(DEFAULT_BIG_SCALE_TEXTSIZE);
		scalePaint.setTextAlign(Paint.Align.CENTER);

		midScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		midScalePaint.setAntiAlias(true);
		midScalePaint.setStyle(Paint.Style.FILL);
		midScalePaint.setColor(scaleColor);
		midScalePaint.setStrokeWidth(DEFAULT_SCALE_STROKE);
		// 初始化文字情况
		midScalePaint.setTextSize(DEFAULT_MID_SCALE_TEXTSIZE);
		midScalePaint.setTextAlign(Paint.Align.CENTER);

		ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		ringPaint.setAntiAlias(true);
		ringPaint.setStyle(Paint.Style.FILL);
		ringPaint.setColor(ringColor);

		eraser = new Paint(Paint.ANTI_ALIAS_FLAG);
		eraser.setAntiAlias(true);
//		eraser.setColor(Color.TRANSPARENT);
//		eraser.setAlpha(0xff);
		eraser.setStyle(Paint.Style.FILL);
		eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

		// 初始化degree值，取值为currentScale的值*每格角度数
		degree = -currentScale * SCALE_ANGEL;

		getCurrentTem();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		bitmap = null;
		tmpCanvas = null;
	}

	private void drawDash(Canvas canvas) {
		// 根据具体情况先旋转一次画布
		tmpCanvas.rotate(degree, centerX, centerY);

		tmpCanvas.drawCircle(centerX, centerY, radius, edgePaint);
		tmpCanvas.drawCircle(centerX, centerY, radius - edgeWidth, ringPaint);
		tmpCanvas.drawCircle(centerX, centerY, radius - edgeWidth - ringWidth, eraser);
		tmpCanvas.drawCircle(centerX, centerY, radius - edgeWidth - ringWidth, edgePaint);
		tmpCanvas.drawCircle(centerX, centerY, radius - edgeWidth - ringWidth - edgeWidth, eraser);

		drawHightLightAreaFilter(tmpCanvas);
		drawScale(tmpCanvas);
		drawOthers(tmpCanvas);
		canvas.drawBitmap(bitmap, 0, 0, null);
	}

	// TODO: 部分hardcode需要更换为常量
	// TODO: 画阴影
	private void drawHightLightAreaFilter(Canvas canvas) {

		float l = 0;
		float r = width;
		float t = 0;
		float b = 2 * radius;

		float outerRadius = radius - edgeWidth;
		float x1 = width / 2 - (int) (outerRadius * Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));
		float y1 = radius - (int) (outerRadius * Math.cos(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));

		float x2 = width / 2 + (int) (outerRadius * Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));
		float y2 = y1;

		float innerRadius = radius - edgeWidth - edgeWidth - ringWidth;
		float x3 = width / 2 - (int) (innerRadius * Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));
		float y3 = radius - (int) (innerRadius * Math.cos(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));

		float x4 = width / 2 + (int) (innerRadius * Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));
		float y4 = y3;

		float y5 = radius - (int) ((innerRadius + edgeWidth) * Math.cos(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));


		canvas.save();
		// 做一个画布逆旋转确保画布固定在同一位置
		canvas.rotate(360 - degree, centerX, centerY);
		RectF rectF = new RectF(l, t, r, b);
		RectF outRect = new RectF(width / 2 - outerRadius, edgeWidth, width / 2 + outerRadius, edgeWidth + 2 *
				outerRadius);
		RectF inRect = new RectF(width / 2 - innerRadius, edgeWidth + edgeWidth + ringWidth, width / 2 + innerRadius,
				edgeWidth + edgeWidth + ringWidth + 2 * innerRadius);

		Shader shader = new LinearGradient((float) ((x3 + x4) * 0.5), (float) y3 - 40, (float) ((x3 + x4) * 0.5),
				(float) (y1), hightLightEndColor, hightLightStartColor, Shader.TileMode.CLAMP);
		hightLightAreaPaint.setShader(shader);

		Path path = new Path();
		path.moveTo(x1, y1);
		path.lineTo(x3, y3);
		path.arcTo(inRect, 270 - 18, 36);
		path.lineTo(x2, y2);
		path.moveTo(x1, y1);
		path.arcTo(outRect, 270 - 18, 36);

		canvas.drawPath(path, hightLightAreaPaint);

		canvas.restore();
	}


	// 画刻度
	// TODO: 待重构， 圆点不需要每次都获取
	private void drawScale(Canvas canvas) {
		// 记录旋转总角度，用于计算是否高亮
		float scaleDegree = 0;
		// 1. 确认圆心
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		int radius = (int) (height * 1.3);
		int x = width / 2;
		int y = radius;
		// 画小刻度
		canvas.save();
		int startX = width / 2;
		int endX = startX;
		float startY = edgeWidth + ringWidth - scaleEdgeWidth;
		float endY = startY - DEFAULT_SMALL_SCALE;
		for (int i = 0; i < (DEFAULT_SCALE_NUMBER * 10); i++) {
			canvas.drawLine(startX, startY, endX, endY, isHightLight(scaleDegree) ? hightLightScalePaint : scalePaint);
			canvas.rotate(SCALE_ANGEL, x, y);
			scaleDegree += SCALE_ANGEL;
		}
		canvas.restore();
		// 画中刻度和大刻度
		scaleDegree = 0;
		canvas.save();
		scalePaint.setTextSize(DEFAULT_BIG_SCALE_TEXTSIZE);
		// 画一条刻度
		startX = width / 2;
		endX = startX;
		startY = edgeWidth + ringWidth - scaleEdgeWidth;
		endY = startY - DEFAULT_BIG_SCALE;
		float midEndY = startY - DEFAULT_MID_SCALE;
		for (int i = 0; i < DEFAULT_SCALE_NUMBER; i++) {
			canvas.drawLine(startX, startY, endX, endY, isHightLight(scaleDegree) ? hightLightScalePaint : scalePaint);
			// TODO: 12这个值需要改成常量或者配置值
			canvas.drawText(String.valueOf(DEFAULT_LOW_SCALE + i), endX, endY - 12, isHightLight(scaleDegree) ?
					hightLightScaleNumPaint : scalePaint);
			canvas.rotate(MID_SCALE_ANGEL, x, y);
			scaleDegree += MID_SCALE_ANGEL;
			canvas.drawLine(startX, startY, endX, midEndY, isHightLight(scaleDegree) ? hightLightScalePaint : midScalePaint);
			// TODO: 是否刻画中刻度数字应该可控制
//			canvas.drawText(String.valueOf(17 + i) + ".5", endX, endY - 12, midScalePaint);
			canvas.rotate(MID_SCALE_ANGEL, x, y);
			scaleDegree += MID_SCALE_ANGEL;
		}
		canvas.restore();

	}

	private void drawOthers(Canvas canvas) {
		canvas.save();
		// 做一个画布逆旋转确保画布固定在同一位置
		canvas.rotate(360 - degree, centerX, centerY);
		// 画低至圆心
		double lowX = width / 2 - (Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180) * (radius - 2 * edgeWidth -
				ringWidth - DEFAULT_RADIUS_HIGHLIGHT_CIRCLE));
		double lowY = radius - (Math.cos(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180) * (radius - 2 * edgeWidth -
				ringWidth - DEFAULT_RADIUS_HIGHLIGHT_CIRCLE));
		canvas.drawCircle((float) lowX, (float) lowY, DEFAULT_RADIUS_HIGHLIGHT_CIRCLE, scalePaint);
		canvas.drawText("低至", (float) lowX, (float) lowY + DEFAULT_BIG_SCALE_TEXTSIZE + DEFAULT_RADIUS_HIGHLIGHT_CIRCLE, scalePaint);
		// 画高至圆心
		double highX = width / 2 + (Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180) * (radius - 2 * edgeWidth -
				ringWidth - DEFAULT_RADIUS_HIGHLIGHT_CIRCLE));
		double highY = lowY;
		canvas.drawCircle((float) highX, (float) highY, DEFAULT_RADIUS_HIGHLIGHT_CIRCLE, scalePaint);
		canvas.drawText("高至", (float) highX, (float) highY + DEFAULT_BIG_SCALE_TEXTSIZE + DEFAULT_RADIUS_HIGHLIGHT_CIRCLE, scalePaint);

	}


	private boolean isHightLight(float scaleDegree) {
		// 实际偏差
		float realDegree = Math.abs(degree + scaleDegree);
		int angel = DEFAULT_HIGHTLIGHT_ANGEL / 2;
		return realDegree < angel - 0.1;
//
//		return (DEFAULT_HIGHTLIGHT_ANGEL / 2 + degree > scaleDegree) && (scaleDegree <
//				DEFAULT_HIGHTLIGHT_ANGEL
//				/ 2 - degree);
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = gestureDetector.onTouchEvent(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_UP:
				if (scrollRatio == 5f || scrollRatio == -5f) {
					if (isLeftScroll) {
						degree -= SCALE_ANGEL;
					} else {
						degree += SCALE_ANGEL;
					}
				}
		}
		return ret;
	}

	/**
	 * 获取当前温度
	 *
	 * @return
	 */
	public double getCurrentTem() {
		double currentTem = currentScale * 0.1 + DEFAULT_LOW_SCALE;
		log("当前设定温度为:" + currentTem);
		return currentTem;
	}

	/**
	 * 获取当前刻度
	 *
	 * @return
	 */
	public double getCurrentScale() {
		return currentScale;
	}

	public void setCurrentScale(int scale) {
		currentScale = scale;
		log("当前刻度:" + currentScale);
		degree = -currentScale * SCALE_ANGEL;
		if (onScaleChangeListener != null) {
			onScaleChangeListener.onScaleChange(scale);
		}
		invalidate();
	}


	private void log(String log) {
		Log.i(TAG, log);
	}

	private float dpToPix(float dp) {
		Resources resources = getContext().getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
	}

	public void setOnScaleChangeListener(OnScaleChangeListener onScaleChangeListener) {
		this.onScaleChangeListener = onScaleChangeListener;
	}

	public interface OnScaleChangeListener {
		void onScaleChange(int scale);
	}

}
