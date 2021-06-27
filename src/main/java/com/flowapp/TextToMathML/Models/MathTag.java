package com.flowapp.TextToMathML.Models;

public enum MathTag {

    ACTION("action"),
    MATH("ath"),
    ENCLOSE("enclose"),
    ERROR("error"),
    FENCED("fenced"),
    FRACTION("frac"), // <mfrac> numerator denominator </mfrac>
    GLYPH("glyph"),
    IDENTIFIER("i"),
    MULTI_SCRIPTS("multiscripts"),
    NUMERIC("n"),
    OPERATOR("o"),
    OVER("over"), // <mover> base overscript </mover>
    UNDER("under"), // <munder> base underscript </munder>
    LIMIT("overunder"), // </munderover> base underscript overscript </munderover>
    PADDED("padded"),
    INVISIBLE_SPACE("phantom"),
    ROOT("root"),
    ROW("row"),
    STRING("s"), // quoted
    TEXT("text"),
    SPACE("space"),
    SQRT("sqrt"),
    STYLE("style"),
    SUBSCRIPT("sub"), //<msub> base subscript </msub>
    SUPERSCRIPT("sup"), // <msup> base superscript </msup>
    SUBSUP("subsup"), // <msubsup> base subscript superscript </msubsup>
    TABLE("table"),
    TABLE_CELL("td"),
    LABELED_TABLE_ROW("labeledtr"),
    TABLE_ROW("tr")
    ;

    private final String tag;
    private final String options;

    MathTag(String tag) {
        this.tag = tag;
        this.options = "";
    }

    MathTag(String tag, String options) {
        this.tag = tag;
        this.options = options;
    }

    public String enclose(String data) {
        return enclose(data, "");
    }

    public String enclose(String data, String options) {
        return "<m" + tag + (options.isBlank() ? "" : (" "+ options))  + ">" + data + "</m" + tag + ">";
    }
}
