package anonymized.sarp.graph.nodes;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class ValueNode extends ContentNode {

    Object content;
    DataType type;


    /**
     * @param name    Name (label) of the node, usually File/Folder name
     * @param id      Unique id for the node
     * @param content Content (value) that should be stored
     */
    public ValueNode(String name, String id, Object content) {
        super(name, id);
        this.content = content;
        inferBasicType();
    }


    public String getContentAString() {
        return content.toString();
    }

    public String toExtensiveString() {
//        return "(Value_Node_" + getName() + "_of_type_" + type + "_and_value_" + content + ")";
        return "[VN: #" + getId() + " " + getName() + "=" + getContentAString()+ "]";
    }

    public String toString() {
        return getContentAString();
    }
    private void inferBasicType() {
        //System.out.println("Inferring basic type for value: " + content + " of class " + content.getClass());
        if (content instanceof Number) {
            type = DataType.NUMBER;
        } else if (content instanceof Boolean) {
           // System.out.println("--------Content is a boolean: " + content);
            type = DataType.BOOLEAN;
        } else if (content instanceof String) {
            String value = (String) content;

            try {
                double number = Double.parseDouble(value);
                type = DataType.NUMBER;
                return;
            } catch (NumberFormatException e) {
                // do nothing, not a number
            }

            if(value.matches("true") || value.matches("false")){
                type = DataType.BOOLEAN;
                return;
            }

            String[] splits = value.split("/");
            if (splits.length>2 && value.matches("^\\/?([a-zA-Z0-9._~!$&'()*+,;=:@-]+\\/)*[a-zA-Z0-9._~!$&'()*+,;=:@-]*$")) {
                try {
                Paths.get(value);
                System.out.println("------Value is a valid URI: " + value);
                type = DataType.URI;
            } catch (InvalidPathException | NullPointerException ex) {
                // not a valid URI, do nothing
                System.out.println("-------Value is not a valid URI " + value);
                type = DataType.TEXT;
            }
            } else {
                type = DataType.TEXT;
            }

        } else {
            type = DataType.UNKNOWN;
        }
    }

    public DataType getDataType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }
}
