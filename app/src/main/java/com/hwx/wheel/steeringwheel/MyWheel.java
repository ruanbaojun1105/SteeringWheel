package com.hwx.wheel.steeringwheel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class MyWheel extends View implements Runnable,View.OnTouchListener {

    public MyWheel(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    //先定义一些绘图要用的基本参数
    public static final int BOTTOM = 7;
    public static final int BOTTOM_LEFT = 8;
    public static final long DEFAULT_LOOP_INTERVAL = 100L;
    public static final int FRONT = 3;
    public static final int FRONT_RIGHT = 4;
    public static final int LEFT = 1;
    public static final int LEFT_FRONT = 2;
    public static final int RIGHT = 5;
    public static final int RIGHT_BOTTOM = 6;
    private final double RAD = 57.295779500000002D;
    private Paint outButton;
    private Paint inButton;
    public double centerX = 0.0D;
    public double centerY = 0.0D;
    private Paint horizontalLine;
    private int joystickRadius;
    private int buttonRadius;
    private int lastAngle = 0;
    private int lastPower = 0;
    private long loopInterval = 100L;
    private Paint mainCircle;   //整个控件的大圆，及小红点的活动范围


    //自定义的接口用于监听处理控件的触摸事件
    private OnMyWheelMoveListener onmywheelMoveListener;
    private Paint secondaryCircle;//第二个内圆，小红圆超过后开始处理角度
    private Thread thread = new Thread(this);
    private Paint verticalLine;
    private int xPosition = 0;
    private int yPosition = 0;
    private int xPosition_in = 0;
    private int yPosition_in = 0;//只用一个Y轴
    private static final String tag="MyWheel";

    public MyWheel(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        initMyWheel();   //好吧，我知道MyWheel这个名字有点太随便了........
    }

    public MyWheel(Context paramContext, AttributeSet paramAttributeSet,
                   int paramInt) {
        super(paramContext, paramAttributeSet, paramInt);
        initMyWheel();
    }

    //根据所处的位置得到角度
    private int getAngle() {
        if (this.xPosition > this.centerX) {
            if (this.yPosition < this.centerY) {
                int m = (int) (90.0D + 57.295779500000002D * Math
                        .atan((this.yPosition - this.centerY)
                                / (this.xPosition - this.centerX)));
                this.lastAngle = m;
                return m;
            }
            if (this.yPosition > this.centerY) {
                int k = 90 + (int) (57.295779500000002D * Math
                        .atan((this.yPosition - this.centerY)
                                / (this.xPosition - this.centerX)));
                this.lastAngle = k;
                return k;
            }
            this.lastAngle = 90;
            return 90;
        }
        if (this.xPosition < this.centerX) {
            if (this.yPosition < this.centerY) {
                int j = (int) (57.295779500000002D * Math
                        .atan((this.yPosition - this.centerY)
                                / (this.xPosition - this.centerX)) - 90.0D);
                this.lastAngle = j;
                return j;
            }
            if (this.yPosition > this.centerY) {
                int i = -90
                        + (int) (57.295779500000002D * Math
                        .atan((this.yPosition - this.centerY)
                                / (this.xPosition - this.centerX)));
                this.lastAngle = i;
                return i;
            }
            this.lastAngle = -90;
            return -90;
        }
        if (this.yPosition <= this.centerY) {
            this.lastAngle = 0;
            return 0;
        }
        if (this.lastAngle < 0) {
            this.lastAngle = -180;
            return -180;
        }
        this.lastAngle = 180;
        return 180;
    }
    private int getAngle(int x,int y) {
        if (x > this.centerX) {
            if (y < this.centerY) {
                int m = (int) (90.0D + 57.295779500000002D * Math
                        .atan((y - this.centerY)
                                / (x- this.centerX)));
                return m;
            }
            if (y > this.centerY) {
                int k = 90 + (int) (57.295779500000002D * Math
                        .atan((y - this.centerY)
                                / (x- this.centerX)));
                return k;
            }
            return 90;
        }
        if (x< this.centerX) {
            if (y < this.centerY) {
                int j = (int) (57.295779500000002D * Math
                        .atan((y - this.centerY)
                                / (x- this.centerX)) - 90.0D);
                return j;
            }
            if (y > this.centerY) {
                int i = -90
                        + (int) (57.295779500000002D * Math
                        .atan((y - this.centerY)
                                / (x- this.centerX)));
                return i;
            }
            return -90;
        }
        if (y <= this.centerY) {
            return 0;
        }
        if (this.lastAngle < 0) {
            return -180;
        }
        return 180;
    }

    //根据红色圆的距离和角度得到方向
    private int getDirection() {
        int k;
        int j = 0;
        int i;
        if ((this.lastPower == 0) && (this.lastAngle == 0)) {
            k = 0;
            return k;
        }

        if (this.lastAngle <= 0)
            j = 90 + -1 * this.lastAngle;
        while (true) {
            k = 1 + (j + 22) / 45;
            if (k <= 8) {
                break;
            }

            if (this.lastAngle <= 90) {
                j = 90 - this.lastAngle;
                continue;
            }
            j = 360 - (-90 + this.lastAngle);
        }
        return k;
    }

    //得到红色圆与中心的距离
    private int getPower() {
        return (this.lastPower=(int) (100.0D * Math.sqrt((this.xPosition - this.centerX)
                * (this.xPosition - this.centerX)
                + (this.yPosition - this.centerY)
                * (this.yPosition - this.centerY)) / this.joystickRadius));
    }

    private int getPower(int x,int y) {
        return (int) (100.0D * Math.sqrt((x - this.centerX)
                * (x - this.centerX)
                + (y - this.centerY)
                * (y - this.centerY)) / this.joystickRadius);
    }

    private int measure(int paramInt) {
        int i = View.MeasureSpec.getMode(paramInt);
        int j = View.MeasureSpec.getSize(paramInt);
        if (i == 0)
            return 500;
        return j;
    }


    //初始化一些基本参数
    protected void initMyWheel() {
        this.mainCircle = new Paint(1);
        this.mainCircle.setColor(Color.BLUE);
        this.mainCircle.setStrokeWidth(3.0f);
        this.mainCircle.setStyle(Paint.Style.STROKE);
        this.secondaryCircle = new Paint();
        this.secondaryCircle.setColor(Color.BLUE);
        this.secondaryCircle.setStrokeWidth(3.0f);
        this.secondaryCircle.setStyle(Paint.Style.STROKE);
        this.verticalLine = new Paint();
        this.verticalLine.setStrokeWidth(2.0F);
        this.verticalLine.setColor(Color.YELLOW);
        this.horizontalLine = new Paint();
        this.horizontalLine.setStrokeWidth(2.0F);
        this.horizontalLine.setColor(Color.CYAN);
        this.outButton = new Paint(1);
        this.outButton.setColor(Color.RED);
        this.outButton.setStyle(Paint.Style.FILL);
        this.inButton = new Paint(1);
        this.inButton.setColor(Color.BLUE);
        this.inButton.setStyle(Paint.Style.FILL);
    }

    //初始化以后绘制方向盘。
    protected void onDraw(Canvas paramCanvas) {
        this.centerX = (getWidth() / 2);
        this.centerY = (getHeight() / 2);
        paramCanvas.drawCircle((int) this.centerX, (int) this.centerY,
                this.joystickRadius, this.mainCircle);
        paramCanvas.drawCircle((int) this.centerX, (int) this.centerY,
                this.joystickRadius / 2, this.secondaryCircle);
        paramCanvas
                .drawLine((float) this.centerX, (float) this.centerY,
                        (float) this.centerX,
                        (float) (this.centerY - this.joystickRadius),
                        this.verticalLine);
        paramCanvas.drawLine((float) (this.centerX - this.joystickRadius),
                (float) this.centerY,
                (float) (this.centerX + this.joystickRadius),
                (float) this.centerY, this.horizontalLine);
        paramCanvas
                .drawLine((float) this.centerX,
                        (float) (this.centerY + this.joystickRadius),
                        (float) this.centerX, (float) this.centerY,
                        this.horizontalLine);
        paramCanvas.drawCircle(this.xPosition, this.yPosition,this.buttonRadius, this.outButton);//角度控制
        paramCanvas.drawText("角度"+getAngle(), this.xPosition-15, this.yPosition, this.inButton);// 画文本
        //绘制矩形
        //RectF rect = new RectF((float)this.centerX-10, (float)this.centerY-10, (float)this.centerX+10, (float)this.centerY+10);
        paramCanvas.drawText("力度"+getPower((int)centerX,yPosition_in), (float)this.centerX+15, (float)this.yPosition_in+5, this.inButton);// 画文本
        paramCanvas.drawRect((float)this.centerX-15, (float)this.yPosition_in-5, (float)this.centerX+15, (float)this.yPosition_in+5, this.inButton);//力度控制
    }

    protected void onFinishInflate() {
    }

    protected void onMeasure(int paramInt1, int paramInt2) {
        int i = Math.min(measure(paramInt1), measure(paramInt2));
        setMeasuredDimension(i, i);
    }

    protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3,
                                 int paramInt4) {
        super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
        this.xPosition_in = (getWidth() / 2);
        this.yPosition_in = (getWidth() / 2);

        this.xPosition = (getWidth() / 2);
        int i = Math.min(paramInt1, paramInt2);
        this.yPosition = (int) (0.2D * (i / 2));
        this.buttonRadius = (int) (0.20D * (i / 2));
        this.joystickRadius = (int) (0.8D * (i / 2));
    }

    //int X_DOWN=0;
    //int Y_DOWN=0;
    boolean isSmallTouch=false;
    @Override
    public boolean onTouchEvent(MotionEvent paramMotionEvent) {
        //根据手触碰的坐标决定红色小圆的位置
        if (this.onmywheelMoveListener != null)
            this.onmywheelMoveListener.onValueChanged(isSmallTouch,getAngle(),(isSmallTouch?getPower((int)centerX,yPosition_in):getPower()));
        if (paramMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            /*X_DOWN = (int) paramMotionEvent.getX();
            Y_DOWN = (int) paramMotionEvent.getY();
            float x = Math.abs(this.xPosition - X_DOWN);
            float y = Math.abs(this.yPosition - Y_DOWN);
            int a=(int) (0.10D * 200);
            LogUtils.e("~~~~~~~~~x:"+x+"y:"+y+"aaaaaaa"+a);
            if (x<a||y<a)
                return false;*/
            if (getPower((int) paramMotionEvent.getX(),(int) paramMotionEvent.getY())<50){
                isSmallTouch=true;
                this.xPosition_in = (getWidth() / 2);
                this.yPosition_in = (getWidth() / 2);
                this.xPosition = (getWidth() / 2);
                this.yPosition = (int) (0.2D * (getHeight() / 2));
                invalidate();//再重新绘制
                if (this.onmywheelMoveListener != null) {
                    this.onmywheelMoveListener.onValueChanged(isSmallTouch, getAngle(), (isSmallTouch ? getPower((int) centerX, yPosition_in) : getPower()));
                    this.onmywheelMoveListener.onValueChanged(isSmallTouch, getAngle(), (isSmallTouch ? getPower((int) centerX, yPosition_in) : getPower()));
                    this.onmywheelMoveListener.onValueChanged(isSmallTouch, getAngle(), (isSmallTouch ? getPower((int) centerX, yPosition_in) : getPower()));
                }
                return true;
            }
        }
        if (paramMotionEvent.getAction() == MotionEvent.ACTION_MOVE) {
            /*if (d<(this.joystickRadius / 2)) {
                isSmallTouch=true;
                return false;//小圈内不截获
            }*/
            if (isSmallTouch) {
                this.xPosition_in = (int) paramMotionEvent.getX();
                this.yPosition_in= (int) paramMotionEvent.getY();
                double d = Math.sqrt((this.xPosition_in - this.centerX)
                        * (this.xPosition_in - this.centerX)
                        + (this.yPosition_in - this.centerY)
                        * (this.yPosition_in - this.centerY));
                if (d>joystickRadius){
                    this.yPosition_in = (int) ((this.yPosition_in - this.centerY)
                            * this.joystickRadius / d + this.centerY);
                }
                //invalidate();//再重新绘制
            }else {
                if (Math.abs(getAngle((int) paramMotionEvent.getX(),(int) paramMotionEvent.getY()))<91) {//限制在90度可以滑动
                    this.xPosition = (int) paramMotionEvent.getX();
                    this.yPosition = (int) paramMotionEvent.getY();
                    double d = Math.sqrt((this.xPosition - this.centerX)
                            * (this.xPosition - this.centerX)
                            + (this.yPosition - this.centerY)
                            * (this.yPosition - this.centerY));
                    this.xPosition = (int) ((this.xPosition - this.centerX)
                            * this.joystickRadius / d + this.centerX);
                    this.yPosition = (int) ((this.yPosition - this.centerY)
                            * this.joystickRadius / d + this.centerY);
                    invalidate();//再重新绘制
                    if (this.onmywheelMoveListener != null)
                        this.onmywheelMoveListener.onValueChanged(isSmallTouch,getAngle(),(isSmallTouch?getPower((int)centerX,yPosition_in):getPower()));
                }
            }
        }

        if (paramMotionEvent.getAction() == MotionEvent.ACTION_UP) {
            this.thread.interrupt();
            this.isSmallTouch=false;
            this.xPosition_in = (getWidth() / 2);
            this.yPosition_in = (getWidth() / 2);
            this.xPosition = (getWidth() / 2);
            this.yPosition = (int) (0.2D * (getHeight() / 2));
            invalidate();
            if (this.onmywheelMoveListener != null) {
                this.onmywheelMoveListener.onValueChanged(isSmallTouch, getAngle(), (isSmallTouch ? getPower((int) centerX, yPosition_in) : getPower()));
                this.onmywheelMoveListener.onValueChanged(isSmallTouch, getAngle(), (isSmallTouch ? getPower((int) centerX, yPosition_in) : getPower()));
                this.onmywheelMoveListener.onValueChanged(isSmallTouch, getAngle(), (isSmallTouch ? getPower((int) centerX, yPosition_in) : getPower()));
            }
        }
        /*if ((this.onmywheelMoveListener != null)
                ||(paramMotionEvent.getAction() == MotionEvent.TOOL_TYPE_UNKNOWN)) {
            if ((this.thread != null) && (this.thread.isAlive()))
                this.thread.interrupt();
            this.thread = new Thread(this);
            this.thread.start();
            this.onmywheelMoveListener.onValueChanged(isSmallTouch,getAngle(),isSmallTouch?getPower(xPosition_in,yPosition_in):getPower());
        }*/
        return true;
    }

    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted())
                return;
            post(new Runnable() {
                public void run() {
//                  Log.e(tag, "运行在"+Thread.currentThread().getName()+"线程中");
                    if (MyWheel.this.onmywheelMoveListener != null)
                        MyWheel.this.onmywheelMoveListener.onValueChanged(isSmallTouch,
                                MyWheel.this.getAngle(),
                                isSmallTouch?getPower((int)centerX,yPosition_in):getPower());
                }
            });
            try {
                Thread.sleep(this.loopInterval);
            } catch (InterruptedException localInterruptedException) {
            }
        }
    }
    public void setOnMyWheelMoveListener(
            OnMyWheelMoveListener paramOnJoystickMoveListener, long paramLong) {
        this.onmywheelMoveListener = paramOnJoystickMoveListener;
        this.loopInterval = paramLong;
    }

    public static abstract interface OnMyWheelMoveListener {
        public abstract void onValueChanged(boolean isSmallTouch,int angle, int power);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        /*处理这个控件的触摸事件*/
        return true;
    }
}