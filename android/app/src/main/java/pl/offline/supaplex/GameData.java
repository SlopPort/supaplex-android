package pl.offline.supaplex;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/** Imports only runtime resources, never executables, settings or saved games. */
final class GameData {
    static final String MARKER="user-data-v1";
    static final Map<String,Integer> REQUIRED=new LinkedHashMap<>();
    static {
        String[] names={"BACK.DAT","CHARS6.DAT","CHARS8.DAT","CONTROLS.DAT","FIXED.DAT","GFX.DAT","MENU.DAT","MOVING.DAT","PALETTES.DAT","PANEL.DAT","TITLE.DAT","TITLE1.DAT","TITLE2.DAT","LEVEL.LST","LEVELS.DAT"};
        int[] sizes={32000,512,512,32000,5120,32000,32000,73920,256,3840,32000,32000,32000,3108,170496};
        for(int i=0;i<names.length;i++)REQUIRED.put(names[i],sizes[i]);
    }
    static boolean ready(File root) {return new File(root,MARKER).isFile() && missing(root).isEmpty();}
    static String missing(File root) {
        ArrayList<String> missing=new ArrayList<>();
        for(Map.Entry<String,Integer> e:REQUIRED.entrySet()) {
            long n=new File(root,e.getKey()).length();
            if(n<e.getValue() || n>e.getValue()+1)missing.add(e.getKey());
        }
        StringBuilder result=new StringBuilder();for(String name:missing) {if(result.length()>0)result.append(", ");result.append(name);}return result.toString();
    }
    static String target(String path) {
        String[] parts=path.replace('\\','/').split("/");
        for(String p:parts)if(p.equals(".."))return null;
        if(parts.length==0)return null;
        String base=parts[parts.length-1].toUpperCase(Locale.ROOT);
        if(REQUIRED.containsKey(base) || base.matches("DEMO[0-9]\\.BIN") || base.matches("LEVEL\\.L[0-9]{2}") || base.matches("LEVELS\\.D[0-9]{2}"))return base;
        if(parts.length>1 && parts[parts.length-2].equalsIgnoreCase("audio") && base.matches("[A-Z0-9_-]+\\.(WAV|XM)"))return "audio/"+base.toLowerCase(Locale.ROOT);
        return null;
    }
    static void copy(InputStream in,File stage,String name) throws IOException {
        File out=new File(stage,name);
        if(out.exists())throw new IOException("Duplicate resource: "+name);
        File parent=out.getParentFile();if(!parent.isDirectory() && !parent.mkdirs())throw new IOException("Cannot create import folder");
        long total=0;
        try(OutputStream stream=new FileOutputStream(out)) {
            byte[] bytes=new byte[16384];int n;
            while((n=in.read(bytes))!=-1) {total+=n;if(total>8*1024*1024)throw new IOException("Resource too large: "+name);stream.write(bytes,0,n);}
        }
    }
    static void zip(InputStream input,File stage) throws IOException {
        long total=0;int entries=0;
        try(ZipInputStream zip=new ZipInputStream(input)) {
            ZipEntry entry;
            while((entry=zip.getNextEntry())!=null) {
                if(++entries>4096)throw new IOException("Too many archive entries");
                String name=target(entry.getName());
                if(name!=null && !entry.isDirectory()) {copy(zip,stage,name);total+=new File(stage,name).length();}
                else {byte[] discard=new byte[16384];int n;while((n=zip.read(discard))!=-1) {total+=n;if(total>32*1024*1024)throw new IOException("Archive too large");}}
                if(total>32*1024*1024)throw new IOException("Archive too large");
                zip.closeEntry();
            }
        }
    }
    static void install(File stage,File root) throws IOException {
        String missing=missing(stage);if(!missing.isEmpty())throw new IOException("Missing or invalid: "+missing);
        // Startup remains blocked until every resource has been installed. Saves are never selected.
        new File(root,MARKER).delete();
        installFiles(stage,root);
        try(FileOutputStream out=new FileOutputStream(new File(root,MARKER))) {out.write(1);out.getFD().sync();}
    }
    private static void installFiles(File stage,File root) throws IOException {
        if(!root.isDirectory() && !root.mkdirs())throw new IOException("Cannot create data folder");
        File[] files=stage.listFiles();if(files==null)throw new IOException("Cannot read import folder");
        for(File file:files) {
            File dest=new File(root,file.getName());
            if(file.isDirectory())installFiles(file,dest);
            else {
                File temp=new File(root,file.getName()+".import");
                try(InputStream in=new FileInputStream(file);FileOutputStream out=new FileOutputStream(temp)) {
                    byte[] bytes=new byte[16384];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);out.getFD().sync();
                }
                if(!temp.renameTo(dest))throw new IOException("Cannot install: "+file.getName());
            }
        }
    }
    static void clear(File file) throws IOException {
        if(file.isDirectory()) {File[] children=file.listFiles();if(children==null)throw new IOException("Cannot clean import folder");for(File c:children)clear(c);}
        if(file.exists() && !file.delete())throw new IOException("Cannot clean import file");
    }
}
