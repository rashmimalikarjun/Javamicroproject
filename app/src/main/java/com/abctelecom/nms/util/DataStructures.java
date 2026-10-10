package com.abctelecom.nms.util;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;

/**
 * Demonstrates Queue (FIFO task buffer) and Stack (LIFO navigation history)
 * used by the application shell. Thin wrappers so behavior is unit-testable.
 */
public final class DataStructures {

    /** Simple bounded FIFO queue used for pending background tasks. */
    public static final class TaskQueue<T> {
        private final Queue<T> q = new ArrayDeque<>();
        private final int capacity;
        public TaskQueue(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
            this.capacity = capacity;
        }
        public synchronized boolean offer(T item) {
            if (q.size() >= capacity) return false;
            return q.offer(item);
        }
        public synchronized T poll() { return q.poll(); }
        public synchronized int size() { return q.size(); }
    }

    /** LIFO history stack used for "Back" navigation between screens. */
    public static final class HistoryStack {
        private final Stack<String> s = new Stack<>();
        public void push(String screenId) { s.push(screenId); }
        public String pop() { return s.isEmpty() ? null : s.pop(); }
        public String peek() { return s.isEmpty() ? null : s.peek(); }
        public boolean isEmpty() { return s.isEmpty(); }
        public int size() { return s.size(); }
    }

    private DataStructures() {}
}
