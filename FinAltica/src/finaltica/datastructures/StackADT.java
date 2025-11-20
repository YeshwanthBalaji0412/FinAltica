package finaltica.datastructures;
//Interface for StackADT
public interface StackADT<T> {
    void push(T item);
    T pop();
    boolean isEmpty();
    int size();
}


