import java.io.File;
import java.io.FileWriter;
import java.util.Stack;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Interpreter {
    
    public String code;
    private HashMap<Integer, Integer> bracket_indexes;
    private boolean viewmemory;
    private ArrayList<Integer> cells;
    private int pointer;
    
    // memory viewer variables
    int max_ind = 0;
    int min_ind = 0;


    public Interpreter(String filepath) throws IOException {
        // without withmemoryviewer, it defaults to false (because it isn't being set)
        // you can call the constructor and set it to false manually, but idk why you'd
        // do that
        this.code = Files.readString(Paths.get(filepath));
        this.bracket_indexes = new HashMap<>();
        Stack<Integer> temp_brackets = new Stack<>();
        for(int i = 0; i < this.code.length(); i++) {
            char c = this.code.charAt(i);
            if(c == '[')
                temp_brackets.add(i);
            else if(c == ']') {
                int first = temp_brackets.pop();
                this.bracket_indexes.put(first, i);
                this.bracket_indexes.put(i, first);
            }
        }
        this.cells = new ArrayList<>();
    }

    public Interpreter(String filepath, boolean withmemoryviewer) throws IOException {
        this(filepath);
        this.viewmemory = withmemoryviewer;
    }

    public void interpret() throws Exception {
        this.pointer = 0;
        int i = 0;
        this.cells.add(0);
        while(i < this.code.length()) {
            char c = this.code.charAt(i);
            if(c == '>') {
                this.pointer++;
                if(this.pointer == this.cells.size())
                    this.cells.add(0);
            }
            else if(c == '<') {
                this.pointer--;
                if(this.pointer == -1)
                    throw new IndexOutOfBoundsException("p managed to get to -1");
            }
            else if(c == '+')
                this.cells.set(this.pointer, (this.cells.get(this.pointer)+1)%256);
            else if(c == '-') {
                this.cells.set(this.pointer, this.cells.get(this.pointer)-1);
                if(this.cells.get(this.pointer) == -1)
                    this.cells.set(this.pointer, 0);
            }
            else if(c == '[' && this.cells.get(this.pointer) == 0) {
                i = bracket_indexes.get(i);    
            }
            else if(c == ']' && this.cells.get(this.pointer) != 0) {
                i = bracket_indexes.get(i);
            }
            else if(c == '.' && !this.viewmemory)
                System.out.print((char)(int)this.cells.get(this.pointer)); // casting Integer to int to char :tf:

            i++;

            if(this.viewmemory)
                viewmemory();
        }

        if(this.viewmemory) {
            File finalmemory = new File("finalmem.txt");
            finalmemory.createNewFile(); // i know im ignoring the result, but idc about the result
            FileWriter writer = new FileWriter(finalmemory);
            for (int j = 0; j < this.cells.size(); j++) {
                String v = Integer.toHexString(this.cells.get(j));
                if(v.length() == 1) v = "0" + v;
                if (j == this.pointer) 
                    v = ">" + v + "<";
                else
                    v = " " + v + " ";
                
                writer.write(v);
            }
            writer.close();
        }
    }

    private void viewmemory() throws Exception {
        max_ind = Math.max(max_ind, this.pointer);
        min_ind = Math.min(min_ind, this.pointer);
        
        if(this.pointer <= max_ind - 40)
            max_ind --;
        if(this.pointer >= min_ind + 40)
            min_ind ++;
        String outString = "";
        for(int j = min_ind; j < max_ind+1; j++) {
            String v = Integer.toHexString(this.cells.get(j));
            if(v.length() == 1)
                v = "0" + v;
            if(j != this.pointer)
                outString += " " + v + " ";
            else
                outString += ">" + v + "<";
        }
        System.out.print("\r" + outString);
        Thread.sleep(15);
    }
}
