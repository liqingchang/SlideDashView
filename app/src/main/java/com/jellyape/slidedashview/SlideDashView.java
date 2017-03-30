package com.jellyape.slidedashview;

import android.content.Context;
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
	// 阴影画笔
	private Paint shadowPaint;
	// 擦除
	private Paint eraser;
	// 刻度盘半径
	private static final int RADIUS = 480;
	// 刻度盘宽度
	private static final int DASHWIDTH = 240;
	// 边缘宽度
	private static final int EDGEWIDTH = 24;
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

	private int width;
	private int height;
	private int centerX;
	private int centerY;
	private int radius;
	// 记录滑动角度，用来在滑动完成的时候判断是否需要做微调整
	// 具体调整是，如果停止角度刚好是半个数字是高亮区，就移动多一格，保证不会出现半个数字是高亮的情况
	private float scrollRatio;
	private boolean isLeftScroll = false;
	// 当前刻度
	private int currentScale = DEFAULT_SCALE;

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


	private void init(Context context) {
		gestureDetector = new GestureDetector(context, onGestureListener);
		//解决屏幕长按后无法拖动
		gestureDetector.setIsLongpressEnabled(false);

		hightLightAreaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		hightLightAreaPaint.setAntiAlias(true);
		hightLightAreaPaint.setStyle(Paint.Style.FILL);
//		hightLightAreaPaint.setStrokeWidth(30);

		hightLightScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		hightLightScalePaint.setAntiAlias(true);
		hightLightScalePaint.setStyle(Paint.Style.FILL);
		hightLightScalePaint.setColor(0xff4cb549);
		hightLightScalePaint.setStrokeWidth(DEFAULT_SCALE_STROKE);
		// 初始化文字情况
		hightLightScalePaint.setTextSize(DEFAULT_BIG_SCALE_TEXTSIZE);
		hightLightScalePaint.setTextAlign(Paint.Align.CENTER);

		hightLightScaleNumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		hightLightScaleNumPaint.setAntiAlias(true);
		hightLightScaleNumPaint.setStyle(Paint.Style.FILL);
		hightLightScaleNumPaint.setColor(0xffffffff);
		hightLightScaleNumPaint.setStrokeWidth(DEFAULT_SCALE_STROKE);
		// 初始化文字情况
		hightLightScaleNumPaint.setTextSize(DEFAULT_BIG_SCALE_TEXTSIZE);
		hightLightScaleNumPaint.setTextAlign(Paint.Align.CENTER);

		edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		edgePaint.setAntiAlias(true);
		edgePaint.setStyle(Paint.Style.FILL);
		edgePaint.setColor(0xff1a2126);

		scalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		scalePaint.setAntiAlias(true);
		scalePaint.setStyle(Paint.Style.FILL);
		scalePaint.setColor(0xff444950);
		scalePaint.setStrokeWidth(DEFAULT_SCALE_STROKE);
		// 初始化文字情况
		scalePaint.setTextSize(DEFAULT_BIG_SCALE_TEXTSIZE);
		scalePaint.setTextAlign(Paint.Align.CENTER);

		midScalePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		midScalePaint.setAntiAlias(true);
		midScalePaint.setStyle(Paint.Style.FILL);
		midScalePaint.setColor(0xff444950);
		midScalePaint.setStrokeWidth(DEFAULT_SCALE_STROKE);
		// 初始化文字情况
		midScalePaint.setTextSize(DEFAULT_MID_SCALE_TEXTSIZE);
		midScalePaint.setTextAlign(Paint.Align.CENTER);

		ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		ringPaint.setAntiAlias(true);
		ringPaint.setStyle(Paint.Style.FILL);
		ringPaint.setColor(0xff212932);

		eraser = new Paint(Paint.ANTI_ALIAS_FLAG);
		eraser.setAlpha(0);
		eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

		shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		shadowPaint.setShadowLayer(10, 2, 2, Color.BLACK);
		shadowPaint.setStyle(Paint.Style.FILL);
		shadowPaint.setColor(0x22000000);
		shadowPaint.setStrokeWidth(12);

		// 初始化degree值，取值为currentScale的值*每格角度数
		degree = -currentScale * SCALE_ANGEL;

		getCurrentTem();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawDash(canvas);
	}

	private void drawDash(Canvas canvas) {
		// 根据具体情况先旋转一次画布
		canvas.rotate(degree, centerX, centerY);

		canvas.drawCircle(centerX, centerY, radius, edgePaint);
		canvas.drawCircle(centerX, centerY, radius - EDGEWIDTH, ringPaint);
		canvas.drawCircle(centerX, centerY, radius - EDGEWIDTH - DASHWIDTH, edgePaint);
		canvas.drawCircle(centerX, centerY, radius - EDGEWIDTH - DASHWIDTH - EDGEWIDTH, eraser);

		// 画高亮区域
		drawHightLightAreaFilter(canvas);

		drawScale(canvas);

		drawOthers(canvas);

	}

	// TODO: 部分hardcode需要更换为常量
	// TODO: 画阴影
	private void drawHightLightAreaFilter(Canvas canvas) {
		ColorMatrix colorMatrix = new ColorMatrix(new float[]{
				0, 0, 0, 0, 33,
				0, 0, 0, 0, 64,
				0, 0, 0, 0, 63,
				0, 0, 0, 1.0f, 0
		});
		ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
		hightLightAreaPaint.setColorFilter(filter);

		int l = 0;
		int r = width;
		int t = 0;
		int b = 2 * radius;

		int outerRadius = radius - EDGEWIDTH;
		int x1 = width / 2 - (int) (outerRadius * Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));
		int y1 = radius - (int) (outerRadius * Math.cos(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));

		int x2 = width / 2 + (int) (outerRadius * Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));
		int y2 = y1;

		int innerRadius = radius - EDGEWIDTH - EDGEWIDTH - DASHWIDTH;
		int x3 = width / 2 - (int) (innerRadius * Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));
		int y3 = radius - (int) (innerRadius * Math.cos(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));

		int x4 = width / 2 + (int) (innerRadius * Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));
		int y4 = y3;

		int y5 = radius - (int) ((innerRadius + EDGEWIDTH) * Math.cos(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180));


		canvas.save();
		// 做一个画布逆旋转确保画布固定在同一位置
		canvas.rotate(360 - degree, centerX, centerY);
		RectF rectF = new RectF(l, t, r, b);
		RectF outRect = new RectF(width / 2 - outerRadius, EDGEWIDTH, width / 2 + outerRadius, EDGEWIDTH + 2 *
				outerRadius);
		RectF inRect = new RectF(width / 2 - innerRadius, EDGEWIDTH + EDGEWIDTH + DASHWIDTH, width / 2 + innerRadius,
				EDGEWIDTH + EDGEWIDTH + DASHWIDTH + 2 * innerRadius);

		Shader shader = new LinearGradient((float) ((x3 + x4) * 0.5), (float) y3 - 40, (float) ((x3 + x4) * 0.5),
				(float) (y1), Color.TRANSPARENT, 0xff21403f, Shader.TileMode.CLAMP);
		hightLightAreaPaint.setShader(shader);

		Path path = new Path();
		path.moveTo(x1, y1);
		path.lineTo(x3, y3);
		path.arcTo(inRect, 270 - 18, 36);
		path.lineTo(x2, y2);
		path.moveTo(x1, y1);
		path.arcTo(outRect, 270 - 18, 36);

		canvas.drawPath(path, hightLightAreaPaint);

//		Path shadowPath = new Path();
//		shadowPath.moveTo(x1, y1);
//		shadowPath.lineTo(x3, y3);
//		shadowPath.moveTo(x2, y2);
//		shadowPath.lineTo(x4, y4);
//		canvas.drawPath(shadowPath, shadowPaint);
//		canvas.drawLine(x1,y1,x3,y5,shadowPaint);
//		canvas.drawLine(x2,y2,x4,y5,shadowPaint);

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
		int startY = EDGEWIDTH + DASHWIDTH - 2 * EDGEWIDTH;
		int endY = startY - DEFAULT_SMALL_SCALE;
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
		startY = EDGEWIDTH + DASHWIDTH - 2 * EDGEWIDTH;
		endY = startY - DEFAULT_BIG_SCALE;
		int midEndY = startY - DEFAULT_MID_SCALE;
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
		double lowX = width / 2 - (Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180) * (radius - 2 * EDGEWIDTH -
				DASHWIDTH - DEFAULT_RADIUS_HIGHLIGHT_CIRCLE));
		double lowY = radius - (Math.cos(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180) * (radius - 2 * EDGEWIDTH -
				DASHWIDTH - DEFAULT_RADIUS_HIGHLIGHT_CIRCLE));
		canvas.drawCircle((float) lowX, (float) lowY, DEFAULT_RADIUS_HIGHLIGHT_CIRCLE, scalePaint);
		canvas.drawText("低至", (float) lowX, (float) lowY + DEFAULT_BIG_SCALE_TEXTSIZE + DEFAULT_RADIUS_HIGHLIGHT_CIRCLE, scalePaint);
		// 画高至圆心
		double highX = width / 2 + (Math.sin(Math.PI * DEFAULT_HIGHTLIGHT_ANGEL / 2 / 180) * (radius - 2 * EDGEWIDTH -
				DASHWIDTH - DEFAULT_RADIUS_HIGHLIGHT_CIRCLE));
		double highY = lowY;
		canvas.drawCircle((float) highX, (float) highY, DEFAULT_RADIUS_HIGHLIGHT_CIRCLE, scalePaint);
		canvas.drawText("高至", (float) highX, (float) highY + DEFAULT_BIG_SCALE_TEXTSIZE + DEFAULT_RADIUS_HIGHLIGHT_CIRCLE, scalePaint);

	}


	private boolean isHightLight(float scaleDegree) {
		// 实际偏差
		float realDegree = Math.abs(degree + scaleDegree);
		int angel = DEFAULT_HIGHTLIGHT_ANGEL / 2;
		return realDegree < angel;
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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		width = getMeasuredWidth();
		height = getMeasuredHeight();
		centerX = width / 2;
		// TODO: 1.3倍改用常量
		radius = (int) (height * 1.3);
		centerY = radius;
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

	public void setOnScaleChangeListener(OnScaleChangeListener onScaleChangeListener) {
		this.onScaleChangeListener = onScaleChangeListener;
	}

	public interface OnScaleChangeListener {
		void onScaleChange(int scale);
	}

}
