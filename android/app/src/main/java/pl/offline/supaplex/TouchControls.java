package pl.offline.supaplex;

import android.graphics.*;
import android.view.*;
import java.util.*;

/** Lightweight vector HUD; no images, services, or rendering loop. GPL-3.0. */
public final class TouchControls extends View {
    private final GameActivity game;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final Typeface font = Typeface.create("sans-serif-medium",Typeface.NORMAL);
    private final HashMap<Integer, Finger> fingers = new HashMap<>();
    private final int aqua = 0xff8bffe0;
    private float unit=1, vw=960, vh=540, size=1, opacity=.64f;
    private float safeLeft, safeRight, safeBottom;
    private boolean leftHanded, padMode, padConnected;
    private float dx,dy,dr,ax,ay,ar,cx,cy,cr;
    private static class Finger {
        String target; float x,y; int direction;
        Finger(String t,float x,float y) { target=t;this.x=x;this.y=y; }
    }
    public TouchControls(GameActivity activity) {
        super(activity); game=activity;
        setContentDescription("Sterowanie Supaplex: krzy\u017cak po lewej, akcja i zatwierdzanie po prawej. Menu u g\u00f3ry po lewej, pauza po prawej.");
        setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
    }
    void configure(float scale,float alpha,boolean left) { size=scale;opacity=alpha;leftHanded=left; layoutControls();invalidate(); }
    void setPadMode(boolean value) { if(padMode!=value) {padMode=value;cancelTouches();invalidate();} }
    void setPadConnected(boolean value) { padConnected=value;invalidate(); }
    void cancelTouches() { fingers.clear();invalidate(); }
    @Override public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if(android.os.Build.VERSION.SDK_INT>=28 && insets.getDisplayCutout()!=null) {
            safeLeft=insets.getDisplayCutout().getSafeInsetLeft();
            safeRight=insets.getDisplayCutout().getSafeInsetRight();
            safeBottom=insets.getDisplayCutout().getSafeInsetBottom();
        } else {safeLeft=safeRight=safeBottom=0;}
        layoutControls(); return insets;
    }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh) {
        unit=Math.min(w/960f,h/540f); vw=w/unit;vh=h/unit;layoutControls();
    }
    private void layoutControls() {
        dr=77*size;ar=49*size;cr=27*size;
        dx=safeLeft/unit+24+dr;dy=vh-safeBottom/unit-40-dr;
        ax=vw-safeRight/unit-30-ar;ay=vh-safeBottom/unit-42-ar;
        cx=vw-safeRight/unit-30-cr;cy=ay-ar-cr-23*size;
        if(leftHanded) {dx=vw-dx;ax=vw-ax;cx=vw-cx;}
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
            text(c,"W\u0141\u0104CZ DOTYK",l+111,36,9,aqua,1);
        } else {
            text(c,padConnected?"SUPAPLEX  /  PAD":"SUPAPLEX",l+112,36,10,0xffe1eef4,.85f);
            int direction=0;float knobX=dx,knobY=dy;
            for(Finger f:fingers.values())if(f.target.equals("dpad")) {
                direction=f.direction;float x=f.x-dx,y=f.y-dy;float length=(float)Math.hypot(x,y);
                if(length>dr*.36f){x*=dr*.36f/length;y*=dr*.36f/length;}knobX+=x;knobY+=y;
            }
            circle(c,dx,dy,dr,active("dpad"),false);
            color(0xffeefaff,.06f);c.drawCircle(dx,dy,dr*.58f,paint);
            arrow(c,dx,dy-dr*.65f,0,direction==KeyEvent.KEYCODE_DPAD_UP);
            arrow(c,dx+dr*.65f,dy,90,direction==KeyEvent.KEYCODE_DPAD_RIGHT);
            arrow(c,dx,dy+dr*.65f,180,direction==KeyEvent.KEYCODE_DPAD_DOWN);
            arrow(c,dx-dr*.65f,dy,270,direction==KeyEvent.KEYCODE_DPAD_LEFT);
            color(active("dpad")?aqua:0xffa9c7d4,active("dpad")?.4f:.16f);
            c.drawCircle(knobX,knobY,dr*.29f,paint);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(1);color(0xffd2e9f4,.3f);c.drawCircle(knobX,knobY,dr*.29f,paint);paint.setStyle(Paint.Style.FILL);
            circle(c,ax,ay,ar,active("action"),true);
            c.save();c.translate(ax,ay-4*size);c.scale(size,size);color(aqua,1);
            Path bolt=path;bolt.reset();bolt.moveTo(3,-19);bolt.lineTo(-12,3);bolt.lineTo(-2,3);bolt.lineTo(-6,18);bolt.lineTo(13,-6);bolt.lineTo(3,-6);bolt.close();c.drawPath(bolt,paint);c.restore();
            text(c,"AKCJA",ax,ay+25*size,8.5f*size,aqua,1);
            circle(c,cx,cy,cr,active("confirm"),false);
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.5f*size);paint.setStrokeCap(Paint.Cap.ROUND);color(0xffe6f5ff,1);
            Path check=path;check.reset();check.moveTo(cx-8*size,cy);check.lineTo(cx-2*size,cy+6*size);check.lineTo(cx+9*size,cy-7*size);c.drawPath(check,paint);paint.setStyle(Paint.Style.FILL);
        }
        c.restore();
    }
    private boolean inside(float x,float y,float cx,float cy,float radius) {return Math.hypot(x-cx,y-cy)<=radius;}
    private String hit(float x,float y) {
        if(inside(x,y,safeLeft/unit+32,32,25))return "menu";
        if(inside(x,y,vw-safeRight/unit-32,32,25))return "pause";
        if(padMode) return y<57 && x>safeLeft/unit+60 && x<safeLeft/unit+165?"restore":null;
        if(inside(x,y,dx,dy,dr+9))return "dpad";
        if(inside(x,y,ax,ay,ar+9))return "action";
        if(inside(x,y,cx,cy,cr+8))return "confirm";
        return null;
    }
    private void sendKeys() {
        Set<Integer> keys=new HashSet<>();
        for(Finger f:fingers.values()) {
            f.direction=0;
            if(f.target.equals("dpad")) {
                float x=f.x-dx,y=f.y-dy;
                if(Math.hypot(x,y)>dr*.17f) {
                    f.direction=Math.abs(x)>Math.abs(y)?(x<0?KeyEvent.KEYCODE_DPAD_LEFT:KeyEvent.KEYCODE_DPAD_RIGHT)
                        :(y<0?KeyEvent.KEYCODE_DPAD_UP:KeyEvent.KEYCODE_DPAD_DOWN);
                    keys.add(f.direction);
                }
            } else if(f.target.equals("action") && inside(f.x,f.y,ax,ay,ar*1.6f))keys.add(KeyEvent.KEYCODE_SPACE);
            else if(f.target.equals("confirm") && inside(f.x,f.y,cx,cy,cr*1.7f))keys.add(KeyEvent.KEYCODE_ENTER);
        }
        game.setTouchKeys(keys);invalidate();
    }
    @Override public boolean performClick() { super.performClick(); game.showOptions(); return true; }
    @Override public boolean onTouchEvent(MotionEvent event) {
        int action=event.getActionMasked(),index=event.getActionIndex(),id=event.getPointerId(index);
        float x=event.getX(index)/unit,y=event.getY(index)/unit;
        if(action==MotionEvent.ACTION_DOWN || action==MotionEvent.ACTION_POINTER_DOWN) {
            String target=hit(x,y);
            if(action==MotionEvent.ACTION_DOWN && target==null)return false;
            if(target!=null)fingers.put(id,new Finger(target,x,y));
        } else if(action==MotionEvent.ACTION_MOVE) {
            for(int i=0;i<event.getPointerCount();i++) {Finger f=fingers.get(event.getPointerId(i));if(f!=null){f.x=event.getX(i)/unit;f.y=event.getY(i)/unit;}}
        } else if(action==MotionEvent.ACTION_UP || action==MotionEvent.ACTION_POINTER_UP) {
            Finger f=fingers.remove(id);
            if(f!=null && f.target.equals(hit(x,y))) {
                if(f.target.equals("menu"))game.showOptions();
                else if(f.target.equals("pause"))game.togglePause();
                else if(f.target.equals("restore"))setPadMode(false);
            }
        } else if(action==MotionEvent.ACTION_CANCEL)fingers.clear();
        sendKeys();return true;
    }
}
