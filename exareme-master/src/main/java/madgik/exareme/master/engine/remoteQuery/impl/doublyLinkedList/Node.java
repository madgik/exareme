/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.doublyLinkedList;

/**
 * @param <E>
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class Node<E> {

    Node<E> nextNode;
    Node<E> previousNode;
    public E value;
    int pinned = 0;

    public Node(E value) {
        this.value = value;
    }
}
