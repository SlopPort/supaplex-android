package pl.offline.supaplex;

import java.io.*;

/** Generator code: GPL-3.0. New composition data and rendered audio: CC0-1.0.
 * Composed by Codex (LLM); no original Supaplex samples, melodies or recordings.
 */
final class GeneratedMusic {
    static final String[] TITLES={"Circuit Garden","Crystal Steps","Clockwork Rush","Neon Maze","Quiet Terminal"};
    static final String REVISION="fm2";
    static final int[] BPM={112,104,132,120,96};
    private static final int[][] ROOTS={
        {45,41,48,43,45,38,41,40}, {38,46,41,43,38,41,43,45},
        {40,43,38,45,40,36,38,47}, {36,44,39,46,36,41,44,43},
        {43,39,46,41,43,36,39,38}
    };
    private static final int[][][] LEADS={
        {{76,79,81,83,79,76,74,76},{72,76,77,81,79,77,76,-1},{79,76,72,74,76,79,84,83},{74,79,81,79,77,74,71,-1}},
        {{74,-1,77,76,74,69,72,-1},{77,81,79,-1,77,74,72,69},{72,-1,76,77,79,-1,76,72},{79,77,74,72,69,72,74,-1}},
        {{76,79,83,79,78,76,74,71},{79,83,86,83,81,79,78,-1},{74,78,81,78,76,74,69,71},{81,84,83,81,79,76,78,-1}},
        {{72,75,-1,79,82,79,75,74},{80,79,75,72,75,-1,79,80},{75,79,82,-1,79,77,75,72},{77,82,80,77,74,77,79,-1}},
        {{79,-1,82,-1,81,79,77,-1},{75,79,-1,82,79,-1,77,75},{82,-1,86,84,82,-1,81,79},{77,81,84,-1,81,77,74,-1}}
    };
    private static void le(byte[] b,int p,int n,int count) {for(int i=0;i<count;i++)b[p+i]=(byte)(n>>>(8*i));}
    private static void tag(byte[] b,int p,String s) {for(int i=0;i<s.length();i++)b[p+i]=(byte)s.charAt(i);}
    private static double hz(int note) {return 440*Math.pow(2,(note-69)/12.0);}
    private static double envelope(double local,double duration) {return Math.min(1,local/.005)*Math.min(1,Math.max(0,(duration-local)/.035));}
    static byte[] wave(int variant) {
        int rate=GeneratedAudio.RATE;
        double beat=60.0/BPM[variant],duration=64*beat;
        int frames=(int)Math.round(duration*rate),length=frames*2;
        byte[] wav=new byte[44+length];tag(wav,0,"RIFF");le(wav,4,36+length,4);tag(wav,8,"WAVE");tag(wav,12,"fmt ");le(wav,16,16,4);
        le(wav,20,1,2);le(wav,22,1,2);le(wav,24,rate,4);le(wav,28,rate*2,4);le(wav,32,2,2);le(wav,34,16,2);tag(wav,36,"data");le(wav,40,length,4);
        // Precompute note frequencies; the sample loop contains no pitch exponentiation.
        double[] frequencies=new double[128];for(int n=0;n<128;n++)frequencies[n]=hz(n);
        int noise=0x1b7623+variant*7919;
        int[] intervals={0,variant==1 || variant==4?3:7,12,7};
        for(int i=0;i<frames;i++) {
            double t=i/(double)rate,beats=t/beat;
            int bar=Math.min(15,(int)(beats/4)),step=(int)(beats*2)%8,quarter=(int)beats%4;
            int root=ROOTS[variant][bar%8],note=LEADS[variant][bar%4][step];
            double local=t%(beat/2),lead=0;
            if(note>=0) {
                if(bar>=8 && bar%4==2)note+=12;
                double phase=2*Math.PI*frequencies[note]*local;
                double index=.35+(1.65+variant*.12)*Math.exp(-14*local);
                double fm=Math.sin(phase+index*Math.sin(phase*(variant==2?1:2)));
                lead=(fm*.88+Math.sin(phase)*.12)*envelope(local,beat*.43)*.15;
            }
            double bassTime=t%(beat/2),bassPhase=2*Math.PI*frequencies[root+(step==7?7:0)]*bassTime;
            double bassFm=Math.sin(bassPhase+(1.15+1.4*Math.exp(-24*bassTime))*Math.sin(bassPhase*2));
            double bass=(bassFm*.8+Math.sin(bassPhase)*.2)*envelope(bassTime,beat*.43)*.25;
            int arpNote=root+24+intervals[(int)(beats*4)%4];
            double arpTime=t%(beat/4),arpPhase=2*Math.PI*frequencies[arpNote]*arpTime,arp=Math.sin(arpPhase+.55*Math.sin(arpPhase*2))*envelope(arpTime,beat*.20)*(variant==4?.065:.045);
            noise=noise*1664525+1013904223;double random=(noise>>>8)/8388607.5-1;
            double drumTime=t%beat;
            double kick=quarter%2==0?Math.sin(2*Math.PI*(51*drumTime+3.1*(1-Math.exp(-38*drumTime))))*Math.exp(-21*drumTime)*.22:0;
            double snare=quarter%2==1?(random*.7+Math.sin(2*Math.PI*185*drumTime)*.3)*Math.exp(-27*drumTime)*.10:0;
            double metal=Math.sin(2*Math.PI*4800*local+1.8*Math.sin(2*Math.PI*6900*local));
            double hat=(metal*.8+random*.2)*Math.exp(-95*local)*.023;
            if(variant==4) {kick*=.5;snare*=.45;hat*=.6;}
            if(variant==2 && step%2==1)kick+=Math.sin(2*Math.PI*65*local)*Math.exp(-35*local)*.05;
            double edge=Math.min(1,Math.min(t/.006,(frames-1-i)/(rate*.006)));
            double value=(lead+bass+arp+kick+snare+hat)*edge;
            int sample=(int)(Math.max(-.65,Math.min(.65,value*.85))*30000);
            le(wav,44+2*i,sample,2);
        }
        return wav;
    }
    static void ensure(File root) throws IOException {
        File folder=new File(root,"audio");if(!folder.isDirectory() && !folder.mkdirs())throw new IOException("Cannot create audio folder");
        for(int variant=0;variant<TITLES.length;variant++) {
            File file=new File(folder,"generated-music-"+REVISION+"-"+variant+".wav");if(file.isFile())continue;
            File temp=new File(folder,file.getName()+".generated");
            try(FileOutputStream out=new FileOutputStream(temp)) {out.write(wave(variant));out.getFD().sync();}
            if(!temp.renameTo(file))throw new IOException("Cannot install generated music");
        }
    }
}
