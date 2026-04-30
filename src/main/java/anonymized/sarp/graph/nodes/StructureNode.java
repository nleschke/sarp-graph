package anonymized.sarp.graph.nodes;

public abstract class StructureNode extends ContentNode {
    /**
     * @param name         Name (label) of the node, usually File/Folder name
     * @param id           Unique id for the node
     */
    StructureNode(String name, String id) {
        super(name, id);
    }
}
