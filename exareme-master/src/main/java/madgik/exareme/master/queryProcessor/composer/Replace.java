package madgik.exareme.master.queryProcessor.composer;

public class Replace {
    public String getReplaced(String jsonstring) {


        jsonstring = jsonstring.replaceAll(" ", "");
        jsonstring = jsonstring.replaceAll("\"field.+?(?=,),", "");
        jsonstring = jsonstring.replaceAll("\"type.+?(?=,),", "");
        jsonstring = jsonstring.replaceAll("\"input.+?(?=,),", "");
        //  jsonstring = jsonstring.replaceAll("\n*", "");
        //  jsonstring = jsonstring.replaceAll("\t*", "");
        jsonstring = jsonstring.replaceAll("\"id\":", "");
        jsonstring = jsonstring.replaceAll(",\"operator\":", "");
        jsonstring = jsonstring.replaceAll(",\"value\":", "");

        jsonstring = jsonstring.replaceAll("\"less\"", "<");
        jsonstring = jsonstring.replaceAll("\"equal\"", "=");
        jsonstring = jsonstring.replaceAll("\"greater\"", ">");
        jsonstring = jsonstring.replaceAll("\"less_or_equal\"", "<=");
        jsonstring = jsonstring.replaceAll("\"greater_or_equal\"", ">=");
        jsonstring = jsonstring.replaceAll("\"not_equal\"", "!=");

        jsonstring = jsonstring.replaceAll("\"condition\":", "");
        jsonstring = jsonstring.replaceAll(",\"rules\":", "");
        jsonstring = jsonstring.replaceAll(",\"valid\":true", "");

        jsonstring = jsonstring.replaceAll("\"OR\"", "|");
        jsonstring = jsonstring.replaceAll("\"AND\"", "&");
        //       jsonstring = jsonstring.replaceAll("\"","");
        jsonstring = jsonstring.replaceAll(",", "");

        return jsonstring;
    }
}
