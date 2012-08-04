package codetester;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class Interpreter {
    
    private String command;
    private Process process;
    private Thread stdth;
    private long startTime;
    private int timeout;
    private PrintWriter stdout;
    private CoderTools tools;
    
    public Interpreter(String command, CoderTools tools) {
        this.command=command;
        this.tools=tools;
        timeout=60;
    }
    
    public void interpret(File file, OutputStream os) {
        final PrintWriter out=new PrintWriter(os);
        String filename=file.getName();
        File path=file.getParentFile();
        String query=command+" "+filename+" <temp_in.txt> temp_out.txt";
        try {
            process=Runtime.getRuntime().exec(query, null, path);
            startTime=System.nanoTime();
            
            stdout=new PrintWriter(process.getOutputStream());
            stdth=new Thread(new Runnable(){
                public void run() {
                    Scanner stdin=new Scanner(process.getInputStream()); 
                    while(stdin.hasNextLine()) out.println(stdin.nextLine());
                    out.flush();
                    float timeTaken=Float.valueOf(elapsedTime()/1000.0f);
                    reset();
                    System.out.println("test complete");
                    JOptionPane.showMessageDialog(tools, "Time: "+timeTaken+" seconds", "Execution Complete", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            stdth.start();
            this.monitorThread();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    public void execute(File file, String infile, String outfile) {
        String filename=file.getName();
        File path=file.getParentFile();
        String query=command+" "+filename+" <"+infile+"> "+outfile;
        try {
            Runtime.getRuntime().exec(query, null, path);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private void monitorThread() {
        Thread th=new Thread(new Runnable(){
            public void run() {
                while(true && startTime>0) {
                    int maxTime=timeout*1000;
                    try {
                        if(elapsedTime()>maxTime) {
                            JOptionPane.showMessageDialog(tools, "Process Timed Out!", "Timeout", JOptionPane.ERROR_MESSAGE);
                            kill();
                            break;
                        }
                        if(startTime==0) break;
                        Thread.sleep(500);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        th.setDaemon(true);
        th.start();
    }
    
    public void setTimeout(int seconds) {
        this.timeout=seconds;
    }
    
    public PrintWriter getStdOut() {
        return this.stdout;
    }
    
    public void feed(String line) {
        if(stdout==null) return;
        stdout.println(line);
        stdout.flush();
    }
    
    public void kill() {
        if(process!=null) process.destroy();
        process=null;
        stdth.interrupt();
    }
    
    public void reset() {
        process=null;
        startTime=0;
    }
    
    public int elapsedTime() {
        long now=System.nanoTime();
        long elapsed=(now-startTime)/(long)Math.pow(10, 6);
        int timeElapsed=(int)elapsed;
        return timeElapsed;
    }
}
