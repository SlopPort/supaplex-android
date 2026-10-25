package pl.offline.supaplex;

import org.libsdl.app.SDLActivity;
import android.app.AlertDialog;
import android.os.*;
import android.content.*;
import android.hardware.input.InputManager;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import java.io.*;
import java.util.*;

public class GameActivity extends SDLActivity implements InputManager.InputDeviceListener {
    private final Handler keysHandler = new Handler(Looper.getMainLooper());
    private final HashSet<Integer> held = new HashSet<>();
    private final HashMap<Integer, Long> pressedAt = new HashMap<>();
    private final HashMap<Integer, Runnable> releases = new HashMap<>();
    private android.content.SharedPreferences prefs;
    private Context uiContext;
    private boolean rebuildingOptions;
    private InputManager inputs;
    private TouchControls controls;
    private boolean nativeReady;
    private AlertDialog options;
    private final java.util.concurrent.CountDownLatch startupChoice = new java.util.concurrent.CountDownLatch(1);
    private volatile boolean resumeOnLaunch;
    private boolean progressBusy;
    private boolean exitAfterSaving;
    public static native boolean nativeCanSaveProgress();
    public static native boolean nativeProgressCommand(int command);
    public static native void nativeSelectContinuation();
    private File progressFile() { return new File(getFilesDir(), "CONTINUE.SAV"); }
    String text(int resource) { return (uiContext==null?this:uiContext).getString(resource); }
    private void applyLanguage(String language) {
        android.content.res.Configuration config = new android.content.res.Configuration(getResources().getConfiguration());
        config.setLocale(new Locale(language.equals("pl")?"pl":"en"));
        uiContext=createConfigurationContext(config);
    }
    private void chooseLanguage(Runnable completed, boolean firstLaunch) {
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(24),dp(8),dp(24),dp(12));
        TextView note=new TextView(this);
        note.setText(firstLaunch?"Przyciski i okna Androida / Android buttons and dialogs\nMenu DOS: English":text(R.string.language_scope));body.addView(note);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Język / Language").setView(body).setCancelable(!firstLaunch).create();
        String[] codes={"pl","en"},names={"Polski","English"};
        for(int i=0;i<codes.length;i++) {
            final String code=codes[i];Button button=new Button(this);button.setAllCaps(false);button.setText(names[i]);body.addView(button);
            button.setOnClickListener(v -> {
                prefs.edit().putString("language",code).apply();applyLanguage(code);refreshControls();
                dialog.dismiss();completed.run();
            });
        }
        dialog.show();
    }
    private void rebuildOptions() {
        rebuildingOptions=true;
        if(options!=null) options.dismiss();
        options=null;showOptions();rebuildingOptions=false;
    }
    private void settingButton(LinearLayout body,String label,Runnable action) {
        Button button=new Button(this);button.setAllCaps(false);button.setText(label);body.addView(button);
        button.setOnClickListener(v -> action.run());
    }
    private void settingNote(LinearLayout body,String value) {
        TextView note=new TextView(this);note.setText(value);note.setTextSize(13);note.setPadding(0,dp(6),0,dp(10));body.addView(note);
    }
    private void showTouchSettings() {
        releaseAll();
        LinearLayout body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(dp(24),dp(8),dp(24),dp(12));
        settingNote(body,text(R.string.controls_intro));
        RadioGroup group=new RadioGroup(this);body.addView(group);
        int[] names={R.string.mode_buttons,R.string.mode_fixed,R.string.mode_floating,R.string.mode_swipe,R.string.mode_split};
        int[] help={R.string.help_buttons,R.string.help_fixed,R.string.help_floating,R.string.help_swipe,R.string.help_split};
        int selected=Math.max(0,Math.min(TouchControls.SPLIT,prefs.getInt("controlStyle",TouchControls.BUTTONS)));
        TextView description=new TextView(this);
        for(int i=0;i<names.length;i++) {
            RadioButton button=new RadioButton(this);button.setId(100+i);button.setText(text(names[i]));group.addView(button);
        }
        group.check(100+selected);description.setText(text(help[selected]));description.setPadding(0,dp(8),0,dp(12));body.addView(description);
        group.setOnCheckedChangeListener((g,id) -> {
            int style=id-100;if(style<0 || style>TouchControls.SPLIT)return;
            prefs.edit().putInt("controlStyle",style).apply();description.setText(text(help[style]));refreshControls();
        });
        slider(body,text(R.string.deadzone),prefs.getFloat("deadzone",.20f),.08f,.38f,"deadzone");
        settingNote(body,text(R.string.deadzone_help));
        slider(body,text(R.string.stability),prefs.getFloat("stability",.45f),0f,1f,"stability");
        settingNote(body,text(R.string.stability_help));
        slider(body,text(R.string.control_size),prefs.getFloat("size",1f),.75f,2.2f,"size");
        slider(body,text(R.string.action_size),prefs.getFloat("actionSize",1f),.75f,1.6f,"actionSize");
        slider(body,text(R.string.control_opacity),prefs.getFloat("opacity",.64f),.10f,1f,"opacity");
        settingNote(body,text(R.string.control_opacity_help));
        toggle(body,text(R.string.left_handed),"leftHanded",false);
        toggle(body,text(R.string.auto_pad),"autoPad",true);
        ScrollView scroll=new ScrollView(this);scroll.addView(body);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(text(R.string.controls)).setView(scroll)
            .setPositiveButton(text(R.string.done),null).create();
        settingButton(body,text(R.string.reset_controls),() -> new AlertDialog.Builder(this)
            .setTitle(text(R.string.reset_controls)).setMessage(text(R.string.reset_controls_message))
            .setNegativeButton(text(R.string.cancel),null).setPositiveButton(text(R.string.ok),(d,w) -> {
                prefs.edit().remove("controlStyle").remove("deadzone").remove("stability").remove("size").remove("actionSize").remove("opacity").remove("leftHanded").remove("autoPad").apply();
                refreshControls();dialog.dismiss();showTouchSettings();
            }).show());
        dialog.show();
    }
    @Override protected String[] getArguments() {
        try { startupChoice.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if (resumeOnLaunch) { nativeSelectContinuation(); return new String[]{"-v"}; }
        return new String[0];
    }
    private void chooseStartup() {
        if (!progressFile().isFile()) { startupChoice.countDown(); return; }
        new AlertDialog.Builder(this).setTitle(text(R.string.resume_title))
            .setMessage(text(R.string.resume_message))
            .setPositiveButton(text(R.string.resume), (d,w) -> { resumeOnLaunch=true; startupChoice.countDown(); })
            .setNegativeButton(text(R.string.game_menu), (d,w) -> startupChoice.countDown())
            .setOnCancelListener(d -> startupChoice.countDown()).show();
    }
    private void requestProgress(int command) {
        if (progressBusy) return;
        releaseAll();
        progressBusy = nativeProgressCommand(command);
        if (!progressBusy) Toast.makeText(this,text(R.string.progress_busy),Toast.LENGTH_SHORT).show();
    }
    public void onProgressResult(int result) {
        runOnUiThread(() -> {
            if (isFinishing()) return;
            progressBusy=false;
            if (result<0) {
                Toast.makeText(this,text(result==-3?R.string.resume_failed:R.string.save_failed),Toast.LENGTH_LONG).show();
                return;
            }
            if (result==2) { exitAfterSaving=true; if(options!=null)options.dismiss(); finish(); }
            else if(result==1) Toast.makeText(this,text(R.string.saved),Toast.LENGTH_LONG).show();
            else Toast.makeText(this,text(R.string.resumed),Toast.LENGTH_SHORT).show();
        });
    }
    private void progressButton(LinearLayout parent,String label,int command,boolean enabled) {
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setEnabled(enabled);
        parent.addView(b,new LinearLayout.LayoutParams(-1,dp(48)));
        b.setOnClickListener(v -> {
            if(command==3) {
                Runnable load=() -> {options.dismiss();nativeSetOverlayPaused(true);requestProgress(3);};
                if(nativeCanSaveProgress()) new AlertDialog.Builder(this).setTitle(text(R.string.load_title))
                    .setMessage(text(R.string.load_message))
                    .setPositiveButton(text(R.string.load),(d,w)->load.run()).setNegativeButton(text(R.string.cancel),null).show();
                else load.run();
            } else requestProgress(command);
        });
    }
    public static native void nativeSetAspect(boolean preserve);
    public static native void nativeSetOverlayPaused(boolean paused);
    @Override protected String[] getLibraries() { return new String[]{"SDL2", "SDL2_mixer", "main"}; }
    private void copyAssets(String source, File destination) throws IOException {
        String[] children = getAssets().list(source);
        if (children.length > 0) {
            destination.mkdirs();
            for (String child : children) copyAssets(source + "/" + child, new File(destination, child));
        } else if (!destination.exists()) {
            try (InputStream in = getAssets().open(source); OutputStream out = new FileOutputStream(destination)) {
                byte[] buffer = new byte[16384]; int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            }
        }
    }
    @Override protected void onCreate(Bundle state) {
        prefs = getSharedPreferences("touch-controls", MODE_PRIVATE);
        applyLanguage(prefs.getString("language", Locale.getDefault().getLanguage().equals("pl")?"pl":"en"));
        try { copyAssets("game", getFilesDir()); }
        catch (IOException e) { throw new IllegalStateException("Cannot install game data", e); }
        super.onCreate(state);
        if (mLayout == null) { startupChoice.countDown(); return; }
        nativeReady = true;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams p = getWindow().getAttributes();
            p.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(p);
        }
        mSurface.setLayoutParams(new android.widget.RelativeLayout.LayoutParams(-1, -1));
        controls = new TouchControls(this);
        mLayout.addView(controls, new android.widget.RelativeLayout.LayoutParams(-1, -1));
        refreshControls();
        nativeSetAspect(prefs.getBoolean("aspect", false));
        inputs = (InputManager)getSystemService(INPUT_SERVICE);
        inputs.registerInputDeviceListener(this, keysHandler);
        updatePadPresence();
        immersive();
        if (prefs.contains("language")) chooseStartup();
        else chooseLanguage(this::chooseStartup, true);
    }
    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
    @Override public void onWindowFocusChanged(boolean focus) {
        super.onWindowFocusChanged(focus);
        if (focus) immersive();
    }
    void setTouchKeys(Set<Integer> desired) {
        // Release the old direction before pressing a new one, so SDL never
        // briefly sees both axes held during a deliberate turn.
        for (int code : new HashSet<>(held)) {
            if (desired.contains(code) || releases.containsKey(code)) continue;
            long minimum = code == KeyEvent.KEYCODE_SPACE || code == KeyEvent.KEYCODE_ENTER ? 75 : 0;
            long delay = Math.max(0, minimum - (SystemClock.uptimeMillis() - pressedAt.get(code)));
            Runnable release = () -> {
                releases.remove(code); pressedAt.remove(code);
                if (held.remove(code)) onNativeKeyUp(code);
            };
            releases.put(code, release);
            if (delay == 0) release.run(); else keysHandler.postDelayed(release, delay);
        }
        for (int code : desired) {
            Runnable pending = releases.remove(code);
            if (pending != null) keysHandler.removeCallbacks(pending);
            if (held.add(code)) { pressedAt.put(code, SystemClock.uptimeMillis()); onNativeKeyDown(code); }
        }
    }
    private void releaseAll() {
        for (Runnable r : releases.values()) keysHandler.removeCallbacks(r);
        releases.clear(); pressedAt.clear();
        for (int code : held) onNativeKeyUp(code);
        held.clear();
        onNativeKeyUp(KeyEvent.KEYCODE_CTRL_LEFT);
        if (controls != null) controls.cancelTouches();
    }
    void pulse(int key, boolean ctrl) {
        if (ctrl) onNativeKeyDown(KeyEvent.KEYCODE_CTRL_LEFT);
        onNativeKeyDown(key);
        keysHandler.postDelayed(() -> {
            onNativeKeyUp(key);
            if (ctrl) onNativeKeyUp(KeyEvent.KEYCODE_CTRL_LEFT);
        }, 100);
    }
    void togglePause() { showOptions(); }
    void showOptions() {
        if (options != null && options.isShowing()) return;
        releaseAll(); nativeSetOverlayPaused(true);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL); body.setPadding(dp(24), dp(8), dp(24), dp(12));
        TextView hint = new TextView(this);
        hint.setText(text(R.string.control_hint));
        hint.setTextSize(13); hint.setTextColor(0xffa9bac4);
        LinearLayout actions = new LinearLayout(this);
        boolean canSave = nativeCanSaveProgress();
        progressButton(body, text(R.string.save_progress), 1, canSave);
        progressButton(body, text(R.string.save_exit), 2, canSave);
        String stamp = progressFile().isFile() ? " (" + new java.text.SimpleDateFormat("dd.MM HH:mm", new Locale(prefs.getString("language","en"))).format(new java.util.Date(progressFile().lastModified())) + ")" : "";
        progressButton(body, text(R.string.resume_saved)+stamp, 3, progressFile().isFile());
        settingButton(body, text(R.string.controls), this::showTouchSettings);
        settingButton(body, text(R.string.language), () -> chooseLanguage(this::rebuildOptions, false));
        body.addView(actions);
        optionAction(actions, text(R.string.back_original), KeyEvent.KEYCODE_ESCAPE, false);
        body.addView(hint);
        Button hide = new Button(this); hide.setText(text(R.string.hide_touch)); hide.setAllCaps(false);
        body.addView(hide); hide.setOnClickListener(v -> { controls.setPadMode(true); options.dismiss(); });
        LinearLayout answers = new LinearLayout(this); body.addView(answers);
        optionAction(answers, text(R.string.yes), KeyEvent.KEYCODE_Y, false);
        optionAction(answers, text(R.string.no), KeyEvent.KEYCODE_N, false);
        toggle(body, text(R.string.preserve_aspect), "aspect", false);
        ScrollView scroll = new ScrollView(this); scroll.addView(body);
        options = new AlertDialog.Builder(this).setTitle(text(R.string.pause_title)).setView(scroll)
            .setPositiveButton(text(R.string.return_game), null).create();
        options.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_START || keyCode == KeyEvent.KEYCODE_BUTTON_MODE || keyCode == KeyEvent.KEYCODE_MENU) {
                if (event.getAction() == KeyEvent.ACTION_UP) options.dismiss();
                return true;
            }
            return false;
        });
        options.setOnDismissListener(d -> { if(!rebuildingOptions) nativeSetOverlayPaused(false); immersive(); });
        options.show();
    }
    private void optionAction(LinearLayout parent, String label, int key, boolean ctrl) {
        Button b = new Button(this); b.setText(label); b.setTextSize(12); b.setAllCaps(false);
        parent.addView(b, new LinearLayout.LayoutParams(0, dp(48), 1));
        b.setOnClickListener(v -> { options.dismiss(); keysHandler.postDelayed(() -> pulse(key, ctrl), 60); });
    }
    private void slider(LinearLayout parent, String label, float value, float min, float max, String name) {
        TextView title = new TextView(this); title.setText(label+" · "+Math.round(value*100)+"%"); title.setTextSize(14);
        title.setPadding(0,dp(8),0,0); parent.addView(title);
        SeekBar bar = new SeekBar(this); bar.setMax(100); bar.setProgress(Math.round((value-min)*100/(max-min)));
        parent.addView(bar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean user) {
                if (!user) return;
                float value=min+p*(max-min)/100;
                title.setText(label+" · "+Math.round(value*100)+"%");
                prefs.edit().putFloat(name,value).apply(); refreshControls();
            }
            public void onStartTrackingTouch(SeekBar b) {}
            public void onStopTrackingTouch(SeekBar b) {}
        });
    }
    private void toggle(LinearLayout parent, String label, String name, boolean def) {
        Switch sw = new Switch(this); sw.setText(label); sw.setTextSize(14);
        sw.setPadding(0, dp(8), 0, dp(8)); sw.setChecked(prefs.getBoolean(name, def)); parent.addView(sw);
        sw.setOnCheckedChangeListener((b, checked) -> {
            prefs.edit().putBoolean(name, checked).apply(); refreshControls();
            if (name.equals("aspect")) nativeSetAspect(checked);
            if (name.equals("autoPad") && !checked) controls.setPadMode(false);
        });
    }
    private void refreshControls() {
        releaseAll();
        controls.configure(prefs.getFloat("size",1f), prefs.getFloat("opacity",.64f),prefs.getBoolean("leftHanded",false));
        controls.configureAction(prefs.getFloat("actionSize",1f));
        controls.configureInput(prefs.getInt("controlStyle",TouchControls.BUTTONS),prefs.getFloat("deadzone",.20f),prefs.getFloat("stability",.45f));
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    public String requestPlayerName(int maxLength) {
        final java.util.concurrent.CountDownLatch wait = new java.util.concurrent.CountDownLatch(1);
        final String[] result = {null};
        runOnUiThread(() -> {
            EditText input = new EditText(this); input.setSingleLine(true);
            input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(maxLength)});
            new AlertDialog.Builder(this).setTitle(text(R.string.player_name)).setView(input)
                .setPositiveButton(text(R.string.ok), (d,w) -> { result[0]=input.getText().toString(); wait.countDown(); })
                .setNegativeButton(text(R.string.cancel), (d,w) -> wait.countDown()).setOnCancelListener(d -> wait.countDown()).show();
            input.requestFocus();
            input.postDelayed(() -> ((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE))
                .showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT),200);
        });
        try { wait.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return result[0];
    }
    private void padUsed() {
        if (controls != null && prefs.getBoolean("autoPad", true)) {
            releaseAll(); controls.setPadMode(true);
        }
    }
    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        int source = event.getSource();
        if (nativeReady && (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_MODE || event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_START || event.getKeyCode() == KeyEvent.KEYCODE_MENU)) {
            if (event.getAction() == KeyEvent.ACTION_UP) showOptions();
            return true;
        }
        if ((source & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
            || (source & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) padUsed();
        return super.dispatchKeyEvent(event);
    }
    @Override public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) padUsed();
        return super.dispatchGenericMotionEvent(event);
    }
    private void updatePadPresence() {
        if (controls == null) return;
        boolean present = false;
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice device = InputDevice.getDevice(id);
            if (device != null && ((device.getSources() & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD
                || (device.getSources() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) present = true;
        }
        controls.setPadConnected(present);
        if (!present) controls.setPadMode(false);
    }
    @Override public void onInputDeviceAdded(int id) { updatePadPresence(); }
    @Override public void onInputDeviceChanged(int id) { updatePadPresence(); }
    @Override public void onInputDeviceRemoved(int id) { updatePadPresence(); }
    @Override protected void onPause() {
        if (nativeReady) releaseAll();
        super.onPause();
    }
    @Override protected void onDestroy() {
        startupChoice.countDown();
        if (inputs != null) inputs.unregisterInputDeviceListener(this);
        if (nativeReady) { nativeSetOverlayPaused(false); releaseAll(); }
        keysHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
        // The DOS-derived engine has process-lifetime globals. After an explicit
        // save-and-exit, start with a fresh native runtime on the next launch.
        if (exitAfterSaving && isFinishing()) android.os.Process.killProcess(android.os.Process.myPid());
    }
}
