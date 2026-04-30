package pepsys.sarp.readers;

import org.jgrapht.Graphs;

import pepsys.sarp.graph.SarpGraph;
import pepsys.sarp.graph.edges.EdgeRelation;
import pepsys.sarp.graph.edges.SarpEdge;
import pepsys.sarp.graph.nodes.ContentNode;
import pepsys.sarp.graph.nodes.FileNode;
import pepsys.sarp.graph.nodes.NodeType;
import pepsys.sarp.graph.nodes.SarpNode;
import pepsys.sarp.graph.nodes.ValueNode;
import pepsys.sarp.utils.SarpFileUtils;

import java.io.File;
import java.io.IOException;


import static java.nio.file.Files.readString;

public class ContentReader {

    private SarpGraph graph;

    public ContentReader(SarpGraph graph) {
        this.graph = graph;
    }

    /**
     * read all file nodes and construct new subgraphs based on file contents
     */
    public void readAllFileContents() {
        Object[] SarpNodes = graph.vertexSet().toArray(); //could use graph traversals if we had time for this...
        for (Object nodeObj : SarpNodes) {
            SarpNode node = (SarpNode) nodeObj;
            if (node.getClass().equals(FileNode.class)) {
                FileNode fileNode = (FileNode) node;
                addContentForFile(new File(fileNode.getId()));
            }
        }
    }


    private void addContentForFile(File file) {
        System.out.println("Adding content to graph from file " + file.getAbsolutePath());

        SarpNode hookNode = graph.getFileToNode().get(file.getAbsolutePath());
        SarpGraph contentGraph = fileToGraph(file,hookNode);
        Graphs.addGraph(graph, contentGraph);
        //graph.addEdge(hookNode, contentGraph.getRootNode(), new SarpEdge(EdgeRelation.CONTAINS));

        System.out.println("Add content for file " + file.getAbsolutePath() + " finished.");
    }

    public static SarpGraph fileToGraph(File file, SarpNode hookNode) {
        String fileName = file.getName();
        NodeType fileType = NodeType.getType(file);

        SarpGraph fileGraph = new SarpGraph(file.getAbsolutePath()); 

        try {
            switch (fileType) {
                case JSON:
                    JsonSarpReader json = new JsonSarpReader();
                    fileGraph = json.readJson(file, hookNode);
                    break;
                case CSV:
                    fileGraph = (new CsvSarpReader()).readCsv(file, hookNode);
                    break;
                //case HTML:
                //    HtmlSarReader html = new HtmlSarReader();
                //    fileGraph = html.readHtml(file);
                //    break;
                case TEXT:
                    fileGraph.addVertex(hookNode);
                    ContentNode textNode = textFileToNode(file);
                    fileGraph.addVertex(textNode);
                    fileGraph.addEdge(hookNode, textNode, new SarpEdge(EdgeRelation.VALUE));
                    break;
//            case UNKNOWN:
//                throw new UnknownFileExtensionException("Couldn't handle file: " + fileName + " Because the file type is unknown");
                default:
                    System.out.println("File " + fileName + " has an unusual file type, which we do not support (yet). Consequently, its content is not considered in the graph");
                    break;
            }

            assert(fileGraph.getRootNode() != null);

        } catch (IOException e) {
//            throw new RuntimeException(e);
            System.err.println("Error Reading file " + fileName + ": " + e);
        }
        System.out.println("Finished reading file " + fileName + " into graph with " + fileGraph.vertexSet().size() + " nodes and " + fileGraph.edgeSet().size() + " edges.");
        return fileGraph;
    }

    /**
     * return a graph containing a single value node with the text (for now)
     */
    public static ValueNode textFileToNode(File f) throws IOException {

        String content = readString(f.toPath());
        ValueNode n = new ValueNode(f.getName() + ".content", SarpFileUtils.createID(), content);

        //System.out.println("Read text: " + content); // debug
        return n;
    }


}
