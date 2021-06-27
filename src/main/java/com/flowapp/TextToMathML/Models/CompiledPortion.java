package com.flowapp.TextToMathML.Models;

public class CompiledPortion {
    private final boolean compiled;
    private final String portion;

    public CompiledPortion(boolean compiled, String portion) {
        this.compiled = compiled;
        this.portion = portion;
    }

    public static CompiledPortion compiled(String portion) {
        return new CompiledPortion(true, portion);
    }

    public static CompiledPortion notCompiled(String portion) {
        return new CompiledPortion(false, portion);
    }

    public boolean isCompiled() {
        return compiled;
    }

    public String getPortion() {
        return portion;
    }

    @Override
    public String toString() {
        return "CompiledPortion{" +
                "compiled=" + compiled +
                ", portion='" + portion + '\'' +
                '}';
    }
}
