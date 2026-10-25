package pl.offline.supaplex;

import android.app.*;
import android.os.*;
import android.content.*;
import android.view.*;
import java.lang.reflect.*;
import java.util.*;

/** Physical-device checks for simultaneous input, sliding and cancellation. */
public class HudInstrumentation extends Instrumentation {
    private GameActivity activity;
    private long down;
    private float unit,w,h;
    private StringBuilder results=new StringBuilder();
    @Override public void onCreate(Bundle args) { super.onCreate(args);start(); }
    private void check(boolean ok,String name) { if(!ok)throw new AssertionError(name);results.append("PASS ").append(name).append('\n'); }
    private Object field(String name) throws Exception {Field f=GameActivity.class.getDeclaredField(name);f.setAccessible(true);return f.get(activity);}
    private HashSet<Integer> held() throws Exception {
        final HashSet<Integer> copy=new HashSet<>();
        runOnMainSync(() -> {try{copy.addAll((Set<Integer>)field("held"));}catch(Exception e){throw new RuntimeException(e);}});
        return copy;
    }
    private void event(int action,float[] xs,float[] ys,int[] ids) {
        MotionEvent.PointerProperties[] props=new MotionEvent.PointerProperties[ids.length];
        MotionEvent.PointerCoords[] coords=new MotionEvent.PointerCoords[ids.length];
        for(int i=0;i<ids.length;i++) {props[i]=new MotionEvent.PointerProperties();props[i].id=ids[i];props[i].toolType=MotionEvent.TOOL_TYPE_FINGER;
            coords[i]=new MotionEvent.PointerCoords();coords[i].x=xs[i]*unit;coords[i].y=ys[i]*unit;coords[i].pressure=1;coords[i].size=.1f;}
        MotionEvent e=MotionEvent.obtain(down,SystemClock.uptimeMillis(),action,ids.length,props,coords,0,0,1,1,0,0,InputDevice.SOURCE_TOUCHSCREEN,0);
        sendPointerSync(e);e.recycle();waitForIdleSync();SystemClock.sleep(35);
    }
    @Override public void onStart() {
        Bundle out=new Bundle();
        try {
            Intent intent=new Intent(getTargetContext(),GameActivity.class);intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity=(GameActivity)startActivitySync(intent);waitForIdleSync();SystemClock.sleep(700);
            android.view.accessibility.AccessibilityNodeInfo root=null;
            for(int attempt=0;attempt<30 && root==null;attempt++) {
                root=getUiAutomation().getRootInActiveWindow();
                if(root==null)SystemClock.sleep(100);
            }
            if(root!=null) for (android.view.accessibility.AccessibilityNodeInfo node : root.findAccessibilityNodeInfosByText("MENU GRY"))
                node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK);
            SystemClock.sleep(700);waitForIdleSync();
            TouchControls controls=(TouchControls)field("controls");
            runOnMainSync(() -> controls.configure(1,.64f,false));
            w=controls.getWidth();h=controls.getHeight();unit=Math.min(w/960f,h/540f);w/=unit;h/=unit;
            Field surfaceField=Class.forName("org.libsdl.app.SDLActivity").getDeclaredField("mSurface");surfaceField.setAccessible(true);
            View surface=(View)surfaceField.get(null);
            check(surface.getWidth()==controls.getWidth() && surface.getHeight()==controls.getHeight(),"game surface fills screen");
            float x=151,y=h-117,ax=w-79,ay=h-91;
            down=SystemClock.uptimeMillis();event(MotionEvent.ACTION_DOWN,new float[]{x},new float[]{y},new int[]{0});
            check(held().contains(KeyEvent.KEYCODE_DPAD_RIGHT),"D-pad right");
            event(MotionEvent.ACTION_POINTER_DOWN | (1<<8),new float[]{x,ax},new float[]{y,ay},new int[]{0,1});
            check(held().contains(KeyEvent.KEYCODE_DPAD_RIGHT)&&held().contains(KeyEvent.KEYCODE_SPACE),"direction plus action with two fingers");
            event(MotionEvent.ACTION_MOVE,new float[]{101,ax},new float[]{y-50,ay},new int[]{0,1});
            check(held().contains(KeyEvent.KEYCODE_DPAD_UP)&&!held().contains(KeyEvent.KEYCODE_DPAD_RIGHT)&&held().contains(KeyEvent.KEYCODE_SPACE),"slide direction while action stays held");
            event(MotionEvent.ACTION_POINTER_UP,new float[]{101,ax},new float[]{y-50,ay},new int[]{0,1});
            check(!held().contains(KeyEvent.KEYCODE_DPAD_UP)&&held().contains(KeyEvent.KEYCODE_SPACE),"release one finger independently");
            event(MotionEvent.ACTION_CANCEL,new float[]{ax},new float[]{ay},new int[]{1});SystemClock.sleep(90);
            check(held().isEmpty(),"cancel releases every key");
            runOnMainSync(() -> controls.configure(1,.64f,true));
            down=SystemClock.uptimeMillis();event(MotionEvent.ACTION_DOWN,new float[]{w-151},new float[]{y},new int[]{0});
            check(held().contains(KeyEvent.KEYCODE_DPAD_LEFT),"left-handed D-pad");
            event(MotionEvent.ACTION_CANCEL,new float[]{w-151},new float[]{y},new int[]{0});
            runOnMainSync(() -> controls.configure(1,.64f,false));
            runOnMainSync(() -> activity.showOptions());SystemClock.sleep(100);
            check(((AlertDialog)field("options")).isShowing(),"settings dialog opens");
            runOnMainSync(() -> ((AlertDialog)getFieldUnchecked("options")).dismiss());SystemClock.sleep(100);
            runOnMainSync(() -> {
                KeyEvent key=new KeyEvent(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_BUTTON_X,0,0,-1,0,0,InputDevice.SOURCE_GAMEPAD);
                activity.dispatchKeyEvent(key);
            });
            Field mode=TouchControls.class.getDeclaredField("padMode");mode.setAccessible(true);
            check(mode.getBoolean(controls),"controller event switches to controller HUD");
            runOnMainSync(() -> controls.setPadMode(false));
            runOnMainSync(() -> activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_BUTTON_MODE)));
            check(((AlertDialog)field("options")).isShowing(),"controller Capture/Mode opens mobile menu");
            runOnMainSync(() -> ((AlertDialog)getFieldUnchecked("options")).dismiss());
            runOnMainSync(() -> activity.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_BUTTON_START)));
            check(((AlertDialog)field("options")).isShowing(),"controller START opens pause and save");
            runOnMainSync(() -> ((AlertDialog)getFieldUnchecked("options")).dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_BUTTON_START)));
            check(!((AlertDialog)field("options")).isShowing(),"controller START returns from pause");
            out.putString("stream",results.toString()+"HUD checks complete\n");finish(Activity.RESULT_OK,out);
        } catch(Throwable error) {out.putString("stream",results.toString()+"FAIL "+error.toString()+"\n");finish(Activity.RESULT_CANCELED,out);}
    }
    private Object getFieldUnchecked(String name) {try{return field(name);}catch(Exception e){throw new RuntimeException(e);}}
}
