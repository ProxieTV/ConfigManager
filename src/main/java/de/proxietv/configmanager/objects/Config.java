package de.proxietv.configmanager.objects;

import java.util.ArrayList;
import java.util.List;

import de.proxietv.configmanager.IOHandler;
import lombok.Getter;

public class Config {

    public static final byte TYPE_TREE = 0x0;
    public static final byte TYPE_CONFIG = 0x1;
    public static final byte TYPE_COMMENT = 0x2;
    public static final byte TYPE_EMPTYLINE = 0x3;
    public static final byte TYPE_DESCRIPTION = 0x4;
    public static final byte TYPE_LISTITEM = 0x5;
    public static final byte TYPE_CONFIGLIST = 0x6;

    @Getter private final String name;
    @Getter private final Tree parent;

    private byte type = TYPE_CONFIG;

    @Getter public String value;

    public Config(String name, Tree parent){
        this(name, parent, null);
    }

    public Config(String name, Tree parent, String value){
        this.name = name;
        this.parent = parent;
        this.value = value;
    }

    public byte getType(){
        return TYPE_CONFIG;
    }

    public byte getConfigType(){
        if (type == TYPE_CONFIGLIST){
            return TYPE_CONFIGLIST;
        } else {
            return TYPE_CONFIG;
        }
    }

    public void setConfigType(byte b){
        type = b;
    }

    public List<String> getList(){
        return IOHandler.getValuesFromString(value);
    }

    public String getAbsolutePath(){
        return parent != null ? parent.getAbsolutePath() + (!parent.getAbsolutePath().isEmpty() ? "." : "") + name : "";
    }

    public void setValue(List<String> values){
        value = IOHandler.getStringFromValues(values);
        setConfigType(TYPE_CONFIGLIST);
    }

    public void setValue(String value){
        this.value = value;
    }

    public String getString(){
        return getValue();
    }

    public int getInt(){
        return Integer.valueOf(value);
    }
    public Boolean getBoolean(){
        return Boolean.valueOf(value);
    }

    public static List<String> valuesToString(List<Config> configs){
        final List<String> list = new ArrayList<String>();

        for(Config c:configs)
            list.add(c.value);

        return list;
    }

}