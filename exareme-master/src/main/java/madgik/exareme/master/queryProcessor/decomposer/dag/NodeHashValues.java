/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package madgik.exareme.master.queryProcessor.decomposer.dag;

import com.google.common.hash.HashCode;
import madgik.exareme.master.queryProcessor.estimator.NodeSelectivityEstimator;

import java.util.HashMap;

/**
 * @author dimitris
 */
public class NodeHashValues extends HashMap<HashCode, Node> {

    /**
     *
     */
    private static final long serialVersionUID = -1463579163197580938L;
    private NodeSelectivityEstimator nse;

    public void setSelectivityEstimator(NodeSelectivityEstimator nse) {
        this.nse = nse;
    }


    public void putWithChildren(Node n) {
        for (Node c : n.getChildren()) {
            this.putWithChildren(c);
        }
        this.put(n.getHashId(), n);
    }


    @Override
    public Node put(HashCode key, Node value) {
        if (nse != null && value.getType() == Node.OR && value.getNodeInfo() == null) {
            nse.makeEstimationForNode(value);
        }
        return super
                .put(key, value); //To change body of generated methods, choose Tools | Templates.
    }


    
    
   /* public Node checkAndPutWithChildren(Node top) {
        if(!this.containsKey(top.getHashId())){
        this.put(top.getHashId(), top);
        return top;
         }
         else{
         for(int i=0;i<top.getChildren().size();i++){
             Node c=top.getChildAt(i);
            top.removeChild(c);
            top.addChildAt(this.checkAndPutWithChildren(c), i);
        }

             Node toReturn=this.get(top.getHashId());
             //System.out.println(toReturn.getObject());
             if(toReturn == top){
                 //same instance???
                 return toReturn;
             }
             else{
             if(!top.isPartitionedOn().isEmpty()){
                 toReturn.setPartitionedOn(top.isPartitionedOn());
             }
             if(top.getNoOfParents()==0){
                 top.removeAllChildren();
              //node is not used anymore
             //decreaseParentCountOfChildren(top);
             //top=null;
             }
             return toReturn;
             }
         }
    }*/


}
