package cz.logicgo.core.misc;

public class DisjointSet implements Cloneable {

    private final int[] parent;
    private final int[] size;

    private int rootCount;

    public DisjointSet(int maxElements) {
        parent = new int[maxElements];
        size = new int[maxElements];
        rootCount = maxElements;

        for (int i = 0; i < maxElements; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    private DisjointSet(int[] parent, int[] size, int rootCount) {
        this.parent = parent.clone();
        this.size = size.clone();
        this.rootCount = rootCount;
    }

    public void makeSet(int x) {
        if (!contains(x)) throw new IllegalArgumentException("Index out of bounds: " + x);
        if (parent[x] != x) {
            parent[x] = x;
            size[x] = 1;
            rootCount++;
        }
    }

    public boolean contains(int x) {
        return x >= 0 && x < parent.length;
    }

    public int find(int x) {
        if (!contains(x)) throw new IllegalArgumentException("Unknown element: " + x);

        if (parent[x] != x) {
            parent[x] = find(parent[x]);
        }
        return parent[x];
    }

    public void union(int a, int b) {
        int ra = find(a);
        int rb = find(b);

        if (ra == rb) return;

        int sa = size[ra];
        int sb = size[rb];

        if (sa < sb) {
            parent[ra] = rb;
            size[rb] += sa;
        } else {
            parent[rb] = ra;
            size[ra] += sb;
        }

        rootCount--;
    }

    public int getRegionSize(int x) {
        return size[find(x)];
    }

    @Override
    public DisjointSet clone() {
        return new DisjointSet(this.parent, this.size, this.rootCount);
    }

    public int numberOfRoots() {
        return rootCount;
    }
}
