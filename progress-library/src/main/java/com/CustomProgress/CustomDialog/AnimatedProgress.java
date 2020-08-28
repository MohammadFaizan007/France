package com.CustomProgress.CustomDialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.widget.TextView;

import com.CustomProgress.CustomTextView;
import com.CustomProgress.ProgressIndicatorView;
import com.CustomProgress.R;

/**
 * Created by appideas-user2 on 18/5/17.
 */

public class AnimatedProgress {
    private  Dialog mDialog;
    private ProgressIndicatorView primaryProgressIndicatorView;
    private ProgressIndicatorView secondryProgressIndicatorView;
    private CustomTextView textView;


    public AnimatedProgress(Activity activity)
    {
        mDialog = new Dialog(activity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT));
        mDialog.setContentView(R.layout.custom_progress);
        textView = mDialog.findViewById(R.id.dialog_content_textView);
        primaryProgressIndicatorView = mDialog.findViewById(R.id.custom_dialog_progress);
        secondryProgressIndicatorView= mDialog.findViewById(R.id.custom_dialog_second_progress);
        mDialog.setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    public void setCanceledOnTouchOutside(boolean b)
    {
        mDialog.setCanceledOnTouchOutside(b);
    }
    public void setCancelable(boolean b)
    {
        mDialog.setCancelable(b);
    }

public void setText(String text)
{
    this.textView.setText(text);
}

public void showProgress()
{
    primaryProgressIndicatorView.show();
    secondryProgressIndicatorView.show();
    mDialog.show();
}

public void hideProgress()
{
   /* primaryProgressIndicatorView.hide();
    secondryProgressIndicatorView.hide();*/
    mDialog.dismiss();
}
}
