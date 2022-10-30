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
    private int p;
    
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
        p = 0;
        int i = 0;
        cells.add(0);
        while(i < code.length()) {
            char c = code.charAt(i);
            if(c == '>') {
                p++;
                if(p == cells.size())
                    cells.add(0);
            }
            else if(c == '<') {
                p--;
                if(p == -1)
                    throw new IndexOutOfBoundsException("p managed to get to -1");
            }

            else if(c == '+')
                cells.set(p, (cells.get(p)+1)%256);
            else if(c == '-') {
                cells.set(p, cells.get(p)-1);
                if(cells.get(p) == -1)
                    cells.set(p, 0);
            }

            else if(c == '[' && cells.get(p) == 0) {
                i = bracket_indexes.get(i);    
            }
            else if(c == ']' && cells.get(p) != 0) {
                i = bracket_indexes.get(i);
            }
            
            else if(c == '.' && !viewmemory)
                System.out.print((char)(int)cells.get(p)); // casting Integer to int to char :tf:

            i++;

            if(viewmemory)
                viewMemory();
        }
    }

    public void viewMemory() throws Exception {
        max_ind = Math.max(max_ind, p);
        min_ind = Math.min(min_ind, p);
        
        if(p <= max_ind - 40)
            max_ind --;
        if(p >= min_ind + 40)
            min_ind ++;
        String outString = "";
        for(int j = min_ind; j < max_ind+1; j++) {
            String v = Integer.toHexString(cells.get(j));
            if(v.length() == 1)
                v = "0" + v;
            if(j != p)
                outString += " " + v + " ";
            else
                outString += ">" + v + "<";
        }
        System.out.print("\r" + outString);
        Thread.sleep(50);
    }

}
