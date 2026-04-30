package anonymized.sarp.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import anonymized.sarp.graph.nodes.DataType;
import anonymized.sarp.graph.nodes.ValueNode;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
//import edu.stanford.nlp.util.*;



public class NER {
    private static Set<String> personTags = Set.of("PERSON", "PER"); // needed because GER and EN models use different tags for person entities
    private static Set<String> organizationTags = Set.of("ORGANIZATION", "ORG"); // needed because GER and EN models use different tags for organization entities
    private static Set<String> locationTags = Set.of("LOCATION", "LOC"); // needed because GER and EN models use different tags for location entities
    private static Map<String, StanfordCoreNLP> languageMap = new HashMap<>();
        
    // could be used in the future if we want to extract entities from text nodes, e.g. to further process messages, or to extract keywords from large text files
        public static List<CoreLabel> recognizeEntities(String value, String language) {

        if(languageMap.size()<1){
            System.out.println("Initiating language Map");
            Properties deProps = new Properties();
            deProps.setProperty("annotators", "tokenize,ssplit,pos,ner");
            deProps.setProperty("tokenize.language", "de");
            deProps.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/german-fast.tagger");
            deProps.setProperty("ner.model", "edu/stanford/nlp/models/ner/german.conll.hgc_175m_600.crf.ser.gz");
            deProps.setProperty("ner.applyNumericClassifiers", "false");
            deProps.setProperty("ner.useSUTime", "false");
            StanfordCoreNLP dePipeline = new StanfordCoreNLP(deProps);
            languageMap.putIfAbsent("de", dePipeline);
            
            Properties enProps = new  Properties();
            enProps.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
            StanfordCoreNLP enPipeline = new StanfordCoreNLP(enProps);
            languageMap.putIfAbsent("en", enPipeline);         

        }
       
        StanfordCoreNLP pipeline = languageMap.get(language);
        CoreDocument document = new CoreDocument(value);
        pipeline.annotate(document);

        List<CoreLabel> entities = new ArrayList<>();
        for (CoreLabel token : document.tokens()) {
            String nerTag = token.ner();
            if (!nerTag.equals("O")) { // "O" means no entity
                entities.add(token);
                System.out.printf("Token: %-20s NER: %s%n", token.word(), nerTag);
            }
        }
        return entities;
    }

    public static boolean isEntity(ValueNode node, String language) {
        Properties props = new Properties();
        String value = node.getContentAString();

        // if the value contains multiple sentences, we do not want to run NER on it, because it is likely to be a large text (e.g. message content) and not an entity
        if(Arrays.stream(value.split("[.!?]"))
        .filter(s -> !s.isBlank())
        .count() >1 ){
            return false;
        }

        if (language.equals("de")) {
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        } else {
            props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        }

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        CoreDocument document = new CoreDocument(value);
        pipeline.annotate(document);

        List<CoreLabel> entities = new ArrayList<>();
        for (CoreLabel token : document.tokens()) {
            String nerTag = token.ner();
            if (!nerTag.equals("O")) { // "O" means no entity
                entities.add(token);
                System.out.printf("Token: %-20s NER: %s%n", token.word(), nerTag);
                if(personTags.contains(nerTag)){
                    node.setType(DataType.PERSON);
                }else if(organizationTags.contains(nerTag)){
                    node.setType(DataType.ORGANIZATION);
                }else if(locationTags.contains(nerTag)){
                    node.setType(DataType.LOCATION);
                }else if(nerTag.equals("CITY")){
                    node.setType(DataType.CITY);
                }else if(nerTag.equals("COUNTRY")){
                    node.setType(DataType.COUNTRY);
                }else if(nerTag.equals("STATE_OR_PROVINCE")){
                    node.setType(DataType.STATE_OR_PROVINCE);
                }else if(nerTag.equals("MONEY")){
                    node.setType(DataType.MONEY);
                }else if(nerTag.equals("TITLE")){
                    node.setType(DataType.TITLE);       
                } else if(nerTag.equals("NUMBER")){
                    node.setType(DataType.NUMBER);       
                } 
                return true;
            }
        }
        return false;
    }
}
