import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

public class NPuzzle {

    public static class Pair<L, R> {
        public L l;
        public R r;
        public Pair(L l, R r){
            this.l = l;
            this.r = r;
        }
    }

    public static int parity(NPuzzleState state) {
        int inversions = 0;
        ArrayList<Integer> nums = new ArrayList<>();
        int[][] board = state.getBoard();
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                nums.add(board[i][j]);
            }
        }
        Integer[] copy = new Integer[nums.size()];
        nums.toArray(copy);
        for (int i = 0; i < copy.length; i++){
            for (int j = i + 1; j < copy.length; j++){
                if (copy[i] != 0 && copy[j] != 0 && copy[i]> copy[j]){
                    inversions++;
                }
            }
        }
        return inversions % 2;
    }

    public static boolean isSolvable(NPuzzleState initState, NPuzzleState goalState) {
        return parity(initState) == parity(goalState);
    }

    public static enum Action{ 
        // Define possible actions for the N-Puzzle search problem
        // [start:1]
    	moveLeft,moveRight,moveUp,moveDown
        // [end:1]
    }

    public static NPuzzleState successor(NPuzzleState state, Action action) {
        int[][] board = state.getBoard();
        int s = board.length;
        int r = state.getR();
        int c = state.getC();
        // Implement a successor function
        // Your code should return a new state if the action is value
        // otherwise return null
        // [start:2]
        int temp;
        boolean swapped = false;
        int[][] newBoard = state.copyBoard();
        if(action == Action.moveUp && r>0){
        	temp = newBoard[r-1][c];
        	newBoard[r-1][c] = 0;
        	newBoard[r][c] = temp;
        	swapped = true;
        }
        if(action == Action.moveDown && r<s-1){
        	temp = newBoard[r+1][c];
        	newBoard[r+1][c] = 0;
        	newBoard[r][c] = temp;
        	swapped = true;
        }
        if(action == Action.moveLeft && c>0){
        	temp = newBoard[r][c-1];
        	newBoard[r][c-1] = 0;
        	newBoard[r][c] = temp;
        	swapped = true;
        }
        if(action == Action.moveRight && c<s-1){
        	temp = newBoard[r][c+1];
        	newBoard[r][c+1] = 0;
        	newBoard[r][c] = temp;
        	swapped = true;
        }
        if(swapped){
        	NPuzzleState newState = new NPuzzleState(newBoard);
        	return newState;
        }
        // [end:2]
        return null;  // <- action is invalid
    }

    public static ArrayList<TreeNode<NPuzzleState>> expandSuccessors(TreeNode<NPuzzleState> node) {
        ArrayList<TreeNode<NPuzzleState>> successors = new ArrayList<>();
        NPuzzleState state = node.getState();
        // Define a successor function for the N-Puzzle search problem
        // Your code should add all child nodes to "successors"
        // Hint: use successor(.,.) function above
        // [start:3]
        if(node.getState().getR() > 0)
        	successors.add(new TreeNode<NPuzzleState>
        	(successor(state, Action.moveUp), node, Action.moveUp, 1));
        if(node.getState().getR() < node.getState().getBoard().length - 1)
        	successors.add(new TreeNode<NPuzzleState>
        	(successor(state, Action.moveDown), node, Action.moveDown, 1));
        if(node.getState().getC() > 0)
        	successors.add(new TreeNode<NPuzzleState>
        	(successor(state, Action.moveLeft), node, Action.moveLeft, 1));
        if(node.getState().getC() < node.getState().getBoard().length - 1)
        	successors.add(new TreeNode<NPuzzleState>
        	(successor(state, Action.moveRight), node, Action.moveRight, 1));
        // [end:3]
        return successors;
    }

    public static boolean isGoal(NPuzzleState state, NPuzzleState goalState) {
        boolean desiredState = true;
        // Implement a goal test function
        // Your code should change desiredState to false if the state is not a goal 
        // [start:4]
        if(!state.toString().equals(goalState.toString())) return desiredState = false;
        // [end:4]
        return desiredState;
    }

    public static Pair<ArrayList<Action>, Integer> solve(
            NPuzzleState initState, NPuzzleState goalState, 
            Queue<TreeNode<NPuzzleState>> frontier, boolean checkClosedSet, int limit) {
        HashSet<String> closed = new HashSet<>();
        ArrayList<Action> solution = new ArrayList<>();
        int numSteps = 0;
        // Implement Graph Search algorithm
        // Your algorithm should add action to 'solution' and
        // for every node you remove from the frontier add 1 to 'numSteps'
        // [start:5]
        frontier.add(new TreeNode<NPuzzleState>(initState));
        while(!frontier.isEmpty() && numSteps < limit){
        	TreeNode<NPuzzleState> node = frontier.poll();	numSteps++;
        	if(isGoal(node.getState(), goalState)){
        		//Backtracking to parent node
        		ArrayList<Action> tempSolution = new ArrayList<>();
        		while(node.getParent() != null){
        			tempSolution.add((Action) node.getAction());
        			node = node.getParent();
        		}
        		//Rearrange the ArrayList to start at parent node
        		for(int i=tempSolution.size()-1;i>=0;i--){
        			solution.add(tempSolution.get(i));
        		}     		
        		break;
        	}
        	if(checkClosedSet){
	        	if(!closed.contains(node.getState().toString())){
	        		closed.add(node.getState().toString());
	    	        ArrayList<TreeNode<NPuzzleState>> nodes = expandSuccessors(node);
	    	        for(TreeNode<NPuzzleState> thisNode: nodes){
	    	        	frontier.add(thisNode);
	    	        }
	        	}
        	}
        	else{
    	        ArrayList<TreeNode<NPuzzleState>> nodes = expandSuccessors(node);
    	        for(TreeNode<NPuzzleState> thisNode: nodes)
    	        	frontier.add(thisNode);
        	}
        }
        // [end:5]
        return new Pair<ArrayList<Action>, Integer>(solution, numSteps);
    }

    public static class HeuristicComparator implements Comparator<TreeNode<NPuzzleState>> {

        private NPuzzleState goalState;
        private int heuristicNum;
        private boolean isAStar;
        private HashMap<Integer, Pair<Integer, Integer>> goalStateMap = null;

        public HeuristicComparator(NPuzzleState goalState, int heuristicNum, boolean isAStar) {
            this.goalState = goalState;
            this.heuristicNum = heuristicNum;
            this.isAStar = isAStar;
        }

        public int compare(TreeNode<NPuzzleState> n1, TreeNode<NPuzzleState> n2) {
            Double s1V = 0.0;
            Double s2V = 0.0;
            if (this.heuristicNum == 1) {
                s1V = h1(n1.getState());
                s2V = h1(n2.getState());
            } else {
                s1V = h2(n1.getState());
                s2V = h2(n2.getState());
            }
            if (this.isAStar) {  // AStar h(n) + g(n)
                s1V += n1.getPathCost();
                s2V += n2.getPathCost();
            }
            return s1V.compareTo(s2V);
        }

        public double h1(NPuzzleState s) {
            double h = 0.0;
            int[][] board = s.getBoard();
            int[][] goalBoard = goalState.getBoard();
            // Implement misplaced tiles heuristic
            // Your code should update 'h'
            // [start:6]
            for(int i=0;i<board.length;i++){
            	for(int j=0;j<board.length;j++){
            		if(board[i][j] != goalBoard[i][j] && board[i][j] != 0) h++;
            	}
            }
            // [end:6]
            s.setEstimatedCostToGoal(h);
            return h;
        }

        public double h2(NPuzzleState s) {
            double h = 0.0;
            int[][] board = s.getBoard();
            int[][] goalBoard = goalState.getBoard();
            // Implement number-of-blocks-away heuristic
            // Your code should update 'value'
            // [start:7]
            for(int i=0;i<board.length;i++){
            	for(int j=0;j<board.length;j++){
            		for(int k=0;k<board.length;k++){
                    	for(int l=0;l<board.length;l++){
                    		if(board[i][j] == goalBoard[k][l] && board[i][j] != 0){
                    			h += Math.abs(i-k);
                    			h += Math.abs(j-l);
                    		} 
                    	}
                    }
            	}
            }
            // [end:7]
            s.setEstimatedCostToGoal(h);
            return h;
        }

    }

    public static void testRun(
            NPuzzleState initState, NPuzzleState goalState, 
            Queue<TreeNode<NPuzzleState>> frontier) {
        if (NPuzzle.isSolvable(initState, goalState)) {
            Pair<ArrayList<Action>, Integer> solution = solve(
                initState, goalState, frontier, true, 500000);
            System.out.println(initState);
            NPuzzleState curState = initState;
            for (Action action : solution.l) {
                curState = successor(curState, action);
                System.out.print("Action: ");
                System.out.println(action.toString());
                System.out.println(curState);
            }
            System.out.print("Number of steps in the solution: ");
            System.out.println(solution.l.size());
            System.out.print("Number of nodes expanded: ");
            System.out.println(solution.r);
        } else{
            System.out.println("Not solvable!");
        }
    }
    
    //Editing the entire experiment method to print out the results seen in question 5.
    //The method only needs to be run once, and the method's signature is not changed.
    public static void experiment(
            NPuzzleState goalState, Queue<TreeNode<NPuzzleState>> frontier) {
    	//Initialize Maps to record length frequency & no.of steps cumulated
        TreeMap<Integer,Integer> freq_h1 = new TreeMap<Integer,Integer>();
        TreeMap<Integer,Integer> freq_h2 = new TreeMap<Integer,Integer>();
        TreeMap<Integer,Double> step_h1 = new TreeMap<Integer,Double>();
        TreeMap<Integer,Double> step_h2 = new TreeMap<Integer,Double>();
        System.out.println("length,step_h1,step_h2,num_h1,num_h2");
        for (int i = 0; i < 1000; i++){
            NPuzzleState initState = new NPuzzleState(8);  // random
            if (!NPuzzle.isSolvable(initState, goalState)) {
                i--;
                continue;
            }
            // Experiment to evaluate a search setting
            // [start:8]
            // Priority Queue: A* with h1  
            Queue<TreeNode<NPuzzleState>> frontierh1 = new PriorityQueue<>(new HeuristicComparator(goalState, 1, true));
            // Priority Queue: A* with h2
            Queue<TreeNode<NPuzzleState>> frontierh2 = new PriorityQueue<>(new HeuristicComparator(goalState, 2, true));
            //Solve the puzzle
            Pair<ArrayList<Action>, Integer> solutionh1 = solve(initState, goalState, frontierh1, true, 500000);
            Pair<ArrayList<Action>, Integer> solutionh2 = solve(initState, goalState, frontierh2, true, 500000);
            //Putting results into the Maps
            if(!freq_h1.containsKey(solutionh1.l.size())){
            	freq_h1.put(solutionh1.l.size(),0);            	
            	step_h1.put(solutionh1.l.size(),0.0);
            }
            if(!freq_h2.containsKey(solutionh2.l.size())){
            	freq_h2.put(solutionh2.l.size(),0);
            	step_h2.put(solutionh2.l.size(),0.0);
            }
            freq_h1.put(solutionh1.l.size(), freq_h1.get(solutionh1.l.size())+1);
            step_h1.put(solutionh1.l.size(), (double)(step_h1.get(solutionh1.l.size())+solutionh1.r));
            freq_h2.put(solutionh2.l.size(), freq_h2.get(solutionh2.l.size())+1);
            step_h2.put(solutionh2.l.size(), (double)(step_h2.get(solutionh2.l.size())+solutionh2.r));
            // [end:8]
        }  
        //Finding average steps for h1 & h2
        for(Integer i:freq_h1.keySet()){
        	step_h1.put(i, (step_h1.get(i)/freq_h1.get(i)));
        }
        for(Integer i:freq_h2.keySet()){
        	step_h2.put(i, (step_h2.get(i)/freq_h2.get(i)));
        }
        DecimalFormat d = new DecimalFormat("#0.#");
        int max = freq_h1.lastKey();
        if(freq_h1.lastKey() < freq_h2.lastKey()) max = freq_h2.lastKey();
        //Printing out results
        for(int i=0;i<=max;i++){
        	double sh1=0.0,sh2=0.0;
        	int nh1=0,nh2=0;
        	if(step_h1.get(i) != null) sh1 = step_h1.get(i);
        	if(step_h2.get(i) != null) sh2 = step_h2.get(i);
        	if(freq_h1.get(i) != null) nh1 = freq_h1.get(i);
        	if(freq_h2.get(i) != null) nh2 = freq_h2.get(i);
        	if(nh1 != 0 || nh2 != 0) System.out.println(i+","+d.format(sh1)+","+d.format(sh2)+","+nh1+","+nh2);
        }
    }

    public static void main(String[] args) {
        NPuzzleState.studentID = 5988040;

        int[][] goalBoard = {{0, 1, 2},{3, 4, 5},{6, 7, 8}};
        NPuzzleState goalState = new NPuzzleState(goalBoard);

        /*
         *  Select an implementation of a frontier from the code below
         */
        
        // Stack
        // Queue<TreeNode<NPuzzleState>> frontier = Collections.asLifoQueue(
        //     new LinkedList<TreeNode<NPuzzleState>>());

        // Queue
        // Queue<TreeNode<NPuzzleState>> frontier = 
        //     new LinkedList<TreeNode<NPuzzleState>>();
        
        // Priority Queue: A* with h1  
         //Queue<TreeNode<NPuzzleState>> frontier = new PriorityQueue<>(
          //new HeuristicComparator(goalState, 1, true));
        
        // Priority Queue: A* with h2
         Queue<TreeNode<NPuzzleState>> frontier = new PriorityQueue<>(
          new HeuristicComparator(goalState, 2, true));
        

        //int[][] easy = {{1, 4, 2},{3, 0, 5},{6, 7, 8}};
        //NPuzzleState initState = new NPuzzleState(easy);

         int[][] hard = {{7, 2, 4}, {5, 0, 6}, {8, 3, 1}};
         NPuzzleState initState = new NPuzzleState(hard);

        testRun(initState, goalState, frontier);
        //experiment(goalState, frontier);

    }
}

