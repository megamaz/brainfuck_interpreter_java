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
        this.code = Files.readString(Paths.get(filepath));
        this.bracket_indexes = new HashMap<>();
        Stack<Integer> temp_brackets = new Stack<>();
        for(int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            if(c == '[')
                temp_brackets.add(i);
            else if(c == ']') {
                int first = temp_brackets.pop();
                this.bracket_indexes.put(first, i);
                this.bracket_indexes.put(i, first);
            }
        }
        cells = new ArrayList<>();
    }

    public Interpreter(String filepath, boolean withmemoryviewer) throws IOException {
        this(filepath);
        this.viewmemory = withmemoryviewer;
    }

    public void interpret() throws Exception {
        pointer = 0;
        int i = 0;
        cells.add(0);
        while(i < code.length()) {
            char c = code.charAt(i);
            if(c == '>') {
                pointer++;
                if(pointer == cells.size())
                    cells.add(0);
            }
            else if(c == '<') {
                pointer--;
                if(pointer == -1)
                    throw new IndexOutOfBoundsException("p managed to get to -1");
            }

            else if(c == '+')
                cells.set(pointer, (cells.get(pointer)+1)%256);
            else if(c == '-') {
                cells.set(pointer, cells.get(pointer)-1);
                if(cells.get(pointer) == -1)
                    cells.set(pointer, 0);
            }

            else if(c == '[' && cells.get(pointer) == 0) {
                i = bracket_indexes.get(i);    
            }
            else if(c == ']' && cells.get(pointer) != 0) {
                i = bracket_indexes.get(i);
            }
            
            else if(c == '.' && !viewmemory)
                System.out.print((char)(int)cells.get(pointer)); // casting Integer to int to char :tf:

            i++;

            if(viewmemory)
                viewMemory();
        }
        if(viewmemory) {
            File finalmemory = new File("finalmem.txt");
            finalmemory.createNewFile(); // i know im ignoring the result, but idc about the result
            FileWriter writer = new FileWriter(finalmemory);
            for (int j = 0; j < cells.size(); j++) {
                String v = Integer.toHexString(cells.get(j));
                if(v.length() == 1) v = "0" + v;
                if (j == pointer) 
                    v = ">" + v + "<";
                else
                    v = " " + v + " ";
                
                writer.write(v);
            }
            writer.close();
        }
        
    }

    public void viewMemory() throws Exception {
        max_ind = Math.max(max_ind, pointer);
        min_ind = Math.min(min_ind, pointer);
        
        if(pointer <= max_ind - 40)
            max_ind --;
        if(pointer >= min_ind + 40)
            min_ind ++;
        String outString = "";
        for(int j = min_ind; j < max_ind+1; j++) {
            String v = Integer.toHexString(cells.get(j));
            if(v.length() == 1)
                v = "0" + v;
            if(j != pointer)
                outString += " " + v + " ";
            else
                outString += ">" + v + "<";
        }
        System.out.print("\r" + outString);
        Thread.sleep(15);
    }

}
