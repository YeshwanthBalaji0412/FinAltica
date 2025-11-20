package finaltica.datastructures;

import finaltica.model.Category;

import java.util.ArrayList;
import java.util.List;
/**
 * A Binary Search Tree (BST) for storing Category objects in alphabetical order based on category name.
 */
public class BST {
	// Inner Node class representing each node in the BST
    private class Node {
        Category category;
        Node left, right;

        Node(Category category) {
            this.category = category;
        }
    }

    private Node root;
    /**
     * Inserts a new category into the BST.
     * @param categoryName Name of the category to insert.
     */
    public void insert(String categoryName) {
        root = insertRec(root, new Category(categoryName));
    }
    // Recursive method to insert a node in alphabetical order
    private Node insertRec(Node root, Category category) {
        if (root == null) {
            return new Node(category);
        }
        if (category.getName().compareTo(root.category.getName()) < 0) {
            root.left = insertRec(root.left, category);
        } else if (category.getName().compareTo(root.category.getName()) > 0) {
            root.right = insertRec(root.right, category);
        }
        return root;
    }
    /**
     * Performs an in-order traversal of the BST and prints category names.
     */
    public void inOrderTraversal() {
        inOrderRec(root);
    }

    private void inOrderRec(Node root) {
        if (root != null) {
            inOrderRec(root.left);
            System.out.println(root.category.getName());
            inOrderRec(root.right);
        }
    }
    /**
     * Returns a list of all category names in alphabetical order.
     * @return List<String> of category names.
     */
    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        getCategoriesRec(root, categories);
        return categories;
    }

    private void getCategoriesRec(Node root, List<String> categories) {
        if (root != null) {
            getCategoriesRec(root.left, categories);
            categories.add(root.category.getName());
            getCategoriesRec(root.right, categories);
        }
    }
}