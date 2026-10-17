package com.ismail.nooktv;
/** Instrumentation-only target: verifies one-press app launching. */
public final class LaunchProbeActivity extends android.app.Activity {
    @Override public void onCreate(android.os.Bundle state){super.onCreate(state);android.widget.TextView text=new android.widget.TextView(this);text.setText("Launched with one press");setContentView(text);}
}
