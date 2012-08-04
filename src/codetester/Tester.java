package codetester;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.JOptionPane;

public class Tester {
    
    private Interpreter interpreter;
    private CoderTools tools;
    private int failCount;
    
    public int genArr[]={5,5,1};
    public int strArr[]={};
    
    public Tester(Interpreter interpreter, CoderTools tools) {
        this.interpreter=interpreter;
        this.tools=tools;
    }
    
    public void startTests(final File file) {
        interpreter.reset();
        if(file==null || !file.exists()) return;
        Thread test=new Thread(new Runnable() {
            public void run() {
                try {
                    String dir=file.getParentFile().getCanonicalPath();
                    PrintWriter out=new PrintWriter(new FileOutputStream(dir+File.separator+"temp_in.txt"));
                    generate(out);
                    interpreter.interpret(file, System.out);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
        test.setDaemon(true);
        test.start();
    }
    
    public void testOutput(final File ref, final File test, final int count) {
        if(!ref.getParentFile().equals(test.getParentFile())) {
            System.out.println("Files must be at the same location");
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                int passed=0;failCount=0;
                for(int i=0;i<count;i++) {
                    try {
                        String dir=ref.getParentFile().getCanonicalPath()+File.separator;
                        generateInputFile(dir+"in.txt");
                        System.out.println("generating reference results...");
                        generateResultsFile(ref.getCanonicalPath(),"in.txt","ref_out.txt");
                        System.out.println("generating test results...");
                        generateResultsFile(test.getCanonicalPath(),"in.txt","out.txt");
                        if(tallyOutputFiles(dir+"ref_out.txt",dir+"out.txt")) passed++;
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                if(passed==count) JOptionPane.showMessageDialog(tools, "Correct Answer!\nPassed All Tests", "Result", JOptionPane.INFORMATION_MESSAGE);
                else JOptionPane.showMessageDialog(tools, "Wrong Answer!\nFailed "+(count-passed)+" Tests", "Result", JOptionPane.INFORMATION_MESSAGE);
            }
        }).start();
    }
    
    public boolean tallyOutputFiles(String refpath, String testpath) {
        File ref=new File(refpath);
        File test=new File(testpath);
        if(!ref.exists() || !test.exists()) {
            System.out.println("Tally files do not exist");
            return false;
        }
        System.out.println("tallying files...");
        try {
            File dir=ref.getParentFile();
            String file1=ref.getName();
            String file2=test.getName();
            
            Runtime.getRuntime().exec("cmd /c fc /L ref_out.txt out.txt > result.txt", null, dir);
            File result=new File(dir.getCanonicalPath()+File.separator+"result.txt");
            if(!result.exists()) {
                System.out.println("something went wrong");
                return false;
            }
            
            Scanner in=new Scanner(new FileInputStream(result));
            boolean found=false;
            while(in.hasNextLine()) {
                String line=in.nextLine();
                if(line.indexOf("FC: no differences encountered")!=-1) found=true;
            }
            if(!found) {
                System.out.println("Failed Test");failCount++;
                Runtime.getRuntime().exec("cmd /c copy result.txt failed"+failCount+".txt", null, dir);
            }
            else System.out.println("Passed Test");
            return found;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void generateResultsFile(String filepath, String infile, String outfile) {
        try {
            File file=new File(filepath);
            if(!file.exists()) return;
            interpreter.execute(file, infile, outfile); 
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void generateInputFile(String filepath) {
        File fullpath=new File(filepath);
        if(!fullpath.exists()) return;
        try {
            PrintWriter out=new PrintWriter(new FileOutputStream(fullpath));
            generate(out);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void generate(PrintWriter out) {
        //algorithm start
        System.out.println("Number of cases: 10^"+genArr[0]);
        System.out.println("Range of numbers: 10^"+genArr[1]);
        System.out.println("Inputs per line: "+genArr[2]);
        System.out.println("generating test cases...");
        long t=(long)(Math.random()*Math.pow(10, genArr[0]));
        out.println(t);
        out.flush();
        for(int i=0;i<t;i++) {
            long num=(long)Math.pow(10, genArr[1]);
            for(int j=0;j<genArr[2];j++) {
                num=(long)(Math.random()*num);
                if(j>0) out.print(" ");
                out.print(num);
            }
            out.println();
            out.flush();
        }
        //algorithm stop
    }
}
