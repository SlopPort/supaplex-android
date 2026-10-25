package pl.offline.supaplex;

import android.graphics.*;
import android.view.*;
import java.util.*;

/** Lightweight vector HUD; no images, services, or rendering loop. GPL-3.0. */
public final class TouchControls extends View {
    static final int BUTTONS=0,FIXED=1,FLOATING=2,SWIPE=3,SPLIT=4;
    private final GameActivity game;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final Typeface font = Typeface.create("sans-serif-medium",Typeface.NORMAL);
    private final HashMap<Integer, Finger> fingers = new HashMap<>();
    private final int aqua = 0xff8bffe0;
    private float unit=1, vw=960, vh=540, size=1, opacity=.64f;
    private float safeLeft, safeRight, safeBottom;
    private boolean leftHanded, padMode, padConnected;
    private float dx,dy,dr,ax,ay,ar,cx,cy,cr,splitX,secondaryActionX;
    private int style=BUTTONS,movementOwner=-1,verticalOwner=-1,horizontalOwner=-1,lastDirection;
    private long movementSequence;
    private float deadzone=.20f,stability=.45f;
    private static class Finger {
        String target; float x,y,originX,originY; int direction;long sequence;
        Finger(String t,float x,float y) { target=t;this.x=x;this.y=y;originX=x;originY=y; }
    }
    public TouchControls(GameActivity activity) {
        super(activity); game=activity;
        setContentDescription(game.text(R.string.hud_description));
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    void configure(float scale,float alpha,boolean left) { cancelTouches();size=scale;opacity=alpha;leftHanded=left;layoutControls();setContentDescription(game.text(R.string.hud_description));invalidate(); }
    void configureInput(int mode,float zone,float directionStability) {
        cancelTouches();style=Math.max(BUTTONS,Math.min(SPLIT,mode));deadzone=Math.max(.08f,Math.min(.38f,zone));
        stability=Math.max(0f,Math.min(1f,directionStability));layoutControls();invalidate();
    }
    void setPadMode(boolean value) { if(padMode!=value) {padMode=value;cancelTouches();invalidate();} }
    void setPadConnected(boolean value) { padConnected=value;invalidate(); }
    void cancelTouches() { fingers.clear();movementOwner=verticalOwner=horizontalOwner=-1;lastDirection=0;game.setTouchKeys(Collections.emptySet());invalidate(); }
    @Override public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if(android.os.Build.VERSION.SDK_INT>=28 && insets.getDisplayCutout()!=null) {
            safeLeft=insets.getDisplayCutout().getSafeInsetLeft();
            safeRight=insets.getDisplayCutout().getSafeInsetRight();
            safeBottom=insets.getDisplayCutout().getSafeInsetBottom();
        } else {safeLeft=safeRight=safeBottom=0;}
        layoutControls(); return insets;
    }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh) {
        cancelTouches();
        unit=Math.min(w/960f,h/540f); vw=w/unit;vh=h/unit;layoutControls();
    }
    private void layoutControls() {
        dr=77*size;ar=49*size;cr=27*size;
        dx=safeLeft/unit+24+dr;dy=vh-safeBottom/unit-40-dr;
        ax=vw-safeRight/unit-30-ar;ay=vh-safeBottom/unit-42-ar;
        cx=vw-safeRight/unit-30-cr;cy=ay-ar-cr-23*size;
        splitX=vw-safeRight/unit-24-dr;secondaryActionX=safeLeft/unit+30+ar;
        if(style==SPLIT) {ay=dy-dr*.65f-ar-36*size;cx=ax-ar-cr-24*size;cy=ay;}
        if(leftHanded) {dx=vw-dx;ax=vw-ax;cx=vw-cx;splitX=vw-splitX;secondaryActionX=vw-secondaryActionX;}
    }
    private void color(int c,float alpha) {
        paint.setColor(c); paint.setAlpha(Math.round(255*alpha*opacity));
    }
    private void circle(Canvas c,float x,float y,float r,boolean active,boolean primary) {
        paint.setStyle(Paint.Style.FILL);
        color(0xff05151e,active?.95f:.8f); c.drawCircle(x,y,r,paint);
        if(active) { color(aqua,.12f);c.drawCircle(x,y,r+5,paint); }
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(active?2:1.1f);
        color(primary||active?aqua:0xffcce4ed,active?1:.65f);c.drawCircle(x,y,r-1,paint);
        if(primary) { paint.setStrokeWidth(1);color(aqua,.22f);c.drawCircle(x,y,r-6,paint); }
        paint.setStyle(Paint.Style.FILL);
    }
    private void text(Canvas c,String value,float x,float y,float font,int col,float alpha) {
        paint.setTypeface(this.font);
        paint.setTextSize(font); paint.setTextAlign(Paint.Align.CENTER); color(col,alpha);c.drawText(value,x,y,paint);
    }
    private boolean active(String target) { for(Finger f:fingers.values())if(f.target.equals(target))return true;return false; }
    private void line(Canvas c,float x1,float y1,float x2,float y2) { c.drawLine(x1,y1,x2,y2,paint); }
    private void arrow(Canvas c,float x,float y,float angle,boolean lit) {
        c.save();c.translate(x,y);c.rotate(angle);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.6f*size);paint.setStrokeCap(Paint.Cap.ROUND);paint.setStrokeJoin(Paint.Join.ROUND);
        color(lit?aqua:0xffe2edf4,lit?1:.85f);
        Path p=path;p.reset();p.moveTo(-7*size,3*size);p.lineTo(0,-4*size);p.lineTo(7*size,3*size);c.drawPath(p,paint);
        paint.setStyle(Paint.Style.FILL);c.restore();
    }
    @Override protected void onDraw(Canvas original) {
        super.onDraw(original);
        Canvas c=original;c.save();c.scale(unit,unit);
        float l=safeLeft/unit,r=vw-safeRight/unit;
        circle(c,l+32,32,21,active("menu"),false);
        color(0xffeefaff,1);for(int i=-1;i<=1;i++)c.drawCircle(l+32+i*6,32,1.7f,paint);
        circle(c,r-32,32,21,active("pause"),false);
        color(0xffeefaff,1);c.drawRoundRect(r-38,25,r-34,39,1,1,paint);c.drawRoundRect(r-30,25,r-26,39,1,1,paint);
        if(padMode) {
            color(0xff05151e,.9f);c.drawRoundRect(l+62,13,l+161,51,19,19,paint);
            text(c,game.text(R.string.enable_touch),l+111,36,9,aqua,1);
        } else {
            text(c,padConnected?"SUPAPLEX  /  PAD":"SUPAPLEX",l+112,36,10,0xffe1eef4,.85f);
            int direction=0;float baseX=dx,baseY=dy;
            Finger movement=null;
            for(Finger f:fingers.values())if(f.target.equals("dpad")) {
                movement=f;direction=f.direction;
                if(style==FLOATING || style==SWIPE) {baseX=f.originX;baseY=f.originY;}
            }
            if(style==SPLIT) {
                direction=lastDirection;
                drawDirectionButton(c,dx,dy-dr*.65f,0,direction==KeyEvent.KEYCODE_DPAD_UP);
                drawDirectionButton(c,dx,dy+dr*.65f,180,direction==KeyEvent.KEYCODE_DPAD_DOWN);
                drawDirectionButton(c,splitX-dr*.65f,dy,270,direction==KeyEvent.KEYCODE_DPAD_LEFT);
                drawDirectionButton(c,splitX+dr*.65f,dy,90,direction==KeyEvent.KEYCODE_DPAD_RIGHT);
            } else if(style==BUTTONS) {
                drawDirectionButton(c,dx,dy-dr*.65f,0,direction==KeyEvent.KEYCODE_DPAD_UP);
                drawDirectionButton(c,dx+dr*.65f,dy,90,direction==KeyEvent.KEYCODE_DPAD_RIGHT);
                drawDirectionButton(c,dx,dy+dr*.65f,180,direction==KeyEvent.KEYCODE_DPAD_DOWN);
                drawDirectionButton(c,dx-dr*.65f,dy,270,direction==KeyEvent.KEYCODE_DPAD_LEFT);
            } else {
                circle(c,baseX,baseY,dr,active("dpad"),false);
                paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1);color(aqua,.24f);c.drawCircle(baseX,baseY,dr*deadzone,paint);paint.setStyle(Paint.Style.FILL);
                arrow(c,baseX,baseY-dr*.65f,0,direction==KeyEvent.KEYCODE_DPAD_UP);
                arrow(c,baseX+dr*.65f,baseY,90,direction==KeyEvent.KEYCODE_DPAD_RIGHT);
                arrow(c,baseX,baseY+dr*.65f,180,direction==KeyEvent.KEYCODE_DPAD_DOWN);
                arrow(c,baseX-dr*.65f,baseY,270,direction==KeyEvent.KEYCODE_DPAD_LEFT);
                float x=movement==null?0:movement.x-baseX,y=movement==null?0:movement.y-baseY;
                if(style==SWIPE && direction!=0) {
                    x=direction==KeyEvent.KEYCODE_DPAD_LEFT?-dr:direction==KeyEvent.KEYCODE_DPAD_RIGHT?dr:0;
                    y=direction==KeyEvent.KEYCODE_DPAD_UP?-dr:direction==KeyEvent.KEYCODE_DPAD_DOWN?dr:0;
                }
                float length=(float)Math.hypot(x,y);
                if(length>dr*.36f){x*=dr*.36f/length;y*=dr*.36f/length;}
                color(active("dpad")?aqua:0xffa9c7d4,active("dpad")?.4f:.16f);c.drawCircle(baseX+x,baseY+y,dr*.29f,paint);
                paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1);color(0xffd2e9f4,.3f);c.drawCircle(baseX+x,baseY+y,dr*.29f,paint);paint.setStyle(Paint.Style.FILL);
            }
            drawAction(c,ax,ay);
            if(style==SPLIT)drawAction(c,secondaryActionX,ay);
            circle(c,cx,cy,cr,active("confirm"),false);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.5f*size);paint.setStrokeCap(Paint.Cap.ROUND);color(0xffe6f5ff,1);
            Path check=path;check.reset();check.moveTo(cx-8*size,cy);check.lineTo(cx-2*size,cy+6*size);check.lineTo(cx+9*size,cy-7*size);c.drawPath(check,paint);paint.setStyle(Paint.Style.FILL);
        }
        c.restore();
    }
    private boolean inside(float x,float y,float cx,float cy,float radius) {return Math.hypot(x-cx,y-cy)<=radius;}
    private void drawAction(Canvas c,float x,float y) {
        circle(c,x,y,ar,active("action"),true);
        c.save();c.translate(x,y-4*size);c.scale(size,size);color(aqua,1);
        Path bolt=path;bolt.reset();bolt.moveTo(3,-19);bolt.lineTo(-12,3);bolt.lineTo(-2,3);bolt.lineTo(-6,18);bolt.lineTo(13,-6);bolt.lineTo(3,-6);bolt.close();c.drawPath(bolt,paint);c.restore();
        text(c,game.text(R.string.action),x,y+25*size,8.5f*size,aqua,1);
    }
    private int splitButtonAt(float x,float y,boolean vertical) {
        float offset=dr*.65f,half=dr*.28f;
        if(vertical && Math.abs(x-dx)<=half) {
            if(Math.abs(y-(dy-offset))<=half)return KeyEvent.KEYCODE_DPAD_UP;
            if(Math.abs(y-(dy+offset))<=half)return KeyEvent.KEYCODE_DPAD_DOWN;
        } else if(!vertical && Math.abs(y-dy)<=half) {
            if(Math.abs(x-(splitX-offset))<=half)return KeyEvent.KEYCODE_DPAD_LEFT;
            if(Math.abs(x-(splitX+offset))<=half)return KeyEvent.KEYCODE_DPAD_RIGHT;
        }
        return 0;
    }
    private void drawDirectionButton(Canvas c,float x,float y,float angle,boolean pressed) {
        float half=dr*.28f;
        color(pressed?aqua:0xff05151e,pressed?.35f:.88f);c.drawRoundRect(x-half,y-half,x+half,y+half,8*size,8*size,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(pressed?2:1);color(pressed?aqua:0xffcce4ed,pressed?1:.65f);
        c.drawRoundRect(x-half,y-half,x+half,y+half,8*size,8*size,paint);paint.setStyle(Paint.Style.FILL);arrow(c,x,y,angle,pressed);
    }
    private int buttonAt(float x,float y) {
        float offset=dr*.65f,half=dr*.28f;
        if(Math.abs(x-dx)<=half && Math.abs(y-(dy-offset))<=half)return KeyEvent.KEYCODE_DPAD_UP;
        if(Math.abs(x-(dx+offset))<=half && Math.abs(y-dy)<=half)return KeyEvent.KEYCODE_DPAD_RIGHT;
        if(Math.abs(x-dx)<=half && Math.abs(y-(dy+offset))<=half)return KeyEvent.KEYCODE_DPAD_DOWN;
        if(Math.abs(x-(dx-offset))<=half && Math.abs(y-dy)<=half)return KeyEvent.KEYCODE_DPAD_LEFT;
        return 0;
    }
    private int joystickDirection(Finger f) {
        if(style==SWIPE && f.direction!=0)return f.direction;
        float x=f.x-f.originX,y=f.y-f.originY;
        if(Math.hypot(x,y)<=dr*deadzone)return 0;
        boolean horizontal=Math.abs(x)>Math.abs(y);
        boolean wasHorizontal=f.direction==KeyEvent.KEYCODE_DPAD_LEFT || f.direction==KeyEvent.KEYCODE_DPAD_RIGHT;
        if(style!=SWIPE && f.direction!=0 && horizontal!=wasHorizontal) {
            float previousAxis=wasHorizontal?Math.abs(x):Math.abs(y),otherAxis=wasHorizontal?Math.abs(y):Math.abs(x);
            if(otherAxis<previousAxis*(1+stability*.9f)+dr*stability*.10f)horizontal=wasHorizontal;
        }
        return horizontal?(x<0?KeyEvent.KEYCODE_DPAD_LEFT:KeyEvent.KEYCODE_DPAD_RIGHT):(y<0?KeyEvent.KEYCODE_DPAD_UP:KeyEvent.KEYCODE_DPAD_DOWN);
    }
    private String hit(float x,float y) {
        if(inside(x,y,safeLeft/unit+32,32,25))return "menu";
        if(inside(x,y,vw-safeRight/unit-32,32,25))return "pause";
        if(padMode) return y<57 && x>safeLeft/unit+60 && x<safeLeft/unit+165?"restore":null;
        if(inside(x,y,ax,ay,ar+9))return "action";
        if(style==SPLIT && inside(x,y,secondaryActionX,ay,ar+9))return "action";
        if(inside(x,y,cx,cy,cr+8))return "confirm";
        if(style==SPLIT) {
            if(inside(x,y,dx,dy,dr+9))return "splitVertical";
            if(inside(x,y,splitX,dy,dr+9))return "splitHorizontal";
            return null;
        }
        if(inside(x,y,dx,dy,dr+9))return "dpad";
        if(style==FLOATING && GameActivity.nativeCanSaveProgress() && y>vh*.50f && y<vh-safeBottom/unit-12
            && (leftHanded?x>vw*.57f:x<vw*.43f))return "dpad";
        return null;
    }
    private void sendKeys() {
        Set<Integer> keys=new HashSet<>();
        int splitDirection=0;long newest=-1;
        for(Finger f:fingers.values()) {
            if(f.target.equals("dpad")) {
                f.direction=style==BUTTONS?buttonAt(f.x,f.y):joystickDirection(f);
                if(f.direction!=0)keys.add(f.direction);
            } else if(f.target.equals("splitVertical") || f.target.equals("splitHorizontal")) {
                int previous=f.direction;f.direction=splitButtonAt(f.x,f.y,f.target.equals("splitVertical"));
                if(f.direction!=0 && f.direction!=previous)f.sequence=++movementSequence;
                if(f.direction!=0 && f.sequence>newest) {newest=f.sequence;splitDirection=f.direction;}
            } else if(f.target.equals("action") && (inside(f.x,f.y,ax,ay,ar*1.6f)
                || style==SPLIT && inside(f.x,f.y,secondaryActionX,ay,ar*1.6f)))keys.add(KeyEvent.KEYCODE_SPACE);
            else if(f.target.equals("confirm") && inside(f.x,f.y,cx,cy,cr*1.7f))keys.add(KeyEvent.KEYCODE_ENTER);
        }
        if(style==SPLIT) {lastDirection=splitDirection;if(splitDirection!=0)keys.add(splitDirection);}
        game.setTouchKeys(keys);invalidate();
    }
    @Override public boolean performClick() { super.performClick(); game.showOptions(); return true; }
    @Override public boolean onTouchEvent(MotionEvent event) {
        int action=event.getActionMasked(),index=event.getActionIndex(),id=event.getPointerId(index);
        float x=event.getX(index)/unit,y=event.getY(index)/unit;
        if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_POINTER_DOWN) {
            String target=hit(x,y);
            if(action==MotionEvent.ACTION_DOWN && target==null)return false;
            if("dpad".equals(target) && movementOwner!=-1)target=null;
            if("splitVertical".equals(target) && verticalOwner!=-1)target=null;
            if("splitHorizontal".equals(target) && horizontalOwner!=-1)target=null;
            if(target!=null) {
                Finger finger=new Finger(target,x,y);
                if(target.equals("dpad")) {
                    movementOwner=id;
                    if(style==BUTTONS || style==FIXED) {finger.originX=dx;finger.originY=dy;}
                }
                if(target.equals("splitVertical"))verticalOwner=id;
                if(target.equals("splitHorizontal"))horizontalOwner=id;
                fingers.put(id,finger);
            }
        } else if(action==MotionEvent.ACTION_MOVE) {
            for(int i=0;i<event.getPointerCount();i++) {Finger f=fingers.get(event.getPointerId(i));if(f!=null){f.x=event.getX(i)/unit;f.y=event.getY(i)/unit;}}
        } else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_POINTER_UP) {
            Finger f=fingers.remove(id);
            if(id==movementOwner)movementOwner=-1;
            if(id==verticalOwner)verticalOwner=-1;
            if(id==horizontalOwner)horizontalOwner=-1;
            if(f!=null && f.target.equals(hit(x,y))) {
                if(f.target.equals("menu"))game.showOptions();
                else if(f.target.equals("pause"))game.togglePause();
                else if(f.target.equals("restore"))setPadMode(false);
            }
        } else if(action==MotionEvent.ACTION_CANCEL) {fingers.clear();movementOwner=verticalOwner=horizontalOwner=-1;}
        sendKeys();return true;
    }
}
