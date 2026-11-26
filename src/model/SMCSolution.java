package model;

import org.apache.commons.lang3.NotImplementedException;
import org.uma.jmetal.solution.Solution;
import problems.MCAProblem;
import problems.SMCProblem;

import java.util.*;
import java.util.logging.Logger;

public class SMCSolution implements ExtendedSolution<Integer> {

    private final SMCProblem problem;
    public int maxModuleSize;
    public int largestModule;
    public int minModuleSize;
    public int smallestModule;
    public HashSet<Integer>[] listOFModulesWithSize;
    public int isolatedModules;
    protected Set<Integer> usedModules;
    private int[] numberOfVerticesInModule;
    private Set<Integer>[] verticesInModule;
    private Deque<Integer> availableModules;
    private int[] variables;
    private int coupling = 0;
    private int[] moduleCohesion;
    private int[] moduleCoupling;
    private int[] modulesTurboMQInteger;
    private int turboMQInteger;
    private int cohesion;
    private double[] normalizedObjectives;
    private Map<Object, Object> attributes = new HashMap<>();

    public SMCSolution(SMCProblem problem) {
        this.problem = problem;
        this.variables = new int[problem.numberOfVariables()];
        this.numberOfVerticesInModule = new int[this.variables.length];
        this.availableModules = new ArrayDeque<>();
        this.usedModules = new HashSet<>();
        this.verticesInModule = new HashSet[this.variables.length];
        this.isolatedModules = 0;
        this.minModuleSize = 1;
        this.maxModuleSize = 1;
        this.smallestModule = 0;
        this.listOFModulesWithSize = new HashSet[this.variables.length + 1];
        this.listOFModulesWithSize[1] = new HashSet<>();
        this.largestModule = 0;
        for (int i = 0; i < this.variables.length; i++) {
            this.variables[i] = i;
            this.numberOfVerticesInModule[i] = 1;
            HashSet<Integer> moduleList = new HashSet<>();
            moduleList.add(i);
            this.verticesInModule[i] = moduleList;
            this.usedModules.add(i);
            this.isolatedModules++;
            this.listOFModulesWithSize[1].add(i);
        }
        this.initializeCachedInformation();
        this.normalizedObjectives = new double[this.getProblem().numberOfObjectives()];
    }

    public SMCSolution(SMCSolution s) {
        this(s.getProblem());
        this.variables = new int[this.problem.numberOfVariables()];
        this.availableModules = new ArrayDeque<>();
        this.availableModules.addAll(s.availableModules);
        this.usedModules = new HashSet<>();
        this.usedModules.addAll(s.usedModules);
        this.verticesInModule = new HashSet[this.variables.length];
        Set<Integer> aux;
        for (int i = 0; i < s.verticesInModule.length; i++) {
            this.verticesInModule[i] = new HashSet<>(s.verticesInModule[i]);
        }
        this.isolatedModules = s.isolatedModules;
        this.minModuleSize = s.minModuleSize;
        this.maxModuleSize = s.maxModuleSize;
        this.smallestModule = s.smallestModule;
        this.largestModule = s.largestModule;
        this.variables = s.variables.clone();
        this.numberOfVerticesInModule = s.numberOfVerticesInModule.clone();
        this.listOFModulesWithSize = new HashSet[this.variables.length + 1];
        // TODO: check if this is copied correctly
        for (int i = 0; i < s.listOFModulesWithSize.length; i++) {
            if (s.listOFModulesWithSize[i] != null) {
                this.listOFModulesWithSize[i] = new HashSet<>(s.listOFModulesWithSize[i]);
            }
        }
        this.turboMQInteger = s.turboMQInteger;
        this.cohesion = s.cohesion;
        this.coupling = s.coupling;
        this.moduleCohesion = s.moduleCohesion.clone();
        this.moduleCoupling = s.moduleCoupling.clone();
        this.modulesTurboMQInteger = s.modulesTurboMQInteger.clone();
        this.attributes = new HashMap<>(s.attributes);
        this.normalizedObjectives = s.normalizedObjectives.clone();
    }

    public SMCProblem getProblem() {
        return this.problem;
    }

    /**
     * Initialize data structures that will later be needed to calculate the score.
     */
    public void initializeCachedInformation() {
        this.moduleCohesion = new int[this.variables.length];
        this.moduleCoupling = new int[this.variables.length];
        this.modulesTurboMQInteger = new int[this.variables.length];
        this.cohesion = 0;
        this.coupling = 0;
        this.turboMQInteger = 0;
        for (int vertex_1 = 0; vertex_1 < this.variables.length; vertex_1++) {
            for (int vertex_2 = 0; vertex_2 <= vertex_1; vertex_2++) {
                int value = this.getConnectionValue(vertex_1, vertex_2);
                if (this.variables[vertex_1] == this.variables[vertex_2]) {
                    this.addCohesion(this.variables[vertex_1], value);
                } else {
                    this.addCoupling(this.variables[vertex_1], this.variables[vertex_2], value);
                }
            }
        }

        for (int i = 0; i < modulesTurboMQInteger.length; i++) {
            if (this.moduleCohesion[i] > 0) {
                this.modulesTurboMQInteger[i] = this.calculateModuleTurboMQ(this.moduleCohesion[i], this.moduleCoupling[i]);
                this.turboMQInteger += this.modulesTurboMQInteger[i];
            }
        }
    }

    public int calculateModuleTurboMQ(int cohesion, int coupling) {
        double MF = ((float) cohesion / ((float) cohesion + (0.5 * (float) coupling)));
        return (int) (MF * 10000);
    }

    public float calculateModuleTurboMQFloat(int cohesion, int coupling) {
        if (cohesion > 0 || coupling > 0) {
            return (float) ((float) cohesion / ((float) cohesion + (0.5 * (float) coupling)));
        }
        return 0;
    }

    public double recalculateTurboMQ() {
        int turboMQ = 0;
        int[] couplings = this.recalculateCouplingModules();
        int[] cohesions = this.recalculateCohesionModules();
        // Recalculate the MQ of this solution
        for (int i = 0; i < this.variables.length; i++) {
            double MF = this.calculateModuleTurboMQ(cohesions[i], couplings[i]);
            turboMQ += MF;
        }
        return turboMQ;
    }

    public double[] recalculateTurboMQModules() {
        double[] turboMQs = new double[this.variables.length];
        int[] couplings = this.recalculateCouplingModules();
        int[] cohesions = this.recalculateCohesionModules();
        // Recalculate the MQ of this solution
        for (int i = 0; i < this.variables.length; i++) {
            turboMQs[i] = this.calculateModuleTurboMQFloat(cohesions[i], couplings[i]);
        }
        return turboMQs;
    }

    public int[] recalculateCohesionModules() {
        int[] cohesions = new int[this.variables.length];

        for (Integer usedModule : usedModules) {
            int cohesion = 0;
            for (int i = 0; i < this.variables.length; i++) {
                for (int j = i; j < this.variables.length; j++) {
                    if (this.variables[i] == usedModule && this.variables[i] == this.variables[j]) {
                        cohesion += this.getConnectionValue(i, j);
                    }
                }
            }
            cohesions[usedModule] = cohesion;
        }

        return cohesions;
    }

    public int[] recalculateCouplingModules() {
        int[] couplings = new int[this.variables.length];

        for (int k = 0; k < this.variables.length; k++) {
            int coupling = 0;
            for (int i = 0; i < this.variables.length; i++) {
                for (int j = 0; j < this.variables.length; j++) {
                    if (this.variables[i] == k && i != j && this.variables[i] != this.variables[j]) {
                        coupling += this.getConnectionValue(i, j);
                    }
                }
            }
            couplings[k] = coupling;
        }

        return couplings;
    }

    /**
     * Get weight of the connection between {@code vertex1} and {@code vertex2}.
     *
     * @param vertex1 a vertex
     * @param vertex2 a vertex
     * @return the weight of the connection between vertex1 and vertex2
     */
    public int getConnectionValue(int vertex1, int vertex2) {
        return this.problem.getConnectionValue(vertex1, vertex2);
    }

    private void removeCohesion(int cluster, int connectionValue) {
        this.moduleCohesion[cluster] -= connectionValue;
        this.cohesion -= connectionValue;
    }

    private void addCohesion(int cluster, int connectionValue) {
        this.moduleCohesion[cluster] += connectionValue;
        this.cohesion += connectionValue;
    }

    private void removeCoupling(int cluster1, int cluster2, int connectionValue) {
        this.moduleCoupling[cluster1] -= connectionValue;
        this.moduleCoupling[cluster2] -= connectionValue;
        this.coupling -= connectionValue;
    }

    private void addCoupling(int cluster1, int cluster2, int connectionValue) {
        this.moduleCoupling[cluster1] += connectionValue;
        this.moduleCoupling[cluster2] += connectionValue;
        this.coupling += connectionValue;
    }

    public HashSet<Integer> getAdjacentModules(int vertex) {
        HashSet<Integer> result = new HashSet<>();
        for (Integer adjacentVertex : this.getProblem().getAdjacentVertices(vertex)) {
            int module = this.getModule(adjacentVertex);
            if (module != this.getModule(vertex)) {
                result.add(module);
            }
        }
        return result;
    }

    @Override
    public List<Integer> variables() {
        return Arrays.stream(this.variables).boxed().toList();
    }

    @Override
    public double[] objectives() {
        if (this.problem instanceof MCAProblem) {
            // TODO: can we avoid creating this object every time? We could store it as an attribute and update it when we move a vertex
            return new double[]{this.coupling, -this.cohesion, (double) -this.turboMQInteger / 10000, -this.usedModules.size(), this.isolatedModules};
        }
//        else if (this.problem instanceof ECAProblem) {
//            return new double[]{this.coupling, -this.cohesion, (double) -this.turboMQInteger / 10000, -this.usedModules.size(), this.maxModuleSize - this.minModuleSize};
//        }
        else {
            throw new NotImplementedException("Problem not implemented");
        }
    }

    @Override
    public double[] constraints() {
        return new double[0];
    }

    @Override
    public Map<Object, Object> attributes() {
        return this.attributes;
    }

    public int hashCode() {
        // TODO: this is very costly. In fact, we are not interested in visiting the same point
        //  in the objective space twice, not the same solution.
        //  Therefore, we might be able to just hash the objectives, which would be much faster
//        return Arrays.hashCode(this.variables);
        return Arrays.hashCode(this.objectives());
    }

    @Override
    public Solution<Integer> copy() {
        return new SMCSolution(this);
    }

    public int getModule(int vertex) {
        return this.variables[vertex];
    }

    public int getModuleCohesion(int module) {
        return this.moduleCohesion[module];
    }

    public int getModuleCoupling(int module) {
        return this.moduleCoupling[module];
    }

    public int getSumConnections(int module1, int module2) {
        int connections = 0;
        for (Integer vertex1 : verticesInModule[module1]) {
            for (Integer vertex2 : verticesInModule[module2]) {
                connections += this.getConnectionValue(vertex1, vertex2);
            }
        }
        return connections;
    }

    public int getModularizationFactor(int module) {
        return this.modulesTurboMQInteger[module];
    }

    public Set<Integer> getUsedModules() {
        return usedModules;
    }

    public int obtainAvailableModule() {
        if (this.availableModules.size() > 0) {
            return this.availableModules.peek();
        } else {
            return 0;
        }
    }

    public int getMostConnectedModule(int vertex) {
        int[] destinationClusters = new int[this.variables.length];
        int maxConnection = Integer.MIN_VALUE;
        int bestCluster = -1;
        for (Integer adjacentVertex : this.problem.getAdjacentVertices(vertex)) {
            int currentModule = this.getModule(adjacentVertex);
            destinationClusters[currentModule] += this.getConnectionValue(vertex, adjacentVertex);
            if (destinationClusters[currentModule] > maxConnection) {
                maxConnection = destinationClusters[currentModule];
                bestCluster = currentModule;
            }
        }
        return bestCluster;
    }

    public int recalculateCohesion() {
        int result = 0;
        for (int vertex_1 = 0; vertex_1 < this.variables.length; vertex_1++) {
            for (int vertex_2 = 0; vertex_2 <= vertex_1; vertex_2++) {
                int value = this.getConnectionValue(vertex_1, vertex_2);
                if (this.variables[vertex_1] == this.variables[vertex_2]) {
                    result += value;
                }
            }
        }
        return result;
    }

    public int recalculateCoupling() {
        int coupling = 0;
        for (int i = 0; i < this.variables.length; i++) {
            for (int j = 0; j < this.variables.length; j++) {
                if (this.variables[i] != this.variables[j]) {
                    coupling += this.getConnectionValue(i, j);
                }
            }
        }
        coupling /= 2;
        return coupling;
    }

    public void moveVertex(int vertex, int targetModule) {
        if (this.variables[vertex] == targetModule) {
            // No movement in this case
            return;
        }
        int originalModule = this.variables[vertex];
        this.turboMQInteger -= this.modulesTurboMQInteger[originalModule];
        this.turboMQInteger -= this.modulesTurboMQInteger[targetModule];
        for (int connectedVertex : this.problem.getAdjacentVertices(vertex)) {
            int connectionValue = this.getConnectionValue(vertex, connectedVertex);
            int connectedModule = this.variables[connectedVertex];
            // Delete previous connections
            // Same vertex
            if (vertex == connectedVertex) {
                this.removeCohesion(originalModule, connectionValue);
                this.addCohesion(targetModule, connectionValue);
            }
            // If they were in the same module
            else if (originalModule == connectedModule) {
                this.removeCohesion(originalModule, connectionValue);
                this.addCoupling(originalModule, targetModule, connectionValue);
            }
            // If they were not in the same module, but they are going to be in the same module
            else if (targetModule == connectedModule) {
                // They cannot be the same vertex
                this.removeCoupling(originalModule, targetModule, connectionValue);
                this.addCohesion(targetModule, connectionValue);
            }
            // If they were in different modules, and they are not going to be in the same module
            else {
                // They cannot be the same vertex
                this.removeCoupling(originalModule, connectedModule, connectionValue);
                this.addCoupling(targetModule, connectedModule, connectionValue);
            }
        }

        // Consider loops (edges from a vertex to itself)
        if (this.getConnectionValue(vertex, vertex) > 0) {
            this.removeCohesion(originalModule, this.getConnectionValue(vertex, vertex));
            this.addCohesion(targetModule, this.getConnectionValue(vertex, vertex));
        }

        if (this.moduleCohesion[originalModule] != 0) {
            this.modulesTurboMQInteger[originalModule] = this.calculateModuleTurboMQ(this.moduleCohesion[originalModule], this.moduleCoupling[originalModule]);
        } else {
            this.modulesTurboMQInteger[originalModule] = 0;
        }
        this.turboMQInteger += this.modulesTurboMQInteger[originalModule];
        if (this.moduleCohesion[targetModule] != 0) {
            this.modulesTurboMQInteger[targetModule] = this.calculateModuleTurboMQ(this.moduleCohesion[targetModule], this.moduleCoupling[targetModule]);
        } else {
            this.modulesTurboMQInteger[targetModule] = 0;
        }
        this.turboMQInteger += this.modulesTurboMQInteger[targetModule];


        this.variables[vertex] = targetModule;
        this.numberOfVerticesInModule[originalModule]--;
        this.verticesInModule[originalModule].remove(vertex);
        if (this.numberOfVerticesInModule[originalModule] == 0) {
            this.usedModules.remove(originalModule);
            if (!availableModules.contains(originalModule)) {
                this.availableModules.push(originalModule);
            }
        }
        if (this.numberOfVerticesInModule[targetModule] == 0) {
            if (!usedModules.contains(targetModule)) {
                this.usedModules.add(targetModule);
            }
            this.availableModules.remove(targetModule);
        }

        this.numberOfVerticesInModule[targetModule]++;
        this.verticesInModule[targetModule].add(vertex);

        if (targetModule == smallestModule || originalModule == smallestModule) {
            smallestModule = this.recalculateSmallestModule();
        } else if (this.numberOfVerticesInModule[targetModule] == 1) {
            smallestModule = targetModule;
        } else if (this.numberOfVerticesInModule[originalModule] < minModuleSize && this.numberOfVerticesInModule[originalModule] > 0) {
            smallestModule = originalModule;
        }


        if (originalModule == largestModule || targetModule == largestModule) {
            largestModule = this.recalculateLargestModule();
        } else if (this.numberOfVerticesInModule[targetModule] > maxModuleSize) {
            largestModule = targetModule;
        }

        this.changeModuleInListOfModulesWithSize(originalModule, this.getVerticesInModule(originalModule).size() + 1);
        this.changeModuleInListOfModulesWithSize(targetModule, this.getVerticesInModule(targetModule).size() - 1);

        minModuleSize = this.numberOfVerticesInModule[smallestModule];
        maxModuleSize = this.numberOfVerticesInModule[largestModule];

        if (this.getVerticesInModule(originalModule).size() == 0) {
            this.isolatedModules--;
        } else if (this.getVerticesInModule(originalModule).size() == 1) {
            this.isolatedModules++;
        }

        if (this.getVerticesInModule(targetModule).size() == 1) {
            this.isolatedModules++;
        } else if (this.getVerticesInModule(targetModule).size() == 2) {
            this.isolatedModules--;
        }

        this.setNormalizedObjectivesValues();

        assert (maxModuleSize - minModuleSize) == this.recalculateDiffMaxMinModulesSize() : String.format("Max: %d - Min: %d - Received diff: %d", maxModuleSize, minModuleSize, this.recalculateDiffMaxMinModulesSize());
        assert this.numberOfVerticesInModule[smallestModule] == this.numberOfVerticesInModule[this.recalculateSmallestModule()];
        assert this.numberOfVerticesInModule[largestModule] == this.numberOfVerticesInModule[this.recalculateLargestModule()];
        assert minModuleSize == this.numberOfVerticesInModule[smallestModule];
        assert maxModuleSize == this.numberOfVerticesInModule[largestModule];
        assert this.recalculateCohesion() == this.cohesion;
        assert this.recalculateCoupling() == this.coupling;
        assert this.isolatedModules == this.recalculateNumberOfIsolatedModules();
        assert this.recalculateTurboMQ() * 10000 == this.turboMQInteger;
    }

    private void setNormalizedObjectivesValues() {
        if (this.problem instanceof MCAProblem) {
            this.normalizedObjectives = this.getProblem().normalize(new double[]{this.coupling, -this.cohesion, (double) -this.turboMQInteger / 10000, -this.usedModules.size(), this.isolatedModules});
        }
//        else if (this.problem instanceof ECAProblem) {
//            return new double[]{this.coupling, -this.cohesion, (double) -this.turboMQInteger / 10000, -this.usedModules.size(), this.maxModuleSize - this.minModuleSize};
//        }
        else {
            throw new NotImplementedException("Problem not implemented");
        }
    }

    public int recalculateDiffMaxMinModulesSize() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int module = 0; module < verticesInModule.length; module++) {
            if (verticesInModule[module].size() > max) {
                max = verticesInModule[module].size();
            }
            if (verticesInModule[module].size() < min && verticesInModule[module].size() > 0) {
                min = verticesInModule[module].size();
            }
        }
        return max - min;
    }

    public int recalculateNumberOfIsolatedModules() {
        int result = 0;
        for (Integer module : usedModules) {
            if (this.getVerticesInModule(module).size() == 1) {
                result++;
            }
        }
        return result;
    }

    private void changeModuleInListOfModulesWithSize(int module, int oldSize) {
        int size = this.getVerticesInModule(module).size();
        this.listOFModulesWithSize[oldSize].remove(module);
        if (this.listOFModulesWithSize[oldSize].isEmpty()) {
            this.listOFModulesWithSize[oldSize] = null;
        }
        if (this.listOFModulesWithSize[size] == null) {
            this.listOFModulesWithSize[size] = new HashSet<>();
        }
        this.listOFModulesWithSize[size].add(module);
    }

    public Set<Integer> getVerticesInModule(int module) {
        return this.verticesInModule[module];
    }

    public int getSmallestModule() {
        return this.smallestModule;
    }

    public int getLargestModule() {
        return this.largestModule;
    }

    public int recalculateSmallestModule() {
        int min = Integer.MAX_VALUE;
        int smallest = 0;
        for (int i = 0; i < this.numberOfVerticesInModule.length; i++) {
            if (numberOfVerticesInModule[i] < min && numberOfVerticesInModule[i] > 0) {
                min = numberOfVerticesInModule[i];
                smallest = i;
            }
        }
        return smallest;
    }

    public int recalculateLargestModule() {
        int max = Integer.MIN_VALUE;
        int largest = 0;
        for (int i = 0; i < this.numberOfVerticesInModule.length; i++) {
            if (numberOfVerticesInModule[i] > max) {
                max = numberOfVerticesInModule[i];
                largest = i;
            }
        }
        return largest;
    }

    @Override
    public double[] objectivesNormalized() {
        return this.problem.normalize(this.objectives());
    }
}
