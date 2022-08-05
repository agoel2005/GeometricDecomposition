
import java.util.*;

public class Algorithm {

	public static void main(String[] args) {
		
		//for the partition, just enter in the parts normally as shown below
		Partition p = new Partition(4,3,2,1);
		//over here, the two inputs are the partition and the value of n
		FlowPolytope f = new FlowPolytope(p, 5);
		
		
		
		//this line is meant to print out the inequalities of the flow polytope
		System.out.println(f);
		//all of these string manipulation lines below are for getting rid of redundancies in the output, such as 1/1 fractions
		String s = getVolume(f);
		String s2 = s.replace("( +", "(");
		String s3 = s2.replace("\\frac{1}{1}", "");
		String s4 = s3.replace("^{1}", "");
		System.out.println(s4.substring(3));
		
		
		
	}
	
	//this is the main recursive function used to get the volume
	static String getVolume(FlowPolytope f) {
		String volume = "";
		//nInequality is a data structure I constructed. It's there because the cases, when joined into one line, form a huge inequality with many (n) parts. 
		//the list of nInequalities polytopeDefinition is just all of the inequalities in the flow polytope
		ArrayList<nInequality> polytopeDefinition = new ArrayList((ArrayList<nInequality>) f.getInequalities());
		
		//base case 1: if the flow polytope is empty, return an empty string (no volume)
		if(polytopeDefinition.size() == 0) {
			return "";
		}
		//base case 2: if the flow polytope has only 1 inequality, that inequality must be type A --> the volume is 1/x! * a_1^(x) where x is the # of terms on the LHS of the Type A
		if (polytopeDefinition.size() == 1) {
			return " + \\frac{1}{" +factorial(polytopeDefinition.get(0).getTerms().get(0).size()) + "} a_1^{" + polytopeDefinition.get(0).getTerms().get(0).size() +  "}";
		}
		
		//number of inequalities in the flow polytope 
		int totalInequalities = polytopeDefinition.size();
		
		//we work with the inequalities from bottom to up. that's why "currentInequality" is the last inequality in f. 
		nInequality currentInequality = f.getInequalities().get(totalInequalities -1 );
		
		//this list just contains all of the cases for currentInequality. It calls the getCases() function which is defined below
		ArrayList<nInequality> cases = new ArrayList(getCases(currentInequality));
			
			//in the following section, we solve each case separately.
			//We see what it adds to the volume and how it affects the upper inequalities.
			//Based on that, we then create a new flow polytope called "newF" that has the updated upper inequalities based on the case and we remove currentInequalities from it
			//this keeps going on recursively so that we are able to cover all of the subcases for each major case. 
			for (nInequality ineq: cases) {
				
				//inequalitiesLeft is all of the inequalities left in polytope f excluding the current inequality.
				ArrayList<nInequality> inequalitiesLeft = new ArrayList<>();
				for (int j = 0; j<totalInequalities - 1; j++) {
					inequalitiesLeft.add(polytopeDefinition.get(j));
				}
				
				List<nInequality> tempList = new ArrayList(f.getInequalities());
				
				//we define the newF polytope here. later on, depending on whether the polytope is Type A' or Type B', we update newF accordingly.
				FlowPolytope newF = new FlowPolytope(tempList);
				
				//if the current inequality case only has 2 terms, that means its of  the form LHS <= RHS. 
				//for netflow with all elements >0, this implies that it's Type A, as any Type B inequality can be broken down further into smaller subcases --> it'll have >2 terms
				if (ineq.getTerms().size() == 2) {
					//chainLength is just the number of edges less than or equal to the netflow element
					int chainLength = ineq.getTerms().get(0).size();
					//update the volume accordingly
					volume = volume + " + \\frac{1}{" +factorial(chainLength) + "} a_" + (totalInequalities) + "^{" + chainLength +  "} \\left (";
					//since a Type A doesn't affect any above inequalities, nothing changes. newF is just f but with the last inequality removed (since we just worked with it) 
					newF = new FlowPolytope(inequalitiesLeft);
					volume =  volume + getVolume(newF) + "\\right )";
				}
				
				
				//here, we work with type A' inequalities
				//the .getEdgeInfo() function is something I defined for nInequality objects. I define it in more detail in the nInequality class definitions. 
				else if (ineq.getEdgeInfo()[0] == true) {
					//please refer to the overleaf doc for more explanations on the logic used to solve a Type A' inequality. 
					//the comments here will just be explaining what each variable is -- not going over the actual logic.
					int currentIndex = 0;
					//chainLength = # of edges <= netflow element
					int chainLength = ineq.getTerms().get(0).size();
					volume = volume + " + \\frac{1}{" +factorial(chainLength) + "} a_" + (totalInequalities) + "^{" + chainLength +  "} \\left (";
					while (currentIndex < ineq.getTerms().size() - 1) {
						chainLength = ineq.getTerms().get(currentIndex).size();
						int chainIndex = currentIndex;
						int expansion = 0;
						//when we form a bijection and say that an edge can be written as the sum of, say, 3 terms, I consider it as an "Expansion"
						//essentially, because we replaced 1 term with 3 terms, that inequality gets "expanded" by 2
						//So, the edge toBeInflated is simply what I'm forming a bijection with 
						//my definition of Edge(1,2) here is arbitrary. I rewrite this definition later. It's just here to initialize the variable. 
						Edge toBeInflated = new Edge(1,2);
						
						//the if statement is asking whether our current index is the second to last term in the nInequality
						//if so, the edge getting expanded is just the last edge in the last term of that nInequality
						//since there's nothing more after, it's expansion is just 1
						if (currentIndex == ineq.getTerms().size() - 2) {
							expansion = 1;
							toBeInflated = (Edge) ineq.getTerms().get(ineq.getTerms().size() - 1).get(ineq.getTerms().get(ineq.getTerms().size() - 1).size() - 1);
							currentIndex ++;
						}
						else {
							//otherwise, we have to see how many consecutive netflow terms there are before our next term that's full of just edges
							//all of the consecutive netflows in the middle don't really matter 
							int nextTerm = currentIndex + 2;
							//i add 2 because currentIndex is the current edge term and currentIndex + 1 is the netflow term right after.
							//so, i start searching from the next index 
							for (int x = currentIndex + 2; x<ineq.getTerms().size(); x++) {
								if (ineq.getEdgeInfo()[x] == true) {
									nextTerm = x;
									break;
								}
							}
							//extra terms is the number of all edge terms in between the two netflow elements
							int extraTerms = 0;
							int indexOfNextNetflow = 0;
							for (int x = nextTerm; x<ineq.getTerms().size(); x++) {
								if (ineq.getEdgeInfo()[x] == true) {
									extraTerms ++;
								}
								else {
									indexOfNextNetflow = x;
									currentIndex = x + 1;
									break;
								}
							}
							//the expansion is the number of edges in the last edge term minus the number of edges in the first edge term in this consecutive sequence
							expansion = ineq.getTerms().get(indexOfNextNetflow - 1).size() - ineq.getTerms().get(chainIndex).size();
							//redefine the toBeInflated edge accordingly 
							toBeInflated = (Edge) ineq.getTerms().get(indexOfNextNetflow).get(ineq.getTerms().get(indexOfNextNetflow).size() - 1);
						}
					
						//this is the row that toBeInflated belongs to. 
						//based on how how much the expansion is, we will add that many terms to the inequality.
						int inequalityRow = toBeInflated.getFirst();
						
						//subtract by 1 because the edges start at edge 1
						nInequality toBeChanged = new nInequality(f.getInequalities().get(inequalityRow - 1).getTerms());
						
						//newTerms is going to be the new version of toBeChanged once we update it accordingly  
						List<List<Object>> newTerms = new ArrayList<>();
						for (List<Object> terms: toBeChanged.getTerms()) {
							List<Object> terms2 = new ArrayList(terms);
							boolean contains = false;
							//we are basically searching through every term in the nInequality that toBeInflated belongs to
							//for each term, we look at every element in that term.
							//if the element (which I define as "term" in the code below) is equal to toBeInflated, then we must add "expansion" number of terms to it 
							for (Object term : terms) {
								if (term instanceof Edge) {
									if (((Edge) term).same(toBeInflated)) {
										contains = true;
										break;
									}
								}
							}
							if (contains) {
								for (int x = 0; x<expansion; x++) {
									// keep adding "expansion" number of terms to that nInequality term. 
									terms2.add(toBeInflated);
								}
							}
							newTerms.add(terms2);
							
						}
						
						//everything below is just updating newF so that the nInequality that was just changed is reflected in newF. 
						//we then call the recursive definition again 
						nInequality newInequality = new nInequality(newTerms);
						List<nInequality> newInequalities = new ArrayList<>();
						for (int i = 0; i< inequalityRow - 1; i++) {
							newInequalities.add(newF.getInequalities().get(i));
						}
						newInequalities.add(newInequality);
						for (int i = inequalityRow; i<inequalitiesLeft.size(); i++) {
							newInequalities.add(inequalitiesLeft.get(i));
						}
						
						newF = new FlowPolytope(newInequalities);
					}
					
					volume =  volume + getVolume(newF) + "\\right )";
				}
				
				//we now work with the Type B' inequality case
				//again, the overall logic is outlined in the Overleaf. refer there for more details. 
				else{
					int currentIndex = 0;
					
					
					while (currentIndex < ineq.getTerms().size() - 1) {
						
						//currentLength is the # of edges in the edge term defining the Type B' inequality 
						int currentLength = ineq.getTerms().get(currentIndex + 1).size();
						int prevNetflowIndex = currentIndex;

						//find next netflow term that's greater than this 
						//we need this info to see how many consecutive edge terms lie between the two netflow terms 
						int nextNetflow = 0;
						for (int x = currentLength + 1; x<ineq.getTerms().size(); x++) {
							if (ineq.getEdgeInfo()[x] == false) {
								nextNetflow = x;
								currentIndex = x;
								break;
							}
						}
						
						//# of edge elements in the final edge term in our consecutive sequence
						int finalLength = ineq.getTerms().get(nextNetflow - 1).size();
						//definition of how much to expand the inequality 
						int expansion = finalLength - currentLength + 1;
						
						//all of this is just finding the toBeInflated edge and updating the nInequality accordingly
						//the code should be the exact same as in the Type A' inequality case. 
						//refer to those comments for clarifications 
						Edge toBeInflated = null;
						for (Object obj: ineq.getTerms().get(nextNetflow)) {
							if (!(ineq.getTerms().get(prevNetflowIndex).contains(obj))) {
								toBeInflated = (Edge) obj;
							}
						}
						
						int inequalityRow = toBeInflated.getFirst();
						//subtract by 1 because the edges start at edge 1
						nInequality toBeChanged = new nInequality(f.getInequalities().get(inequalityRow - 1).getTerms());
						List<List<Object>> newTerms = new ArrayList<>();
						for (List<Object> terms: toBeChanged.getTerms()) {
							List<Object> terms2 = new ArrayList(terms);
							boolean contains = false;
							for (Object term : terms) {
								if (term instanceof Edge) {
									if (((Edge) term).same(toBeInflated)) {
										contains = true;
										break;
									}
								}
							}
							if (contains) {
								for (int x = 0; x<expansion; x++) {
									terms2.add(toBeInflated);
								}
							}
							newTerms.add(terms2);
							
						}
						
						nInequality newInequality = new nInequality(newTerms);
						List<nInequality> newInequalities = new ArrayList<>();
						for (int i = 0; i< inequalityRow - 1; i++) {
							newInequalities.add(newF.getInequalities().get(i));
						}
						newInequalities.add(newInequality);
						for (int i = inequalityRow; i<inequalitiesLeft.size(); i++) {
							newInequalities.add(inequalitiesLeft.get(i));
						}
						
						newF = new FlowPolytope(newInequalities);
					}
					volume = volume + getVolume(newF);
				}
				
	
			}
				
		return volume;
	}
	
	//this is just a factorial function. returns n! 
	static int factorial(int n) {
		int ans = 1;
		for (int i = 1; i<=n; i++) {
			ans = ans * i;
		}
		return ans;
	}
	
	
	
	//this function returns all cases for a given nInequality 
	//it implements the logic outlined in the "General Methods For Solving 
	static ArrayList<nInequality> getCases(nInequality i) {
		ArrayList<nInequality> cases = new ArrayList<>();
		
		//list of all the terms in the nInequality
		List<List<Object> >terms = i.getTerms();
		int numTerms = terms.size();
		
		//if we have sum of edges <= netflow + (maybe) sum of elements to start the nInequality 
		if (i.getEdgeInfo()[0] == true) {
			
			//this is the beginning case logic of having a Type A inequality
			if (terms.size() == 2) {
				if (terms.get(1).size() == 1) {
					cases.add(i);
					return cases;
				}
				
				//otherwise, we split it into the n+1 cases outlined in overleaf 
				
				int num = terms.get(1).size();
				for (int j = 0; j<num; j++) {
					//in general, each of the n+1 cases has 3 terms. those are left, middle, right
					//we update left, middle, right according to the logic in Overleaf 
					List<Object> left = new ArrayList<>();
					List<Object> middle =terms.get(0);
					List<Object> right = new ArrayList<>();
					
					for (int k = 0; k<j; k++) {
						left.add(terms.get(1).get(k));
					}
					for (int k = 0; k<j+1; k++) {
						right.add(terms.get(1).get(k));
					}
					
					//this handles the logic if the left inequality has no terms. That is the case 1 in the overleaf
					if (left.size() > 0) {
						nInequality partialCase = new nInequality(left, middle, right);
						ArrayList<nInequality> finalCases = getCases(partialCase);
						for (nInequality ineq: finalCases) {
							cases.add(ineq);
						}
					}
					
					//these are the cases 2 to n+1 in the overleaf 
					else {
						nInequality partialCase = new nInequality(middle, right);
						ArrayList<nInequality> finalCases = getCases(partialCase);
						
						for (nInequality ineq: finalCases) {
							cases.add(ineq);
						}
					}
					
				}
			}
			
			//this is when we have a Type A' inequality. We only consider the first 2 terms and do the exact breakup as in the above case
			//everything here is just technical details which are basically the same as above 
			else {
				//this is the ell = 1 base case 
				if (terms.get(1).size() == 1) {
					cases.add(i);
					return cases;
				}
				
				//implementation of the case logic in overleaf
				//lots of technical details, math details. nothing too important 
				List<Object> terms1 = terms.get(0);
				List<Object> terms2 = terms.get(1);
				
				
				int num = terms2.size();
				for (int j = 0; j<num; j++) {
					List<List<Object>> newCase = new ArrayList<>();
					for (List<Object> ineq: terms) {
						 newCase.add(ineq);
					}
					List<Object> left = new ArrayList<>();
					List<Object> middle =terms.get(0);
					List<Object> right = new ArrayList<>();
					newCase.remove(0);
					
					for (int k = 0; k<j; k++) {
						left.add(terms.get(1).get(k));
					}
					for (int k = 0; k<j+1; k++) {
						right.add(terms.get(1).get(k));
					}
					
					if (left.size() == 0) {
						newCase.add(0, right);
						newCase.add(0, middle);
						
					}
					else {
						if(j == num - 1) {
							newCase.add(0, middle);
							newCase.add(0, left);
						}
						
						else {
							newCase.add(0, right);
							newCase.add(0, middle);
							newCase.add(0, left);
						}
	
					}
					
					//recursive step: keep finding the cases for each new cases we create (basically forms a tree) 

					ArrayList<nInequality> finalCases = getCases(new nInequality(newCase));
					
					for (nInequality ineq: finalCases) {
						cases.add(ineq);
					}
					
				}
				
				
				
				
				
				
			}
			
		}
		
		//this is if we have a Type B' inequality 
		else {
			int length = terms.get(1).size();
			//ell = 1 base case 
			if (length == 1) {
				cases.add(i);
				return cases;
			}
			
			//implementation of the logic described in Overleaf
			//again, it's lots of technical details and loops 
			//it's pretty similar to the above stuff 
			List<Object> terms1 = terms.get(0);
			List<Object> terms2 = terms.get(1);
			
			
			for (int j = 0; j<length; j++) {
				List<List<Object>> newCase = new ArrayList<>();
				for (List<Object> ineq: terms) {
					 newCase.add(ineq);
				}
				List<Object> left = new ArrayList<>();
				List<Object> middle =terms.get(0);
				List<Object> right = new ArrayList<>();
				newCase.remove(0);
				
				for (int k = 0; k<j; k++) {
					left.add(terms.get(1).get(k));
				}
				for (int k = 0; k<j+1; k++) {
					right.add(terms.get(1).get(k));
				}
				
				if (left.size() == 0) {
					newCase.add(0, right);
					newCase.add(0, middle);
					
				}
				else {
					if(j == length - 1) {
						newCase.add(0, middle);
						newCase.add(0, left);
					}
					
					else {
						newCase.add(0, right);
						newCase.add(0, middle);
						newCase.add(0, left);
					}

				}
				
				//recursive, tree part 
				ArrayList<nInequality> finalCases = getCases(new nInequality(newCase));
				
				for (nInequality ineq: finalCases) {
					cases.add(ineq);
				}
				
			}
			
			
			return cases;
	
		}	
		
		return cases;
	}


}

//nInequality class definition 
class nInequality{
	//this contains every term and each element in every term 
	List<List<Object>> terms;
	
	//for each term in the nInequality, isEdges array will have a single boolean value
	//it's True if every element in the term is an edge
	//it's False otherwise (implying that there's a netflow element) 
	boolean[] isEdges;
	
	@Override
	//lets a inequalty print in a readable format 
	public String toString() {
		String s = "";
		for (List<Object> term: terms) {
			for(int k = 0; k<term.size(); k++) {
				if (k ==0) {
					s = s + term.get(k).toString();
				}
				else {
					s = s + " + " + term.get(k).toString();					
				}
			}
			s = s + " <= ";
		}
		return s.substring(0, s.length() - 3);
	}
	
	//how to form an nInequality if you already have the list of terms 
	public nInequality(List<List<Object>> a) {
		terms = new ArrayList(a);
		
		isEdges = new boolean[terms.size()];
		
		for (int i = 0; i<terms.size(); i++) {
			boolean good = true;
			for(Object obj: terms.get(i)) {
				if (!(obj instanceof Edge)) {
					good = false;
				}
			}
			isEdges[i] = good;
		}
	}
	
	//another way to form the nInequality 
	public nInequality(List<Object>...a ) {
		terms = new ArrayList<>();
		for (List<Object> term: a) {
			terms.add(new ArrayList(term));
		}
		isEdges = new boolean[terms.size()];
		
		for (int i = 0; i<terms.size(); i++) {
			boolean good = true;
			for(Object obj: terms.get(i)) {
				if (!(obj instanceof Edge)) {
					good = false;
				}
			}
			isEdges[i] = good;
		}

	}
	
	
	public List<List<Object>> getTerms(){
		return terms;
	}
	
	public boolean[] getEdgeInfo(){
		return isEdges;
	}
	
	
	
}



class FlowPolytope{
	//flow polytope class is just a list of nInequalities to solve 
	List<nInequality> conditions;
	
	public String toString() {
		String s = "";
		for (nInequality ineq: conditions) {
			s = s + ineq.toString() + "\n";
		}
		return s;
	}
	
	public FlowPolytope() {
		conditions = new ArrayList<nInequality>();
	}
	
	//can form it with a list of nInequalities 
	public FlowPolytope(List<nInequality> a) {
		conditions = a;
	}
	
	public FlowPolytope(nInequality...a) {
		conditions = new ArrayList<>();
		for (nInequality ineq: a) {
			conditions.add(ineq);
		}
	}
	
	
	//can form it with a Digraph 
	public FlowPolytope(Digraph G) {
		conditions = new ArrayList<>();
		ArrayList<Edge> edgeList = (ArrayList<Edge>) G.getEdges();
		int sinkVertex = edgeList.get(edgeList.size() - 1).getLast();
		for (int i = 1; i<sinkVertex; i++) {
			List<Object> left = new ArrayList<>();
			List<Object> right = new ArrayList<>();
			right.add(new Netflow(i));
			for (Edge e: edgeList) {
				if (e.getFirst() == e.getLast()) {
					System.out.println("Invalid flow polytope. Choose a higher n value. Disregard this result.");
					break;
				}
				if (e.getFirst() == i && e.getLast() != sinkVertex) {
					left.add(e);
				}
				else if (e.getLast() == i) {
					right.add(e);
				}
			}
			
			if (left.size() >0) {
				nInequality temp = new nInequality(left, right);
				conditions.add(temp);
			}
		}
		
		
	}
	
	//can form it with a partition and value of n 
	public FlowPolytope(Partition p, int n) {
		Digraph G = new Digraph(p, n);
		conditions = new ArrayList<>();
		ArrayList<Edge> edgeList = (ArrayList<Edge>) G.getEdges();
		int sinkVertex = edgeList.get(edgeList.size() - 1).getLast();
		System.out.println(edgeList);
		for (int i = 1; i<sinkVertex; i++) {
			List<Object> left = new ArrayList<>();
			List<Object> right = new ArrayList<>();
			right.add(new Netflow(i));
			for (Edge e: edgeList) {
				if (e.getFirst() == e.getLast()) {
					System.out.println("Invalid flow polytope. Choose a higher n value. Disregard this result.");
					break;
				}
				if (e.getFirst() == i && e.getLast() != sinkVertex) {
					left.add(e);
				}
				else if (e.getLast() == i) {
					right.add(e);
				}
			}
			
			if (left.size() >0) {
				nInequality temp = new nInequality(left, right);
				conditions.add(temp);
			}
		}
	}
	
	
	
	public void addInequality(nInequality a) {
		conditions.add(a);
	}
	
	//how to change an inequality of a flow polytope at a given index 
	public void changeInequality(nInequality a, int index) {
		conditions.remove(index);
		conditions.add(index, a);
	}
	
	public void printPolytope() {
		for(nInequality i: conditions) {
			System.out.println(i);
		}
	}

	public List<nInequality> getInequalities(){
		return conditions;
	}
	
}



class Edge{
	
	//what vertex the edge starts from and where it ends
	int start;
	int end;
	
	//how to make an edge 
	public Edge(int a, int b) {
		start = a;
		end = b;
	}
	public void printEdge() {
		System.out.println(this.start + ", " + this.end);
	}
	
	public int getFirst() {
		return start;
	}
	
	public int getLast() {
		return end;
	}
	
	//how to see if two edges are the same (they both start and end at the same place) 
	public boolean same(Edge e) {
		if (this.start == e.getFirst() && this.end == e.getLast()) {
			return true;
		}
		
		return false;
	}
	//how to print an edge 
	public String toString() {
		return "x_{" + start + "" + end + "}";
	}
	
}

class Netflow{
	//a netflow is just a single vertex (that's all you need) 
	int vertex;
	
	public String toString() {
		return ("a_" + (Integer)vertex).toString();
	}
	public Netflow(int a) {
		vertex = a;
	}
	 public void printNetflow() {
		 System.out.println("a_" + vertex);
	 }
	
}

class Digraph{
	//digraph is just a list of edges 
	List<Edge> edgeList;
	
	public Digraph(List<Edge> a) {
		edgeList = new ArrayList(a);
	}
	
	public Digraph(Edge...edges) {
		edgeList = new ArrayList<>();
		for (Edge e: edges) {
			edgeList.add(e);
		}
	}
	
	//how to form a digraph given a partition and value of n
	//it's the same logic that you defined in your paper 
	public Digraph(Partition part, int n) {
		edgeList = new ArrayList<>();
		ArrayList<Integer> partition = new ArrayList(part.getPartition());
		for (int i = 0; i<partition.size(); i++) {
			for (int j = n - partition.get(i) + 1; j<= n+1; j++) {
				edgeList.add(new Edge(i+1, j));
			}
		}
		edgeList.add(new Edge(partition.size() + 1, n+1));
	}
	
	public String toString() {
		String s = "[";
		for (Edge e: edgeList) {
			s = s + e.toString() + ", ";
		}
		
		s = s.substring(0, s.length() -2) + "]";
		return s;
	}
	
	public List<Edge> getEdges(){
		return edgeList;
	}
}

class Partition{
	//a partition is just a list of numbers 
	ArrayList<Integer> part;
	public Partition(int...a) {
		part = new ArrayList<>();
		for (int i: a) {
			part.add(i);
		}
	}
	
	public ArrayList<Integer> getPartition(){
		return part;
	}
}


