package dhallstr;

public class ParallelSlidingList<E> {

    private E[] array;
    public static int location = 0;

    @SuppressWarnings("unchecked")
    public ParallelSlidingList(int capacity) {
        super();
        array = (E[]) new Object[capacity];
    }

    public int size() {
        return array.length - location;
    }

    public boolean isEmpty() {
        return location < array.length;
    }

    public boolean contains(Object o) {
        for (int i = location; i < array.length; i++) {
            if (array[i] != null && array[i].equals(o))
                return true;
        }
        return false;
    }

    public boolean add(E e, int index) {
        array[location+index] = e;
        return true;
    }

    public void clear() {
        for (int i = location; i < array.length; i++) {
            array[i] = null;
        }
    }

    public E get(int index) {
        return array[location+index];
    }

    public E set(int index, E element) {
        array[location+index] = element;
        return element;
    }
}
