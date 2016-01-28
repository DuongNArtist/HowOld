package com.dsa.howold;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class ProcessingDialog extends Dialog {

    public static final int[] IMAGE_IDS = { R.id.iv_loading_0,
            R.id.iv_loading_1, R.id.iv_loading_2, R.id.iv_loading_3,
            R.id.iv_loading_4 };
    public static final int[] ANIM_IDS = { R.anim.anim_loading_0,
            R.anim.anim_loading_1, R.anim.anim_loading_2,
            R.anim.anim_loading_3, R.anim.anim_loading_4 };

    private Context mContext;
    private ImageView[] mivLoadings;
    private Animation[] mAnimations;

    public ProcessingDialog(Context context) {
        super(context);
        mContext = context;
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_processing);
        getWindow().setLayout(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        getWindow().setGravity(Gravity.CENTER);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mivLoadings = new ImageView[5];
        mAnimations = new Animation[5];
        for (int index = 0; index < mivLoadings.length; index++) {
            mivLoadings[index] = (ImageView) findViewById(IMAGE_IDS[index]);
            mAnimations[index] = AnimationUtils.loadAnimation(mContext,
                    ANIM_IDS[index]);
        }
    }

    @Override
    public void show() {
        super.show();
        for (int index = 0; index < mivLoadings.length; index++) {
            mivLoadings[index].startAnimation(mAnimations[index]);
        }
    }
}
