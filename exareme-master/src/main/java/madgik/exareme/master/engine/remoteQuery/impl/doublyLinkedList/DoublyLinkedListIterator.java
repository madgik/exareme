/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.doublyLinkedList;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class DoublyLinkedListIterator<E> implements ListIterator {

    private DoublyLinkedList list;
    private Node<E> nextIndex;
    private Node<E> previousIndex;
    private Node<E> lastAccessed;
    private IteratorOp lastIteratorAction;
    private int currentNodePosition;

    public DoublyLinkedListIterator(DoublyLinkedList linkedList) {
        list = linkedList;
        nextIndex = list.head;
        previousIndex = null;
        lastAccessed = null;
        lastIteratorAction = null;
        currentNodePosition = 0;
    }

    @Override
    public boolean hasNext() {
        return (nextIndex != null);
    }

    @Override
    public Node<E> next() {

        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        lastAccessed = nextIndex;
        previousIndex = lastAccessed.previousNode;
        nextIndex = nextIndex.nextNode;
        if (lastIteratorAction != IteratorOp.previous) {
            currentNodePosition++;
        } else {
            currentNodePosition += 2;
        }
        lastIteratorAction = IteratorOp.next;

        return lastAccessed;
    }

    @Override
    public boolean hasPrevious() {
        return (previousIndex != null);
    }

    @Override
    public Node<E> previous() {

        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        nextIndex = lastAccessed;
        lastAccessed = previousIndex;
        if (lastAccessed != null) {
            previousIndex = lastAccessed.previousNode;
        }

        if (lastIteratorAction != IteratorOp.next) {
            currentNodePosition--;
        } else {
            currentNodePosition -= 2;
        }
        lastIteratorAction = IteratorOp.previous;

        return lastAccessed;
    }

    @Override
    public int nextIndex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int previousIndex() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set(Object e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(Object e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
