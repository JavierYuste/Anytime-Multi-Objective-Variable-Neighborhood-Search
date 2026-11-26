package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapList<E> {

    private final Map<E, Integer> mapPositions = new HashMap<>();
    private final ArrayList<E> elements = new ArrayList<>();

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public ArrayList<E> getElements() {
        return elements;
    }

    public boolean contains(E element) {
        return mapPositions.containsKey(element);
    }

    public boolean add(E element) {
        if (mapPositions.containsKey(element)) {
            return false;
        } else {
            mapPositions.put(element, elements.size());
            elements.add(element);
            return true;
        }
    }

    /**
     * Removes the element from the list and updates the map. Since the insertion order is not maintained,
     * the operation is performed in O(1) time.
     *
     * @param element
     * @return the removed element or null if the element was not found
     */
    public E remove(E element) {
        if (mapPositions.containsKey(element)) {
            int index = mapPositions.get(element);
            mapPositions.remove(element);
            if (index == elements.size() - 1) {
                return elements.remove(index);
            } else { // if the element to remove is not the last one
                int lastIndex = elements.size() - 1;
                E lastElement = elements.get(lastIndex);
                elements.set(index, lastElement);
                mapPositions.put(lastElement, index);
                return elements.remove(lastIndex);
            }
        } else {
            return null;
        }
    }
}
