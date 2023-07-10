import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Main {

    public static void main(String[] args) throws ScriptException {
        ScriptEngine ee = new ScriptEngineManager().getEngineByName("Nashorn");
        Object result = ee.eval("var hello = 123; hello;");
        System.out.println(result);
    }
}
