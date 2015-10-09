/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.doublyLinkedList;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Christos Mallios <br>
 *         University of Athens / Department of Informatics and Telecommunications.
 */
public class LinkedListIterator<E> implements Iterator {

    private DoublyLinkedList list;
    private Node<E> current;
    private Node<E> lastAccessed;
    private int currentNodePosition;

    public LinkedListIterator(DoublyLinkedList linkedList) {
        list = linkedList;
        current = list.head;
        lastAccessed = null;
        currentNodePosition = 0;
    }

    @Override public boolean hasNext() {
        return (currentNodePosition < list.numberOfNodes);
    }

    @Override public Node<E> next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        lastAccessed = current;
        current = current.nextNode;
        currentNodePosition++;

        return lastAccessed;
    }

    @Override public void remove() {

        if (lastAccessed == null) {
            throw new IllegalStateException();
        }

        Node<E> previousOfLastAccessed = lastAccessed.previousNode;
        Node<E> nextOfLastAccessed = lastAccessed.nextNode;

        if (previousOfLastAccessed == null) {
            list.head = nextOfLastAccessed;
            if (list.head != null) {
                list.head.previousNode = null;
            } else {
                list.tail = null;
            }
        } else if (nextOfLastAccessed == null) {
            list.tail = list.tail.previousNode;
            if (list.tail != null) {
                list.tail.nextNode = null;
            } else {
                list.head = null;
            }
        } else {
            previousOfLastAccessed.nextNode = nextOfLastAccessed;
            nextOfLastAccessed.previousNode = previousOfLastAccessed;
        }
        list.numberOfNodes--;
        currentNodePosition--;
        lastAccessed = null;
    }
}
