package com.example.lydia.wechatautoaddfriends;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * Edit text with right icon to clear text
 */
@SuppressLint("AppCompatCustomView")
public class ClearEditText extends EditText implements View.OnFocusChangeListener,TextWatcher, View.OnLongClickListener {
    /**
     * 删除按钮的引用
     */
    private Drawable mClearDrawable;

    private static final int POP_MENU_WIDTH = 150;
    private static final int POP_MENU_HEIGHT = 60;

    /**
     * 控件是否有焦点
     */
    private boolean hasFocus;

    public ClearEditText(Context context) {
        this(context,null);
    }
    public ClearEditText(Context context,AttributeSet attrs){
        //这里构造方法也很重要，不加这个很多属性不能再XML里面定义
        this(context, attrs, android.R.attr.editTextStyle);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    private void init() {
        //获取EditText的DrawableRight,假如没有设置我们就使用默认的图片
        mClearDrawable = getCompoundDrawables()[2];
        if (mClearDrawable == null) {
            mClearDrawable = getResources().getDrawable(R.drawable.delete_selector);
        }
        mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(), mClearDrawable.getIntrinsicHeight());
        //默认设置隐藏图标
        setClearIconVisible(false);
        //设置焦点改变的监听
        setOnFocusChangeListener(this);
        //设置输入框里面内容发生改变的监听
        addTextChangedListener(this);
        setOnLongClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mClearDrawable != null && event.getAction() == MotionEvent.ACTION_UP) {
            int x = (int) event.getX();
            //判断触摸点是否在水平范围内
            boolean isInnerWidth = (x > (getWidth() - getTotalPaddingRight())) &&
                    (x < (getWidth() - getPaddingRight()));
            //获取删除图标的边界，返回一个Rect对象
            Rect rect = mClearDrawable.getBounds();
            //获取删除图标的高度
            int height = rect.height();
            int y = (int) event.getY();
            //计算图标底部到控件底部的距离
            int distance = (getHeight() - height) / 2;
            //判断触摸点是否在竖直范围内(可能会有点误差)
            //触摸点的纵坐标在distance到（distance+图标自身的高度）之内，则视为点中删除图标
            boolean isInnerHeight = (y > distance) && (y < (distance + height));
            if (isInnerHeight && isInnerWidth) {
                this.setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 设置清除图标的显示与隐藏，调用setCompoundDrawables为EditText绘制上去
     *
     * @param visible
     */
    private void setClearIconVisible(boolean visible) {
        Drawable right = visible ? mClearDrawable : null;
        setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1],
                right, getCompoundDrawables()[3]);
    }

    /**
     * 当ClearEditText焦点发生变化的时候，判断里面字符串长度设置清除图标的显示与隐藏
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        this.hasFocus = hasFocus;
        if (hasFocus) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    /**
     * 当输入框里面内容发生变化的时候回调的方法
     */
    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (hasFocus) {
            setClearIconVisible(text.length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public boolean onLongClick(View view) {
        showPopWindows();
        return true;
    }

    /**
     *  loy.ouyang: show popWindow to paste in floatView
     */
    private void showPopWindows() {

        /// loy.ouyang: init pop view
        View mPopView = LayoutInflater.from(getContext()).inflate(R.layout.copy_menu_layout, null);
        final PopupWindow popWindow = new PopupWindow(mPopView, POP_MENU_WIDTH, POP_MENU_HEIGHT);
        /// loy.ouyang: set background
        popWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.pop_window_background));
        popWindow.setFocusable(true);
        popWindow.setOutsideTouchable(true);

        int popWindowHeight = popWindow.getHeight();


        /// loy.ouyang: set show position
        popWindow.showAtLocation(this, Gravity.NO_GRAVITY, getLeft(), getTop() - popWindowHeight / 2);
        popWindow.update();
        TextView pasteView = (TextView) mPopView.findViewById(R.id.action);
        pasteView.setText(android.R.string.paste);
        pasteView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                paste();
                popWindow.dismiss();
            }
        });
    }

    private void paste() {
        final ClipboardManager clipboard = (ClipboardManager) getContext().getApplicationContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard == null){
            return;
        }
        final ClipData primaryClip = clipboard.getPrimaryClip();
        if (primaryClip != null) {
            final ClipData.Item item = primaryClip.getItemCount() == 0 ? null : primaryClip.getItemAt(0);
            if (item == null) {
                // nothing to paste, bail early...
                return;
            }
            int cursorIndex = getSelectionStart();
            String resultText;
            String originText = getText().toString();
            String copyText = item.coerceToText(getContext().getApplicationContext()).toString();
            if (cursorIndex == 0 || originText.isEmpty()){
                resultText = copyText + originText;
            }else if (cursorIndex == originText.length()){
                resultText = originText + copyText;
            }else {
                resultText = originText.substring(0, cursorIndex) + copyText + originText.substring(cursorIndex);
            }
            setText(resultText);
            setSelection(cursorIndex + copyText.length());
        }
    }
}
