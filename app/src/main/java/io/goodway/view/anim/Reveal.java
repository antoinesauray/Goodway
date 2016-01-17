package io.goodway.view.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewAnimationUtils;

/**
 * Created by antoine on 12/2/15.
 */
public class Reveal {

    public static void enterReveal(View v) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // get the center for the clipping circle_red
            int cx = v.getMeasuredWidth() / 2;
            int cy = v.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle_red
            int finalRadius = Math.max(v.getWidth(), v.getHeight()) / 2;

            // create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            v.setVisibility(View.VISIBLE);
            anim.start();
        }
        else{
            v.setVisibility(View.VISIBLE);
        }

    }
    public static void exitReveal(final View v) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // get the center for the clipping circle_red
            int cx = v.getMeasuredWidth() / 2;
            int cy = v.getMeasuredHeight() / 2;

            // get the initial radius for the clipping circle_red
            int initialRadius = v.getWidth() / 2;

            // create the animation (the final radius is zero)
            Animator anim =
                    null;
            anim = ViewAnimationUtils.createCircularReveal(v, cx, cy, initialRadius, 0);


            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    v.setVisibility(View.INVISIBLE);
                }
            });

            // start the animation
            anim.start();
        }
        else{
            v.setVisibility(View.INVISIBLE);
        }


    }
}
