package de.proxietv.configmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import de.proxietv.configmanager.objects.*;
import lombok.Getter;

public class IOHandler {

    @Getter private final ConfigContainer container;

    public IOHandler(ConfigContainer container){
        this.container = container;
    }

    public int load(){
        // prepare
        container.getRootTree().clear();

        int linesAmmount = 0;

        // start
        try{
            final BufferedReader reader = new BufferedReader(new InputStreamReader(this.container.openInputStream(), StandardCharsets.UTF_8));

            String line = null;

            Tree tree = container.getRootTree();

            while((line = reader.readLine()) != null){
                line = replaceFirstSpaces(line);
                linesAmmount++;

                if(line.length() == 0){
                    tree.addChild(new EmptyLine(tree));
                    continue;
                }

                final char firstChar = line.charAt(0);
                final char lastChar = line.charAt(line.length()-1);
                boolean newParent = false;

                if(firstChar == '#')
                    tree.addChild(new Comment(tree, replaceFirstSpaces(line.substring(1))));
                else if(line.contains(":")){
                    final String[] strs = line.split(":");
                    String value = "";

                    for(int i=1; i<strs.length; i++){
                        value += strs[i];

                        if(i+1<strs.length)
                            value += ":";
                    }

                    if(!line.startsWith("!")) {
                        if (replaceLastSpaces(line).endsWith(":")){
                            final BufferedReader reader2 = new BufferedReader(new InputStreamReader(this.container.openInputStream(), StandardCharsets.UTF_8));
                            for (int i = 1; i <= linesAmmount; i++) {
                                reader2.readLine();
                            }
                            List<String> values = new ArrayList<>();
                            String str = replaceFirstSpaces(reader2.readLine());
                            if (countLetter(line, ':') == 1 && replaceFirstSpaces(str).startsWith("-") && str != null && !str.isEmpty()) {
                                values.add(replaceFirstSpaces(replaceFirstSpaces(str).substring(1)));
                                while (str.startsWith("-")) {
                                    String str2 = reader2.readLine();
                                    if (str2 != null && !str2.isEmpty()){
                                        str = replaceFirstSpaces(str2);
                                        values.add(str.substring(1));
                                    } else {
                                        str = "()";
                                    }
                                }
                                Config c = new Config(replaceLastSpaces(strs[0]), tree, replaceLastSpaces(replaceFirstSpaces(getStringFromValues(values))));
                                tree.addChild(c);
                                c.setConfigType(Config.TYPE_CONFIGLIST);
                            } else {
                                tree.addChild(new Config(replaceLastSpaces(strs[0]), tree, replaceFirstSpaces(value)));
                            }
                        } else {
                            tree.addChild(new Config(replaceLastSpaces(strs[0]), tree, replaceFirstSpaces(value)));
                        }
                    } else
                        tree.addChild(new Description(tree, replaceLastSpaces(strs[0]).substring(1), replaceFirstSpaces(value)));
                }else if(lastChar == '{'){
                    final String name = replaceLastSpaces(line.substring(0, line.length()-1));

                    tree = new Tree(name, tree);
                    tree.getParent().addChild(tree);
                    newParent = true;
                }else if(replaceLastSpaces(line).equals("}")){
                    tree = tree.getParent();
                    newParent = true;

                    if(tree == null){
                        reader.close();
                        return IOResult.RESULT_FAILED_LOAD_NOTVALID;
                    }

                }else
                    tree.addChild(new EmptyLine(tree));

                if(!newParent){
                    // add list item for every config
                    final String value = replaceLastSpaces(line);

                    tree.getRawChilds().add(value);
                }
            }

            reader.close();

            if(!tree.equals(container.getRootTree()))
                return IOResult.RESULT_FAILED_LOAD_NOTVALID;

        }catch(IOException e){
            e.printStackTrace();

            return IOResult.RESULT_FAILED_UNKOWN;
        }

        return IOResult.RESULT_SUCCESS;
    }

    public int save(){
        // start
        try{
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.container.openOutputStream(), StandardCharsets.UTF_8));
            final List<String> lines = getLines(container.getRootTree(), "");

            System.out.println(lines.size());
            for(String line:lines){
                writer.write(line);
                writer.newLine();
            }

            writer.close();
        }catch(IOException e){
            e.printStackTrace();

            return IOResult.RESULT_FAILED_UNKOWN;
        }


        return IOResult.RESULT_SUCCESS;
    }

    private List<String> getLines(Tree tree, String currentPrefix){
        return getLines(tree, currentPrefix, tree.equals(container.getRootTree()));
    }

    private List<String> getLines(Tree tree, String currentPrefix, boolean root){
        final List<String> lines = new ArrayList<>();
        int i = 0;
        for(Config c:tree.getChilds()){
            if (i == 0){
                if(c.getType() == Config.TYPE_TREE){

                    lines.add(currentPrefix + c.getName() + " {");
                    lines.addAll(getLines((Tree) c, currentPrefix + "	", false));
                    lines.add(currentPrefix + "}");

                }else if(c.getType() == Config.TYPE_COMMENT)
                    lines.add(currentPrefix + "# " + c.getValue());
                else if(c.getType() == Config.TYPE_EMPTYLINE)
                    lines.add("");
                else if(c.getType() == Config.TYPE_DESCRIPTION)
                    lines.add(currentPrefix + "!" + c.getName() + ": " + c.getValue());
                else if(c.getType() == Config.TYPE_LISTITEM)
                    lines.add(currentPrefix + c.getValue());
                else {
                    if (c.getConfigType() == Config.TYPE_CONFIGLIST){
                        lines.add(currentPrefix + c.getName() + ":");
                        i = c.getList().size();
                        for (int i1 = 0; i1 < c.getList().size(); i1++) {
                            lines.add("  - " + c.getList().get(i1));
                        }
                    } else {
                        lines.add(currentPrefix + c.getName() + ": " + c.getValue());
                    }
                }
            } else {
                i = i - 1;
            }
        }
        System.out.println(lines.size());
        return lines;
    }

    private static String replaceFirstSpaces(String str){
        if (str != null){
        while(str.startsWith(" ") || str.startsWith("	"))
            str = str.substring(1, str.length());

            return str;
        }
        return null;
    }

    private static String replaceLastSpaces(String str){
        if (str != null){
            while(str.endsWith(" ") || str.endsWith("	"))
                str = str.substring(0, str.length()-1);

            return str;
        }
        return null;
    }
    private int countLetter(String str, char letter) {
        str = str.toLowerCase();
        letter = Character.toLowerCase(letter);
        int count = 0;
        for (int pos = -1; (pos = str.indexOf(letter, pos + 1)) != -1; count++) ;
        return count;
    }

    public static List<String> getValuesFromString(String string){
        String[] values = string.split("~-#-~");
        List<String> list = new ArrayList<>();
        for (int i = 0; i<values.length; i++){
            list.add(values[i]);
        }
        return list;
    }

    public static String getStringFromValues(List<String> values){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i<values.size(); i++){
            sb.append(replaceFirstSpaces(replaceLastSpaces(values.get(i))) + "~-#-~");
        }
        return sb.toString();
    }
}