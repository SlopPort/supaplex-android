package pl.offline.supaplex;

import android.app.Instrumentation;
import android.os.Bundle;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/** Generated zero-filled fixtures: no original game resources in the test APK. */
public class ImportInstrumentation {
    private StringBuilder results=new StringBuilder();
    private void check(boolean ok,String label) {if(!ok)throw new AssertionError(label);results.append("PASS ").append(label).append('\n');}
    private byte[] zip(boolean duplicate,boolean incomplete) throws IOException {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        try(ZipOutputStream zip=new ZipOutputStream(bytes)) {
            for(Map.Entry<String,Integer> e:GameData.REQUIRED.entrySet()) {
                if(incomplete && e.getKey().equals("GFX.DAT"))continue;
                zip.putNextEntry(new ZipEntry("dos/"+e.getKey().toLowerCase(Locale.ROOT)));zip.write(new byte[e.getValue()]);zip.closeEntry();
            }
            zip.putNextEntry(new ZipEntry("dos/CONTINUE.SAV"));zip.write(99);zip.closeEntry();
            zip.putNextEntry(new ZipEntry("../escape.DAT"));zip.write(99);zip.closeEntry();
            if(duplicate){zip.putNextEntry(new ZipEntry("other/GFX.DAT"));zip.write(new byte[32000]);zip.closeEntry();}
        }
        return bytes.toByteArray();
    }
    static void run(Instrumentation runner) {new ImportInstrumentation().runChecks(runner);}
    private void runChecks(Instrumentation runner) {
        Bundle bundle=new Bundle();File base=new File(runner.getTargetContext().getCacheDir(),"import-tests");
        try {
            GameData.clear(base);base.mkdirs();File stage=new File(base,"stage"),dest=new File(base,"installed");stage.mkdirs();dest.mkdirs();
            check(!GameData.ready(dest),"empty installation cannot start");
            check(GameData.target("../GFX.DAT")==null,"parent traversal rejected");
            check(GameData.target("PLAYER.LST")==null && GameData.target("CONTINUE.SAV")==null && GameData.target("SUPAPLEX.CFG")==null,"player data never imported");
            check(GameData.target("SUPAPLEX.EXE")==null,"executables never imported");
            check("audio/music-adlib.xm".equals(GameData.target("dos/Audio/MUSIC-ADLIB.XM")),"optional audio recognized");
            GameData.zip(new ByteArrayInputStream(zip(false,true)),stage);
            boolean failed=false;try{GameData.install(stage,dest);}catch(IOException expected){failed=true;}
            check(failed && !GameData.ready(dest),"incomplete data rejected before installation");
            GameData.clear(stage);stage.mkdirs();
            failed=false;try{GameData.zip(new ByteArrayInputStream(zip(true,false)),stage);}catch(IOException expected){failed=true;}
            check(failed,"duplicate resources rejected");
            GameData.clear(stage);stage.mkdirs();
            GameData.zip(new ByteArrayInputStream(zip(false,false)),stage);
            check(GameData.missing(stage).isEmpty(),"nested lowercase ZIP normalized and validated");
            check(!new File(stage,"CONTINUE.SAV").exists() && !new File(base,"escape.DAT").exists(),"ZIP excludes saves and traversal");
            try(FileOutputStream out=new FileOutputStream(new File(dest,"CONTINUE.SAV"))){out.write(42);}
            GameData.install(stage,dest);
            check(GameData.ready(dest),"complete import committed with startup marker");
            try(FileInputStream in=new FileInputStream(new File(dest,"CONTINUE.SAV"))){check(in.read()==42,"existing save preserved");}
            new File(dest,"GFX.DAT").delete();check(!GameData.ready(dest),"missing installed data blocks startup again");
            File big=new File(base,"big");big.mkdirs();
            failed=false;try{GameData.copy(new ByteArrayInputStream(new byte[8*1024*1024+1]),big,"GFX.DAT");}catch(IOException expected){failed=true;}
            check(failed,"oversized resource rejected");
            File audioRoot=new File(base,"synth");audioRoot.mkdirs();
            java.util.HashSet<Integer> hashes=new java.util.HashSet<>();
            for(int effect=0;effect<GeneratedAudio.EFFECTS.length;effect++) {
                byte[] wav=GeneratedAudio.wave(effect);int peak=0;long energy=0;
                for(int i=44;i<wav.length;i+=2) {int sample=(short)((wav[i]&255)|((wav[i+1]&255)<<8));peak=Math.max(peak,Math.abs(sample));energy+=(long)sample*sample;}
                hashes.add(java.util.Arrays.hashCode(wav));
                check(wav[0]=='R' && wav[8]=='W' && wav.length>2000 && peak>1000 && peak<=10500 && energy>0 && wav[wav.length-1]==0 && wav[wav.length-2]==0,"valid bounded procedural effect: "+GeneratedAudio.EFFECTS[effect]);
            }
            check(hashes.size()==7,"seven distinct generated effects");
            File own=new File(audioRoot,"audio/infotron-beep.wav");own.getParentFile().mkdirs();
            try(FileOutputStream out=new FileOutputStream(own)){out.write(42);}
            GeneratedAudio.ensure(audioRoot);
            try(FileInputStream in=new FileInputStream(own)){check(own.length()==1 && in.read()==42,"user-supplied audio is never replaced");}
            check(new File(audioRoot,"audio").list().length==35,"fallback files cover all existing sound modes");
            android.media.MediaPlayer player=new android.media.MediaPlayer();
            try {player.setDataSource(new File(audioRoot,"audio/exit-beep.wav").getPath());player.prepare();check(player.getDuration()>=370 && player.getDuration()<=390,"Android decoder accepts generated WAV");}finally{player.release();}
            check(!new File(audioRoot,"audio/music-beep.xm").exists(),"no original music or melody generated");
            bundle.putString("stream",results.toString());runner.finish(-1,bundle);
        }catch(Throwable e){bundle.putString("stream",results+"FAIL "+e);runner.finish(0,bundle);}
        finally {try{GameData.clear(base);}catch(IOException ignored){}}
    }
}
