package com.tuershen.nbtlibraryfix.minecraft.nbt.difference;

/**
 * @auther Tuershen update Date on 2020/12/3
 */
public class NBT_v1_11_R1 extends NBT_v1_10_R1 {

    private static NBT_v1_11_R1 nbt_v1_11_r1;

    public NBT_v1_11_R1(String version) {
        super(version);
    }

    public static NBT_v1_11_R1 getInstance(){
        return nbt_v1_11_r1;
    }

    public static void init(String version) { nbt_v1_11_r1 = new NBT_v1_11_R1(version); }
}
