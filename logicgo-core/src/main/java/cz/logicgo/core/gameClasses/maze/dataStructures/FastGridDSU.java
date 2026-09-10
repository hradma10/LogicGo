package cz.logicgo.core.gameClasses.maze.dataStructures;

public class FastGridDSU {
    private final int[] parent;
    private final int[] size;
    private int components;

    public FastGridDSU(int[][] mask) {
        int rows = mask.length;
        int cols = mask[0].length;
        int n = rows * cols;

        this.parent = new int[n];
        this.size = new int[n];
        this.components = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mask[r][c] == 1) {
                    int id = r * cols + c;
                    parent[id] = id;
                    size[id] = 1;
                    components++;
                }
            }
        }
    }

    public int find(int i) {
        int root = i;
        while (root != parent[root]) {
            root = parent[root];
        }
        while (i != root) {
            int next = parent[i];
            parent[i] = root;
            i = next;
        }
        return root;
    }

    public void union(int p, int q) {
        int rootP = find(p);
        int rootQ = find(q);
        if (rootP == rootQ) return;

        if (size[rootP] < size[rootQ]) {
            parent[rootP] = rootQ;
            size[rootQ] += size[rootP];
        } else {
            parent[rootQ] = rootP;
            size[rootP] += size[rootQ];
        }
        components--;
    }

    public int getComponents() {
        return components;
    }
}
