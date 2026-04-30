package pepsys.sarp.graph.nodes;


import java.io.File;
import java.util.HashMap;

public class FileNode extends SarpNode{
    protected String path;
    protected NodeType type;

    // only available for (semi-)structured content
    protected File content;

    // only available after SarpGraph.analyze()
    protected HashMap<String, Object> stats;


    /**
     * Constructor for Semistructured Files
     * @param name
     * @param path
     * @param type
     * @param file
     */
    public FileNode(String name, String path, NodeType type, File file) {
        super(name, path);
        this.type = type;
        this.path = path;
        this.content = file;
    }

    /**
     * Constructor for unstructured Files
     * @param name
     * @param path
     * @param type
     */
    public FileNode(String name, String path, NodeType type) {
        super(name, path);
        this.type = type;
        this.path = path;
    }
    /*
    public URI getRelativePath() {
        // current path is absolute, we want to convert it to a relative path, so that we can easily compare paths across different machines
        URI fileUri = new File(path).toURI();
        URI relativeUri = fileUri; // default to absolute path if relative path cannot be determined
        try{        
            File currentDirFile = new File(".");
            URI currentDirUri = currentDirFile.toURI();
            
            relativeUri = currentDirUri.relativize(fileUri);
        }catch(InvalidPathException | NullPointerException ex){
            System.err.println("Could not convert path " + path + " to relative path. Returning absolute path instead.");
            return fileUri;
        }
        return relativeUri;
    }
     */
    public String getPath(){
        return path;
    }
    
    public boolean isText(){
        return (type.equals(NodeType.TEXT));
    }

    public boolean isStructured(){
        return (type.is(NodeType.STRUCTURED));
    } 

    public boolean isSemiStructured(){
        return (type.is(NodeType.SEMI_STRUCTURED));
    }

    public boolean isUnstructured(){
        return (type!=NodeType.EMPTY || type!=NodeType.STRUCTURED || type!=NodeType.SEMI_STRUCTURED);
    }
}
