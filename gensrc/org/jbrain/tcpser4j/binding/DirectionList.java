// autogenerated from SchemaBinder

package org.jbrain.tcpser4j.binding;

/**
 * Title: DirectionList
 * Description: Description of the class
 * @author SchemaBinder
 * @version 1.0
 * 
 */

public class DirectionList implements java.io.Serializable {
    private static final String sObjName="DirectionList";
    public static final DirectionList VALUE_local = new DirectionList("local");
    public static final DirectionList VALUE_remote = new DirectionList("remote");


    private static java.util.HashMap _memberTable = init();

    private String _sValue = null;

    private DirectionList(String val) {
        _sValue=val;
    }

    private static java.util.HashMap init() {
        java.util.HashMap members=new java.util.HashMap();
        members.put("local", VALUE_local);
        members.put("remote", VALUE_remote);
        return members;
    }

    public String toString() {
        return _sValue;
    }

    public static DirectionList valueOf(String type) {
        try {
            // quick fix for leading zero supression.  Fix later jlb
            type=String.valueOf(Integer.parseInt(type));
        } catch (Exception e) {;}
        return (DirectionList)_memberTable.get(type);
    }

    public java.util.Iterator iterator() {
        return _memberTable.values().iterator();
    }

}
