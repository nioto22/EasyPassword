package com.example.nioto.easypasword.service;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nioto.easypasword.ui.AddEditNoteActivity;
import com.example.nioto.easypasword.R;

import static com.example.nioto.easypasword.ui.MainActivity.COPY_LOGIN_KEY;
import static com.example.nioto.easypasword.ui.MainActivity.COPY_PASSWORD_KEY;

public class TransferService extends Service implements View.OnTouchListener {
    private static final String TAG = "TransferService";

    private WindowManager windowManager;
    private View floatView;

    WindowManager.LayoutParams params;
    boolean touchconsumedbyMove = false;
    int floatWindowLastX;
    int floatWindowLastY;
    int floatWindowFirstX;
    int floatWindowFirstY;

    private TextView tvTitle;
    private TextView tvLogin;
    private TextView tvPassword;
    private ImageButton buttonClose;
    private ImageButton buttonCopyLogin;
    private ImageButton buttonCopyPassword;

    private String title;
    private String login;
    private String password;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        title = (String)intent.getExtras().get(AddEditNoteActivity.EXTRA_TITLE);
        login = (String)intent.getExtras().get(AddEditNoteActivity.EXTRA_LOGIN);
        password = (String) intent.getExtras().get(AddEditNoteActivity.EXTRA_PASSWORD);
        Log.d(TAG, "onStartCommand: title = "+ title);
        addOverlayView();
        return super.onStartCommand(intent, flags, startId);
    }

    private void addOverlayView() {
        final WindowManager.LayoutParams params = getFloatingParams();


        FrameLayout interceptorLayout = new FrameLayout(this) {
            @Override
            public boolean dispatchKeyEvent(KeyEvent event) {
                // Only fire on the ACTION_DOWN event, or you'll get two events (one for _DOWN, one for _UP)
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    // Check if the HOME button is pressed
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                        Log.v(TAG, "BACK Button Pressed");
                        // As we've taken action, we'll return true to prevent other apps from consuming the event as well
                        return true;
                    }
                }
                // Otherwise don't intercept the event
                return super.dispatchKeyEvent(event);
            }
        };

        floatView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.floating_view, interceptorLayout);
        configureViews();
        floatView.setOnTouchListener(this);
        windowManager.addView(floatView, params);

    }

    private void configureViews() {
        Log.d(TAG, "configureViews: ok. Title =" +title);
        tvTitle = floatView.findViewById(R.id.float_title);
        tvTitle.setText(title);
        Log.d(TAG, "configureViews: "+ tvTitle.getText().toString());
        tvLogin = floatView.findViewById(R.id.float_login);
        tvLogin.setText(login);
        tvPassword = floatView.findViewById(R.id.float_password);
        tvPassword.setText(password);
        buttonClose = floatView.findViewById(R.id.button_float_close);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onDestroy();
            }
        });
        buttonCopyLogin = floatView.findViewById(R.id.button_float_copy_login);
        buttonCopyLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyTextToClipboard(COPY_LOGIN_KEY);
            }
        });
        buttonCopyPassword = floatView.findViewById(R.id.button_float_copy_password);
        buttonCopyPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyTextToClipboard(COPY_PASSWORD_KEY);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatView != null) {
            windowManager.removeView(floatView);
            floatView = null;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        WindowManager.LayoutParams prm = getFloatingParams();
        int totalDeltaX = floatWindowLastX - floatWindowFirstX;
        int totalDeltaY = floatWindowLastY - floatWindowFirstY;

        switch(motionEvent.getActionMasked())
        {
            case MotionEvent.ACTION_DOWN:
                floatWindowLastX = (int) motionEvent.getRawX();
                floatWindowLastY = (int) motionEvent.getRawY();
                floatWindowFirstX = floatWindowLastX;
                floatWindowFirstY = floatWindowLastY;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = (int) motionEvent.getRawX() - floatWindowLastX;
                int deltaY = (int) motionEvent.getRawY() - floatWindowLastY;
                floatWindowLastX = (int) motionEvent.getRawX();
                floatWindowLastY = (int) motionEvent.getRawY();
                if (Math.abs(totalDeltaX) >= 5  || Math.abs(totalDeltaY) >= 5) {
                    if (motionEvent.getPointerCount() == 1) {
                        params.x += deltaX;
                        params.y += deltaY;
                        touchconsumedbyMove = true;
                        windowManager.updateViewLayout(getFloatView(), prm);
                    }
                    else{
                        touchconsumedbyMove = false;
                    }
                }else{
                    touchconsumedbyMove = false;
                }
                break;
            default:
                break;
        }
        return touchconsumedbyMove;
    }



    private WindowManager.LayoutParams getFloatingParams() {
        if (params != null) {
            return params;
        }
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                0,
                PixelFormat.TRANSLUCENT);
        params.flags = WindowManager.LayoutParams.FORMAT_CHANGED;
        params.gravity = Gravity.CENTER_VERTICAL ;
        params.x = 0;
        params.y = 0;
        return params;
    }

    public View getFloatView() {
        return floatView;
    }


    private void copyTextToClipboard(int copyKey) {
        String label;
        String text;

        switch (copyKey){
            case COPY_LOGIN_KEY :
                label = "Login";
                text = login;
                break;
            case COPY_PASSWORD_KEY :
                label = "Password";
                text = password;
                break;
            default:
                label ="";
                text ="";
                break;
        }
        if (text.equals("")){
            Toast.makeText(this, "Please enter a "+ label, Toast.LENGTH_SHORT).show();
            return;
        } else {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, label + " copy to clipboard.", Toast.LENGTH_SHORT).show();
        }
    }
}

