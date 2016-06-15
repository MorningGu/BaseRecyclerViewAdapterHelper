
package com.chad.library.adapter.base;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.chad.library.R;
import com.chad.library.adapter.base.animation.AlphaInAnimation;
import com.chad.library.adapter.base.animation.BaseAnimation;
import com.chad.library.adapter.base.animation.ScaleInAnimation;
import com.chad.library.adapter.base.animation.SlideInBottomAnimation;
import com.chad.library.adapter.base.animation.SlideInLeftAnimation;
import com.chad.library.adapter.base.animation.SlideInRightAnimation;
import com.chad.library.adapter.base.arrowrefreshheader.ArrowRefreshHeader;
import com.chad.library.adapter.base.arrowrefreshheader.ProgressStyle;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;


/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public abstract class BaseQuickAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //下拉刷新的开关
    private boolean mRefreshEnable = false;
    //上拉加载的开关
    private boolean mNextLoadEnable = false;
    //是否正在加载
    private boolean mLoadingMoreEnable = false;
    //是否只有初次加载item的时候才有动画
    private boolean mFirstOnlyEnable = true;
    //动画效果的开关
    private boolean mOpenAnimationEnable = false;
    //空布局的开关
    private boolean mEmptyEnable;
    //插值器  赋值是线性的
    private Interpolator mInterpolator = new LinearInterpolator();
    //item载入动画的时间
    private int mDuration = 300;
    //最新执行动画的position
    private int mLastPosition = -1;
    //自定义的item点击事件监听
    private OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;
    //自定义的item长按事件监听
    private OnRecyclerViewItemLongClickListener onRecyclerViewItemLongClickListener;
    //自定义的上拉加载监听
    private RequestLoadMoreListener mRequestLoadMoreListener;
    //下拉刷新的监听
    private RequestRefreshListener mRequestRefreshListener;
    //用户自定义的动画
    @AnimationType
    private BaseAnimation mCustomAnimation;
    //默认的动画
    private BaseAnimation mSelectAnimation = new AlphaInAnimation();

    //下拉刷新样式
    private int mRefreshProgressStyle = ProgressStyle.SysProgress;
    //下拉刷新的顶部view
    private ArrowRefreshHeader mRefreshView;
    //上拉加载的底部view
    private View mLoadMoreView;
    //headerView 只有一个
    private View mHeaderView;
    //footerView 只有一个
    private View mFooterView;
    //一页加载的数量
    private int pageSize = -1;
    //普通的item内容布局
    private View mContentView;
    //空内容时的布局
    private View mEmptyView;
    //tag
    protected static final String TAG = BaseQuickAdapter.class.getSimpleName();
    //上下文
    protected Context mContext;
    //普通的item内容布局资源id
    protected int mLayoutResId;
    protected LayoutInflater mLayoutInflater;
    //数据源
    protected List<T> mData;
    //item类型 顶部的headerView，在刷新的loading下面
    protected static final int HEADER_VIEW = 0x00000111;
    //item类型 底部的loading，和FooterView不同时出现
    protected static final int LOADING_VIEW = 0x00000222;
    //item类型 最底部的FooterView 和底部loading不同时出现
    protected static final int FOOTER_VIEW = 0x00000333;
    //item类型 最顶部的刷新loading
    protected static final int REFRESH_VIEW = 0x00000444;
    //item类型 内容为空时的view
    protected static final int EMPTY_VIEW = 0x00000555;
    //动画类型
    @IntDef({ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {
    }

    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int ALPHAIN = 0x00000001;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SCALEIN = 0x00000002;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_BOTTOM = 0x00000003;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_LEFT = 0x00000004;
    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int SLIDEIN_RIGHT = 0x00000005;


    /**
     * call the method will not enable the loadMore funcation and the params pageSize is invalid
     * more infomation see{@link  public void openLoadMore(int pageSize, boolean enable),@link  public void setOnLoadMoreListener(RequestLoadMoreListener requestLoadMoreListener)} method
     *
     * @param pageSize
     * @param requestLoadMoreListener
     */
    @Deprecated
    public void setOnLoadMoreListener(int pageSize, RequestLoadMoreListener requestLoadMoreListener) {
        this.pageSize = pageSize;
        setOnLoadMoreListener(requestLoadMoreListener);
    }
    //设置加载更多的监听
    public void setOnLoadMoreListener(RequestLoadMoreListener requestLoadMoreListener) {
        this.mRequestLoadMoreListener = requestLoadMoreListener;
    }

    /**
     * 设置刷新的监听
     * @param refreshListener
     */
    public void setOnRefreshListener(RequestRefreshListener refreshListener){
        this.mRequestRefreshListener = refreshListener;
        mRefreshEnable = true;
        if(mRefreshView==null){
            mRefreshView = new ArrowRefreshHeader(mContext);
            mRefreshView.setProgressStyle(mRefreshProgressStyle);
        }
    }

    /**
     * 刷新完成
     */
    public void refreshComplete() {
        mRefreshView.refreshComplete();
    }

    /**
     * 设置下拉刷新头部View
     * @param refreshHeader
     */
    public void setRefreshHeader(ArrowRefreshHeader refreshHeader) {
        mRefreshView = refreshHeader;
    }

    /**
     * 设置下拉刷新头部箭头控件
     * @param resId
     */
    public void setArrowImageView(int resId) {
        if (mRefreshView != null) {
            mRefreshView.setArrowImageView(resId);
        }
    }
    /**
     * 设置下拉刷新的样式
     * @param style
     */
    public void setRefreshProgressStyle(int style) {
        mRefreshProgressStyle = style;
        if (mRefreshView != null) {
            mRefreshView.setProgressStyle(style);
        }
    }
    /**
     * when adapter's data size than pageSize and enable is true,the loading more function is enable,or disable
     * 加载更多的开关，以及当前每页size的设置
     * @param pageSize
     * @param enable
     */
    public void openLoadMore(int pageSize, boolean enable) {
        this.pageSize = pageSize;
        mNextLoadEnable = enable;

    }

    /**
     * call the method before you should call setPageSize() method to setting up the enablePagerSize value,whether it will  invalid
     * enable the loading more data function if enable's value is true,or disable
     * 加载更多的开关
     * @param enable
     */
    public void openLoadMore(boolean enable) {
        mNextLoadEnable = enable;
    }

    /**
     * setting up the size to decide the loading more data funcation whether enable
     * enable if the data size than pageSize,or diable
     * 当前每页size的设置
     *
     * @param pageSize
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * return the value of pageSize
     *
     * @return
     */
    public int getPageSize() {
        return this.pageSize;
    }

    /**
     * 自定义item点击事件的监听 设置
     * @param onRecyclerViewItemClickListener
     */
    public void setOnRecyclerViewItemClickListener(OnRecyclerViewItemClickListener onRecyclerViewItemClickListener) {
        this.onRecyclerViewItemClickListener = onRecyclerViewItemClickListener;
    }

    /**
     * 自定义的点击事件响应
     */
    public interface OnRecyclerViewItemClickListener {
        public void onItemClick(View view, int position);
    }

    /**
     * 自定义item长按事件的监听设置
     * @param onRecyclerViewItemLongClickListener
     */
    public void setOnRecyclerViewItemLongClickListener(OnRecyclerViewItemLongClickListener onRecyclerViewItemLongClickListener) {
        this.onRecyclerViewItemLongClickListener = onRecyclerViewItemLongClickListener;
    }

    /**
     * 自定义item长按事件的监听
     */
    public interface OnRecyclerViewItemLongClickListener {
        public boolean onItemLongClick(View view, int position);
    }
    //viewHolder中每个子view的点击事件
    private OnRecyclerViewItemChildClickListener mChildClickListener;

    /**
     * 通过外部添加子view点击事件的监听的集合
     * @param childClickListener
     */
    public void setOnRecyclerViewItemChildClickListener(OnRecyclerViewItemChildClickListener childClickListener) {
        this.mChildClickListener = childClickListener;
    }
    /**
     * ViewHolder中每个子view的点击事件的集合
     */

    public interface OnRecyclerViewItemChildClickListener {
        void onItemChildClick(BaseQuickAdapter adapter, View view, int position);
    }
    /**
     *  ViewHolder中每个子view的点击事件的封装 通过这层封装，
     *  可以将各个子view的点击事件放到一个方法中处理
     *  形式： switch (view.getId()) {
     *           case R.id.tweetAvatar:
     *              content = "img:" + status.getUserAvatar();
     *              break;
     *          case R.id.tweetName:
     *              content = "name:" + status.getUserName();
     *              break;
     *          }
     */

    public class OnItemChildClickListener implements View.OnClickListener {
        public int position;

        @Override
        public void onClick(View v) {
            if (mChildClickListener != null)
                mChildClickListener.onItemChildClick(BaseQuickAdapter.this, v, position - getHeaderViewsCount());
        }
    }


    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param context     The context.
     * @param layoutResId The layout resource id of each item.
     * @param data        A new list is created out of this one to avoid mutable list
     */
    public BaseQuickAdapter(Context context, int layoutResId, List<T> data) {
        this.mData = data == null ? new ArrayList<T>() : new ArrayList<T>(data);
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }
    }

    public BaseQuickAdapter(Context context, List<T> data) {
        this(context, 0, data);
    }

    public BaseQuickAdapter(Context context, View contentView, List<T> data) {
        this(context, 0, data);
        mContentView = contentView;
    }

    public BaseQuickAdapter(Context context) {
        this(context, null);
    }

    /**
     * 删除一条数据
     * @param position
     */
    public void remove(int position) {
        mData.remove(position);
        notifyItemRemoved(position);

    }

    /**
     * 在指定位置添加数据
     * @param position 位置
     * @param item
     */
    public void add(int position, T item) {
        mData.add(position, item);
        notifyItemInserted(position);
    }


    /**
     * 重新设置数据源
     *
     * @param data
     */
    public void setNewData(List<T> data) {
        this.mData = data;
        if (mRequestLoadMoreListener != null) mNextLoadEnable = true;
        notifyDataSetChanged();
    }

    /**
     * 在数据源末尾添加一条数据
     *
     * @param data
     */
    public void addData(List<T> data) {
        this.mData.addAll(data);
        notifyDataSetChanged();
    }


    public List getData() {
        return mData;
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position Position of the item whose data we want within the adapter's
     *                 data set.
     * @return The data at the specified position.
     */
    public T getItem(int position) {
        return mData.get(position);
    }

    public int getHeaderViewsCount() {
        return mHeaderView == null ? 0 : 1;
    }
    public int getRefreshViewCount(){
        return (mRefreshView==null?0:1);
    }
    public int getFooterViewsCount() {
        return mFooterView == null ? 0 : 1;
    }

    public int getmEmptyViewCount() {
        return mEmptyView == null ? 0 : 1;
    }

    @Override
    public int getItemCount() {
        int i = isLoadMore() ? 1 : 0;
        int j = isRefresh()? 1 : 0;
        int count = mData.size() + i + j + getHeaderViewsCount() + getFooterViewsCount();
        mEmptyEnable = false;
        if (count == 0) {
            mEmptyEnable = true;
            count += getmEmptyViewCount();
        }
        return count;
    }

    /**
     * 根据position确定item的type
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            if(mRefreshView!=null && mRefreshEnable){
                return REFRESH_VIEW;
            }else if(mHeaderView!=null){
                return HEADER_VIEW;
            }
        } else if(position == 1 && mHeaderView!=null && mRefreshView!=null){
            //当position为1时，只有refreshView存在，且headerView不为空才是headerView
            return HEADER_VIEW;
        }else if (mEmptyView != null && (mData == null || mData.size() == 0) && mEmptyEnable) {
            //数据源为空或者长度为0 且emptyView不为空且开关打开
            return EMPTY_VIEW;
        } else if (position == getItemCount()-1) {
            if (mNextLoadEnable)
                return LOADING_VIEW;
            else if(mFooterView!=null){
                return FOOTER_VIEW;
            }
        }
        return getDefItemViewType(position);
    }

    protected int getDefItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder baseViewHolder = null;
        switch (viewType) {
            case LOADING_VIEW:
                baseViewHolder = createBaseViewHolder(parent, R.layout.def_loading);
                break;
            case REFRESH_VIEW:
                baseViewHolder = new BaseViewHolder(mContext, mRefreshView);
                break;
            case HEADER_VIEW:
                baseViewHolder = new BaseViewHolder(mContext, mHeaderView);
                break;
            case EMPTY_VIEW:
                baseViewHolder = new BaseViewHolder(mContext, mEmptyView);
                break;
            case FOOTER_VIEW:
                baseViewHolder = new BaseViewHolder(mContext, mFooterView);
                break;
            default:
                baseViewHolder = onCreateDefViewHolder(parent, viewType);
        }
        return baseViewHolder;

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int positions) {
        switch (holder.getItemViewType()) {
            case 0:
                convert((BaseViewHolder) holder, mData.get(holder.getLayoutPosition() - getHeaderViewsCount()-getRefreshViewCount()));
                initItemClickListener(holder, (BaseViewHolder) holder);
                addAnimation(holder);
                break;
            case LOADING_VIEW:
                addLoadMore(holder);
                break;
            case REFRESH_VIEW:
                mRefreshView.setVisibleHeight(0);
                break;
            case HEADER_VIEW:
                break;
            case EMPTY_VIEW:
                break;
            case FOOTER_VIEW:
                break;
            default:
                convert((BaseViewHolder) holder, mData.get(holder.getLayoutPosition() - getHeaderViewsCount()-getRefreshViewCount()));
                onBindDefViewHolder((BaseViewHolder) holder, mData.get(holder.getLayoutPosition() - getHeaderViewsCount()-getRefreshViewCount()));
                initItemClickListener(holder, (BaseViewHolder) holder);
                break;
        }

    }


    protected BaseViewHolder onCreateDefViewHolder(ViewGroup parent, int viewType) {
        return createBaseViewHolder(parent, mLayoutResId);
    }

    protected BaseViewHolder createBaseViewHolder(ViewGroup parent, int layoutResId) {
        if (mContentView == null) {
            return new BaseViewHolder(mContext, getItemView(layoutResId, parent));
        }
        return new BaseViewHolder(mContext, mContentView);
    }


    public void addHeaderView(View header) {
        if (header == null) {
            throw new RuntimeException("header is null");
        }
        this.mHeaderView = header;
        this.notifyDataSetChanged();
    }

    public void addFooterView(View footer) {
        mNextLoadEnable = false;
        if (footer == null) {
            throw new RuntimeException("footer is null");
        }
        this.mFooterView = footer;
        this.notifyDataSetChanged();
    }

    /**
     * Sets the view to show if the adapter is empty
     */
    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
    }

    /**
     * When the current adapter is empty, the BaseQuickAdapter can display a special view
     * called the empty view. The empty view is used to provide feedback to the user
     * that no data is available in this AdapterView.
     *
     * @return The view to show if the adapter is empty.
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * see more {@link  public void notifyDataChangedAfterLoadMore(boolean isNextLoad)}
     *
     * @param isNextLoad
     */
    @Deprecated
    public void isNextLoad(boolean isNextLoad) {
        mNextLoadEnable = isNextLoad;
        mLoadingMoreEnable = false;
        notifyDataSetChanged();
    }

    public void notifyDataChangedAfterLoadMore(boolean isNextLoad) {
        mNextLoadEnable = isNextLoad;
        mLoadingMoreEnable = false;
        notifyDataSetChanged();
    }

    /**
     * 加载完成后调用这个方法来刷新view
     * @param data
     * @param isNextLoad
     */
    public void notifyDataChangedAfterLoadMore(List<T> data, boolean isNextLoad) {
        if(data==null){
            return;
        }
        mData.addAll(data);
        notifyDataChangedAfterLoadMore(isNextLoad);
    }

    /**
     * 刷新完成后调用这个方法来刷新view
     * @param data
     */
    public void notifyDataChangedAfterRefresh(List<T> data){
        refreshComplete();
        if(data==null){
            return;
        }
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    private void addLoadMore(RecyclerView.ViewHolder holder) {
        if (isLoadMore()) {
            mLoadingMoreEnable = true;
            mRequestLoadMoreListener.onLoadMoreRequested();
            if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
                params.setFullSpan(true);
            }
        }
    }

    private void initItemClickListener(final RecyclerView.ViewHolder holder, BaseViewHolder baseViewHolder) {
        if (onRecyclerViewItemClickListener != null) {
            baseViewHolder.convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onRecyclerViewItemClickListener!=null){
                        onRecyclerViewItemClickListener.onItemClick(v, holder.getLayoutPosition() - getHeaderViewsCount());
                    }
                }
            });
        }
        if (onRecyclerViewItemLongClickListener != null) {
            baseViewHolder.convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(onRecyclerViewItemLongClickListener!=null){
                        return onRecyclerViewItemLongClickListener.onItemLongClick(v, holder.getLayoutPosition() - getHeaderViewsCount());
                    }
                    return false;
                }
            });
        }
    }

    private void addAnimation(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mLastPosition) {
                BaseAnimation animation = null;
                if (mCustomAnimation != null) {
                    animation = mCustomAnimation;
                } else {
                    animation = mSelectAnimation;
                }
                for (Animator anim : animation.getAnimators(holder.itemView)) {
                    anim.setDuration(mDuration).start();
                    anim.setInterpolator(mInterpolator);
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    /**
     * 是否允许上拉加载
     * @return
     */
    private boolean isLoadMore() {
        return mNextLoadEnable && pageSize != -1 && !mLoadingMoreEnable && mRequestLoadMoreListener != null && mData.size() >= pageSize;
    }
    private boolean isRefresh(){
        return mRefreshEnable && mRequestRefreshListener!=null && mRefreshView!=null;
    }

    protected View getItemView(int layoutResId, ViewGroup parent) {
        return mLayoutInflater.inflate(layoutResId, parent, false);
    }

    /**
     * Two item type can override it
     *
     * @param holder
     * @param item
     */
    @Deprecated
    protected void onBindDefViewHolder(BaseViewHolder holder, T item) {
    }

    /**
     * 上拉加载的监听
     */
    public interface RequestLoadMoreListener {

        void onLoadMoreRequested();
    }

    /**
     * 下拉刷新的监听
     */
    public interface  RequestRefreshListener{
        void onRefreshRequested();
    }

    /**
     * Set the view animation type.
     *
     * @param animationType One of {@link #ALPHAIN}, {@link #SCALEIN}, {@link #SLIDEIN_BOTTOM}, {@link #SLIDEIN_LEFT}, {@link #SLIDEIN_RIGHT}.
     */
    public void openLoadAnimation(@AnimationType int animationType) {
        this.mOpenAnimationEnable = true;
        mCustomAnimation = null;
        switch (animationType) {
            case ALPHAIN:
                mSelectAnimation = new AlphaInAnimation();
                break;
            case SCALEIN:
                mSelectAnimation = new ScaleInAnimation();
                break;
            case SLIDEIN_BOTTOM:
                mSelectAnimation = new SlideInBottomAnimation();
                break;
            case SLIDEIN_LEFT:
                mSelectAnimation = new SlideInLeftAnimation();
                break;
            case SLIDEIN_RIGHT:
                mSelectAnimation = new SlideInRightAnimation();
                break;
            default:
                break;
        }
    }

    /**
     * Set Custom ObjectAnimator
     *
     * @param animation ObjectAnimator
     */
    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mCustomAnimation = animation;
    }

    public void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }


    public void isFirstOnly(boolean firstOnly) {
        this.mFirstOnlyEnable = firstOnly;
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param helper A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    protected abstract void convert(BaseViewHolder helper, T item);


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            float mLastY = -1;
            @Override
            public boolean onTouch(View v, MotionEvent ev) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mLastY = ev.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(mLastY==-1){
                            //这里很奇怪，down没有直接是move
                            mLastY = ev.getRawY();
                        }
                        float deltaY = ev.getRawY() - mLastY;
                        mLastY = ev.getRawY();
                        if (isOnTop(recyclerView) && mRefreshEnable) {
                            mRefreshView.onMove(deltaY/3);
                            if (mRefreshView.getVisibleHeight() > 0 && mRefreshView.getState() < ArrowRefreshHeader.STATE_REFRESHING) {
//                                Log.i("getVisibleHeight", "getVisibleHeight = " + mRefreshView.getVisibleHeight());
//                                Log.i("getVisibleHeight", " mRefreshHeader.getState() = " + mRefreshView.getState());
                                return false;
                            }
                        }
                        break;
                    default:
                        mLastY = -1;
                        if (isOnTop(recyclerView)&&mRefreshEnable) {
                            if (mRefreshView.releaseAction()) {
                                if (mRequestRefreshListener != null) {
                                    mRequestRefreshListener.onRefreshRequested();
                                }
                            }
                        }
                        break;
                }
                return recyclerView.onTouchEvent(ev);
            }
        });
    }
    //recyclerView是不是滑动到顶部
    private boolean isOnTop(RecyclerView recyclerView){
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        int firstVisibleItemPosition;
        if (layoutManager instanceof GridLayoutManager) {
            firstVisibleItemPosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if ( layoutManager instanceof StaggeredGridLayoutManager ) {
            int[] into = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(into);
            firstVisibleItemPosition = findMin(into);
        } else {
            firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }
        if ( firstVisibleItemPosition <= 1 ) {
             return true;
        }
        return false;
    }
    //找到最小值
    private int findMin(int[] firstPositions) {
        int min = firstPositions[0];
        for (int value : firstPositions) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }
}
