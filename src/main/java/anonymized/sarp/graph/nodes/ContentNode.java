package anonymized.sarp.graph.nodes;

public abstract class ContentNode extends SarpNode {


    /**
     * @param name         Name (label) of the node, usually File/Folder name
     * @param absolutePath Unique id for the node
     */
    ContentNode(String name, String id) {
        super(name, id);
    }
}
