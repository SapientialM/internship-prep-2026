package com.sap.collections;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 *
 *
 * @author SapientialM
 * @date 2026/3/28 18:50
 * @since 1.0
 */
public class ArrayList<E> implements List<E> {
    private Object[] table = new Object[10];
    private int size;

    @Override
    public void add(E element) {
        if(size == table.length) {
            resize();
        }
        table[size++] = element;
    }

    private void resize() {
        Object[] newTable = new Object[table.length * 2];
        System.arraycopy(table, 0, newTable, 0, table.length);
        this.table = newTable;
    }

    @Override
    public void add(E element, int index) {
        if(index > size || index < 0){
            throw new IndexOutOfBoundsException();
        }
        if(size == table.length) {
            resize();
        }
        System.arraycopy(table, index, table, index + 1, size - index);
        table[index] = element;
    }

    @Override
    public E remove(int index) {
        if(index >= size || index < 0){
            throw new IndexOutOfBoundsException();
        }
        E removeElement = (E)table[index];
        System.arraycopy(table, index + 1, table, index, size - index - 1);
        // 置 null，防止内存泄露
        size --;
        table[size] = null;
        return removeElement;
    }

    @Override
    public boolean remove(E element) {
        for (int i = 0; i < size; i++) {
            if(Objects.equals(element, table[i])){
                remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public E set(int index, E element) {
        if(index >= size || index < 0){
            throw new IndexOutOfBoundsException();
        }
        E oldValue = (E)table[index];
        table[index] = element;
        return oldValue;
    }

    @Override
    public E get(int index) {
        if(index >= size || index < 0){
            throw new IndexOutOfBoundsException();
        }
        return (E)table[index];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Iterator<E> iterator() {
        return new ArrayListIterator();
    }

    class ArrayListIterator implements Iterator<E>{
        int cursor;
        @Override
        public boolean hasNext() {
            return cursor != size;
        }

        @Override
        public E next() {
            if(cursor >= size){
                throw new NoSuchElementException();
            }
            E element = (E) table[cursor];
            cursor ++;
            return element;
        }
    }
}
