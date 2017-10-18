/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphnearestneighbor;

import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Set;

//implemented this because Java's default priority queue has no
//decrease key operation
class BinaryHeap<T> {

	Set<T> getValueSet() {
		return valueToHeap.keySet();
	}

    class HeapNode {
        T value;
        double key;
        int pos;
        HeapNode(T value, double key, int pos) {
            this.value = value; this.key = key; this.pos = pos;
        }
		@Override
		public String toString() {
			return "{"+key+": "+value+" ("+pos+")}";
		}
    }
    ArrayList<HeapNode> heap;
    HashMap<T,HeapNode> valueToHeap;
    
	BinaryHeap() {
		heap = new ArrayList<> ();
		valueToHeap = new HashMap<> ();
	}
	
    BinaryHeap(List<T> items, List<Double> keys) {
		heap = new ArrayList<> ();
		valueToHeap = new HashMap<> ();
		for (int i = 0; i < items.size(); i++) {
            HeapNode elem = new HeapNode(items.get(i), keys.get(i), i);
            heap.add(elem);
            valueToHeap.put(items.get(i), elem);
        }	
		heapify();
    }
    
	BinaryHeap(List<T> items, double[] keys) {
		heap = new ArrayList<> (items.size());
		valueToHeap = new HashMap<> ();
		for (int i = 0; i < items.size(); i++) {
            HeapNode elem = new HeapNode(items.get(i), keys[i], i);
            heap.add(elem);
            valueToHeap.put(items.get(i), elem);
        }
		heapify();
	}
	
	BinaryHeap(List<T> items, double commonKey) {
		heap = new ArrayList<> (items.size());
		valueToHeap = new HashMap<>();
		for (int i = 0; i < items.size(); i++) {
            HeapNode elem = new HeapNode(items.get(i), commonKey, i);
            heap.add(elem);
            valueToHeap.put(items.get(i), elem);
        }
	}
	
	private void heapify() {
        for (int i = heap.size()/2; i >= 0; i--) {
            bubbleDown(i);
        }			
	}
	
	@Override
	public String toString() {
		return "Heap: "+heap+" values:" + valueToHeap;
	}

    boolean isEmpty() {
        return heap.isEmpty();
    }
	
	boolean contains(T value) {
		return valueToHeap.containsKey(value);
	}
	
	int size() {
		return heap.size();
	}
    
    T getMin() {
        if (isEmpty()) return null;
        return heap.get(0).value;
    }
    
    double getMinKey() {
		if (isEmpty()) throw new RuntimeException();
        return heap.get(0).key;
    }

    void add(T item, double key) {
		if (valueToHeap.containsKey(item)) {
			throw new RuntimeException("cannot contain repeated items");
		}
        HeapNode elem = new HeapNode(item, key, heap.size());
        valueToHeap.put(item, elem);
        heap.add(elem);
        bubbleUp(heap.size()-1);
    }
    
    void remove(T value) {
        if (!valueToHeap.containsKey(value)) return;
		int pos = valueToHeap.get(value).pos;
        valueToHeap.remove(value);
		if (pos == heap.size()-1) {
			heap.remove(heap.size()-1);
		} else {
			replaceWithLast(pos);
			if (!hasParent(pos)) bubbleDown(pos);
			else {
				double thisKey = heap.get(pos).key;			
				double parentKey = heap.get(parent(pos)).key;
				if (thisKey < parentKey) bubbleUp(pos);
				else bubbleDown(pos);
			}
		}
    }
    
    T extractMin() {
        T value = heap.get(0).value;
        replaceWithLast(0);
        bubbleDown(0);
        valueToHeap.remove(value);
        return value;
    }
	
	ArrayList<T> extractAllSorted() {
		ArrayList<T> res = new ArrayList<> (heap.size());
		while (!isEmpty()) {
			res.add(extractMin());
		}
		return res;
	}
    
    void decreaseKey(T value, double newKey) {
		if (!valueToHeap.containsKey(value)) return;
        HeapNode elem = valueToHeap.get(value);
        elem.key = newKey;
        bubbleUp(elem.pos);
    }

	
	
    private boolean hasLeft(int i) {return 2*i+1 < heap.size();}
    private boolean hasRight(int i) {return 2*i+2 < heap.size();}    
    private boolean hasParent(int i) {return i != 0;}
    private int left(int i) {return 2*i+1;}    
    private int right(int i) {return 2*i+2;}
    private int parent(int i) {return (i-1)/2;}
        
    private void bubbleUp(int i) {
        if (!hasParent(i)) return;
        double thisKey = heap.get(i).key;
        double parentKey = heap.get(parent(i)).key;
        if (thisKey < parentKey) {
            swapNodes(i, parent(i));
            bubbleUp(parent(i));
        }
    }
    
    private void bubbleDown(int i) {
        if (!hasLeft(i) && !hasRight(i)) return;
        double thisKey = heap.get(i).key;
        double leftKey = heap.get(left(i)).key;
        if (!hasRight(i)) {
			if (thisKey > leftKey) {
				swapNodes(i, left(i));
				bubbleDown(left(i));
			}
        } else {
            double rightKey = heap.get(right(i)).key;
            int minChild = rightKey < leftKey ? right(i) : left(i);
            double minKey = rightKey < leftKey ? rightKey : leftKey;
            if (thisKey > minKey) {
                swapNodes(i, minChild);
                bubbleDown(minChild);
            }
        }
    }
   
    private void swapNodes(int i, int j) {
        HeapNode aux = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, aux);
        heap.get(i).pos = i;
        heap.get(j).pos = j;
    }

    private void replaceWithLast(int i) {
        heap.set(i, heap.get(heap.size()-1));
        heap.get(i).pos = i;
        heap.remove(heap.size()-1);
    }
}