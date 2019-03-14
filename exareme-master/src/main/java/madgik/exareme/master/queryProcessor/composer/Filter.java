package madgik.exareme.master.queryProcessor.composer;

import java.util.ArrayList;
import java.util.List;


public class Filter {
    public String getFilter(String jsonstring) {

        String jsonstringReplace = new Replace().getReplaced(jsonstring);

        String[] filter_array = jsonstringReplace.split("(?<=\\G([\\w\\.\\\"]+((?![\\w\\.\\\"]+))|\\&|\\||==|<=|>=|\\+|\\[|\\]|\\{|\\}|\\,|\\(|\\)|/|\\*|-|(<|>|=)(?!=)))\\s*");
        List mylist = new ArrayList();

        ArrayList<String> rule_conditions = new ArrayList<String>();
        rule_conditions.add("<=");
        rule_conditions.add(">=");
        rule_conditions.add("<");
        rule_conditions.add("=");
        rule_conditions.add(">");

        String rule_open = "{";
        String rule_close = "}";

        String transactions = "&|";
        String transaction_open = "[";
        String transaction_close = "]";

        String filter_string = "";
        String filternow = "";
        for (int i = filter_array.length - 1; i >= 0; i--) {
            mylist.add(filter_array[i]);
            if (filter_array[i].contentEquals(rule_open)) {
                mylist.remove(mylist.size() - 1);
                filternow = "(";
                while (!mylist.get(mylist.size() - 1).toString().contentEquals(rule_close)) {
                    if (rule_conditions.contains(mylist.get(mylist.size() - 2).toString())) {
                        //1
                        String charnow1 = mylist.get(mylist.size() - 1).toString();
                        mylist.remove(mylist.size() - 1);
                        String charnow2 = mylist.get(mylist.size() - 1).toString();
                        mylist.remove(mylist.size() - 1);
                        String charnow3 = mylist.get(mylist.size() - 1).toString();
                        mylist.remove(mylist.size() - 1);

                        //if// (charnow3.matches("[-+]?\\d*\\.?\\d+"))
                        //filternow += "colname=" + charnow1 + " and val::numeric" + charnow2 + charnow3; //condition
                        //else
                        //filternow += "colname=" + charnow1 + " and val" + charnow2 + charnow3; //condition
                        filternow += charnow1 + charnow2 + charnow3;
                    } else {
                        String charnow = mylist.get(mylist.size() - 1).toString();
                        mylist.remove(mylist.size() - 1);
                        filternow += charnow;
                    }
                }
                filternow += ")";
                mylist.remove(mylist.size() - 1);
                mylist.add(filternow.toString());
            }
            if (filter_array[i].contentEquals("&") || filter_array[i].contentEquals("|")) {
                String operatornow = mylist.get(mylist.size() - 1).toString();
                mylist.remove(mylist.size() - 1);
                mylist.remove(mylist.size() - 1);
                filternow = "";
                while (!mylist.get(mylist.size() - 1).toString().contentEquals(transaction_close)) {
                    String charnow = mylist.get(mylist.size() - 1).toString();
                    mylist.remove(mylist.size() - 1);
                    filternow += charnow;
                    if (!mylist.get(mylist.size() - 1).toString().contentEquals(transaction_close))
                        filternow += operatornow;
                }
                mylist.remove(mylist.size() - 1);
                mylist.add(filternow.toString());
            }
        }
        String myresult = (mylist.get(mylist.size() - 1).toString()).replaceAll("\"", "");
        return myresult;
    }
}
