package com.tuershen.nbtlibraryfix.minecraft;


import com.tuershen.nbtlibraryfix.api.NBTTagCompoundApi;
import com.tuershen.nbtlibraryfix.common.AbstractNBTTagCompound;
import com.tuershen.nbtlibraryfix.minecraft.item.AbstractMinecraftItem;

import java.lang.reflect.Field;

public class MinecraftItemStack {

//    private static Class<?> minecraftItemStackClass;
//
//    private static Class<?> nbtTagCompoundClass;


    private volatile Object minecraftItemStack;

    public volatile AbstractMinecraftItem item;

    public <T> MinecraftItemStack(T obj){
        if (obj == null){
            throw new NullPointerException("[NBTLibrary] "+MinecraftItemStack.class.getPackage().getName()+".MinecraftItemStack T null");
        }
        this.minecraftItemStack = obj;
    }

//    public synchronized <T> void setMinecraftItemStack(T obj){
//        if (obj == null){
//            throw new NullPointerException("[NBTLibrary] "+MinecraftItemStack.class.getPackage().getName()+".setMinecraftItemStack T null");
//        }
//        this.minecraftItemStack = obj;
//    }

    public synchronized Object getMinecraftItemStack(){
        return this.minecraftItemStack;
    }

    public synchronized static void initMinecraftItemStackClass(){
//        minecraftItemStackClass = AbstractMinecraftItem.getInstance().classItemStack();
//        nbtTagCompoundClass = AbstractMinecraftNBTTag.getInstance().getNBTTagClass((byte) 10);
    }

    public synchronized <T> void setMinecraftItemStackTag(T obj){
        if (this.minecraftItemStack == null){
            throw new NullPointerException("[NBTLibrary] "+MinecraftItemStack.class.getPackage().getName()+".minecraftItemStack null");
        }
        this.set(obj);
    }

    public synchronized NBTTagCompoundApi getMinecraftItemStackTag(){
        if (this.minecraftItemStack == null){
            throw new NullPointerException("[NBTLibrary] "+MinecraftItemStack.class.getPackage().getName()+".minecraftItemStack null");
        }
        return getCompound();
    }

    synchronized <T> void set(T obj){
        try {
            Field setTag;
            setTag = this.getNMSField();
            setTag.setAccessible(true);
            setTag.set(this.minecraftItemStack , obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    synchronized NBTTagCompoundApi getCompound(){
        Object tag = null;
        Field getTag;
        try {
            getTag = this.getNMSField();
            getTag.setAccessible(true);
            tag = getTag.get(this.minecraftItemStack);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (tag == null){
            tag = AbstractNBTTagCompound.newCompound();
        }
        return AbstractNBTTagCompound.getMinecraftNBTTag(tag);
    }

    synchronized Field getNMSField() {
        Field[] fields = this.getMinecraftItemStack().getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getType().getSimpleName().equalsIgnoreCase("NBTTagCompound")) {
                return fields[i];
            }
        }
        return null;
    }

}
