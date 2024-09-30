import java.io.File;
import java.io.FileWriter;
import java.util.Stack;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Interpreter {
    
    public String code;
    private HashMap<Integer, Integer> bracket_indexes;
    private boolean viewmemory;
    private ArrayList<Integer> cells;
    private ArrayList<String> code_blocks;
    private int pointer;
    
    // memory viewer variables
    int max_ind = 0;
    int min_ind = 0;

    String output;

    public Interpreter(String filepath) throws IOException {
        // without withmemoryviewer, it defaults to false (because it isn't being set)
        // you can call the constructor and set it to false manually, but idk why you'd
        // do that
        for (String c : Files.readAllLines(Paths.get(filepath))) {
            this.code += c;
        };
        //region filter comments from code
        String actualcode = "";
        char[] validchars = new char[] {'[', ']', '<', '>', '+', '-', '.', ','};
        for (int i = 0; i < this.code.length(); i++) {
            // odd way to check if an array contains an item
            // from stackoverflow: https://stackoverflow.com/questions/18581531/in-java-how-can-i-determine-if-a-char-array-contains-a-particular-character
            if(new String(validchars).indexOf(this.code.charAt(i)) != -1) {
                actualcode += Character.toString(this.code.charAt(i));
            }
        }
        this.code = actualcode;
        //endregion

        // for faster interpreting, we're going to "compress" the code
        // this means that we're going to turn ">>>>>>>>>>>>" into a single block
        // that way instead of interpreting each one as its own instruction, we're going
        // to interpret each as a big block
        this.code_blocks = new ArrayList<>();
        String block = "";
        for (char c : this.code.toCharArray()) {
            // we don't want to stack '[' or ']'
            if (c == '[' || c == ']') {
                if (!block.equals("")) {
                    this.code_blocks.add(block);
                    block = "";
                }
                this.code_blocks.add(Character.toString(c));
                continue;
            }
            if (block.equals("")) {
                block += c;
                continue;
            }
            if (c == block.charAt(block.length() - 1)) {
                block += c;
            } else {
                this.code_blocks.add(block);
                block = "";
                block += c;
            }
        }
        if(!block.equals(""))
            this.code_blocks.add(block);

        // verify code blocks
        String verify = "";
        for (String s : this.code_blocks)
            verify += s;
        if (!verify.equals(actualcode)) {
            System.out.println("Code verification failed");
            File verification = new File("verification.txt");
            verification.createNewFile();
            FileWriter writer = new FileWriter(verification);
            writer.write(actualcode + "\n" + verify);
            writer.close();
            System.exit(1);
        }

        this.output = "";
        // for faster reader jumping between square brackets, we store their connected indices
        // a closing bracket will have an index to its opening bracket
        // this super speeds us up compared to scanning the code backwards until we arrive at 
        // the connected opening bracket
        this.bracket_indexes = new HashMap<>();
        Stack<Integer> temp_brackets = new Stack<>(); // stacks are cool for this
        for(int i = 0; i < this.code_blocks.size(); i++) {
            char c = this.code_blocks.get(i).charAt(0);
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

    public Interpreter(String filepath, boolean withmemoryviewer, boolean startwithrandommem) throws IOException {
        this(filepath, withmemoryviewer);
        if(startwithrandommem) { // this has no reaon to be false
            Random rand = new Random();
            for (int i = 0; i < 100; i++) {
                cells.add(rand.nextInt(255));
            }
        }
    }

    public Interpreter(String filepath, boolean withmemoryviewer) throws IOException {
        this(filepath);
        this.viewmemory = withmemoryviewer;
    }

    // again with the one below, this may as well go unused
    // but the thing is I like having default values, and idk
    // if that's possible with java parameters
    public void interpret() throws Exception {
        this.interpret(false);
    }

    public void interpret(boolean wait) throws Exception {
        // if wait is true, there'll be a small pause between each instruction if the memory viewer is active
        this.pointer = 0;
        int i = 0;
        this.cells.add(0);
        while (i < this.code_blocks.size()) {
            int block_size = this.code_blocks.get(i).length();
            char c = this.code_blocks.get(i).charAt(0);
            switch (c) {
                case '>':
                    this.pointer += block_size;
                    while (this.pointer >= this.cells.size())
                        this.cells.add(0);
                    break;
    
                case '<':
                    this.pointer -= block_size;
                    if (this.pointer <= -1)
                        throw new IndexOutOfBoundsException("Pointer went out of bounds");
                    break;
    
                case '+':
                    this.cells.set(this.pointer, (this.cells.get(this.pointer) + block_size) % 256);
                    break;
    
                case '-':
                    this.cells.set(this.pointer, this.cells.get(this.pointer) - block_size);
                    while (this.cells.get(this.pointer) <= -1) {
                        int val = this.cells.get(this.pointer);
                        this.cells.set(this.pointer, (val + 256));
                    }
                    this.cells.set(this.pointer, (this.cells.get(this.pointer) % 256));
                    break;
    
                case '[':
                    if (this.cells.get(this.pointer) == 0) {
                        i = bracket_indexes.get(i);
                    }
                    break;
    
                case ']': 
                    if (this.cells.get(this.pointer) != 0) {
                        i = bracket_indexes.get(i);
                    }
                    break;
    
                case '.':
                    char out = (char) (int) this.cells.get(this.pointer);
                    for (int j = 0; j < block_size; j++) {
                        if (!this.viewmemory)
                            System.out.print(out); // casting Integer to int to char :tf:
                        this.output += Character.toString(out);
                    }
                    break;
    
                case ',':
                    // I chose to ignore the fact that the memory viewer
                    // would look ugly if we ignore the input. After the input,
                    // I just clear out the screen by printing a couple hundred newlines.
                    // /shrug untested
                    Scanner scan = new Scanner(System.in);
                    for (int j = 0; j < block_size; j++) {
                        System.out.print("\n> ");
                        char val = scan.nextLine().charAt(0);
                        scan.close();
                        this.cells.set(this.pointer, (int) val);
                    }
                    System.out.print(String.format("%0" + 100 + "d", 0).replace("0", "\n"));
                    break;
            }

            i++;

            if (this.viewmemory)
                viewmemory(wait);
        }
        
        // save the entire final memory to a file        
        File finalmemory = new File("finalmem.txt");
        finalmemory.createNewFile(); // i know im ignoring the result, but idc about the result
        FileWriter writer = new FileWriter(finalmemory);
        for (int j = 0; j < this.cells.size(); j++) {
            String v = Integer.toHexString(this.cells.get(j));
            if (v.length() == 1)
                v = "0" + v;
            if (j == this.pointer)
                v = "[" + v + "]";
            else
                v = " " + v + " ";

            writer.write(v);
        }
        writer.close();
        

        // save the output to a file
        File outFile = new File("output.txt");
        outFile.createNewFile();
        FileWriter outFileWriter = new FileWriter(outFile);
        outFileWriter.write(this.output);
        outFileWriter.close();
    }
    
    // yes, this goes unused, but I like having defaults
    private void viewmemory() throws Exception {
        this.viewmemory(false);
    }

    private void viewmemory(boolean wait) throws Exception {
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

        // if(wait)
            // Thread.sleep(15);
    }
}
