package com.wangshijia.www.panellistviewlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by wangshijia on 2017/11/11
 *
 * @author wangshijia
 *         <p>
 *         可横向纵滑动 并有下拉刷新上拉加载功能的列表
 */
public class PanelListView extends RelativeLayout {
    /**
     * 列表头的高和宽
     */
    private LinearLayout mLayoutTitleMovable;
    private float mStartX = 0;
    private int mMoveOffsetX = 0;
    private int mFixX = 0;
    private int defaultSortHeaderRes = R.layout.layout_default_sort_header;
    private int titleLayoutRes = 0;
    private String[] mFixLeftListColumnsText;
    private int[] mFixLeftListColumnsWidth;
    private boolean needShortTitle = false;

    private String[] mMovableListColumnsText = new String[]{};
    private int[] mMovableListColumnsWidth = null;

    private CustomRefreshListView mStockListView;
    private Object mAdapter;

    private ArrayList<View> mMovableViewList = new ArrayList();

    private int mMovableTotalWidth = 0;
    private int titleBgColor = Color.parseColor("#FFFFFF");
    private int titleTextColor = Color.parseColor("#8997A5");
    private int titleTextSize = 14;
    private float moveViewWidth = 90;
    /**
     * 表名列宽度 务必和你定义的
     */
    private float mColumnTitleViewWidth = 137;
    private float mTitleHeight = 40;
    private float columnPadding = 15;
    private String columnTitleName;

    private float mDownPosX;
    private float mDownPosY;


    public PanelListView(Context context) {
        this(context, null);
    }

    public PanelListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PanelListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.PanelListView);
        titleBgColor = ta.getColor(R.styleable.PanelListView_titleBgColor, Color.parseColor("#FFFFFF"));
        titleTextColor = ta.getColor(R.styleable.PanelListView_titleTextColor, Color.parseColor("#8997A5"));
        columnPadding = px2dp(context, ta.getDimensionPixelSize(R.styleable.PanelListView_columnPadding, dp2px(15)));
        moveViewWidth = px2dp(context, ta.getDimension(R.styleable.PanelListView_moveViewWidth, dp2px(90)));
        mColumnTitleViewWidth = px2dp(context, ta.getDimensionPixelSize(R.styleable.PanelListView_columnTitleWidth, dp2px(137)) + dp2px(columnPadding));
        mTitleHeight = px2dp(context, ta.getDimensionPixelSize(R.styleable.PanelListView_titleItemHeight, dp2px(40)));
        titleTextSize = px2sp(context, ta.getDimensionPixelSize(R.styleable.PanelListView_titleTextSize, sp2px(14)));
        columnTitleName = ta.getString(R.styleable.PanelListView_columnTitleName);
        ta.recycle();
    }

    private void initView() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(buildHeadLayout());
        linearLayout.addView(buildMoveableListView());
        this.addView(linearLayout, new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setNeedShortTitle(boolean needShortTitle) {
        this.needShortTitle = needShortTitle;
        if (needShortTitle) {
            titleLayoutRes = defaultSortHeaderRes;
        }

    }

    /**
     * 设置标题栏背景颜色
     *
     * @param titleBgColor
     */
    public void setTitleBgColor(@IdRes int titleBgColor) {
        this.titleBgColor = titleBgColor;
    }

    /**
     * 设置标题标题布局 默认是用 textView 填充
     *
     * @param titleLayoutRes
     */
    public void setTitleLayoutRes(@IdRes int titleLayoutRes) {
        this.titleLayoutRes = titleLayoutRes;
    }

    /**
     * 第一列 固定栏的宽度
     *
     * @param rowNameColumnWidth 单位 dp 默认 137
     */
    public void setNameColumnWidth(int rowNameColumnWidth) {
        this.mColumnTitleViewWidth = rowNameColumnWidth;
    }

    /**
     * 设置列表左右间距
     *
     * @param padding 单位 dp 默认值15dp
     */
    public void setColumnPadding(int padding) {
        this.columnPadding = padding;
    }

    /**
     * 设置列标题高度
     *
     * @param mTitleHeight
     */
    public void setmTitleHeight(int mTitleHeight) {
        this.mTitleHeight = mTitleHeight;
    }

    /**
     * 设置 可移动行列高
     *
     * @param mMoveTitleViewHeight 单位 dp 默认值 40dp
     */
    public void setmMoveTitleViewHeight(int mMoveTitleViewHeight) {
        this.mTitleHeight = mMoveTitleViewHeight;
    }

    /**
     * 设置 可移动行列宽
     *
     * @param moveViewWidth 单位 dp 默认值 90dp
     */
    public void setMoveViewWidth(int moveViewWidth) {
        this.moveViewWidth = moveViewWidth;
    }

    private View buildHeadLayout() {
        LinearLayout headLayout = new LinearLayout(getContext());
        headLayout.setGravity(Gravity.CENTER);
        LinearLayout fixHeadLayout = new LinearLayout(getContext());
        headLayout.setBackgroundColor(titleBgColor);
        //设置表头数据
        addListHeaderTextView(mFixLeftListColumnsText[0], mFixLeftListColumnsWidth[0], true, fixHeadLayout);
        fixHeadLayout.setGravity(Gravity.CENTER);
        headLayout.addView(fixHeadLayout, 0, new ViewGroup.LayoutParams(dp2px(mColumnTitleViewWidth), dp2px(mTitleHeight)));
        mLayoutTitleMovable = new LinearLayout(getContext());
        for (int i = 0; i < mMovableListColumnsText.length; i++) {
            addTitleLayout(i, mMovableListColumnsText[i], mMovableListColumnsWidth[i], mLayoutTitleMovable);
        }
        headLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(mTitleHeight)));
        headLayout.addView(mLayoutTitleMovable);
        return headLayout;
    }

    private void addTitleLayout(final int cIndex, String name, int width, LinearLayout fixHeadLayout) {
        View view;
        if (titleLayoutRes != 0 || needShortTitle) {
            view = addListHeader(titleLayoutRes, name, width, fixHeadLayout);
            final ImageView drawRightSort = (ImageView) view.findViewById(R.id.iv_short_type);

            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onHeaderImageChangeClickListener != null) {
                        onHeaderImageChangeClickListener.onHeadViewClick(cIndex, drawRightSort);
                    }
                }
            });
        } else {
            view = addListHeaderTextView(mMovableListColumnsText[cIndex], mMovableListColumnsWidth[cIndex], false, mLayoutTitleMovable);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onHeaderClickedListener != null) {
                        onHeaderClickedListener.onHeadViewClick(((TextView) v).getText().toString());
                    }
                }
            });
        }

        if (cIndex == mMovableListColumnsText.length - 1) {
            view.setPadding(0, 0, dp2px(columnPadding), 0);
        }
    }

    public void initTitleImageRight(int cIndex, boolean showSortImg) {
        int childCount = mLayoutTitleMovable.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mLayoutTitleMovable.getChildAt(i);
            ImageView drawRight = (ImageView) view.findViewById(R.id.iv_draw_right);
            ImageView drawRightShort = (ImageView) view.findViewById(R.id.iv_short_type);
            drawRight.setImageResource(R.mipmap.stocksign);
            if (cIndex == i && showSortImg) {
                drawRightShort.setVisibility(VISIBLE);
                drawRight.setVisibility(INVISIBLE);
            } else {
                drawRight.setVisibility(VISIBLE);
                drawRightShort.setVisibility(INVISIBLE);
            }
        }
    }

    private View buildMoveableListView() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        mStockListView = new CustomRefreshListView(getContext());
        mStockListView.setLoadingMoreEnable(false);
        if (null != mAdapter) {
            if (mAdapter instanceof CommonAdapter) {
                mStockListView.setAdapter((CommonAdapter) mAdapter);
                mMovableViewList = ((CommonAdapter) mAdapter).getMovableViewList();
            }
        }

        mStockListView.setOnRefreshListener(new CustomRefreshListView.OnRefreshListener() {
            @Override
            public void onPullRefresh() {
                if (onRefreshListener != null) {
                    onRefreshListener.onPullRefresh();
                }
            }

            @Override
            public void onLoadingMore() {
                if (null != onLoadMoreListener) {
                    onLoadMoreListener.onLoadingMore();
                }
            }
        });
        mStockListView.setOnItemClickListener(mOnItemClickListener);
        mStockListView.setOnItemLongClickListener(mOnItemLongClickListener);
        linearLayout.addView(mStockListView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return linearLayout;
    }

    public void onLoadingComplete() {
        if (mStockListView != null) {
            mStockListView.completeRefresh();
        }
    }

    private OnRefreshListener onRefreshListener;

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.onRefreshListener = onRefreshListener;
    }

    public void setRefreshCompleted() {
        if (mStockListView != null) {
            mStockListView.completeRefresh();
        }
    }

    public interface OnRefreshListener {
        /**
         * 下拉刷新监听
         */
        void onPullRefresh();
    }

    public void setAdapter(Object adapter) {
        this.mAdapter = adapter;
        initView();
    }


    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (null != onItemClickedListener) {
                onItemClickedListener.onItemClick(parent, view, position, id);
            }
        }
    };

    private AdapterView.OnItemLongClickListener mOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (null != onItemLongClickedListener) {
                onItemLongClickedListener.onItemLongClick(parent, view, position, id);
            }
            return false;
        }
    };

    private OnItemClickedListener onItemClickedListener;
    private OnItemLongClickedListener onItemLongClickedListener;

    /**
     * 列表item单机事件
     */
    public void setOnItemClick(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }

    /**
     * 列表item长按事件
     */
    public void setOnItemLongClick(OnItemLongClickedListener onItemLongClickedListener) {
        this.onItemLongClickedListener = onItemLongClickedListener;
    }


    /**
     * 设置表头，列名称
     *
     * @param headerName
     * @param width
     * @param fixHeadLayout
     * @return
     */
    private TextView addListHeaderTextView(String headerName, int width, boolean isColumnName, LinearLayout fixHeadLayout) {
        TextView textView = new TextView(getContext());
        textView.setText(headerName);
        textView.setTextSize(titleTextSize);
        textView.setTextColor(titleTextColor);
        if (isColumnName) {
            textView.setPadding(dp2px(columnPadding), 0, dp2px(columnPadding), 0);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        } else {
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.END);
        }
        fixHeadLayout.addView(textView, width, dp2px(mTitleHeight));
        return textView;
    }

    /**
     * 设置表头，列名称
     *
     * @param headerName
     * @param width
     * @param fixHeadLayout
     * @return
     */

    private View addListHeader(int layoutRes, String headerName, int width, LinearLayout fixHeadLayout) {
        View view = LayoutInflater.from(getContext()).inflate(layoutRes, this, false);
        TextView textView = (TextView) view.findViewById(R.id.tv_title_name);
        textView.setText(headerName);
        textView.setTextColor(titleTextColor);
        textView.setTextSize(titleTextSize);
        textView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        view.setLayoutParams(new LayoutParams(width, dp2px(mTitleHeight)));
        fixHeadLayout.addView(view, width, dp2px(mTitleHeight));
        return view;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        float x = ev.getX();
        float y = ev.getY();
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartX = ev.getX();
                mDownPosX = x;
                mDownPosY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(x - mDownPosX);
                float deltaY = Math.abs(y - mDownPosY);
                // 左右滑动拦截 避免跟内部 listView 冲突
                if (deltaX > deltaY) {
                    return true;
                }
            case MotionEvent.ACTION_UP:
                actionUP();
                break;
            default:
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private void actionUP() {
        if (mFixX < 0) {
            mFixX = 0;
            mLayoutTitleMovable.scrollTo(0, 0);
            if (null != mMovableViewList) {
                for (int i = 0; i < mMovableViewList.size(); i++) {
                    mMovableViewList.get(i).scrollTo(0, 0);
                }
            }

        } else {
            if (mLayoutTitleMovable.getWidth() + Math.abs(mFixX) > MovableTotalWidth()) {
                mLayoutTitleMovable.scrollTo(MovableTotalWidth() - mLayoutTitleMovable.getWidth(), 0);
                if (null != mMovableViewList) {
                    for (int i = 0; i < mMovableViewList.size(); i++) {
                        mMovableViewList.get(i).scrollTo(MovableTotalWidth() - mLayoutTitleMovable.getWidth(), 0);
                    }
                }
            }
        }
    }


    private int MovableTotalWidth() {
        if (0 == mMovableTotalWidth) {
            for (int aMMovableListColumnsWidth : mMovableListColumnsWidth) {
                mMovableTotalWidth = mMovableTotalWidth + aMMovableListColumnsWidth;
            }
        }
        return mMovableTotalWidth;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mStartX = event.getX();
                return true;
            case MotionEvent.ACTION_MOVE:
                int offsetX = (int) Math.abs(event.getX() - mStartX);
                if (offsetX > 30) {
                    mMoveOffsetX = (int) (mStartX - event.getX() + mFixX);
                    if (0 > mMoveOffsetX) {
                        mMoveOffsetX = 0;
                    } else {
                        if ((mLayoutTitleMovable.getWidth() + mMoveOffsetX) > MovableTotalWidth()) {
                            mMoveOffsetX = MovableTotalWidth() - mLayoutTitleMovable.getWidth();
                        }
                    }
                    mLayoutTitleMovable.scrollTo(mMoveOffsetX, 0);
                    if (null != mMovableViewList) {
                        for (int i = 0; i < mMovableViewList.size(); i++) {

                            mMovableViewList.get(i).scrollTo(mMoveOffsetX, 0);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mFixX = mMoveOffsetX;
                actionUP();
                break;
            default:
                break;

        }

        return super.onTouchEvent(event);
    }

    /**
     * 必须先初始化顶部标题栏
     *
     * @param headerListData 标题栏名称
     */
    public void setHeaderListData(String[] headerListData) {
        if (headerListData == null) {
            return;
        }
        this.mMovableListColumnsText = headerListData;
        mMovableListColumnsWidth = new int[headerListData.length];
        for (int i = 0; i < headerListData.length; i++) {
            mMovableListColumnsWidth[i] = dp2px(moveViewWidth);
        }
        mFixLeftListColumnsWidth = new int[]{dp2px(mColumnTitleViewWidth)};
        if (TextUtils.isEmpty(columnTitleName)) {
            mFixLeftListColumnsText = new String[]{"名称"};
        } else {
            mFixLeftListColumnsText = new String[]{columnTitleName};
        }
    }


    private OnHeaderClickedListener onHeaderClickedListener = null;

    public OnHeaderClickedListener getOnHeaderClickedListener() {
        return onHeaderClickedListener;
    }

    public void setOnHeaderClickedListener(OnHeaderClickedListener onHeaderClickedListener) {
        this.onHeaderClickedListener = onHeaderClickedListener;
    }

    /**
     * 设置 表头
     *
     * @param name 表头的名称
     */
    public void setColumnTitleName(String name) {
        this.columnTitleName = name;
    }


    /**
     * 列头点击事件
     */
    public interface OnHeaderClickedListener {
        void onHeadViewClick(String string);
    }

    private OnHeaderImageChangeClickListener onHeaderImageChangeClickListener = null;

    public interface OnHeaderImageChangeClickListener {
        void onHeadViewClick(int cIndex, ImageView rightImage);
    }

    public OnHeaderImageChangeClickListener getOnHeaderImageChangeClickListener() {
        return onHeaderImageChangeClickListener;
    }

    public void setOnHeaderSortClickListener(OnHeaderImageChangeClickListener onHeaderImageChangeClickListener) {
        this.onHeaderImageChangeClickListener = onHeaderImageChangeClickListener;
    }

    private OnLoadMoreListener onLoadMoreListener;

    public OnLoadMoreListener getOnLoadMoreListener() {
        return onLoadMoreListener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        if (mStockListView != null) {
            mStockListView.setLoadingMoreEnable(true);
        }
        this.onLoadMoreListener = onLoadMoreListener;
    }

    /**
     * 列表的上拉加载功能
     */
    public interface OnLoadMoreListener {
        void onLoadingMore();
    }

    /**
     * listview item单击事件
     */
    public interface OnItemClickedListener {
        void onItemClick(AdapterView<?> parent, View view, int position, long id);

    }

    /**
     * listview item单击事件
     */
    public interface OnItemLongClickedListener {
        void onItemLongClick(AdapterView<?> parent, View view, int position, long id);

    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    protected int dp2px(float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }


    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * sp 2 px
     *
     * @param spVal
     * @return
     */
    protected int sp2px(float spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());

    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     *
     * @param pxValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }
}

