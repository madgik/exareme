///**
// * Copyright MaDgIK Group 2010 - 2015.
// */
//package madgik.exareme.db.art.executionPlan.serializers;
//
//import com.google.gson.*;
//import madgik.exareme.db.art.executionPlan.PlanExpressionConstants;
//import madgik.exareme.db.art.executionPlan.parser.expression.Buffer;
//import madgik.exareme.db.art.executionPlan.parser.expression.Parameter;
//
//import java.lang.reflect.Type;
//
///**
// *
// * @author John Chronis
// * @author Vaggelis Nikolopoulos
// */
//public class BufferSerialiser implements JsonSerializer<Buffer> {
//
//  @Override
//  public JsonElement serialize(Buffer buffer, Type type, JsonSerializationContext jsc) {
//    JsonObject jsonObject = new JsonObject();
//    jsonObject.addProperty(PlanExpressionConstants.NAME, buffer.bufferName);
//    jsonObject.addProperty(PlanExpressionConstants.CONTAINERNAME, buffer.containerName);
//    jsonObject.addProperty(PlanExpressionConstants.QOS, buffer.QoS);
//
//    if (buffer.paramList != null) {
//      JsonParser parser = new JsonParser();
//      JsonArray params = new JsonArray();
//      for (Parameter param : buffer.paramList) {
//        JsonArray p = new JsonArray();
//        p.add(parser.parse(param.name));
//        p.add(parser.parse(param.value));
//        params.add(p);
//      }
//      jsonObject.add(PlanExpressionConstants.PARAMETERS, params);
//    }
//    return jsonObject;
//  }
//}
