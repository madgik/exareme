///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.executionPlan.deserializers;
//
//import com.google.gson.*;
//import madgik.exareme.db.art.executionPlan.PlanExpressionConstants;
//import madgik.exareme.db.art.executionPlan.parser.expression.Buffer;
//import madgik.exareme.db.art.executionPlan.parser.expression.Parameter;
//
//import java.lang.reflect.Type;
//import java.util.LinkedList;
//
///**
// *
// * @author johnchronis
// */
//public class BufferDeserialiser implements JsonDeserializer<Buffer> {
//
//  @Override
//  public Buffer deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
//    throws JsonParseException {
//    final JsonObject jsonObject = je.getAsJsonObject();
//
//    String name = jsonObject.get(PlanExpressionConstants.NAME).getAsString();
//    String containerName = jsonObject.get(PlanExpressionConstants.CONTAINERNAME).getAsString();
//    String QoS = jsonObject.get(PlanExpressionConstants.QOS).getAsString();
//    LinkedList<Parameter> paramList = null;
//
//    if (jsonObject.has(PlanExpressionConstants.PARAMETERS)) {
//      JsonArray parameters = jsonObject.get(PlanExpressionConstants.PARAMETERS).getAsJsonArray();
//      paramList = new LinkedList<>();
//      JsonArray param;
//      for (JsonElement jee : parameters) {
//        param = jee.getAsJsonArray();
//        paramList.add(new Parameter(param.get(0).getAsString(), param.get(1).getAsString()));
//      }
//    }
//    final Buffer buffer = new Buffer(name, QoS, containerName, paramList);
//
//    return buffer;
//  }
//
//}
