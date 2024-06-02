package io.github.mortuusars.exposure_catalog.gui.screen;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SelectionHandler {
    private final Set<Integer> selectedIndexes = new HashSet<>();
    private int lastSelectedIndex = 0;

    public Set<Integer> get() {
        return selectedIndexes;
    }

    public int getLastSelectedIndex() {
        return lastSelectedIndex;
    }

    public void select(int index) {
        selectedIndexes.add(index);
        lastSelectedIndex = index;
    }

    public void select(Collection<Integer> indexes) {
        selectedIndexes.addAll(indexes);
    }

    public void clear() {
        selectedIndexes.clear();
    }

    public void remove(int index) {
        selectedIndexes.remove(index);
    }

    public void remove(Collection<Integer> indexes) {
        selectedIndexes.removeAll(indexes);
    }

    public boolean isEmpty() {
        return selectedIndexes.isEmpty();
    }

    public int size() {
        return selectedIndexes.size();
    }
}
