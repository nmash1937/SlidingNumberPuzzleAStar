
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;


/**
 * Puzzle game that can solve a board using the A* method of any size board with number starting
 * at 1 and only having one space. A correct board is used in this game as the space being in the bottom right
 * of the board 
 * 
 * ex: 
 * 1	2	3
 * 4	5	6
 * 7	8	 
 * 
 * @author Nick Masciandaro
 *
 */
public class SlidingPuzzle {
	State currentState;	
	
	/**
	 * Runs the puzzle game
	 * @param args
	 * @throws CannotSlideException
	 */
	public static void main(String[] args) throws CannotSlideException {
		System.out.print("Please enter the arrangement of numbers that you would like, along with"
				+ "the board width and height. For example: \n12\n34\n5_\n would be entered as:\n"
				+ "\"1 2 3 4 5 _\"\n\"2\"\n\"3\"\n\nConfiguration of numbers:");
		
		Scanner in = new Scanner(System.in);
		String config = in.nextLine();
		System.out.print("Width: ");
		String width = in.nextLine();
		System.out.print("\nHeight: ");
		String height = in.nextLine();
		System.out.print("\nHow many slides do you want the scramble to be? ");
		String scramb = in.nextLine();
		in.close();
		System.out.println();
		
		
		
		SlidingPuzzle puzzle = new SlidingPuzzle(config,Integer.parseInt(width),Integer.parseInt(height));
		puzzle.scramble(Integer.parseInt(scramb));
		puzzle.solve();

	}
	
	public SlidingPuzzle(String config, int width, int length) {
		currentState = new State(config, width, length,0, new LinkedList<State>());
	}
	
	public boolean canSlide(State b, Dir dir) {
		ArrayList<String> shouldNotContain = new ArrayList<String>();
		switch(dir) {
		case DOWN:
			for(int i=0; i<b.width;i++) {
				shouldNotContain.add(b.board[0][i]);
			}
			break;
		case LEFT:
			for(int i=0; i<b.height;i++) {
				shouldNotContain.add(b.board[i][b.width-1]);
			}
			break;
		case RIGHT:
			for(int i=0; i<b.height;i++) {
				shouldNotContain.add(b.board[i][0]);
			}
			break;
		case UP:
			for(int i=0; i<b.width;i++) {
				shouldNotContain.add(b.board[b.height-1][i]);
			}
			break;
		default:
			break;
		}
		
		if(shouldNotContain.contains(" ")) return false;
		return true;
	}

	public State slide(State b, Dir dir) throws CannotSlideException {
		LinkedList<State> path = new LinkedList<State>();
		for(State a : b.path) {
			path.add(a);
		}
		State temp = new State(b.values,b.width,b.height, b.moves, path);
		for(int x=0; x<b.height;x++) {
			for(int y=0; y<b.width;y++) {
				temp.board[x][y] = b.board[x][y];
			}
		}
		Pair p = goToSpace(temp);
		int x = p.x;
		int y = p.y;
		
		temp.board[x][y] = temp.board[x+dir.x][y+dir.y];
		temp.board[x+dir.x][y+dir.y] = " ";
		temp.moves++;
		return temp;
	}

	
	/**
	 * takes the state of a puzzle and generates all possible moves from that point
	 * @param b the state of the board
	 * @return Arraylist of possible moves
	 * @throws CannotSlideException
	 */
	public ArrayList<State> successors(State b) throws CannotSlideException{
		ArrayList<State> ret = new ArrayList<State>();
		b.path.add(b);
		if(canSlide(b, Dir.UP)) {
			ret.add(slide(b, Dir.UP));
		}
		if(canSlide(b, Dir.DOWN)) {
			ret.add(slide(b, Dir.DOWN));
		}
		if(canSlide(b, Dir.RIGHT)) {
			ret.add(slide(b, Dir.RIGHT));
		}
		if(canSlide(b, Dir.LEFT)) {
			ret.add(slide(b, Dir.LEFT));
		}
		return ret;
		
	}
	
	/**
	 * Scrambles the board to specified number
	 * @param slides - how scrambled the board will be
	 * @throws CannotSlideException
	 */
	public void scramble(int slides) throws CannotSlideException {
		Random r = new Random();
		for(int x=0; x<slides;x++) {
			double random = r.nextDouble();
			if(random<.25) {
				if(canSlide(currentState, Dir.UP)) currentState = slide(currentState, Dir.UP);
				else currentState = slide(currentState, Dir.DOWN);
			}
			else if(random<.5) {
				if(canSlide(currentState, Dir.LEFT))currentState = slide(currentState, Dir.LEFT);
				else currentState = slide(currentState, Dir.RIGHT);
			}
			else if(random<.75) {
				if(canSlide(currentState, Dir.DOWN))currentState = slide(currentState, Dir.DOWN);
				else currentState = slide(currentState, Dir.UP);
			}
			else {
				if(canSlide(currentState, Dir.RIGHT))currentState = slide(currentState, Dir.RIGHT);
				else currentState = slide(currentState, Dir.LEFT);
			}
		}
		currentState.moves = 0;
		currentState.path = new LinkedList<State>();
		System.out.println("Scrambled Board: ");
		currentState.print();
	}
	
	/**
	 * Solves the puzzle using A* search algorithm, prints the path it took
	 * @return 1 if solved, -1 if not solved
	 * @throws CannotSlideException
	 */
	public int solve() throws CannotSlideException {
		PriorityQueue<State> open = new PriorityQueue<State>();
		ArrayList<State> closed = new ArrayList<State>();
		int gen = 0;
		State b = currentState;
		open.add(b);
		while(!open.isEmpty()) {
			b = open.poll();
			int i = closed.indexOf(b);
			if(i>=0 && getAStarValue(closed.get(i)) <= getAStarValue(b)) continue;
			closed.add(b);
			if(calcH(b) == 0) {
				System.out.println("\n\nPATH TAKEN:");
				b.path.add(b);
				for(State s : b.path) {
					s.print();
				}
				System.out.println("Gemnerated: "+gen);
				return 1;
			}else {
				List<State> children = successors(b);
				gen += children.size();
				for(State child : children) {
					i = closed.indexOf(child);
					Integer priorCost = i >= 0 ? getAStarValue(closed.get(i)) : null;
					if(priorCost == null) {
						open.add(child);
					}
					else if(priorCost > getAStarValue(child)) {
						closed.remove(i);
						open.add(child);
					}
				}
			}
		}
		
		System.out.println("No Solution Found");
		return -1;
	}
	
	/**
	 * Given a state, goes to the space and gives back the x and y coordinates
	 * @param b the state
	 * @return x and y coordinates
	 */
	public Pair goToSpace(State b) {
		int x = 0;
		int y = 0;
		BREAK: for(x=0; x<b.height;x++) {
			for(y=0; y<b.width; y++) {
				if(b.board[x][y].equals(" ")) {
					break BREAK;
				}
			}
		}
		Pair ret = new Pair();
		ret.x = x;
		ret.y = y;
		return ret;
	}
	
	/**
	 * Calculates the A* value which is moves + heuristic of the current state
	 * @param b the state
	 * @return the A* value
	 */
	public static int getAStarValue(State b) {
		return calcH(b)+b.moves;
	}
	
	/**
	 * calculates the manhatten distance from a correct state of the given state
	 * @param b the state
	 * @return the heuristic value
	 */
	public static int calcH(State b) {
		int counter = 1;
		int total = 0;
		for(int x=1; x<b.height*b.width;x++) {
			int i = 0;
			int j = 0;
			HERE:for(i=0; i<b.height;i++) {
				for(j=0; j<b.width;j++) {
					if(b.board[i][j].equals(String.valueOf(counter))) {
						break HERE;
					}
					
				}
			}
			counter++;
			int actualI = (x-1)/b.width;
			int actualJ = (x-1)%b.width;
			total += Math.abs(i-actualI)+Math.abs(j-actualJ);
		}
		return total;
	}
	
	/**
	 * State that is used in Puzzle game to represent the board and its history
	 * @author Nick Masciandaro
	 *
	 */
	private static class State implements Comparable<State>{
		public String board[][];
		public int width;
		public int height;
		public String values;
		public int moves;
		Queue<State> path;
		
		/**
		 * Constructor
		 * @param values: String that represents the board
		 * @param width: width of board
		 * @param height: height of board
		 * @param moves: amount of moves that has been made
		 * @param path: the history of the board
		 */
		public State(String values, int width, int height, int moves, Queue<State> path) {
			this.values = values;
			this.width = width;
			this.height = height;
			this.moves = moves;
			board = new String[height][width];
			this.path = path;
			
			String[] nums = values.split(" ");
			
			
			for(int i=0; i<height;i++) {
				for(int j=0; j<width; j++) {
					String num = nums[j+i*width];
					if(num.equals("_")) {
						num = " ";
					}
					board[i][j] = num;
				}
			}
		}
		
		/**
		 * Prints the board
		 */
		public void print() {
			for(int i=0; i<height;i++) {
				for(int j=0; j<width; j++) {
					String num = board[i][j];
					if(!num.equals(" ")) {
						if(Integer.parseInt(num)>9) {
							System.out.print(num+" ");
						}else {
							System.out.print(board[i][j]+"  ");
						}
					}else {
						System.out.print("   ");
					}
					
					
				}
				System.out.println();
			}
			System.out.println("A* value: "+getAStarValue(this));
			System.out.println("Heuristic value: "+calcH(this));
			System.out.println("Moves :"+this.moves);
			System.out.println("-----------\n");
		}

		/**
		 * Overrides the compareTo method to only sort priority by A* value
		 */
		@Override
		public int compareTo(State o) {
			return getAStarValue(this)-getAStarValue(o);
		}
		
		/**
		 * Overrides Object equals method to only account for board state
		 */
		@Override
		public boolean equals(Object other) {
			if(other instanceof State) {
				State o = (State)other;
				if(o.height == this.height && o.width == this.width) {
					for(int x=0; x<o.height;x++) {
						for(int y=0; y<o.width;y++) {
							if(!(o.board[x][y].equals(this.board[x][y]))) {
								return false;
							}
						}
					}
				}else {
					return false;
				}
				return true;
			}
			return false;		
		}
		
	}
	
	/**
	 * An exception if the user tries to slide the board that cannot be slid up
	 * @author Nick Masciandaro
	 */
	public class CannotSlideException extends Exception{
		private static final long serialVersionUID = 1L;

		/**
		 * Constructor
		 * @param message: the passed message of the excpetion
		 */
		public CannotSlideException(String message) {
			super(message);
		}
	}
	
	/**
	 * Pair method to return X and Y values
	 * @author Nick Masciandaro
	 */
	public class Pair{
		public int x,y;
	}
	
	/**
	 * Enumerated type with values corresponding to offsets
	 * @author nickmasciandaro
	 */
	public enum Dir{
		UP(1, 0), DOWN(-1, 0), LEFT(0, 1), RIGHT(0, -1);
		int x,y;
		private Dir(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
}