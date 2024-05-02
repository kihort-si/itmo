package ru.itmo.programming.client.utils;

import java.util.Stack;

public class ScriptManager {
    Stack<String> scriptStack;

    public ScriptManager() {
        this.scriptStack = new Stack<>();
    }

    /**
     * @param filePath script file path
     */
    public void addScript(String filePath) {
        scriptStack.push(filePath);
    }

    /**
     * removes the last script from the stack
     */
    public void removeScript() {
        if (!scriptStack.isEmpty()) {
            scriptStack.pop();
        }
    }

    /**
     * @param fileName name of the file to be checked if it is the last one in the stack
     * @return boolean value of the check
     */
    public boolean isScriptInStack(String fileName) {
        return scriptStack.contains(fileName);
    }
}
