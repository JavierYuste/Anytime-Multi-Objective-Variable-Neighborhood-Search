package archive;

import org.uma.jmetal.solution.Solution;
import utils.DominanceUtils;
import utils.MapList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * An extension of the NDTree implementation provided by Jonathan Fieldsend
 * at https://github.com/fieldsend/multiobjective_data_structures.
 */
public class NDTree<S extends Solution<?>> implements ArchiveExtended<S> {

    protected final int numberOfChildrenPerNode;
    protected final Random rng = new Random();
    protected NDTreeNodeExtended<S> root;
    protected int maxListSizePerNode = 20;
    protected MapList<S> listOfSolutions = new MapList<>();

    public NDTree(int numberOfObjectives) {
        numberOfChildrenPerNode = numberOfObjectives + 1;
        root = new NDTreeNodeExtended<>(maxListSizePerNode, numberOfChildrenPerNode);
    }

    public NDTree(int maxListSizePerNode, int numberOfChildrenPerNode) {
        this.maxListSizePerNode = maxListSizePerNode;
        this.numberOfChildrenPerNode = numberOfChildrenPerNode;
        root = new NDTreeNodeExtended<>(maxListSizePerNode, numberOfChildrenPerNode);
    }

    @Override
    public ArchiveExtended<S> getEmptyArchive() {
        return new NDTree<>(this.maxListSizePerNode, this.numberOfChildrenPerNode);
    }

    @Override
    public boolean add(S s) throws IllegalStateException {
        if (root.isEmpty()) {
            root.add(s);
            return true;
        } else {
            if (root.updateNode(s, null)) { // returns true if solution not covered by any member in tree
                if (root.isEmpty()) {
                    root.add(s); // Special case where s has dominated and cleared tree entirely
                } else root.insert(s);
                return true;
            }
        }
        return false;
    }

    public boolean weaklyDominates(S s) throws IllegalStateException {
        if (root.isEmpty()) {
            return false;
        } else {
            return root.weaklyDominates(s);
        }
    }

    /**
     * Returns true if at least one member of the archive dominates the argument. If no members
     * dominate, returns false
     */
    public boolean dominates(S s) throws IllegalStateException {
        if (root.isEmpty()) {
            return false;
        } else {
            return root.dominates(s);
        }
    }

    public Collection<? extends S> getContents() {
        return this.solutions();
    }

    public double[] getEstimatedIdeal() {
        if (root == null) return null;
        return root.getEstimatedIdeal();
    }

    public double[] getEstimatedNadir() {
        if (root == null) return null;
        return root.getEstimatedNadir();
    }

    public double[] getMidpoint() {
        if (root == null) return null;
        return root.getMidpoint();
    }

    @Override
    public S get(int index) {
        return this.solutions().get(index);
    }

    @Override
    public List<S> solutions() {
        return listOfSolutions.getElements();
    }

    @Override
    public int size() {
        return root.coverage();
    }

    public void clean() {
        root = null;
    }

    public S getRandomMember() throws UnsupportedOperationException {
        if (size() == 0) return null; //empty tree, so return null
        return root.getRandom(rng);
    }

    /**
     * Returns a list of solutions residing in one of the NDTree's leaves
     */
    public List<S> getRandomLeaf() {
        if (size() == 0) return null; //empty tree, so return null
        return root.getRandomLeaf(rng);
    }


    /**
     * Returns the solution extremising the index objective
     */
    public S getExtremeMember(int index) {
        if (size() == 0) return null; //empty tree, so return null
        ArrayList<S> solutions = new ArrayList<>();
        root.getExtremeMember(solutions, index);
        S extreme = solutions.get(0);
        for (int i = 1; i < solutions.size(); i++)
            if (solutions.get(i).objectives()[index] < extreme.objectives()[index]) extreme = solutions.get(i);
        return extreme;
    }

    public void writeGraphVizFile(String filename) throws FileNotFoundException, UnsupportedOperationException {
        StringBuilder sb = new StringBuilder();

        StringBuilder nodes = new StringBuilder();
        StringBuilder graph = new StringBuilder();

        sb.append("digraph D {\n");

        int index = 0;
        ArrayList<Integer> interiorIndices = new ArrayList<>();
        ArrayList<Integer> leafIndices = new ArrayList<>();
        interiorIndices.add(0);
        // link nodes
        if (root != null) {
            graphVizLinkToChildren(0, 1, root, graph, leafIndices, interiorIndices);
        }

        // define nodes
        for (int i : leafIndices) {
            nodes.append(i + " [shape=box fillcolor=yellow]\n");
        }
        for (int i : interiorIndices) {
            nodes.append(i + " [shape=oval fillcolor=red]\n");
        }

        sb.append(nodes);
        sb.append(graph);
        sb.append("}");
        PrintWriter pw = new PrintWriter(new File(filename));
        pw.write(sb.toString());
        pw.close();
    }

    /*
     * Helper method for the GraphViz file writer
     */
    private int graphVizLinkToChildren(int parentIndex, int currentIndex, NDTreeNodeExtended<S> current, StringBuilder sb, ArrayList<Integer> leafIndices, ArrayList<Integer> interiorIndices) {

        if (!current.isLeaf()) {
            for (NDTreeNodeExtended<S> child : current.getChildren()) {
                sb.append(parentIndex + " -> " + currentIndex + "\n");
                interiorIndices.add(currentIndex);
                currentIndex = graphVizLinkToChildren(currentIndex, currentIndex + 1, child, sb, leafIndices, interiorIndices);
            }
        } else {
            for (int i = 0; i < current.coverage(); i++) {
                leafIndices.add(currentIndex);
                sb.append(parentIndex).append(" -> ").append(currentIndex++).append("\n");
            }
        }
        return currentIndex;
    }

    public boolean isDominated(S solution) {
        return this.weaklyDominates(solution);
    }

    @Override
    public Iterator<S> iterator() {
//        return this.solutions().iterator();
        return new NodesIterator();
    }

    private class NodesIterator implements Iterator<S> {

        private final Queue<NDTreeNodeExtended<S>> nodes;
        private final Queue<S> solutions;

        public NodesIterator() {
            this.nodes = new LinkedList<>();
            this.solutions = new LinkedList<>();
            if (root != null) {
                this.nodes.add(root);
            }
        }

        @Override
        public boolean hasNext() {
            return !this.solutions.isEmpty() || !this.nodes.isEmpty();
        }

        @Override
        public S next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (!this.solutions.isEmpty()) {
                return this.solutions.poll();
            } else {
                NDTreeNodeExtended<S> node = this.nodes.poll();
                if (node.getChildren() != null) {
                    this.nodes.addAll(node.getChildren());
                }
                if (node.getList() != null) {
                    this.solutions.addAll(node.getList());
                }
                return this.next();
            }
        }
    }

    private class NDTreeNodeExtended<R extends S> {
        protected static int MAX_LIST_SIZE;
        protected static int NUMBER_OF_CHILDREN;
        protected static double MIN_VALUE_FOR_VOLUME_CALCULATIONS = 0.00000001;
        protected List<R> list;
        protected double[] idealPointEstimate;
        protected double[] nadirPointEstimate;
        protected double[] midpoint;
        protected List<NDTreeNodeExtended<R>> children;
        protected NDTreeNodeExtended<R> parent;

        /**
         * Node constructor -- only used to make root as doesn't connect upwards to a parent
         */
        NDTreeNodeExtended(int maxListSize, int numberOfChildren) {
            if (maxListSize < numberOfChildren) {
                System.out.println("Maximum list size must be at least as big as the number of children");
                numberOfChildren = maxListSize;
            }
            MAX_LIST_SIZE = maxListSize;
            NUMBER_OF_CHILDREN = numberOfChildren;
            list = new ArrayList<>(MAX_LIST_SIZE + 1);
        }

        /*
         * Construct a node connected to parent
         */
        protected NDTreeNodeExtended(int maxListSize, int numberOfChildren, NDTreeNodeExtended<R> parent) {
            this(maxListSize, numberOfChildren);
            this.parent = parent;
        }

        /*
         * Helper method to get squared distances of objective vectors of two solutions
         */
        protected static double squaredDistance(Solution<?> a, Solution<?> b) {
            double distance = 0.0;
            for (int i = 0; i < a.objectives().length; i++)
                distance += Math.pow(a.objectives()[i] - b.objectives()[i], 2);
            return distance;
        }

        /*
         * Helper method to get squared distances of objective vector and a solutions
         */
        protected static double squaredDistance(double[] a, Solution<?> b) {
            double distance = 0.0;
            for (int i = 0; i < a.length; i++)
                distance += Math.pow(a[i] - b.objectives()[i], 2);
            return distance;
        }

        /**
         * Returns a list of the (NDTreeNodeExtended) children of this node
         */
        List<NDTreeNodeExtended<R>> getChildren() {
            return children;
        }

        /**
         * Returns a list of the (R) solutions of this node
         */
        public List<R> getList() {
            return list;
        }

        /**
         * Adds a solution covered by this node and updates the Ideal and Nadir points correspondingly
         */
        void add(R solution) {
            list.add(solution);
            listOfSolutions.add(solution);
            if (list.size() == 1) setIdealNadir(solution);
            else updateIdealNadir(solution);
        }

        /*
         * Helper method to convert double array to a string representation of contents
         */
        protected String stringConvert(double[] x) {
            StringBuilder s = new StringBuilder();
            if (x == null) return s.toString();
            for (double d : x)
                s.append(d).append(", ");
            return s.toString();
        }

        /**
         * Returns a String representation of the ideal point of this node
         */
        String idealAsString() {
            return stringConvert(idealPointEstimate);
        }

        /**
         * Returns a String representation of the nadir point of this node
         */
        String nadirAsString() {
            return stringConvert(nadirPointEstimate);
        }

        /**
         * Returns a String representation of the mid point of this node
         */
        String midpointAsString() {
            return stringConvert(midpoint);
        }

        /*
         * Helper method to set the ideal and nadir based on solution argument
         */
        protected void setIdealNadir(R solution) {
            idealPointEstimate = new double[solution.objectives().length];
            nadirPointEstimate = new double[solution.objectives().length];
            midpoint = new double[solution.objectives().length];
            for (int i = 0; i < midpoint.length; i++) {
                idealPointEstimate[i] = solution.objectives()[i];
                nadirPointEstimate[i] = idealPointEstimate[i];
                midpoint[i] = idealPointEstimate[i];
            }
        }

        /*
         * Helper method to update the ideal and nadir based on solution argument
         */
        protected void updateIdealNadir(R solution) {
            for (int i = 0; i < solution.objectives().length; i++) {
                if (solution.objectives()[i] < idealPointEstimate[i]) {
                    idealPointEstimate[i] = solution.objectives()[i];
                    midpoint[i] = idealPointEstimate[i] + (nadirPointEstimate[i] - idealPointEstimate[i]) / 2;
                } else if (solution.objectives()[i] > nadirPointEstimate[i]) {
                    nadirPointEstimate[i] = solution.objectives()[i];
                    midpoint[i] = idealPointEstimate[i] + (nadirPointEstimate[i] - idealPointEstimate[i]) / 2;
                }
            }
            if (parent != null) // got back up tree
                parent.updateIdealNadir(solution);
        }

        /**
         * Return true if node list is empty (i.e. this is an internal node) otherwise
         * returns false (i.e. this is a leaf node)
         */
        boolean isEmpty() {
            if (list == null) return false; // if list is null, then is an internal node with children
            return list.isEmpty();
        }

        /**
         * Returns true if this is a leaf node, false otherwise
         */
        boolean isLeaf() {
            return children == null;
        }

        /**
         * Returns true if this is the root node, false otherwise
         */
        boolean isRoot() {
            return parent == null;
        }

        /**
         * Checks if a solution is weakly-dominated by the archive -- added functionality to meet
         * requirements of archive management interface
         */
        boolean weaklyDominates(R solution) {
            if (DominanceUtils.weaklyDominates(nadirPointEstimate, solution)) return true;
            if (DominanceUtils.weaklyDominates(solution, idealPointEstimate)) {
                return false;
            }
            if (DominanceUtils.weaklyDominates(idealPointEstimate, solution) || DominanceUtils.weaklyDominates(solution, nadirPointEstimate)) { // short-circuit or
                if (this.isLeaf()) {
                    Iterator<R> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        R member = iterator.next();
                        if (DominanceUtils.weaklyDominates(member, solution)) return true;
                        if (DominanceUtils.weaklyDominates(solution, member))
                            return false; // existing member dominated (can't be equal given previous if check), so archive does not weakly-dominate
                    }
                } else for (NDTreeNodeExtended<R> n : children)
                    if (n.weaklyDominates(solution))
                        return true; // if it is weakly-dominated further down tree, return false and stop processing further
            }
            return false; // not weakly-dominated by any solution ith node
        }

        /**
         * Checks if a solution is dominated by the archive
         */
        boolean dominates(R solution) {
            if (DominanceUtils.stronglyDominates(nadirPointEstimate, solution)) return true;
            if (DominanceUtils.weaklyDominates(solution, idealPointEstimate)) {
                return false;
            }
            if (DominanceUtils.stronglyDominates(idealPointEstimate, solution) || DominanceUtils.stronglyDominates(solution, nadirPointEstimate)) { // short-circuit or
                if (this.isLeaf()) {
                    Iterator<R> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        R member = iterator.next();
                        if (DominanceUtils.stronglyDominates(member, solution)) return true;
                        if (DominanceUtils.stronglyDominates(solution, member))
                            return false; // existing member weakly-dominated, so archive does not dominate
                    }
                } else for (NDTreeNodeExtended<R> n : children)
                    if (n.weaklyDominates(solution))
                        return true; // if it is weakly-dominated further down tree, return false and stop processing further
            }
            return false; // not dominated by any solution ith node
        }

        protected void removeNodeAndChildrenRecursively(NDTreeNodeExtended<R> node) {
            // Remove all solutions in this node from the list of solutions
            if (node.list != null) {
                for (R solution : node.list) {
                    listOfSolutions.remove(solution);
                }
            }
            // Recursively remove all children
            if (node.children != null) {
                for (NDTreeNodeExtended<R> child : node.children) {
                    removeNodeAndChildrenRecursively(child);
                }
            }
        }

        /**
         * returns false if solution is dominated, else removed all dominated solutions and
         * returns true -- does not insert the argument though!
         */
        boolean updateNode(R solution, ListIterator<NDTreeNodeExtended<R>> iteratorAbove) {
            if (DominanceUtils.weaklyDominates(nadirPointEstimate, solution)) {
                //System.out.println("Nadir dominates -- reject");
                return false;
            }
            if (DominanceUtils.weaklyDominates(solution, idealPointEstimate)) {
                //System.out.println("Ideal dominated -- accept");
                this.removeNodeAndChildrenRecursively(this);
                if (parent != null) {
                    iteratorAbove.remove(); // detach this node and all subcomponents from tree
                } else {
                    this.list = new ArrayList<>(MAX_LIST_SIZE + 1); // clean this root node
                    this.children = null;
                }
                return true;
            }
            if (DominanceUtils.weaklyDominates(idealPointEstimate, solution) || DominanceUtils.weaklyDominates(solution, nadirPointEstimate)) { // short-circuit or
                //System.out.println("Lies inside hyper-rectangle of node, so checking relationship with composite nodes/designs");
                if (this.isLeaf()) {
                    //System.out.println("Node is a leaf, so check against all designs");
                    Iterator<R> iterator = list.iterator();
                    while (iterator.hasNext()) {
                        R member = iterator.next();
                        if (DominanceUtils.weaklyDominates(member, solution)) return false;
                        if (DominanceUtils.weaklyDominates(solution, member)) {
                            listOfSolutions.remove(member);
                            iterator.remove(); // existing member dominated, so remove
                        }
                    }
                } else {
                    //System.out.println("Node is interior, so check against hyper-rectangles of children");
                    ListIterator<NDTreeNodeExtended<R>> iter = children.listIterator();
                    while (iter.hasNext()) {// number may change in place if dominated, need to cope with concurrent update
                        NDTreeNodeExtended<R> n = iter.next();
                        if (!n.updateNode(solution, iter))
                            return false; // if it is dominated further down tree, return false and stop processing further
                        if (n.isEmpty()) {
                            this.removeNodeAndChildrenRecursively(n);
                            iter.remove(); // detach this node and all subcomponents from tree
                        }
                    }

                    if (children.size() == 1) { // replace current node state with child state, and detatch remaining child for gargage collection
                        //System.out.println("Replacing current state with remaining child state");
                        NDTreeNodeExtended<R> child = children.get(0);
                        this.list = child.list;
                        this.idealPointEstimate = child.idealPointEstimate;
                        this.nadirPointEstimate = child.nadirPointEstimate;
                        this.midpoint = child.midpoint;
                        this.children = child.children;
                        if (children != null) for (NDTreeNodeExtended<R> c : children)
                            c.parent = this;
                    } else if (children.isEmpty()) {
                        // special case if all child nodes cleared out
                        children = null; // make a leaf
                        list = new ArrayList<>(MAX_LIST_SIZE + 1);
                    }
                }
            }
            return true; // not dominated by any solution ith node
        }

        /**
         * Inserts solution in this node or further down tree
         */
        void insert(R solution) {
            if (this.isLeaf()) {
                list.add(solution);
                listOfSolutions.add(solution);
                this.updateIdealNadir(solution);
                if (list.size() > NDTreeNodeExtended.MAX_LIST_SIZE) this.split();
            } else {
                NDTreeNodeExtended<R> closest = getClosestChild(solution);
                closest.insert(solution);
            }
        }

        /*
         * Helper method to split this node  -- used when leaf capacity reached
         */
        protected void split() {
            //System.out.println("Splitting node...");
            // find solution with the highest average distance to all other solutions
            // highest average distance is equivalent to highest total distance -- no need for division
            double[][] distanceMatrix = new double[list.size()][list.size()];
            double[] distances = new double[list.size()];
            int[] indicesOfFirstChildren = new int[list.size()]; // for efficiency will track first individuals in each child node
            boolean[] added = new boolean[list.size()];
            for (int i = 0; i < list.size(); i++) {
                distances[i] = 0.0;
                added[i] = false;
                for (int j = 0; j < list.size(); j++) {
                    if (i != j) { // no need to calculate on diagonal as distance is zero
                        distanceMatrix[i][j] = squaredDistance(list.get(i), list.get(j));
                        distances[i] += distanceMatrix[i][j];
                    } else {
                        distanceMatrix[i][j] = 0.0;
                    }
                }
            }
            // get first child node
            int indexOfMostDistantChild = 0;
            double maxDistance = distances[0];
            for (int i = 1; i < list.size(); i++) {
                if (distances[i] > maxDistance) {
                    maxDistance = distances[i];
                    indexOfMostDistantChild = i;
                }
            }
            children = new ArrayList<>(NUMBER_OF_CHILDREN);
            NDTreeNodeExtended<R> child = new NDTreeNodeExtended<>(MAX_LIST_SIZE, NUMBER_OF_CHILDREN, this);
            children.add(child);
            child.add(list.get(indexOfMostDistantChild));
            //children[0] = new NDTreeNodeExtended(MAX_LIST_SIZE,NUMBER_OF_CHILDREN,this);
            //children[0].add(list.get(indexOfMostDistantChild));
            indicesOfFirstChildren[0] = indexOfMostDistantChild;
            added[indexOfMostDistantChild] = true;
            // fill up remaining child nodes

            // first put one child in each subnode, based on max distance from existing subnodes
            for (int i = 1; i < NUMBER_OF_CHILDREN; i++) { // for the total number of children to make
                maxDistance = -1.0;
                indexOfMostDistantChild = -1;
                for (int k = 0; k < list.size(); k++) {
                    if (!added[k]) { // only check those not yet added
                        double distanceAccumulator = 0.0;
                        for (int j = 0; j < i; j++) { // go through child nodes already initialised
                            // find solution furtherest from current child nodes to make next node
                            distanceAccumulator += distanceMatrix[k][indicesOfFirstChildren[j]];
                        }
                        if (distanceAccumulator > maxDistance) {
                            maxDistance = distanceAccumulator;
                            indexOfMostDistantChild = k;
                        }
                    }
                }
                indicesOfFirstChildren[i] = indexOfMostDistantChild;
                child = new NDTreeNodeExtended<>(MAX_LIST_SIZE, NUMBER_OF_CHILDREN, this);
                children.add(child);
                child.add(list.get(indexOfMostDistantChild));
                //children[i] = new NDTreeNodeExtended(MAX_LIST_SIZE,NUMBER_OF_CHILDREN,this);
                //children[i].add(list.get(indexOfMostDistantChild));
                added[indexOfMostDistantChild] = true;
            }
            // now empty remaining list members into closest children
            for (int i = 0; i < list.size(); i++) {
                if (!added[i]) {
                    //System.out.println("emptying remaining list element " + i);
                    NDTreeNodeExtended<R> closestChild = getClosestChild(list.get(i));
                    closestChild.insert(list.get(i));
                }
            }
            // detach previous list for garbage collection, as list members now all transferred to containers in children
            list = null;
        }

        /*
         * Helper method to get closest child node for solution
         */
        protected NDTreeNodeExtended<R> getClosestChild(R solution) {
            double distance = NDTreeNodeExtended.squaredDistance(children.get(0).midpoint, solution);
            int closestChild = 0;
            for (int i = 1; i < children.size(); i++) {
                double alternativeDistance = NDTreeNodeExtended.squaredDistance(children.get(i).midpoint, solution);
                if (alternativeDistance < distance) {
                    closestChild = i;
                    distance = alternativeDistance;
                }
            }
            return children.get(closestChild);
        }

        /**
         * Returns the number of solutions covered by the (sub)tree rooted at this node
         */
        public int coverage() {
            if (this.isLeaf()) {
                return list.size();
            } else {
                int coverage = 0;
                for (NDTreeNodeExtended<R> child : children) {
                    coverage += child.coverage();
                }
                return coverage;
            }
        }


        /**
         * Returns the bucket (list) of solutions at a random leaf. Traversal down the
         * tree is proportional to the (est) volume bewteen the nadir and ideal defined by a node
         */
        List<R> getRandomLeaf(Random rng) {
            if (this.isLeaf()) return list;
            double totalVolume = 0.0;
            double[] cumulativeVolumes = new double[children.size()];
            int j = 0;
            for (NDTreeNodeExtended<R> c : children) {
                double tempVolume = 1.0;
                // need small value to prevent zero volume where all objectives the same for one dimension
                for (int i = 0; i < c.idealPointEstimate.length; i++) {
                    tempVolume *= Math.max(c.nadirPointEstimate[i] - c.idealPointEstimate[i], MIN_VALUE_FOR_VOLUME_CALCULATIONS);
                    totalVolume += tempVolume;
                }
                cumulativeVolumes[j] = (j > 0) ? totalVolume + cumulativeVolumes[j - 1] : totalVolume;
                j++;
            }
            // now draw a real value on the range and go to the respective child
            double draw = rng.nextDouble() * cumulativeVolumes[j - 1];
            j = 0;
            for (NDTreeNodeExtended<R> c : children) {
                if (cumulativeVolumes[j] >= draw) {
                    return c.getRandomLeaf(rng);
                }
                j++;
            }
            return null; // should never reach here
        }

        /**
         * Returns a member of the (sub)tree rooted at this node uniformly at random
         */
        R getRandom(Random rng) {
            if (this.isLeaf()) return list.get(rng.nextInt(list.size()));

            int num = rng.nextInt(coverage());
            int total = 0;
            for (NDTreeNodeExtended<R> c : children) {
                total += c.coverage();
                if (total >= num) {
                    return c.getRandom(rng);
                }
            }
            return null; // should never occur
        }

        /**
         * Recursively extracts all solutions covered by the (sub)tree rooted at this node
         */
        public void recursivelyExtract(List<R> a) {
            if (!this.isLeaf()) {
                for (int i = 0; i < children.size(); i++) {
                    children.get(i).recursivelyExtract(a);
                }
            } else {
                a.addAll(list);
            }
        }

        /**
         * Fills the solutions list with solutions which approximately extremise the index
         * objective
         */
        void getExtremeMember(ArrayList<R> solutions, int index) {
            if (this.isLeaf()) {
                R c = list.get(0);
                for (int i = 1; i < list.size(); i++)
                    if (list.get(i).objectives()[index] < c.objectives()[index]) c = list.get(i);
                solutions.add(c);
                return;
            }
            for (NDTreeNodeExtended<R> c : children) {
                if (idealPointEstimate[index] >= c.idealPointEstimate[index]) {
                    c.getExtremeMember(solutions, index);
                }
            }
        }

        public double[] getEstimatedIdeal() {
            return idealPointEstimate;
        }

        public double[] getEstimatedNadir() {
            return nadirPointEstimate;
        }

        public double[] getMidpoint() {
            return midpoint;
        }
    }

}
