package com.flowapp.TextToMathML;

import com.flowapp.TextToMathML.Models.CompiledPortion;
import com.flowapp.TextToMathML.Models.Tuple2;
import com.flowapp.TextToMathML.Services.TagBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TextToMathML1 {

    private static Character[] OPERATORS = {'+', '=','*','/','-', '^'};

    public static void main(String[] args) {
        final String toProcess = "x = 4x + (10 + 7/7 + 10 ^ 6)^ 1 /2";

        final List<Tuple2<Integer, List<CompiledPortion>>> leveled = new ArrayList<>();
        final int maxLevel = getMaxLevel(toProcess, leveled);

        for (int levelToProcess = maxLevel; levelToProcess >= 0; levelToProcess--) {
            final int finalLevelToProcess = levelToProcess;

            joinLevels(leveled);

            final var levels = leveled.stream().filter(tuple -> {
                return tuple.getFirst() == finalLevelToProcess;
            }).collect(Collectors.toList());

            for (var level: levels) {
                final int index = leveled.indexOf(level);
                leveled.set(index, Tuple2.of(levelToProcess-1, compile(level.getSecond())));
            }
        }
        joinLevels(leveled);
        final var result = leveled.get(0).getSecond().stream().map(CompiledPortion::getPortion).collect(Collectors.toList());
        System.out.println(String.join("", result));
    }

    private static List<CompiledPortion> compile(List<CompiledPortion> expressions) {
        final List<CompiledPortion> split = splitForOperators(expressions);
        final List<CompiledPortion> compiled = new ArrayList<>();
        final TagBuilder tagBuilder = new TagBuilder();
        CompiledPortion lastSide = null;
        for (int i = 0; i < split.size(); i++) {
            final var side = split.get(i);
            final String portion = side.getPortion();
            if (portion.equals("/") || portion.equals("^")) {
                if (i > 0 && i < split.size() -1) {
                    final String lhs = lastSide.getPortion();
                    final var next = split.get(i+1);
                    final String rhs;
                    if (next.isCompiled()) {
                        rhs = next.getPortion();
                    } else {
                        rhs = compileTag(next.getPortion(), tagBuilder).getPortion();
                    }
                    String compiledTag = null;
                    if (side.equals("/")) {
                        compiledTag = tagBuilder.createFraction(lhs, rhs);
                    } else if (side.equals("^")) {
                        compiledTag = tagBuilder.createPower(lhs, rhs);
                    }
                    if (compiledTag != null) {
                        compiled.add(CompiledPortion.compiled(compiledTag));
                    }
                    i++;
                    continue;
                }
            }
            if (lastSide != null) {
                compiled.add(lastSide);
            }
            if (side.isCompiled()) {
                lastSide = side;
            } else {
                lastSide = compileTag(side.getPortion(), tagBuilder);
            }
        }
        if (lastSide != null) {
            compiled.add(lastSide);
        }
        return compiled;
    }

    private static CompiledPortion compileTag(String symbol, TagBuilder tagBuilder) {
        final String strippedSymbol = symbol.strip();
        final String compiledTag;
        if (strippedSymbol.equals("+") || strippedSymbol.equals("=") || strippedSymbol.equals("-") || strippedSymbol.equals("*")) {
            compiledTag = tagBuilder.createOperator(strippedSymbol);
        } else if (isNumeric(strippedSymbol)) {
            compiledTag = tagBuilder.createNumber(strippedSymbol);
        } else if (strippedSymbol.length() == 1) {
            compiledTag = tagBuilder.createIdentifier(strippedSymbol);
        } else {
            compiledTag = tagBuilder.createText(symbol);
        }
        return CompiledPortion.compiled(compiledTag);
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    private static List<CompiledPortion> splitForOperators(List<CompiledPortion> expressions) {
        final List<CompiledPortion> split = new ArrayList<>();
        for (var expression: expressions) {
            if (expression.isCompiled()) {
                split.add(expression);
            } else {
                split.addAll(splitExpressionForOperators(expression.getPortion()));
            }
        }
        return split;
    }

    private static List<CompiledPortion> splitExpressionForOperators(String expression) {
        final List<CompiledPortion> split = new ArrayList<>();
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
                    split.add(CompiledPortion.notCompiled(last.toString()));
                    split.add(CompiledPortion.notCompiled(Character.toString(c)));
                }
                last = new StringBuilder();
            }
        }
        if (last .length() > 0) {
            split.add(CompiledPortion.notCompiled(last.toString()));
        }
        return split;
    }

    private static void joinLevels(List<Tuple2<Integer, List<CompiledPortion>>> leveled) {
        final List<Tuple2<Integer, List<CompiledPortion>>> newLeveled = new ArrayList<>();
        Tuple2<Integer, List<CompiledPortion>> lastTuple = null;
        for (var tuple: leveled) {
            if (lastTuple == null) {
                lastTuple = tuple;
            } else if (lastTuple.getFirst().equals(tuple.getFirst())) {
                lastTuple.getSecond().addAll(tuple.getSecond());
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

    private static int getMaxLevel(String toProcess, List<Tuple2<Integer, List<CompiledPortion>>> leveled) {
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
                    final List<CompiledPortion> temp = new ArrayList<>();
                    temp.add(CompiledPortion.notCompiled(last.toString()));
                    leveled.add(Tuple2.of(lastLevel, temp));
                }
                last = new StringBuilder();
            }
            maxLevel = Math.max(maxLevel, level);
        }
        if (last.length() > 0) {
            final List<CompiledPortion> temp = new ArrayList<>();
            temp.add(CompiledPortion.notCompiled(last.toString()));
            leveled.add(Tuple2.of(level, temp));
        }
        return maxLevel;
    }

}
