package uk.co.bigredlobster.blockingQueue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DaveBlockingQueue<T> {

    private final int size;
    private final Deque<T> data;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition fullCond = lock.newCondition();
    private final Condition emptyCond = lock.newCondition();

    public DaveBlockingQueue(final int size) {
        assert size > 0;
        this.size = size;

        this.data = new ArrayDeque<>(size);
    }

    public void put(T t) {
        try {
            lock.lock();
            if (isFull()) {
                try {
                    fullCond.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Unable to await on isFull()");
                }
                this.data.addLast(t);
                emptyCond.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public T get() {
        try {
            lock.lock();
            if(isEmpty()) {
                try {
                    emptyCond.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException("Unable to await on isEmpty");
                }
            }
            final T t = this.data.getFirst();
            fullCond.signal();
            return t;
        } finally {
            lock.unlock();
        }
    }

    private boolean isFull() {
        return this.data.size() == size;
    }

    private boolean isEmpty() {
        return this.data.isEmpty();
    }
}
