package com.tuershen.nbtlibraryfix.minecraft.nbt;

import com.tuershen.nbtlibraryfix.annotation.TagAnnotation;

import java.io.Serializable;
import java.util.Arrays;

public class TagLongArray extends TagBase implements Serializable {

    private static final long serialVersionUID = 221815319446814580L;

    private long[] data;

    public TagLongArray(long[] data){
        this.data = data;
    }

    public TagLongArray(){}

    public long[] getData() {
        return data;
    }

    @TagAnnotation( tagType = "longArray")
    public void setData(long[] data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "TagLongArray{data=" + Arrays.toString(data) + "}";
    }

    @Override
    public Object data() {
        return this.data;
    }

    @Override
    public byte getTypeId() {
        return 12;
    }
}
