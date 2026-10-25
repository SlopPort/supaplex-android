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
    private boolean importOnly;
    @Override public void onCreate(Bundle args) { importOnly=args!=null && "true".equals(args.getString("importOnly"));super.onCreate(args);start(); }
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
    private void single(int action,float x,float y) { event(action,new float[]{x},new float[]{y},new int[]{0}); }
    private void mode(TouchControls controls,int style,float stability) {
        runOnMainSync(() -> controls.configureInput(style,.20f,stability));
    }
    private void checkDirection(int key,String name) throws Exception {
        HashSet<Integer> keys=held();
        boolean valid=key==0?keys.isEmpty():keys.size()==1 && keys.contains(key);
        if(!valid)throw new AssertionError(name+"; held="+keys);
        check(true,name);
    }
    private boolean clickVisible(String text) {
        android.view.accessibility.AccessibilityNodeInfo root=getUiAutomation().getRootInActiveWindow();
        if(root==null)return false;
        for(android.view.accessibility.AccessibilityNodeInfo node:root.findAccessibilityNodeInfosByText(text))
            if(node.isClickable() && node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK))return true;
        return false;
    }
    private float geometry(TouchControls controls,String name) throws Exception {
        Field f=TouchControls.class.getDeclaredField(name);f.setAccessible(true);return f.getFloat(controls);
    }
    private void sizingChecks(TouchControls controls) throws Exception {
        float directionRadius=geometry(controls,"dr"),actionRadius=geometry(controls,"ar");
        runOnMainSync(() -> controls.configure(2.2f,.1f,false));
        check(geometry(controls,"dr")>directionRadius && geometry(controls,"ar")==actionRadius,"larger directions leave ACTION size unchanged");
        directionRadius=geometry(controls,"dr");
        runOnMainSync(() -> controls.configureAction(.75f));float smallAction=geometry(controls,"ar");
        runOnMainSync(() -> controls.configureAction(1.6f));
        check(geometry(controls,"ar")>smallAction && geometry(controls,"dr")==directionRadius,"ACTION slider leaves direction size unchanged");
        runOnMainSync(() -> controls.configureAction(.75f));
        for(int style=TouchControls.BUTTONS;style<=TouchControls.DUAL;style++) {
            mode(controls,style,.45f);float x=geometry(controls,"ax"),y=geometry(controls,"ay");
            down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,x,y);
            check(held().contains(KeyEvent.KEYCODE_SPACE),"small ACTION works with 220 percent directions and low opacity in style "+style);
            single(MotionEvent.ACTION_CANCEL,x,y);SystemClock.sleep(90);
            checkDirection(0,"small ACTION releases in style "+style);
        }
        runOnMainSync(() -> {controls.configure(1,.64f,false);controls.configureAction(1);});
        mode(controls,TouchControls.BUTTONS,.45f);
    }
    private void dualChecks(TouchControls controls) throws Exception {
        mode(controls,TouchControls.DUAL,.45f);
        runOnMainSync(() -> controls.configurePlacement(0,1,1));
        float left=geometry(controls,"dx"),right=geometry(controls,"splitX"),y=geometry(controls,"dy");
        float sx=geometry(controls,"stepX"),sy=geometry(controls,"stepY");
        int[] directions={KeyEvent.KEYCODE_DPAD_UP,KeyEvent.KEYCODE_DPAD_RIGHT,KeyEvent.KEYCODE_DPAD_DOWN,KeyEvent.KEYCODE_DPAD_LEFT};
        for(float centre:new float[]{left,right})for(int i=0;i<4;i++) {
            float x=centre+(i==1?sx:i==3?-sx:0),cy=y+(i==0?-sy:i==2?sy:0);
            down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,x,cy);
            checkDirection(directions[i],"dual cross direction "+i+" at "+(centre==left?"left":"right"));
            single(MotionEvent.ACTION_CANCEL,x,cy);
        }
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,left,y-sy);
        event(MotionEvent.ACTION_POINTER_DOWN | (1<<8),new float[]{left,right+sx},new float[]{y-sy,y},new int[]{0,1});
        checkDirection(KeyEvent.KEYCODE_DPAD_RIGHT,"dual latest direction wins across thumbs");
        event(MotionEvent.ACTION_POINTER_UP | (1<<8),new float[]{left,right+sx},new float[]{y-sy,y},new int[]{0,1});
        checkDirection(KeyEvent.KEYCODE_DPAD_UP,"dual releasing latest returns to other thumb");
        single(MotionEvent.ACTION_CANCEL,left,y-sy);
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,right,y);
        checkDirection(0,"dual centre is neutral");single(MotionEvent.ACTION_CANCEL,right,y);
        for(boolean mirror:new boolean[]{false,true})for(float scale:new float[]{.75f,2.2f})for(float spacing:new float[]{.9f,1.8f}) {
            runOnMainSync(() -> {controls.configure(scale,.64f,mirror);controls.configurePlacement(1,spacing,spacing);});
            float a=geometry(controls,"dx"),b=geometry(controls,"splitX"),rx=geometry(controls,"reachX"),ry=geometry(controls,"reachY"),cy=geometry(controls,"dy");
            check(Math.min(a,b)-rx>=0 && Math.max(a,b)+rx<=w && Math.abs(a-b)>=2*rx && cy-ry>=64 && cy+ry<=h,
                "dual bounds and nonoverlap: mirror="+mirror+" size="+scale+" spacing="+spacing);
            float step=geometry(controls,"stepX"),half=geometry(controls,"dr")*.28f;
            check(step>2*half,"dual arrow buttons do not overlap at limits");
        }
        runOnMainSync(() -> {controls.configure(1,.64f,false);controls.configurePlacement(0,1,1);});
        float before=geometry(controls,"dx"),size=geometry(controls,"dr"),step=geometry(controls,"stepX");
        runOnMainSync(() -> controls.configurePlacement(1,1.8f,1.8f));
        check(geometry(controls,"dx")>before && geometry(controls,"stepX")>step && geometry(controls,"dr")==size,"placement and spacing move buttons without resizing");
        runOnMainSync(() -> controls.configurePlacement(0,1,1));
        mode(controls,TouchControls.BUTTONS,.45f);
    }
    private void newInputChecks(TouchControls controls,float y) throws Exception {
        mode(controls,TouchControls.BUTTONS,.6f);
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,101,y);checkDirection(0,"button centre is neutral");
        single(MotionEvent.ACTION_MOVE,136,y-35);checkDirection(0,"button diagonal gap is neutral");
        single(MotionEvent.ACTION_MOVE,101,y-50);checkDirection(KeyEvent.KEYCODE_DPAD_UP,"separate up button");
        single(MotionEvent.ACTION_CANCEL,101,y-50);
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,151,y);
        event(MotionEvent.ACTION_POINTER_DOWN | (1<<8),new float[]{151,101},new float[]{y,y-50},new int[]{0,1});
        checkDirection(KeyEvent.KEYCODE_DPAD_RIGHT,"second movement finger cannot override direction");
        event(MotionEvent.ACTION_POINTER_UP,new float[]{151,101},new float[]{y,y-50},new int[]{0,1});
        checkDirection(0,"movement owner release does not transfer to ignored finger");single(MotionEvent.ACTION_CANCEL,101,y);
        mode(controls,TouchControls.FIXED,.6f);
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,101,y);single(MotionEvent.ACTION_MOVE,111,y);
        checkDirection(0,"fixed joystick dead zone");
        single(MotionEvent.ACTION_MOVE,151,y);checkDirection(KeyEvent.KEYCODE_DPAD_RIGHT,"fixed joystick right");
        single(MotionEvent.ACTION_MOVE,151,y-53);checkDirection(KeyEvent.KEYCODE_DPAD_RIGHT,"diagonal jitter keeps previous axis");
        single(MotionEvent.ACTION_MOVE,116,y-60);checkDirection(KeyEvent.KEYCODE_DPAD_UP,"deliberate turn changes axis");
        single(MotionEvent.ACTION_MOVE,101,y);checkDirection(0,"centre stops joystick immediately");
        single(MotionEvent.ACTION_MOVE,51,y);checkDirection(KeyEvent.KEYCODE_DPAD_LEFT,"joystick reverses direction");
        single(MotionEvent.ACTION_UP,51,y);checkDirection(0,"lifting stops joystick immediately");
        mode(controls,TouchControls.FIXED,0);
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,151,y);single(MotionEvent.ACTION_MOVE,151,y-53);
        checkDirection(KeyEvent.KEYCODE_DPAD_UP,"zero stability allows immediate axis change");single(MotionEvent.ACTION_CANCEL,151,y-53);
        mode(controls,TouchControls.FLOATING,.6f);
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,151,y);checkDirection(0,"floating joystick anchors under initial touch");
        single(MotionEvent.ACTION_MOVE,191,y);checkDirection(KeyEvent.KEYCODE_DPAD_RIGHT,"floating joystick uses touch-relative motion");
        single(MotionEvent.ACTION_MOVE,151,y);checkDirection(0,"floating joystick returns to its own centre");single(MotionEvent.ACTION_CANCEL,151,y);
        mode(controls,TouchControls.SWIPE,.6f);
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,101,y);single(MotionEvent.ACTION_MOVE,151,y);
        checkDirection(KeyEvent.KEYCODE_DPAD_RIGHT,"swipe chooses direction");
        single(MotionEvent.ACTION_MOVE,101,y-60);checkDirection(KeyEvent.KEYCODE_DPAD_RIGHT,"swipe locks direction against thumb drift");
        single(MotionEvent.ACTION_MOVE,101,y);checkDirection(KeyEvent.KEYCODE_DPAD_RIGHT,"swipe hold remains locked at origin");
        single(MotionEvent.ACTION_UP,101,y);checkDirection(0,"swipe release stops input");
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,101,y);single(MotionEvent.ACTION_MOVE,101,y-50);
        checkDirection(KeyEvent.KEYCODE_DPAD_UP,"next swipe can choose another direction");single(MotionEvent.ACTION_CANCEL,101,y-50);
        mode(controls,TouchControls.SPLIT,.45f);
        float splitAction=geometry(controls,"ax"),splitActionLeft=geometry(controls,"secondaryActionX"),splitActionY=geometry(controls,"ay");
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,101,y);checkDirection(0,"split vertical centre is neutral");
        single(MotionEvent.ACTION_MOVE,101,y-50);checkDirection(KeyEvent.KEYCODE_DPAD_UP,"split left side controls up");
        event(MotionEvent.ACTION_POINTER_DOWN | (1<<8),new float[]{101,w-51},new float[]{y-50,y},new int[]{0,1});
        checkDirection(KeyEvent.KEYCODE_DPAD_RIGHT,"split most recent axis wins without a diagonal");
        event(MotionEvent.ACTION_POINTER_UP | (1<<8),new float[]{101,w-51},new float[]{y-50,y},new int[]{0,1});
        checkDirection(KeyEvent.KEYCODE_DPAD_UP,"split release returns to other held direction");
        event(MotionEvent.ACTION_POINTER_DOWN | (1<<8),new float[]{101,splitAction},new float[]{y-50,splitActionY},new int[]{0,1});
        check(held().contains(KeyEvent.KEYCODE_DPAD_UP)&&held().contains(KeyEvent.KEYCODE_SPACE),"split vertical plus right-side action with two fingers");
        event(MotionEvent.ACTION_CANCEL,new float[]{101,splitAction},new float[]{y-50,splitActionY},new int[]{0,1});SystemClock.sleep(90);
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,splitActionLeft,splitActionY);
        event(MotionEvent.ACTION_POINTER_DOWN | (1<<8),new float[]{splitActionLeft,w-151},new float[]{splitActionY,y},new int[]{0,1});
        check(held().contains(KeyEvent.KEYCODE_DPAD_LEFT)&&held().contains(KeyEvent.KEYCODE_SPACE),"split horizontal plus left-side action with two fingers");
        event(MotionEvent.ACTION_CANCEL,new float[]{splitActionLeft,w-151},new float[]{splitActionY,y},new int[]{0,1});SystemClock.sleep(90);checkDirection(0,"split cancel releases direction and action");
        runOnMainSync(() -> controls.configure(1,.64f,true));
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,w-101,y-50);
        checkDirection(KeyEvent.KEYCODE_DPAD_UP,"left-handed split mirrors vertical side");
        single(MotionEvent.ACTION_MOVE,w-101,y+50);checkDirection(KeyEvent.KEYCODE_DPAD_DOWN,"split vertical side controls down");
        single(MotionEvent.ACTION_CANCEL,w-101,y+50);
        down=SystemClock.uptimeMillis();single(MotionEvent.ACTION_DOWN,51,y);checkDirection(KeyEvent.KEYCODE_DPAD_LEFT,"left-handed split mirrors horizontal side");
        single(MotionEvent.ACTION_CANCEL,51,y);
        runOnMainSync(() -> controls.configure(1,.64f,false));
        mode(controls,TouchControls.BUTTONS,.45f);
        Method language=GameActivity.class.getDeclaredMethod("applyLanguage",String.class);language.setAccessible(true);
        runOnMainSync(() -> {try{language.invoke(activity,"en");}catch(Exception e){throw new RuntimeException(e);}});
        check(activity.text(R.string.save_exit).equals("Save and quit"),"English UI resource override");
        runOnMainSync(() -> {try{language.invoke(activity,"pl");}catch(Exception e){throw new RuntimeException(e);}});
        check(activity.text(R.string.save_exit).equals("Zapisz i zakończ"),"Polish UI resource override");
        String chosen=((android.content.SharedPreferences)field("prefs")).getString("language","en");
        runOnMainSync(() -> {try{language.invoke(activity,chosen);}catch(Exception e){throw new RuntimeException(e);}});
    }
    @Override public void onStart() {
        if(importOnly) {ImportInstrumentation.run(this);return;}
        Bundle out=new Bundle();
        int resultCode=Activity.RESULT_OK;
        android.content.SharedPreferences settings=getTargetContext().getSharedPreferences("touch-controls",Context.MODE_PRIVATE);
        Map<String,?> originalSettings=new HashMap<>(settings.getAll());
        try {
            Intent intent=new Intent(getTargetContext(),GameActivity.class);intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity=(GameActivity)startActivitySync(intent);waitForIdleSync();SystemClock.sleep(700);
            for(int attempt=0;attempt<30;attempt++) {
                clickVisible("English");
                if(clickVisible("GAME MENU") || clickVisible("MENU GRY"))break;
                SystemClock.sleep(100);
            }
            SystemClock.sleep(700);waitForIdleSync();
            TouchControls controls=(TouchControls)field("controls");
            runOnMainSync(() -> controls.configure(1,.64f,false));
            runOnMainSync(() -> controls.configureAction(1));
            runOnMainSync(() -> controls.configurePlacement(0,1,1));
            mode(controls,TouchControls.BUTTONS,.45f);
            w=controls.getWidth();h=controls.getHeight();unit=Math.min(w/960f,h/540f);w/=unit;h/=unit;
            Field surfaceField=Class.forName("org.libsdl.app.SDLActivity").getDeclaredField("mSurface");surfaceField.setAccessible(true);
            View surface=(View)surfaceField.get(null);
            check(surface.getWidth()==controls.getWidth() && surface.getHeight()==controls.getHeight(),"game surface fills screen");
            float x=151,y=h-117,ax=geometry(controls,"ax"),ay=geometry(controls,"ay");
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
            newInputChecks(controls,y);
            sizingChecks(controls);
            dualChecks(controls);
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
            out.putString("stream",results.toString()+"HUD checks complete\n");
        } catch(Throwable error) {out.putString("stream",results.toString()+"FAIL "+error.toString()+"\n");resultCode=Activity.RESULT_CANCELED;}
        finally {
            android.content.SharedPreferences.Editor editor=settings.edit().clear();
            for(Map.Entry<String,?> item:originalSettings.entrySet()) {
                Object value=item.getValue();String key=item.getKey();
                if(value instanceof String)editor.putString(key,(String)value);
                else if(value instanceof Float)editor.putFloat(key,(Float)value);
                else if(value instanceof Boolean)editor.putBoolean(key,(Boolean)value);
                else if(value instanceof Integer)editor.putInt(key,(Integer)value);
                else if(value instanceof Long)editor.putLong(key,(Long)value);
            }
            editor.commit();
        }
        finish(resultCode,out);
    }
    private Object getFieldUnchecked(String name) {try{return field(name);}catch(Exception e){throw new RuntimeException(e);}}
}
