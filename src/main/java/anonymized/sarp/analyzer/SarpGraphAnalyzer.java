package anonymized.sarp.analyzer;

import anonymized.sarp.graph.SarpGraph;
import anonymized.sarp.graph.edges.SarpEdge;
import anonymized.sarp.graph.nodes.*;

import org.apache.commons.validator.GenericValidator;
import org.jgrapht.alg.scoring.ClosenessCentrality;
import org.jgrapht.traverse.BreadthFirstIterator;

import static anonymized.sarp.analyzer.NER.isEntity;

import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SarpGraphAnalyzer {


    private SarpGraph graph;

    public SarpGraphAnalyzer(SarpGraph graph) {
        this.graph = graph;
    }

    public void analyze() {
        BreadthFirstIterator<SarpNode, SarpEdge> iterator = new BreadthFirstIterator<>(graph);
        ClosenessCentrality<SarpNode, SarpEdge> closenessCentrality = new ClosenessCentrality<>(graph);
        long nerTime = 0;
        while (iterator.hasNext()) {
            SarpNode node = iterator.next();
            node.addStat("depth", iterator.getDepth(node));
            node.addStat("nodeType", node.getClass().getSimpleName());
            node.addStat("closenessCentrality", closenessCentrality.getVertexScore(node));
            node.addStat("totalAncestors", graph.getAncNodes(node).length);
        
            if (node.getClass().equals(ValueNode.class)) {
                ValueNode valueNode = (ValueNode) node;
                // refine type
                System.out.println("Refining type for node: " + valueNode + " of type " + valueNode.getType());
                //simple types      

                if(valueNode.getDataType().is(DataType.URI)){
                    // resolve relative paths if possible, e.g. if the value is a file path, we can check if it exists and if so, we can add the file graph stats to the node
                    isURIresolvable(valueNode);
                } else if(valueNode.getDataType().is(DataType.TEXT)){
                    // Refine type 1 complex type
                    String value = valueNode.getContentAString();
                    if(GenericValidator.isDate(value, Locale.getDefault())){
                        valueNode.setType(DataType.DATE);
                    }else if(GenericValidator.isEmail(value)){
                        valueNode.setType(DataType.EMAIL);
                    }else if(GenericValidator.isUrl(value)){
                        valueNode.setType(DataType.URL);
                    }else if(GenericValidator.isCreditCard(value)){
                        valueNode.setType(DataType.CREDITCARD); 
                    }else{
                        // refine type 2 discovered type 
                        // Try NER: is name, organization, location

                            System.out.println("=== NER ===");
                            long startTime = System.currentTimeMillis();
                            if(isEntity(valueNode, "en")){
                                System.out.println("Entity detected (en), type set to " + valueNode.getType());
                            }else if(isEntity(valueNode, "de")){
                                System.out.println("=== German NER ===");
                                System.out.println("Entity detected (de), type set to " + valueNode.getType()); 
                            }
                            long endTime = System.currentTimeMillis();
                            nerTime += (endTime - startTime);
                            // else: no entity detected, do nothing
                    }
    
                }
                
            } else if (node instanceof StructureNode) {
//                setFileGraphStats(node, );
                StructureNode strNode = (StructureNode) node;
                if (node.getClass().equals(ListNode.class)) {
                    ListNode listNode = (ListNode) node;
                    listNode.addAllChilderen(graph);
                    if(listNode.sameChildrenType()){
                        node.addStat("collection_of_same_type", true);
                        if(listNode.getChildren()[0] instanceof ValueNode){
                            ValueNode child = (ValueNode) listNode.getChildren()[0];
                            if(child.getDataType().is(DataType.NUMBER)){
                                List<Double> values = Arrays.stream(listNode.getChildren())
                                        .map(n -> (ValueNode) n)
                                        .map(vn -> Double.parseDouble(vn.getContentAString()))
                                        .collect(Collectors.toList());
                                double avg = values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                                double min = values.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                                double max = values.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                                node.addStat("avg", avg);
                                node.addStat("min", min);
                                node.addStat("max", max);
                            }else if(child.getDataType().is(DataType.TEXT)){
                                List<Integer> lengths = Arrays.stream(listNode.getChildren())
                                        .map(n -> (ValueNode) n)
                                        .map(vn -> vn.getContentAString().length())
                                        .collect(Collectors.toList());
                                double avg = lengths.stream().mapToInt(Integer::intValue).average().orElse(0.0);
                                int min = lengths.stream().mapToInt(Integer::intValue).min().orElse(0);
                                int max = lengths.stream().mapToInt(Integer::intValue).max().orElse(0);
                                node.addStat("avg_length", avg);
                                node.addStat("min_length", min);
                                node.addStat("max_length", max);

                                // most common 3-gram in the text 
                                String [] textStrings = listNode.getTextChildren();
                                Map<String, Long> ngrams = Arrays.stream(textStrings)
                                        .flatMap(s -> {
                                            String[] tokens = s.split("\\s+");
                                            if (tokens.length < 3) {
                                                return Arrays.stream(new String[0]);
                                            }
                                            String[] trigrams = new String[tokens.length - 2];
                                            for (int i = 0; i < tokens.length - 2; i++) {
                                                trigrams[i] = tokens[i] + " " + tokens[i + 1] + " " + tokens[i + 2];
                                            }
                                            return Arrays.stream(trigrams);
                                        })
                                        .collect(Collectors.groupingBy(s -> s, Collectors.counting()));

                                System.out.println("Most common 3-grams: ");
                                ngrams.entrySet().stream()
                                        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                        .limit(5)
                                        .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
                                       node.addStat("n-gram", ngrams);
                        }
                    }else if(listNode.discoverMotif(graph)){
                        // Motif Discovery: are all subtrees of same structure, if so, are there non-contain labels that build a structure?
                        // for now only validated for triangles, but could be extended in the future
                        node.addStat("motif", "triangle");
                    }
                } else if (node.getClass().equals(MapNode.class)) {
                    // Motif Discovery: are all subtrees of same structure, if so, are there non-contain labels that build a structure?
                    // for now only validated for triangles, but could be extended in the future
                    node.addStat("motif", "triangle");
                }

                // if node only contains valuenodes as children, we can run some statistics
                SarpNode[] children = graph.getAncNodes(node);
                if (children.length > 0 && Arrays.stream(children).allMatch(n -> n instanceof ValueNode)) {
                    node.addStat("groupParent", true);

                    List<ValueNode> vns = Arrays.stream(children)
                            .map(n -> (ValueNode) n)
//                            .map(n -> n.getClass().getSimpleName())
//                            .peek(c -> System.out.println(c))
                            .collect(Collectors.toList());
                    NodeGroupAnalyzer nga = new NodeGroupAnalyzer(node, vns);
                    nga.analyze();
                }
                } else if (node instanceof FileNode) {
                //graphstats
//                setFileGraphStats(node, closenessCentrality.getVertexScore(node));
//                file.addStat("depth", )
                // height: max number of levele
                } else if (node instanceof FolderNode) {
                //add graph stats
                node.addStat("centrality", closenessCentrality.getVertexScore(node));
                }
            }
        }
        System.out.println("Total NER time for graph " + graph.getName() + ": " + nerTime + " ms.");
    }

    public void printAllStats() {
        System.out.println("STATS: ");
        graph.vertexSet().forEach(node -> {
            if (!node.getStats().isEmpty()) {
                System.out.println(node.getStats());
            }
        });
    }

    private boolean isURIresolvable(ValueNode UriNode) {
        String uri_value = UriNode.getContentAString();
        if(!uri_value.startsWith("/")){
            String root = graph.getRootPath();
            //root = root.replace("/", "\\");
            String newValue = root+"\\"+uri_value;
            System.out.println("Added prefix to path " + newValue);
            uri_value = newValue.replace("/","\\");
        }
        //URI nodeURI = URI.create(value);
        // iterate over all nodes an when file, check if their id matches the value
        Set<SarpNode> nodes = graph.vertexSet();
        for(SarpNode node : nodes){
            if(node instanceof FileNode){
                FileNode fileNode = (FileNode) node;
                try{
                   
                    String fileNodeURI = fileNode.getPath();
                    //System.out.println("Comparing Path Value " + uri_value + " with file node path " + fileNodeURI);
                    if(uri_value.equals(fileNodeURI)){
                        graph.addDirectedEdge(UriNode, fileNode);
                        System.out.println("***********Value " + uri_value + " is a URI pointing to file node " + fileNode.getName() + ". Graph has " + graph.vertexSet().size() + " nodes and " + graph.edgeSet().size() + " edges.");
                        return true;
                    }else if((fileNode.getName()).matches("\\d+")){
                        // workaround: if the file name is 4 digits, we compare only prefixes
                        String uriNodeFileEnding = uri_value.substring(uri_value.lastIndexOf("."));
                        String uriNodeFileName = uri_value.substring(uri_value.lastIndexOf("\\")+1);
                        String fileNodeFileName = fileNodeURI.substring(fileNodeURI.lastIndexOf("\\")+1);
                        String fileEnding = fileNode.getPath().substring(fileNode.getPath().lastIndexOf("."));
                        boolean endingMatches = uri_value.endsWith(fileEnding);
                        boolean prefixMatches = uriNodeFileName.startsWith(fileNode.getName());
                        if( prefixMatches && endingMatches){
                            
                            graph.addDirectedEdge(UriNode, fileNode);
                            System.out.println("***********Value " + uri_value + " is a URI pointing to file node " + fileNode.getName() + ". Figured it out with the workaround. Graph has now " + graph.edgeSet().size() + " edges.");
                            return true;
                        }
                    }
                }catch(InvalidPathException | NullPointerException ex){
                // not a valid path or file does not exist, do nothing
                }
            }
        }
        System.out.println("---------Value " + uri_value + " is a URI but could not be resolved to any file node in the graph");
        return false;
    }

}
