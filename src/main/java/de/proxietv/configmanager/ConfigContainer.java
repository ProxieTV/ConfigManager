package de.proxietv.configmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import de.proxietv.configmanager.objects.Config;
import de.proxietv.configmanager.objects.Tree;
import lombok.Getter;

public abstract class ConfigContainer {

    public static final String VERSION = "2.2";

    public boolean getConfigNeverNull = false;

    private final IOHandler ioHandler;
    @Getter private final ConfigPicker picker;

    @Getter private final Tree rootTree = new Tree(this);

    public ConfigContainer(){
        this(false);
    }

    public ConfigContainer(boolean getConfigNeverNull){
        this.ioHandler = new IOHandler(this);
        this.picker = new ConfigPicker(this);

        this.getConfigNeverNull = getConfigNeverNull;
    }

    public abstract InputStream openInputStream() throws IOException;

    public abstract OutputStream openOutputStream() throws IOException;



    public void clear(){
        this.rootTree.clear();
        this.picker.getAllConfigs().clear();
    }

    /**
     *
     * @return Returns the result of the class IOResult
     */
    public int load(){
        return ioHandler.load();
    }

    /**
     *
     * @return Returns the result of the class IOResult
     */
    public int save(){
        return ioHandler.save();
    }
}