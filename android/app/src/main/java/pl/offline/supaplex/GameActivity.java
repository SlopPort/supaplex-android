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
    @Override protected String[] getArguments() {
        try { startupChoice.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if (resumeOnLaunch) { nativeSelectContinuation(); return new String[]{"-v"}; }
        return new String[0];
    }
    private void chooseStartup() {
        if (!progressFile().isFile()) { startupChoice.countDown(); return; }
        new AlertDialog.Builder(this).setTitle("Wznowić zapisany poziom?")
            .setMessage("Wrócisz do miejsca, w którym zapisano grę, ze stanem planszy, zebranymi przedmiotami i czasem.")
            .setPositiveButton("Wznów", (d,w) -> { resumeOnLaunch=true; startupChoice.countDown(); })
            .setNegativeButton("Menu gry", (d,w) -> startupChoice.countDown())
            .setOnCancelListener(d -> startupChoice.countDown()).show();
    }
    private void requestProgress(int command) {
        if (progressBusy) return;
        releaseAll();
        progressBusy = nativeProgressCommand(command);
        if (!progressBusy) Toast.makeText(this,"Poczekaj na zakończenie poprzedniej operacji.",Toast.LENGTH_SHORT).show();
    }
    public void onProgressResult(int result) {
        runOnUiThread(() -> {
            if (isFinishing()) return;
            progressBusy=false;
            if (result<0) {
                Toast.makeText(this,"Nie udało się " + (result==-3?"wznowić poziomu.":"zapisać postępu. Poprzedni zapis pozostał bez zmian."),Toast.LENGTH_LONG).show();
                return;
            }
            if (result==2) { exitAfterSaving=true; if(options!=null)options.dismiss(); finish(); }
            else if(result==1) Toast.makeText(this,"Postęp zapisany. Możesz bezpiecznie zamknąć grę.",Toast.LENGTH_LONG).show();
            else Toast.makeText(this,"Wznowiono zapisany poziom.",Toast.LENGTH_SHORT).show();
        });
    }
    private void progressButton(LinearLayout parent,String label,int command,boolean enabled) {
        Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setEnabled(enabled);
        parent.addView(b,new LinearLayout.LayoutParams(-1,dp(48)));
        b.setOnClickListener(v -> {
            if(command==3) {
                Runnable load=() -> {options.dismiss();nativeSetOverlayPaused(true);requestProgress(3);};
                if(nativeCanSaveProgress()) new AlertDialog.Builder(this).setTitle("Wczytać zapis?")
                    .setMessage("Niezapisane zmiany bieżącego poziomu zostaną zastąpione.")
                    .setPositiveButton("Wczytaj",(d,w)->load.run()).setNegativeButton("Anuluj",null).show();
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
        try { copyAssets("game", getFilesDir()); }
        catch (IOException e) { throw new IllegalStateException("Cannot install game data", e); }
        super.onCreate(state);
        if (mLayout == null) { startupChoice.countDown(); return; }
        nativeReady = true;
        prefs = getSharedPreferences("touch-controls", MODE_PRIVATE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 28) {
            WindowManager.LayoutParams p = getWindow().getAttributes();
            p.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(p);
        }
        mSurface.setLayoutParams(new android.widget.RelativeLayout.LayoutParams(-1, -1));
        controls = new TouchControls(this);
        mLayout.addView(controls, new android.widget.RelativeLayout.LayoutParams(-1, -1));
        controls.configure(prefs.getFloat("size", 1f), prefs.getFloat("opacity", .64f), prefs.getBoolean("leftHanded", false));
        nativeSetAspect(prefs.getBoolean("aspect", false));
        inputs = (InputManager)getSystemService(INPUT_SERVICE);
        inputs.registerInputDeviceListener(this, keysHandler);
        updatePadPresence();
        immersive();
        chooseStartup();
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
        for (int code : desired) {
            Runnable pending = releases.remove(code);
            if (pending != null) keysHandler.removeCallbacks(pending);
            if (held.add(code)) { pressedAt.put(code, SystemClock.uptimeMillis()); onNativeKeyDown(code); }
        }
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
        hint.setText("Kierunki + AKCJA: zbieranie obok Murphy\u2019ego. Przytrzymaj AKCJ\u0118, aby po\u0142o\u017cy\u0107 czerwony dysk.\nPad: krzy\u017cak / lewa ga\u0142ka \u00b7 X: akcja \u00b7 A: OK \u00b7 B: wstecz \u00b7 START: pauza.");
        hint.setTextSize(13); hint.setTextColor(0xffa9bac4);
        LinearLayout actions = new LinearLayout(this);
        boolean canSave = nativeCanSaveProgress();
        progressButton(body, "Zapisz postęp poziomu", 1, canSave);
        progressButton(body, "Zapisz i zakończ", 2, canSave);
        String stamp = progressFile().isFile() ? " (" + new java.text.SimpleDateFormat("dd.MM HH:mm", java.util.Locale.getDefault()).format(new java.util.Date(progressFile().lastModified())) + ")" : "";
        progressButton(body, "Wznów zapisany poziom"+stamp, 3, progressFile().isFile());
        body.addView(actions);
        optionAction(actions, "Wstecz", KeyEvent.KEYCODE_ESCAPE, false);
        body.addView(hint);
        Button hide = new Button(this); hide.setText("Ukryj przyciski dotykowe"); hide.setAllCaps(false);
        body.addView(hide); hide.setOnClickListener(v -> { controls.setPadMode(true); options.dismiss(); });
        LinearLayout answers = new LinearLayout(this); body.addView(answers);
        optionAction(answers, "Tak / Y", KeyEvent.KEYCODE_Y, false);
        optionAction(answers, "Nie / N", KeyEvent.KEYCODE_N, false);
        slider(body, "Rozmiar sterowania", prefs.getFloat("size", 1f), .75f, 1.4f, "size");
        slider(body, "Widoczno\u015b\u0107 przycisk\u00f3w", prefs.getFloat("opacity", .64f), .25f, .9f, "opacity");
        toggle(body, "Uk\u0142ad dla lewor\u0119cznych", "leftHanded", false);
        toggle(body, "Zachowaj proporcje obrazu", "aspect", false);
        toggle(body, "Ukrywaj dotyk po u\u017cyciu pada", "autoPad", true);
        ScrollView scroll = new ScrollView(this); scroll.addView(body);
        options = new AlertDialog.Builder(this).setTitle("Pauza i zapis gry").setView(scroll)
            .setPositiveButton("Wr\u00f3\u0107 do gry", null).create();
        options.setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_BUTTON_START || keyCode == KeyEvent.KEYCODE_BUTTON_MODE || keyCode == KeyEvent.KEYCODE_MENU) {
                if (event.getAction() == KeyEvent.ACTION_UP) options.dismiss();
                return true;
            }
            return false;
        });
        options.setOnDismissListener(d -> { nativeSetOverlayPaused(false); immersive(); });
        options.show();
    }
    private void optionAction(LinearLayout parent, String label, int key, boolean ctrl) {
        Button b = new Button(this); b.setText(label); b.setTextSize(12); b.setAllCaps(false);
        parent.addView(b, new LinearLayout.LayoutParams(0, dp(48), 1));
        b.setOnClickListener(v -> { options.dismiss(); keysHandler.postDelayed(() -> pulse(key, ctrl), 60); });
    }
    private void slider(LinearLayout parent, String label, float value, float min, float max, String name) {
        TextView title = new TextView(this); title.setText(label); title.setTextSize(14);
        title.setPadding(0,dp(8),0,0); parent.addView(title);
        SeekBar bar = new SeekBar(this); bar.setMax(100); bar.setProgress(Math.round((value-min)*100/(max-min)));
        parent.addView(bar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar b, int p, boolean user) {
                if (!user) return;
                prefs.edit().putFloat(name, min + p * (max-min)/100).apply(); refreshControls();
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
        controls.configure(prefs.getFloat("size",1f), prefs.getFloat("opacity",.64f),prefs.getBoolean("leftHanded",false));
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    public String requestPlayerName(int maxLength) {
        final java.util.concurrent.CountDownLatch wait = new java.util.concurrent.CountDownLatch(1);
        final String[] result = {null};
        runOnUiThread(() -> {
            EditText input = new EditText(this); input.setSingleLine(true);
            input.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(maxLength)});
            new AlertDialog.Builder(this).setTitle("Imi\u0119 gracza").setView(input)
                .setPositiveButton("OK", (d,w) -> { result[0]=input.getText().toString(); wait.countDown(); })
                .setNegativeButton("Anuluj", (d,w) -> wait.countDown()).setOnCancelListener(d -> wait.countDown()).show();
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
