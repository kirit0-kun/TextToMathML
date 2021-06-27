package com.flowapp.TextToMathML.Services;

import com.flowapp.TextToMathML.Models.MathTag;

public class TagBuilder {

    public String createMath(String math) {
        return MathTag.MATH.enclose(math);
    }

    public String createText(String text) {
        return MathTag.TEXT.enclose(text);
    }

    public String createIdentifier(String identifier) {
        return MathTag.IDENTIFIER.enclose(identifier);
    }

    public String createOperator(String operator) {
        String replacement;
        if (operator.equals("*")) {
            replacement = "x";
        } else {
            replacement = operator;
        }
        return MathTag.OPERATOR.enclose(replacement);
    }

    public String createNumber(String number) {
        return MathTag.NUMERIC.enclose(number);
    }

    public String createRoot(String base, String power) {
        if (power.equals("2")) {
            return MathTag.SQRT.enclose(base);
        } else {
            return MathTag.ROOT.enclose(base + " " + power);
        }
    }

    public String createLinebreak() {
        return MathTag.SPACE.enclose("", "linebreak='newline'");
    }

    public String createPower(String base, String power) {
        return MathTag.SUPERSCRIPT.enclose(base + " " + power);
    }

    public String createSub(String base, String power) {
        return MathTag.SUBSCRIPT.enclose(base + " " + power);
    }

    public String createBraces(String data) {
        final var newData = MathTag.OPERATOR.enclose("(") + data + MathTag.OPERATOR.enclose(")");
        return createRow(newData);
    }

    public String createEquality(String lhs, String rhs) {
        return MathTag.ROW.enclose(lhs + MathTag.OPERATOR.enclose("=") + rhs);
    }

    public String createFraction(String numerator, String denominator) {
        return MathTag.FRACTION.enclose(numerator + " " + denominator);
    }

    public String createRow(String symbol) {
        return MathTag.ROW.enclose(symbol);
    }
}
