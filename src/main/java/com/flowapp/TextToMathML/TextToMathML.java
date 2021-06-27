package com.flowapp.TextToMathML;

import com.flowapp.TextToMathML.Models.CompiledPortion;
import com.flowapp.TextToMathML.Models.MathTag;
import com.flowapp.TextToMathML.Models.Tuple2;
import com.flowapp.TextToMathML.Models.TwoArgInterface;
import com.flowapp.TextToMathML.Services.TagBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TextToMathML {

    private static Character[] OPERATORS = {'+', '=','*','/','-', '@', '^', ' '};

    public static void main(String[] args) throws IOException {
        final String toProcess = "x = 4x + (10 + 7/7 + 10 ^ 6)^ (1 /2)";
        final BufferedReader reader = new BufferedReader(new FileReader("toconvert.text"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(convert(line, true, true));
        }
        reader.close();
    }

    public static String convert(String toProcess, boolean breakLine, boolean enclose) {

        final List<Tuple2<Integer, String>> leveled = new ArrayList<>();

        final int maxLevel = getMaxLevel(toProcess, leveled);

        final List<String> compiled = new ArrayList<>();

        for (int levelToProcess = maxLevel; levelToProcess >= 0; levelToProcess--) {
            final int finalLevelToProcess = levelToProcess;

            joinLevels(leveled);

            final var levels = leveled.stream().filter(tuple -> {
                return tuple.getFirst() == finalLevelToProcess;
            }).collect(Collectors.toList());

            for (var level: levels) {
                final int index = leveled.indexOf(level);
                final var compiledLevel = compile(level.getSecond());
                leveled.set(index, Tuple2.of(levelToProcess-1, "{" +
                        appendToCompiledList(compiledLevel, compiled)
                        +"}"));
            }
        }
        joinLevels(leveled);
        String result = leveled.get(0).getSecond();
        for (int i = compiled.size()-1; i >= 0; i--) {
            final var toReplace = compiled.get(i);
            result = result.replace("{" + i +"}", toReplace);
        }
        final var data = MathTag.ROW.enclose(result) + (breakLine ? MathTag.SPACE.enclose("", "linebreak='newline'") : "");
        if (enclose) {
            return MathTag.MATH.enclose(data) + (breakLine ? "</br>" : "");
        } else {
            return data;
        }
    }

    private static String compile(String expressions) {
        List<String> compiled = splitForOperators(expressions);
        final TagBuilder tagBuilder = new TagBuilder();
        final List<Tuple2<String, TwoArgInterface<String>>> ORDERED_OPERATORS = List.of(
                Tuple2.of("@", tagBuilder::createSub),
                Tuple2.of("^", tagBuilder::createPower),
                Tuple2.of("/", tagBuilder::createFraction)
        );
        for (var op: ORDERED_OPERATORS) {
            boolean brace = !op.getFirst().equals("/");
            compiled = compileFor(compiled, op.getFirst(), tagBuilder, brace, op.getSecond());
        }
        compiled = compiled.stream().map(e -> compileTag(e, tagBuilder, true)).collect(Collectors.toList());
        return String.join("", compiled);
    }

    private static List<String> compileFor(List<String> split, String forString, TagBuilder tagBuilder, boolean braceLhs, TwoArgInterface<String> apply) {
        String lastSide = null;
        final List<String> compiled = new ArrayList<>();
        for (int i = 0; i < split.size(); i++) {
            final var side = split.get(i);
            if (side.equals(forString)) {
                if (i > 0 && i < split.size() -1) {
                    final String lhs = compileTag(split.get(i-1), tagBuilder,braceLhs);
                    final String rhs = compileTag(split.get(i+1), tagBuilder,false);
                    String compiledTag = apply.operation(lhs, rhs);
                    if (compiledTag != null) {
                        lastSide = null;
                        split.set(i-1, compiledTag);
                        split.remove(i);
                        split.remove(i);
                        i--;
                        compiled.add(compiledTag);
                    }
                    continue;
                }
            } else {
                if (lastSide != null) {
                    compiled.add(lastSide);
                }
                lastSide = side;
            }
        }
        if (lastSide != null) {
            compiled.add(lastSide);
        }
        return compiled;
    }

    private static int appendToCompiledList(String compiled, List<String> compiledList) {
        final int compiledIndex = compiledList.size();
        compiledList.add(compiled);
        return compiledIndex;
    }

    private static String compileTag(String symbol, TagBuilder tagBuilder, boolean brace) {
        final String strippedSymbol = symbol.strip();
        if (strippedSymbol.matches("\\{[\\d]+}")) {
            if (brace) {
                return tagBuilder.createBraces(symbol);
            } else {
                return tagBuilder.createRow(symbol);
            }
        } else if (strippedSymbol.equals("+") || strippedSymbol.equals("=") || strippedSymbol.equals("-") || strippedSymbol.equals("*")) {
            return tagBuilder.createOperator(strippedSymbol);
        } else if (isNumeric(strippedSymbol)) {
            return tagBuilder.createNumber(strippedSymbol);
        } else if (strippedSymbol.length() == 1) {
            return tagBuilder.createIdentifier(strippedSymbol);
        } else if (strippedSymbol.matches("<(.+)>.*</(.+)>")) {
            return symbol;
        } else {
            return tagBuilder.createText(symbol);
        }
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private static List<String> splitForOperators(String expression) {
        final List<String> split = new ArrayList<>();
        final List<Character> operators = Arrays.asList(OPERATORS);
        StringBuilder last = new StringBuilder();
        for (var c: expression.toCharArray()) {
            boolean addAndClear = false;
            if (operators.contains(c)) {
                addAndClear = true;
            } else {
                last.append(c);
            }
            if (addAndClear) {
                if (last.length() > 0) {
                    split.add(last.toString());
                    last = new StringBuilder();
                }
                //noinspection StatementWithEmptyBody
                if (c != ' ') {
                    split.add(Character.toString(c));
                } else {
                    //last.append(c);
                }
            }
        }
        if (last .length() > 0) {
            split.add(last.toString());
        }
        return split;
    }

    private static void joinLevels(List<Tuple2<Integer, String>> leveled) {
        final List<Tuple2<Integer, String>> newLeveled = new ArrayList<>();
        Tuple2<Integer, String> lastTuple = null;
        for (var tuple: leveled) {
            if (lastTuple == null) {
                lastTuple = tuple;
            } else if (lastTuple.getFirst().equals(tuple.getFirst())) {
                lastTuple = Tuple2.of(lastTuple.getFirst(), lastTuple.getSecond() + tuple.getSecond());
            } else {
                newLeveled.add(lastTuple);
                lastTuple = tuple;
            }
        }
        if (lastTuple != null) {
            newLeveled.add(lastTuple);
        }
        leveled.clear();
        leveled.addAll(newLeveled);
    }

    private static int getMaxLevel(String toProcess, List<Tuple2<Integer, String>> leveled) {
        int maxLevel = 0;
        int level =0;
        StringBuilder last = new StringBuilder("");
        for (var c: toProcess.toCharArray()) {
            boolean addAndClear = false;
            final var lastLevel = level;
            if (c == '(') {
                level++;
                addAndClear = true;
            } else if (c == ')') {
                level--;
                addAndClear = true;
            } else {
                last.append(c);
            }
            if (addAndClear) {
                if (last.length() > 0) {
                    leveled.add(Tuple2.of(lastLevel, last.toString()));
                }
                last = new StringBuilder();
            }
            maxLevel = Math.max(maxLevel, level);
        }
        if (last.length() > 0) {
            leveled.add(Tuple2.of(level, last.toString()));
        }
        return maxLevel;
    }

}
