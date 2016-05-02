package com.studio.jason.application.customized_view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import com.studio.jason.application.R;

/**
 * Created by Jason on 2014/12/14.
 */
public class CheckableListItemView extends RelativeLayout implements Checkable {

    private CheckBox mCheckBox;
//    private TextView mTextView;
    private Boolean mIsChecked;

    public CheckableListItemView(Context context) {
        super(context);
    }

    public CheckableListItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckableListItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        this.mCheckBox = (CheckBox) findViewById(R.id.item_checkBox);
//        this.mTextView = (TextView) findViewById(R.id.textView2);
        super.onFinishInflate();
    }

    @Override
    public boolean isChecked() {
        return this.mIsChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        this.mIsChecked = checked;
        this.mCheckBox.setChecked(checked);
        if(checked){
            this.mCheckBox.setVisibility(View.VISIBLE);
//            this.mTextView.setTextColor(getResources().getColor(R.color.purple));

        }else{
            this.mCheckBox.setVisibility(View.GONE);
//            this.mTextView.setTextColor(getResources().getColor(R.color.orange));
        }

    }

    @Override
    public void toggle() {
        setChecked(!mIsChecked);
    }
}
