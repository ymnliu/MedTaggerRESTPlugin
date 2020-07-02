package org.ohnlp.medtagger.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.mayo.dhs.uima.server.api.UIMANLPResultSerializer;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.cas.FSArray;
import org.ohnlp.medtagger.type.ConceptMention;
import org.ohnlp.medxn.type.Drug;
import org.ohnlp.medxn.type.MedAttr;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DrugResultSerializer implements UIMANLPResultSerializer{
    @Override
    public JsonNode serializeNLPResult(CAS cas) {
        try {
            List<JsonNode> out = new LinkedList();
            Collection<Drug> drug = JCasUtil.select(cas.getJCas(), Drug.class);
            drug.forEach(d -> {
                out.add(serialize(d));
            });
            return new ObjectMapper().valueToTree(out);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serialize drug string based on MedXN outputs
     * @param drug
     * @return ret
     * @throws CASException
     */
    private JsonNode serialize(Drug drug) {

        ObjectNode ret = JsonNodeFactory.instance.objectNode();

        String drugName = drug.getName().getCoveredText();
        String drugNameRxcui = drug.getName().getSemGroup();
        String normRxname = drug.getNormRxName2()==null ? "" : drug.getNormRxName2();
        String normRxcui = drug.getNormRxCui2()==null ? "" : drug.getNormRxCui2();
        String drugNameInfo = drugName+"::"+drug.getName().getBegin()+"::"+drug.getName().getEnd();
        String strengthInfo = "";
        String dosageInfo = "";
        String formInfo = "";
        String routeInfo = "";
        String frequencyInfo = "";
        String durationInfo = "";

        int drugSenBegin = drug.getName().getSentence().getBegin();
        int drugSenEnd = drug.getName().getSentence().getEnd();

        FSArray attrs = drug.getAttrs();

        for(int i=0; i<attrs.size(); i++) {
            MedAttr ma = (MedAttr)attrs.get(i);
            String info = ma.getCoveredText()+"::"+ma.getBegin()+"::"+ma.getEnd();
            if(ma.getTag().equals("strength")) {
                if(strengthInfo.equals("")) strengthInfo = info;
                else strengthInfo += "`" + info ;
            }
            else if(ma.getTag().equals("dosage")) {
                if(dosageInfo.equals("")) dosageInfo = info;
                else dosageInfo += "`" + info ;
            }
            else if(ma.getTag().equals("form")) {
                if(formInfo.equals("")) formInfo = info;
                else formInfo += "`" + info ;
            }
            else if(ma.getTag().equals("route")) {
                if(routeInfo.equals("")) routeInfo = info;
                else routeInfo += "`" + info ;
            }
            else if(ma.getTag().equals("frequency")) {
                if(frequencyInfo.equals("")) frequencyInfo = info;
                else frequencyInfo += "`" + info ;
            }
            else if(ma.getTag().equals("duration")) {
                if(durationInfo.equals("")) durationInfo = info;
                else durationInfo += "`" + info ;
            }
        }

        drugNameRxcui = drugNameRxcui.replaceAll("::BN|::IN|::PIN|::MIN", "");
        normRxcui = normRxcui.replaceAll("::BN|::IN|::PIN|::MIN", "");

        //docName|drug::b::e|drugRxcui|stren::b::e|dos::b::e|form::b::e|route::b::e|freq::b::e|dur::b::e|normRxname|normRxcui|text
        ret.put("drugName", drugNameInfo);
        ret.put("drugNameRxcui", drugNameRxcui);
        ret.put("strength", strengthInfo);
        ret.put("dosage", dosageInfo);
        ret.put("form", formInfo);
        ret.put("route", routeInfo);
        ret.put("frequency", frequencyInfo);
        ret.put("duration", durationInfo);
        ret.put("normRxname", normRxname);
        ret.put("normRxcui", normRxcui);

        return ret;

    }
}
