package com.sap.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 *
 *
 * @author SapientialM
 * @date 2026/3/28 19:46
 * @since 1.0
 */
public class LinkedList<E> implements List<E>{
    private int size;
    // dummy 节点会一直存在，无法被GC回收
    // 所以这里不用 dummyNode
    private Node<E> head;
    private Node<E> tail;

    @Override
    public void add(E element) {
        Node<E> node = new Node<>(element, tail, null);
        if(tail != null){
            tail.next = node;
        }
        else {
            head = node;
        }
        tail = node;
        size ++;
    }

    @Override
    public void add(E element, int index) {
        if(index >= size || index < 0){
            throw new IndexOutOfBoundsException();
        }
        if(index == size){
            add(element);
            return;
        }
        Node<E> indexNode = findNode(index);
        Node<E> node = new Node<>(element, indexNode.pre, indexNode);
        if(indexNode.pre == null){
            head = node;
        }
        else {
            indexNode.pre.next = node;
        }
        indexNode.pre = node;
        size ++;
    }

    private Node<E> findNode(int index) {
        Node<E> cursor = null;
        if(index < size/2){
            cursor = head;
            for (int i = 0; i < index; i++) {
                cursor = cursor.next;
            }
        }
        else {
            cursor = tail;
            for (int i = size-1; i > index; i--) {
                cursor = cursor.pre;
            }
        }
        return cursor;
    }

    @Override
    public E remove(int index) {
        if(index >= size || index < 0){
            throw new IndexOutOfBoundsException();
        }
        Node<E> node = findNode(index);
        removeNode(node);
        return node.value;
    }

    public void removeNode(Node<E> node){
        Node<E> pre = node.pre;
        Node<E> next = node.next;
        if(pre == null) {
            head = next;
        }else {
            pre.next = next;
        }
        if(next == null){
            tail = pre;
        }
        else {
            next.pre = pre;
        }
        node.pre = null;
        node.next = null;
        size --;
    }

    @Override
    public boolean remove(E element) {
        Node<E> cursor = head;
        for (int i = 0; i < size; i++) {
            if(Objects.equals(cursor.value, element)){
                removeNode(cursor);
                return true;
            }
            cursor = cursor.next;
        }
        return false;
    }

    @Override
    public E set(int index, E element) {
        if(index >= size || index < 0){
            throw new IndexOutOfBoundsException();
        }
        Node<E> cursor = findNode(index);
        E oldValue = cursor.value;
        cursor.value = element;
        return oldValue;
    }

    @Override
    public E get(int index) {
        if(index >= size || index < 0){
            throw new IndexOutOfBoundsException();
        }
        return findNode(index).value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<E> iterator() {
        return new LinkedListIterator();
    }

    class LinkedListIterator implements Iterator<E> {
        Node<E> cursor = head;

        @Override
        public boolean hasNext() {
            return cursor != null;
        }

        @Override
        public E next() {
            if(cursor == null){
                throw new NoSuchElementException();
            }
            E result = cursor.value;
            cursor = cursor.next;
            return result;
        }
    }


    class Node<E>{
        Node<E> pre;
        Node<E> next;
        E value;

        public Node(E value){
            this.value = value;
        }

        public Node(E value, Node<E> pre, Node<E> next){
            this.value = value;
            this.pre = pre;
            this.next = next;
        }
    }
}
