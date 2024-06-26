package com.tuershen.nbtlibraryfix.minecraft.item.difference;

/**
 * @auther Tuershen update Date on 2020/12/3
 */
public class Item_v1_10_R1 extends Item_v1_9_R1_R2 {

    private static Item_v1_10_R1 item_v_1_10_r1;

    public static void init(String version){
        try {
            item_v_1_10_r1 = new Item_v1_10_R1();
            item_v_1_10_r1.itemStackClass = Class.forName("net.minecraft.server."+ version +".ItemStack");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Item_v1_10_R1 getInstance(){
        return item_v_1_10_r1;
    }

    @Override
    public Class<?> classItemStack() {
        return item_v_1_10_r1.itemStackClass;
    }

}
