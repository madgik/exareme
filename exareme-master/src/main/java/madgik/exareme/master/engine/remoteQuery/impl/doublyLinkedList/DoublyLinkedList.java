/**
 * Copyright MaDgIK Group 2010 - 2015.
 */
package madgik.exareme.master.engine.remoteQuery.impl.doublyLinkedList;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * @param <E>
 * @author Christos Mallios <br>
 * University of Athens / Department of Informatics and Telecommunications.
 */
public class DoublyLinkedList<E> implements List {

    Node<E> head;
    Node<E> tail;
    int numberOfNodes;
    //Log Declaration
    private static final Logger log = Logger.getLogger(DoublyLinkedList.class);

    public DoublyLinkedList() {

        numberOfNodes = 0;
        head = null;
        tail = null;
    }

    /*
     * Returns the first(least recently used) element in this list.
     */
    public Node<E> getFirst() {

        if (head == null) {
            return null;
        }
        return head;
    }

    /*
     * Returns the first unpinned and least recently used element in this list.
     */
    public Node<E> getFirstUnpinned() {

        Node<E> node;
        LinkedListIterator iterator;
        iterator = new LinkedListIterator(this);

        while (iterator.hasNext()) {
            node = iterator.next();
            if (node.pinned == 0) {
                return node;
            }
        }

        return null;
    }

    /*
     * Returns the last(most recently used) element in this list.
     */
    public Node<E> getLast() {

        if (tail == null) {
            return null;
        }

        return tail;

    }

    /*
     * Pins the specified node
     */
    public void pin(Node<E> node) {

        /*System.out.println("Stin arxi einai "+node.pinned);*/
        if (node == null) {
            throw new NullPointerException();
        } else if (numberOfNodes == 0) {
            throw new NoSuchElementException();
        }

        node.pinned++;
        /*System.out.println("Sto telos einai "+node.pinned);*/
    }

    /*
     * Unpins the specified node
     */
    public void unpin(Node<E> node) {

        /*System.out.println("Kanw unpin");*/
        if (node == null) {
            throw new NullPointerException();
        } else if (numberOfNodes == 0) {
            throw new NoSuchElementException();
        }

        node.pinned--;
    }

    /*
     * Checks if the specified node is pinned
     */
    public boolean isPinned(Node<E> node) {

        if (node == null) {
            throw new NullPointerException();
        } else if (numberOfNodes == 0) {
            throw new NoSuchElementException();
        }
        /*System.out.println("Sto erwtima gia pinned exw "+node.pinned);*/
        return node.pinned != 0;
    }

    /*
     * Removes the specified node from this list.
     */
    public void remove(Node<E> node) {

        if (node == null) {
            throw new NullPointerException();
        } else if (numberOfNodes == 0) {
            throw new NoSuchElementException();
        }

        if (node == head) {
            if (node.nextNode != null) {
                head = node.nextNode;
                head.previousNode = null;
            } else {
                tail = null;
                head = null;
            }
        } else if (node == tail) {
            tail = tail.previousNode;
            tail.nextNode = null;
        } else {
            node.previousNode.nextNode = node.nextNode;
            node.nextNode.previousNode = node.previousNode;
        }

        node = null;
        numberOfNodes--;
    }

    /*
     * Updates the specified node in the list as the most
     * recently used.
     */
    public void lruUpdate(Node<E> node) {

        if (node == null) {
            throw new NullPointerException();
        } else if (numberOfNodes == 0) {
            throw new NoSuchElementException();
        }

        if (node != tail) {

            if (node == head) {
                head = node.nextNode;
                head.previousNode = null;
            } else {
                node.previousNode.nextNode = node.nextNode;
                node.nextNode.previousNode = node.previousNode;
            }
            tail.nextNode = node;
            node.nextNode = null;
            node.previousNode = tail;
            tail = node;
        }
    }

    /*
     * Appends the specified element to the end of this list.
     */
    @Override
    public boolean add(Object element) {

        if (element == null) {
            throw new NullPointerException();
        }

        Node node = new Node(element);

        node.nextNode = null;
        if (head == null) {
            node.previousNode = null;
            head = node;
        } else {
            node.previousNode = tail;
            tail.nextNode = node;
        }
        tail = node;
        numberOfNodes++;

        return true;
    }

    /*
     * Returns the number of elements in this list.
     */
    @Override
    public int size() {
        return numberOfNodes;
    }

    /*
     * Removes all of the elements from this list.
     */
    @Override
    public void clear() {

        numberOfNodes = 0;
        head = null;
        tail = null;
    }

    /*
     * Prints all the elements from the list from the least
     * recently used to the most recently used.
     */
    public void printAll() {

        LinkedListIterator iterator;
        iterator = new LinkedListIterator(this);

        while (iterator.hasNext()) {

            //log.debug("--> " + ((ListNode) iterator.next().value).query);
            /*System.out.println("--> " + ((ListNode) iterator.next().value).query);*/ /*SOS*/
        }
    }

    /*
     * Returns true if this list contains no elements.
     */
    @Override
    public boolean isEmpty() {

        return (numberOfNodes == 0);
    }

    /*
     * Returns true if this list contains the specified element value.
     */
    @Override
    public boolean contains(Object object) {

        if (object == null) {
            throw new NullPointerException();
        }

        LinkedListIterator iterator;
        iterator = new LinkedListIterator(this);

        while (iterator.hasNext()) {
            if (iterator.next().value.equals(object)) {
                return true;
            }
        }
        return false;
    }

    /*
     * Returns an iterator over the elements in this list in proper sequence.
     */
    @Override
    public Iterator iterator() {
        return new LinkedListIterator(this);
    }

    /*
     * Returns an array containing all of the elements in this list in
     * proper sequence (from first to last element).
     */
    @Override
    public Object[] toArray() {

        if (numberOfNodes == 0) {
            return null;
        }

        Object[] objectList = new Object[numberOfNodes];

        LinkedListIterator iterator;
        iterator = new LinkedListIterator(this);
        int counter = 0;

        while (iterator.hasNext()) {
            objectList[counter] = iterator.next();
            counter++;
        }
        return objectList;
    }

    @Override
    public Object[] toArray(Object[] ts) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*
     * Removes the first occurrence of the specified element from
     * this list, if it is present.
     */
    @Override
    public boolean remove(Object o) {

        if (o == null) {
            throw new NullPointerException();
        }

        Iterator it = iterator();
        if (it == null) {
            return false;
        }

        while (it.hasNext()) {
            Node<E> element = (Node<E>) it.next();

            if ((element.value).equals(o)) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection clctn) {

        if (clctn == null) {
            throw new NullPointerException();
        }

        for (Object element : clctn) {
            if (!contains(element)) {
                return false;
            }
        }
        return true;
    }

    /*
     * Appends all of the elements in the specified collection to the end
     * of this list, in the order that they are returned by the specified
     * collection's iterator.
     */
    @Override
    public boolean addAll(Collection clctn) {

        if (clctn == null) {
            throw new NullPointerException();
        }
        boolean result;

        for (Object element : clctn) {
            result = add(element);
            if (result == false) {
                return false;
            }
        }
        return true;
    }

    /*
     * Inserts all of the elements in the specified collection into this
     * list at the specified position.
     */
    @Override
    public boolean addAll(int i, Collection clctn) {

        if (clctn == null) {
            throw new NullPointerException();
        } else if (i > numberOfNodes) {
            throw new IndexOutOfBoundsException();
        }
        boolean result;

        int position = i;
        for (Object element : clctn) {
            add(position, element);
            position++;
        }
        return true;
    }

    /*
     * Removes from this list all of its elements that are contained in the
     * specified collection.
     */
    @Override
    public boolean removeAll(Collection clctn) {

        if (clctn == null) {
            throw new NullPointerException();
        }
        boolean result;

        for (Object element : clctn) {
            result = remove(element);
            if (result == false) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection clctn) {

        if (clctn == null) {
            throw new NullPointerException();
        }

        Node element;
        Iterator iterator = iterator();
        while (iterator.hasNext()) {
            element = (Node) iterator.next();
            if (!clctn.contains(element)) {
                iterator.remove();
            }
        }
        return true;
    }

    /*
     * Returns the element at the specified position in this list.
     */
    @Override
    public Node<E> get(int i) {

        if (i >= numberOfNodes) {
            throw new IndexOutOfBoundsException();
        }

        int counter = 0;
        Iterator it = iterator();

        while (it.hasNext()) {
            if (counter == i) {
                return (Node<E>) it.next();
            }
            it.next();
            counter++;
        }
        return null;
    }

    /*
     * Replaces the element at the specified position in this list
     * with the specified element.
     */
    @Override
    public E set(int i, Object e) {

        if (e == null) {
            throw new NullPointerException();
        } else if (i >= numberOfNodes) {
            throw new IndexOutOfBoundsException();
        }

        int counter = 0;
        Node<E> element;
        Iterator it = iterator();

        while (it.hasNext()) {
            if (counter == i) {
                element = (Node<E>) it.next();
                element.value = (E) e;
                return element.value;
            }
            it.next();
            counter++;
        }
        return null;
    }

    /*
     * Inserts the specified element at the specified position in this list.
     */
    @Override
    public void add(int i, Object e) {

        if (e == null) {
            throw new NullPointerException();
        } else if (i > numberOfNodes) {
            throw new IndexOutOfBoundsException();
        }

        int counter = 0;
        Node<E> element, node = new Node(e);

        if (i == 0) {
            head.previousNode = node;
            node.previousNode = null;
            node.nextNode = head;
            head = node;
        } else if (i == numberOfNodes) {
            tail.nextNode = node;
            node.previousNode = tail;
            node.nextNode = null;
            tail = node;
        } else {
            Iterator it = iterator();
            while (it.hasNext()) {
                if (counter == i - 1) {
                    element = (Node<E>) it.next();
                    element.nextNode.previousNode = node;
                    node.previousNode = element;
                    node.nextNode = element.nextNode;
                    element.nextNode = node;
                    break;
                }
                it.next();
                counter++;
            }
        }

        numberOfNodes++;
    }

    /*
     * Removes the element at the specified position in this list.
     */
    @Override
    public E remove(int i) {

        if (i >= numberOfNodes) {
            throw new IndexOutOfBoundsException();
        }

        int counter = 0;
        Node<E> element = null;
        Iterator it = iterator();

        while (it.hasNext()) {
            if (counter == i) {
                element = (Node<E>) it.next();
                it.remove();
                break;
            }
            it.next();
            counter++;
        }
        return element.value;
    }

    /*
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     */
    @Override
    public int indexOf(Object o) {

        if (o == null) {
            throw new NullPointerException();
        }

        int counter = 0;
        Node<E> element;
        Iterator it = iterator();

        while (it.hasNext()) {
            element = (Node<E>) it.next();
            if ((element.value).equals(o)) {
                return counter;
            }
            counter++;
        }
        return -1;
    }

    /*
     * Returns the index of the last occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     */
    @Override
    public int lastIndexOf(Object o) {

        if (o == null) {
            throw new NullPointerException();
        }

        int counter = 0;
        int position = -1;
        Node<E> element;
        Iterator it = iterator();

        while (it.hasNext()) {
            element = (Node<E>) it.next();
            if ((element.value).equals(o)) {
                position = counter;
            }
            counter++;
        }
        return position;
    }

    @Override
    public ListIterator listIterator() {
        return new DoublyLinkedListIterator(this);
    }

    @Override
    public ListIterator listIterator(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List subList(int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
