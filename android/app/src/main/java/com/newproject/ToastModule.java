
package com.newproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class ToastModule extends Button {

    public Boolean isTurnedOn = false;

    public void setIsTurnedOn (Boolean ToastModuleStatus){
        isTurnedOn = ToastModuleStatus;
        changeColor();
    }
    public ToastModule(Context context) {
        super(context);
        this.setTextColor(Color.BLUE);
        this.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                isTurnedOn = !isTurnedOn;
                changeColor();
            }
        });
        changeColor();
    }

    private void changeColor() {
        if (isTurnedOn) {
            setBackgroundColor(Color.YELLOW);
            setText("I am ON");
        } else {
            setBackgroundColor(Color.GRAY);
            setText("I am OFF");
        }
    }

    public ToastModule(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToastModule(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}