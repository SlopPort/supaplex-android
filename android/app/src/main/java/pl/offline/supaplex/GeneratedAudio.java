package pl.offline.supaplex;

import java.io.*;

/** Original procedural effects by Codex (LLM), GPL-3.0. No game samples or melodies. */
final class GeneratedAudio {
    static final int RATE=22050;
    static final String[] EFFECTS={"explosion","infotron","push","fall","bug","base","exit"};
    static final String[] SUFFIXES={"beep","sample","adlib","blaster","roland"};
    private static final double[] LENGTH={.32,.14,.12,.16,.18,.07,.38};
    static byte[] wave(int effect) {
        int frames=(int)(RATE*LENGTH[effect]),length=frames*2;
        byte[] wav=new byte[44+length];
        tag(wav,0,"RIFF");le(wav,4,36+length,4);tag(wav,8,"WAVE");tag(wav,12,"fmt ");le(wav,16,16,4);
        le(wav,20,1,2);le(wav,22,1,2);le(wav,24,RATE,4);le(wav,28,RATE*2,4);le(wav,32,2,2);le(wav,34,16,2);
        tag(wav,36,"data");le(wav,40,length,4);
        double phase=0;int noise=0x614ea9;
        for(int i=0;i<frames;i++) {
            double t=i/(double)RATE,u=i/(double)(frames-1),hz,value;
            noise=noise*1664525+1013904223;
            double random=(noise>>>8)/8388607.5-1;
            switch(effect) {
                case 0: hz=105-65*u;break;
                case 1: hz=u<.45?760:1140;break;
                case 2: hz=160-65*u;break;
                case 3: hz=500-340*u;break;
                case 4: hz=430+180*Math.sin(t*95);break;
                case 5: hz=690-230*u;break;
                default: hz=u<.33?520:u<.66?650:870;break;
            }
            phase+=2*Math.PI*hz/RATE;
            value=Math.sin(phase)*.8+Math.sin(phase*2)*.2;
            if(effect==0)value=value*.30+random*.70;
            else if(effect==2 || effect==5)value=value*.75+random*.25;
            double envelope=Math.min(1,t/.004)*Math.pow(1-u,effect==6?.8:1.7);
            int sample=(int)(value*envelope*10500);
            le(wav,44+2*i,sample,2);
        }
        return wav;
    }
    private static void tag(byte[] bytes,int offset,String value) {for(int i=0;i<value.length();i++)bytes[offset+i]=(byte)value.charAt(i);}
    private static void le(byte[] bytes,int offset,int value,int count) {for(int i=0;i<count;i++)bytes[offset+i]=(byte)(value>>>(8*i));}
    static void ensure(File root) throws IOException {
        File folder=new File(root,"audio");if(!folder.isDirectory() && !folder.mkdirs())throw new IOException("Cannot create audio folder");
        for(int effect=0;effect<EFFECTS.length;effect++) {
            byte[] wav=null;
            for(String suffix:SUFFIXES) {
                File file=new File(folder,EFFECTS[effect]+"-"+suffix+".wav");
                if(file.exists())continue; // User-supplied compatible recordings have priority.
                if(wav==null)wav=wave(effect);
                File temp=new File(folder,file.getName()+".generated");
                try(FileOutputStream out=new FileOutputStream(temp)) {out.write(wav);out.getFD().sync();}
                if(!temp.renameTo(file))throw new IOException("Cannot install generated effect");
            }
        }
    }
}
