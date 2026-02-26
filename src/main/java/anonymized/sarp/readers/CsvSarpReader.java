package anonymized.sarp.readers;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import anonymized.sarp.graph.SarpGraph;
import anonymized.sarp.graph.edges.EdgeRelation;
import anonymized.sarp.graph.edges.SarpEdge;
import anonymized.sarp.graph.nodes.MapNode;
import anonymized.sarp.graph.nodes.SarpNode;
import anonymized.sarp.graph.nodes.ValueNode;
import anonymized.sarp.utils.SarFileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class CsvSarpReader {
    /**
     * @param file
     * @return Sarp(sub-graph) with content of csv
     */

    private static Logger logger = Logger.getLogger(SarFileUtils.class.getName());

    /*
    Assumptions: seperator ;
    Can have header row, but not necessarily. If header row exists, it is the first row. We determine if header row exists by checking if all values in the first row are non-numeric and at least one value in the second row is numeric. If there is a header row, we use the values in the header row as column names. If there is no header row, we use "col1", "col2", etc. as column names.
    */
    public SarpGraph readCsv(File file, SarpNode hookNode) throws FileNotFoundException {
        SarpGraph graph = new SarpGraph(file.getAbsolutePath());
        System.out.println("Reading csv");

        List<String[]> rows;


    // read csv into list of string arrays, try with ; seperator first, if it fails, try again with , seperator
    char separator = ',';
    String firstLine;
    try {
        BufferedReader reader = Files.newBufferedReader(Paths.get(file.getAbsolutePath()));
        firstLine = reader.readLine();
        if (firstLine != null) {
            int commaCount = firstLine.length() - firstLine.replace(",", "").length();
            int semicolonCount = firstLine.length() - firstLine.replace(";", "").length();
            separator = (semicolonCount > commaCount) ? ';' : ',';
         }
        }catch(IOException e){
            System.err.println("Couldn't identify the separator for CSV file " + file.getName());
        }

        CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withCSVParser(
                        new CSVParserBuilder()
                                .withSeparator(separator)
                                .build()
                ).build();

        try {
            rows = reader.readAll();
        }catch (IOException | CsvException e) {
            System.err.println("Could not read csv file " + file.getAbsolutePath() + " with seperator '" + separator + "'. Please check if the file is a valid csv file and if the seperator is correct.");
            return graph; // return empty graph
        } 

        boolean headers = true;

        // algorithm of the year: header row if
        // a) all headers are string (if your header name is a number you deserve it) AND
        // b) the second row is not all string

        headers = rows.size() >= 2 &&
                Arrays.stream(rows.get(0)).noneMatch(CsvSarpReader::startsWithDigit) &&
                Arrays.stream(rows.get(1)).anyMatch(CsvSarpReader::startsWithDigit);

        logger.info("Headers for " + file + ": " + headers);

        String colPrefix = "col";
        String rowPrefix = "row";

        String rootID = hookNode.getId();
        //ContentNode root = new MapNode(rootID, rootID); 
        String rowsID = rootID + "_rows";
        String colsID = rootID + "_cols";
        MapNode rowsNode = new MapNode("rows", rowsID);
        MapNode colsNode = new MapNode("cols", colsID);

        graph.addVertex(hookNode);
        graph.setRootNode(hookNode);
        graph.addVertex(rowsNode);
        graph.addVertex(colsNode);
        graph.addEdge(hookNode, rowsNode, new SarpEdge(EdgeRelation.MAPS_TO));
        graph.addEdge(hookNode, colsNode, new SarpEdge(EdgeRelation.MAPS_TO));

        Map<String, MapNode> colNodes = new LinkedHashMap<>();
        String[] headerCols = new String[0];

        int ri = 0;
        for (String[] row : rows) {

            // read headers
            if (headers && headerCols.length == 0) {
                headerCols = row;
                continue;
            }

            ri++;
            String rowName = rowsID + "_" + ri;

            MapNode rowNode = new MapNode(rowName, rowName);
            graph.addVertex(rowNode);
            graph.addEdge(rowsNode, rowNode, new SarpEdge(EdgeRelation.CONTAINS));

            int ci = 0;
            for (String c : row) {
                ci++;
                String colName;
                if (headers) {
                    colName =  headerCols[ci - 1];
                } else {
                    colName = "col" + ci;
                }

                MapNode colNode;
                if (colNodes.containsKey(colName)) {
                    colNode = colNodes.get(colName);
                } else {
                    colNode = new MapNode(colName, colsID + "_" + colName);
                    graph.addVertex(colNode);
                    graph.addEdge(colsNode, colNode, new SarpEdge(EdgeRelation.CONTAINS));
                    colNodes.put(colName, colNode);
                }

                String cellName = rowName + "_" + colName;
//                logger.info(c);
                ValueNode cellNode = new ValueNode(cellName, cellName, c);
                graph.addVertex(cellNode);
                graph.addEdge(rowNode, cellNode, new SarpEdge(EdgeRelation.VALUE));
                graph.addEdge(colNode, cellNode, new SarpEdge(EdgeRelation.VALUE));

            }
        }

        // expected nodes: if headers: 1 + 2 + rows.size() + (colNodes.size()-1) + (rows.size() * (colNodes.size()-1)) else 1 + 2 + rows.size() + colNodes.size() + (rows.size() * colNodes.size())
        int expectedNodes = 3 + rows.size() + colNodes.size() + (rows.size() * colNodes.size());
        int expectedEdges = (2 * rows.size() * colNodes.size() + rows.size() + colNodes.size()+2);
        if(headers){
            expectedNodes = 3 + rows.size() + (colNodes.size()-1) + (rows.size() * (colNodes.size()-1));
            expectedEdges = (2 * rows.size() * (colNodes.size()-1) + rows.size() + (colNodes.size()-1)+2);
        }
        System.out.println("Finished reading csv with " + rows.size() + " rows and " + colNodes.size() + " columns. Includes header: " + headers + ". Hence, graph should have " + expectedNodes + " nodes (including root, row and column nodes) and " + expectedEdges + " edges.");
        System.out.println("Graph has " + graph.vertexSet().size() + " vertices and " + graph.edgeSet().size() + " edges.");
        return graph;
    }


    static boolean startsWithDigit(String str) {
        if(str == null || str.isEmpty()) {
            return false;
        }
        return Character.isDigit(str.charAt(0));
    }

}