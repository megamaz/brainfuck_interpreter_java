public class App {
    public static void main(String[] args) throws Exception {
        Interpreter interpreter = new Interpreter("./brainfuck.bf", true);
        interpreter.interpret(); // wow the interpreter interprets? who knew!
    }
}