package anonymized.sarp.graph.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public enum NodeType {
    EMPTY(null),
    FOLDER(null),
    FILE(null),
        STRUCTURED(FILE),
        SEMI_STRUCTURED(FILE),
            JSON(SEMI_STRUCTURED),
            XML(SEMI_STRUCTURED),
            HTML(SEMI_STRUCTURED),
            CSV(STRUCTURED),
        UNSTRUCTURED(FILE),
            TEXT(UNSTRUCTURED),
            IMAGE(UNSTRUCTURED),
            MEDIA(UNSTRUCTURED),
                PICTURES(MEDIA),
                VIDEOS(MEDIA),
                MUSIC(MEDIA),
        UNKNOWN(FILE),
    CONTENT(null),
        STRUCTURENODE(CONTENT),
        VALUE(CONTENT),
        ENTITY(CONTENT)
    ;
    private NodeType parent = null;
    private List<NodeType> children = new ArrayList<NodeType>();

    private NodeType(NodeType parent){
        this.parent = parent;
        if(this.parent !=null){
            this.parent.children.add(this);
        }
    }


    public boolean is(NodeType type){
        if (type != null) {
            for(NodeType t=this; t!=null; t=t.parent){
                if (type == t){
                    return true;
                }
            }
        }
        // else or if not found something to return true
        return false;
    }

    public NodeType[] getChildren(){
        return children.toArray(new NodeType[children.size()]);
    }


        public static NodeType getType(File file) {
        if (file.isDirectory() && file.listFiles().length == 0) {
            //todo: test, maybe we want to keep is directory, but add an empty child node if file.listFiles==0
            return NodeType.EMPTY;
        } else if (file.isDirectory()) {
            return NodeType.FOLDER;

        }
        //else: file is "file" and not folder!
        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
        if (fileName.contains("no") && fileName.contains("data")) {
            //specific catch phrase for e.g. facebook data with no_data.txt files
            return NodeType.EMPTY;
        } else if (extension.equals("json")) {
            return NodeType.JSON;
        } else if (extension.equals("xml")) {
            return NodeType.XML;
        } else if (extension.equals("html")) {
            return NodeType.HTML;
        } else if (extension.equals("csv")) {
            return NodeType.CSV;
        } else if (extension.equals("txt") || extension.equals("docx") || extension.equals("doc") || extension.equals("odt") || extension.equals("pdf") || extension.equals("md")) { //  other text documents (not known)
            return NodeType.TEXT;
        } else if (extension.equals("png") || extension.equals("jpg")) {
            return NodeType.IMAGE;
        } else if (extension.equals("mp3") || extension.equals("mp4") || extension.equals("wmv")) {
            return NodeType.MEDIA;
        } else if (extension.equals("db") || extension.equals("sqlitedb") || extension.equals("accdb") || extension.equals("nfs") || extension.equals("fp7")) {
            //extensions drawn from most common files: https://fileinfo.com/filetypes/database
            return NodeType.STRUCTURED;
        } else {
            return NodeType.UNKNOWN;
        }
    }

}
