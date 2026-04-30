package anonymized.sarp.graph.nodes;

import java.util.ArrayList;

import org.jgrapht.Graph;
import org.jgrapht.GraphMetrics;

import anonymized.sarp.graph.SarpGraph;
import anonymized.sarp.graph.edges.SarpEdge;

/**
 * StructureNode representing a list, may only containt UNNAMED edges
 */
public class ListNode extends StructureNode {
    private ArrayList<SarpNode> children = new ArrayList<>();
    /**
     * @param name         Name (label) of the node, usually File/Folder name
     * @param id           Unique id for the node
     */
    public ListNode(String name, String id) {
        super(name, id);
    }
    public ListNode(String name, String id, SarpNode[] children) {
        super(name, id);
        addAllChildren(children);
    }

    public void addChild(SarpNode child) {
        children.add(child);
    }

     public void addAllChildren(SarpNode[] children) {
        for(SarpNode child : children) {
            addChild(child);
        }
    }

    public void addAllChilderen(SarpGraph graph) {
        for(SarpNode child : graph.getAncNodes(this)) {
            addChild(child);
        }
    }

    public SarpNode[] getChildren() {
        return children.toArray(new SarpNode[children.size()]);
    }

    public boolean sameChildrenType(){
        if(children.size() == 0){
            return false; // no children, so we cannot say they are of the same type
        }
        Class<?> firstType = children.get(0).getClass();
        for(SarpNode child : children){
            if(!child.getClass().equals(firstType)){
                return false;
            }
        }
        return true;
    }

    public boolean discoverMotif(SarpGraph graph){
        Graph<SarpNode, SarpEdge> firstSubgraph = graph.getSubGraph(children.get(0));
        long numberofTriangles = GraphMetrics.getNumberOfTriangles(firstSubgraph);
        if(numberofTriangles > 0){
            return true;
        }

        for(SarpNode child : children){
            Graph<SarpNode, SarpEdge> subgraph = graph.getSubGraph(child);
            //approximate edit distance by comparing edge and vertex sets, could be extended in the future by a more sophisticated graph edit distance algorithm, but this should be sufficient for now
             if(!(subgraph.vertexSet().size() == firstSubgraph.vertexSet().size() &&
                     subgraph.edgeSet().size() == firstSubgraph.edgeSet().size())){
                return false;
            }
        }
        return true;

    }

    public String[] getTextChildren(){
        ArrayList<String> textChildren = new ArrayList<>();
        for(SarpNode child : children){
            if(child instanceof ValueNode){
                ValueNode valueNode = (ValueNode) child;
                if(valueNode.getDataType().is(anonymized.sarp.graph.nodes.DataType.TEXT)){
                    textChildren.add(valueNode.getContentAString());
                }
            }
        }
        return textChildren.toArray(new String[textChildren.size()]);
    }

    public String toExtensiveString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ListNode: " + getName() + " (id: " + getId() + ")\n");
        sb.append("Children:\n");
        for(SarpNode child : children) {
            sb.append(" - " + child.toExtensiveString() + "\n");
        }
        return sb.toString();
    }
}
