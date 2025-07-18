package space.commandf1.amlegit.data;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class LRUCache<E> {
    private final int sizeLimit;
    private int size;
    private Element<E> head;
    private Element<E> tail;
    private final Map<E, Element<E>> map;

    public LRUCache(int var1) {
        this.sizeLimit = var1;
        this.map = new HashMap<>();
    }

    private void moveToTheHead(@NotNull Element<E> var1) {
        if (var1 != this.head) {
            if (var1 == this.tail) {
                this.tail = var1.prev;
                this.tail.next = this.tail;
            } else {
                var1.next.prev = var1.prev;
                var1.prev.next = var1.next;
            }

            var1.prev = var1;
            var1.next = this.head;
            this.head.prev = var1;
            this.head = var1;
        }
    }

    private void addToTheHead(E var1) {
        if (this.size == 0) {
            this.head = this.tail = new Element<>(var1);
            this.map.put(var1, this.head);
            this.head.prev = this.head;
            this.tail.next = this.tail;
            ++this.size;
        } else if (this.size < this.sizeLimit) {
            Element<E> var2 = new Element<E>(var1);
            this.map.put(var1, var2);
            var2.prev = var2;
            var2.next = this.head;
            this.head.prev = var2;
            this.head = var2;
            ++this.size;
        } else {
            this.map.put(var1, this.tail);
            this.map.remove(this.tail.data);
            this.tail.data = var1;
            this.moveToTheHead(this.tail);
        }

    }

    public synchronized boolean contains(E var1) {
        return this.map.containsKey(var1);
    }

    public synchronized void put(E object) {
        Element<E> element = this.map.get(object);
        if (element != null) {
            this.moveToTheHead(element);
        } else {
            this.addToTheHead(object);
        }
    }

    public synchronized void remove(E object) {
        Element<E> element = this.map.remove(object);
        if (element == null) {
            return;
        }
        element.prev.next = element.next;
        element.next.prev = element.prev;

        if (--size == 0) {
            head = null;
            tail = null;
            return;
        }

        if (element == head) {
            head = element.next;
        }
        if (element == tail) {
            tail = element.prev;
        }
    }

    public synchronized E getLeastUsed() {
        return this.tail.data;
    }

    public synchronized E getPrevUsed() {
        return this.head.data;
    }

    public synchronized void clear() {
        size = 0;
        head = null;
        tail = null;
        map.clear();
    }

    public synchronized int size() {
        return size;
    }

    public synchronized boolean isEmpty() {
        return size == 0;
    }

    public static final class Element<E> {
        public E data;
        public Element<E> next;
        public Element<E> prev;

        public Element(E var1) {
            this.data = var1;
            this.next = null;
            this.prev = null;
        }
    }
}