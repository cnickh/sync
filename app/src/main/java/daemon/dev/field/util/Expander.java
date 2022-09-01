package daemon.dev.field.util;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import org.jetbrains.annotations.Nullable;

public class Expander {
    public void expand(final View v, int duration, int targetHeight) {

        int prevHeight  = v.getHeight();

        v.setVisibility(View.VISIBLE);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }

    public void collapse(final View v, int duration, int targetHeight) {
        int prevHeight  = v.getHeight();
        ValueAnimator valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                v.getLayoutParams().height = (int) animation.getAnimatedValue();
                v.requestLayout();
            }
        });
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.setDuration(duration);
        valueAnimator.start();
    }
//    public void expand(View v) {
//        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
//        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
//
//        final int targetHeight = v.getMeasuredHeight();
//
//        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
//        v.getLayoutParams().height = 1;
//        v.setVisibility(View.VISIBLE);
//        Animation a = new Animation()
//        {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//
//                v.getLayoutParams().height = interpolatedTime == 1
//                        ? LinearLayout.LayoutParams.WRAP_CONTENT
//                        : (int)(targetHeight * interpolatedTime);
//                v.requestLayout();
//            }
//
//            @Override
//            public boolean willChangeBounds() {
//                return true;
//            }
//        };
//
//        // Expansion speed of 1dp/ms
//        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density) * 4);
//        v.startAnimation(a);
//    }
//
//    public void collapse(final View v) {
//        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
//        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
//        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
//        final int initialHeight = v.getMeasuredHeight();
//
//        Animation a = new Animation()
//        {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                if(interpolatedTime == 1){
//                    v.setVisibility(View.GONE);
//                }else{
//                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
//                    v.requestLayout();
//                }
//            }
//
//            @Override
//            public boolean willChangeBounds() {
//                return true;
//            }
//        };
//
//        // Collapse speed of 1dp/ms
//        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density)*4);
//        v.startAnimation(a);
//    }

}
