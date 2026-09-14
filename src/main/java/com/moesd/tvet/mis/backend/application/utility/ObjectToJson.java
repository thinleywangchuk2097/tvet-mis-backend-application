package com.moesd.tvet.mis.backend.application.utility;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;

public class ObjectToJson {
    public List<ObjectNode> _toJson(List<Tuple> results) {
        List<ObjectNode> json = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            for (Tuple t : results) {
                List<TupleElement<?>> cols = t.getElements();
                ObjectNode one = mapper.createObjectNode();
                for (TupleElement<?> col : cols) {
                    String alias = col.getAlias();
                    Object value = t.get(alias);
                    if (value != null) {
                        one.put(alias, value.toString());
                    } else {
                        one.putNull(alias);
                    }
                }
                json.add(one);
            }
        } catch (Exception e) {
            // Just add an empty ObjectNode directly using the mapper.
            // No need for JsonNodeFactory.
            json.add(mapper.createObjectNode());
        }

        return json;
    }
}