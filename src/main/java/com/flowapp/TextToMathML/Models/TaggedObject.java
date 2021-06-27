package com.flowapp.TextToMathML.Models;

public class TaggedObject {
    private final Object object;
    private final MathTag tag;

    public TaggedObject(Object object, MathTag tag) {
        this.object = object;
        this.tag = tag;
    }

    public static TaggedObject of (Object object, MathTag tag) {
        return new TaggedObject(object, tag);
    }

    public Object getObject() {
        return object;
    }

    public MathTag getTag() {
        return tag;
    }
}
