package finaltica.datastructures;

import finaltica.model.Transaction;
// Implementation of Stack ADT
public class UndoStack implements StackADT<Transaction> {
    private Transaction[] stack;
    private int top;
    private static final int DEFAULT_CAPACITY = 10;
    
    public UndoStack() {
        stack = new Transaction[DEFAULT_CAPACITY];
        top = -1;
    }
    
    @Override
    public void push(Transaction transaction) {
        if (top == stack.length - 1) {
         
            Transaction[] newStack = new Transaction[stack.length * 2];
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            stack = newStack;
        }
        
        top++;
        stack[top] = transaction;
    }
    
    @Override
    public Transaction pop() {
        if (isEmpty()) {
            return null;
        }
        
        Transaction item = stack[top];
        stack[top] = null; 
        top--;
        return item;
    }
    
    @Override
    public boolean isEmpty() {
        return top == -1;
    }
    
    @Override
    public int size() {
        return top + 1;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("UndoStack{");
        for (int i = 0; i <= top; i++) {
            sb.append(stack[i]);
            if (i < top) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }
}