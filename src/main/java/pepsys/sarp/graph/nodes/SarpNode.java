package pepsys.sarp.graph.nodes;

import java.util.HashMap;

public abstract class SarpNode {
    private String name = null;
    private String id; // id = absoulte path
    private HashMap<String, Object> stats;
    private NodeType type; // type = folder, file, content, empty

    /**
     * @param name         Name (label) of the node, usually File/Folder name
     * @param absolutePath Unique id for the node, usually absulute path of file/folder
     */
    SarpNode(String name, String absolutePath) {
        this.name = name;
        this.id = absolutePath;
        type = determineType();
        stats = new HashMap<>();
    }

    private NodeType determineType() {
        if (name == null || name.isEmpty()) {
            return NodeType.EMPTY;
        } else if (this instanceof FolderNode) {
            return NodeType.FOLDER;
        } else if (this instanceof FileNode) {
            return NodeType.FILE;
        } else if(this instanceof ContentNode){
            if(this instanceof StructureNode){
                return NodeType.STRUCTURENODE;
            }
            else if(this instanceof ValueNode){
                return NodeType.VALUE;
            } else{
                return NodeType.CONTENT;
            }
        } else{
            return NodeType.UNKNOWN;

        }
    } 
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String toExtensiveString() {
        return "(Sar_Node_" + name + " type " + type +")";
    }

    public String toString() {
        return name;
    }

    public String getType() {
        return type.toString();
    }
    public void addStat(String name, Object value) {
        stats.put(name, value);
    }

    public HashMap<String, Object> getStats() {
        return stats;
    }
}
