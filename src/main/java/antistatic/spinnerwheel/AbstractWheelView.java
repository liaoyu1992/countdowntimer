package antistatic.spinnerwheel;

import ru.gelin.android.countdown.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;


/** 
 * 翻滚wheelView
 * @author yu.liao
 */
public abstract class AbstractWheelView extends AbstractWheel {

    private static int itemID = -1;

    @SuppressWarnings("unused")
    private final String LOG_TAG = AbstractWheelView.class.getName() + " #" + (++itemID);

      /**默认的初始化设置*/

    protected static final int DEF_ITEMS_DIMMED_ALPHA = 50; 
    protected static final int DEF_SELECTION_DIVIDER_ACTIVE_ALPHA = 70;

    protected static final int DEF_SELECTION_DIVIDER_DIMMED_ALPHA = 70;

    protected static final int DEF_ITEM_OFFSET_PERCENT = 10;

    protected static final int DEF_ITEM_PADDING = 0;
    
    protected static final int DEF_SELECTION_DIVIDER_SIZE = 2;

    
    /** wheelView飞出时的透明度 */
    protected int mItemsDimmedAlpha;

    /** wheelView飞入时的透明度 */
    protected int mSelectionDividerActiveAlpha;

    /** 分离飞入飞出wheelView的透明度 */
    protected int mSelectionDividerDimmedAlpha;

    /** top和bottom的偏移量 */
    protected int mItemOffsetPercent;

    /** Left和right的padding值 */
    protected int mItemsPadding;

    
    protected Drawable mSelectionDivider;

    
    protected Paint mSelectorWheelPaint;


    protected Paint mSeparatorsPaint;


    protected Animator mDimSelectorWheelAnimator;

    
    protected Animator mDimSeparatorsAnimator;

    /**
     * 设置分隔的paint属性
     */
    protected static final String PROPERTY_SELECTOR_PAINT_COEFF = "selectorPaintCoeff";

    
    protected static final String PROPERTY_SEPARATORS_PAINT_ALPHA = "separatorsPaintAlpha";


    protected Bitmap mSpinBitmap;
    protected Bitmap mSeparatorsBitmap;



    public AbstractWheelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initAttributes(AttributeSet attrs, int defStyle) {
        super.initAttributes(attrs, defStyle);
        
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.AbstractWheelView, defStyle, 0);
        mItemsDimmedAlpha = a.getInt(R.styleable.AbstractWheelView_itemsDimmedAlpha, DEF_ITEMS_DIMMED_ALPHA);
        mSelectionDividerActiveAlpha = a.getInt(R.styleable.AbstractWheelView_selectionDividerActiveAlpha, DEF_SELECTION_DIVIDER_ACTIVE_ALPHA);
        mSelectionDividerDimmedAlpha = a.getInt(R.styleable.AbstractWheelView_selectionDividerDimmedAlpha, DEF_SELECTION_DIVIDER_DIMMED_ALPHA);
        mItemOffsetPercent = a.getInt(R.styleable.AbstractWheelView_itemOffsetPercent, DEF_ITEM_OFFSET_PERCENT);
        mItemsPadding = a.getDimensionPixelSize(R.styleable.AbstractWheelView_itemsPadding, DEF_ITEM_PADDING);
        mSelectionDivider = a.getDrawable(R.styleable.AbstractWheelView_selectionDivider);
        a.recycle();
    }

    @Override
    protected void initData(Context context) {
        super.initData(context);

        mDimSelectorWheelAnimator = ObjectAnimator.ofFloat(this, PROPERTY_SELECTOR_PAINT_COEFF, 1, 0);

        mDimSeparatorsAnimator = ObjectAnimator.ofInt(this, PROPERTY_SEPARATORS_PAINT_ALPHA,
                mSelectionDividerActiveAlpha, mSelectionDividerDimmedAlpha
        );

        mSeparatorsPaint = new Paint();
        mSeparatorsPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mSeparatorsPaint.setAlpha(mSelectionDividerDimmedAlpha);

        mSelectorWheelPaint = new Paint();
        mSelectorWheelPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    /**
     * 资源文件大小变化时重新创建
     */
    @Override
    protected void recreateAssets(int width, int height) {
        if (width <= 0) {
            width = 1;
        }
        if (height <= 0) {
            height = 1;
        }
        mSpinBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mSeparatorsBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        setSelectorPaintCoeff(0);
    }

    @SuppressWarnings("unused")  // 反射调用该方法
    public void setSeparatorsPaintAlpha(int alpha) {
        mSeparatorsPaint.setAlpha(alpha);
        invalidate();
    }


    abstract public void setSelectorPaintCoeff(float coeff);

    public void setSelectionDivider(Drawable selectionDivider) {
        this.mSelectionDivider = selectionDivider;
    }

    @Override
    protected void onScrollTouched() {
        mDimSelectorWheelAnimator.cancel();
        mDimSeparatorsAnimator.cancel();
        setSelectorPaintCoeff(1);
        setSeparatorsPaintAlpha(mSelectionDividerActiveAlpha);
    }

    @Override
    protected void onScrollTouchedUp() {
        super.onScrollTouchedUp();
        fadeSelectorWheel(750);
        lightSeparators(750);
    }

    @Override
    protected void onScrollFinished() {
        fadeSelectorWheel(500);
        lightSeparators(500);
    }


    /**
     * 动画飞出选择器
     *
     */
    private void fadeSelectorWheel(long animationDuration) {
        mDimSelectorWheelAnimator.setDuration(animationDuration);
        mDimSelectorWheelAnimator.start();
    }

    /**
     * 动画飞入选择器
     *
     */
    private void lightSeparators(long animationDuration) {
        mDimSeparatorsAnimator.setDuration(animationDuration);
        mDimSeparatorsAnimator.start();
    }


    /**
     * 布局的测量
     */
    abstract protected void measureLayout();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewAdapter != null && mViewAdapter.getItemsCount() > 0) {
            if (rebuildItems()) {
                measureLayout();
            }
            doItemsLayout();
            drawItems(canvas);
        }
    }

    /**
     * 将item画到布局上
     */
    abstract protected void drawItems(Canvas canvas);
}
