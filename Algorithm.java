
import java.util.*;

public class Algorithm {

	public static void main(String[] args) {
		
		//for the partition, just enter in the parts normally as shown below
		Partition p = new Partition(4,3,2,1);
		//over here, the two inputs are the partition and the value of n
		FlowPolytope f = new FlowPolytope(p, 8);
	
		System.out.println(f);
		String s = getVolume(f);
		String s2 = s.replace("( +", "(");
		String s3 = s2.replace("\\frac{1}{1}", "");
		String s4 = s3.replace("^{1}", "");
		System.out.println(s4.substring(3));
		
	}
	
	static String getVolume(FlowPolytope f) {
		String volume = "";
		ArrayList<nInequality> polytopeDefinition = new ArrayList((ArrayList<nInequality>) f.getInequalities());
		
		if(polytopeDefinition.size() == 0) {
			return "";
		}
		if (polytopeDefinition.size() == 1) {
			return " + \\frac{1}{" +factorial(polytopeDefinition.get(0).getTerms().get(0).size()) + "} a_1^{" + polytopeDefinition.get(0).getTerms().get(0).size() +  "}";
		}
		
		
		int totalInequalities = polytopeDefinition.size();
		//for (int i = totalInequalities - 1; i>-1; i--) {
			nInequality currentInequality = f.getInequalities().get(totalInequalities -1 );
			ArrayList<nInequality> cases = new ArrayList(getCases(currentInequality));
			
			for (nInequality ineq: cases) {
				ArrayList<nInequality> inequalitiesLeft = new ArrayList<>();
				for (int j = 0; j<totalInequalities - 1; j++) {
					inequalitiesLeft.add(polytopeDefinition.get(j));
				}
				
				List<nInequality> tempList = new ArrayList(f.getInequalities());
				
				FlowPolytope newF = new FlowPolytope(tempList);
				
				if (ineq.getTerms().size() == 2) {
					int chainLength = ineq.getTerms().get(0).size();
					volume = volume + " + \\frac{1}{" +factorial(chainLength) + "} a_" + (totalInequalities) + "^{" + chainLength +  "} \\left (";
					newF = new FlowPolytope(inequalitiesLeft);
					volume =  volume + getVolume(newF) + "\\right )";
				}
				
				
				//chains of length more than 2 that start with sum of edges <= netflow 
				else if (ineq.getEdgeInfo()[0] == true) {
					int currentIndex = 0;
					int chainLength = ineq.getTerms().get(0).size();
					volume = volume + " + \\frac{1}{" +factorial(chainLength) + "} a_" + (totalInequalities) + "^{" + chainLength +  "} \\left (";
					while (currentIndex < ineq.getTerms().size() - 1) {
						chainLength = ineq.getTerms().get(currentIndex).size();
						int chainIndex = currentIndex;
						int expansion = 0;
						Edge toBeInflated = new Edge(1,2);
						//find next term of all edges that's greater than this netflow
						if (currentIndex == ineq.getTerms().size() - 2) {
							expansion = 1;
							toBeInflated = (Edge) ineq.getTerms().get(ineq.getTerms().size() - 1).get(ineq.getTerms().get(ineq.getTerms().size() - 1).size() - 1);
							currentIndex ++;
						}
						else {
							int nextTerm = currentIndex + 2;
							for (int x = currentIndex + 2; x<ineq.getTerms().size(); x++) {
								if (ineq.getEdgeInfo()[x] == true) {
									nextTerm = x;
									break;
								}
							}
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
							expansion = ineq.getTerms().get(indexOfNextNetflow - 1).size() - ineq.getTerms().get(chainIndex).size();
							toBeInflated = (Edge) ineq.getTerms().get(indexOfNextNetflow).get(ineq.getTerms().get(indexOfNextNetflow).size() - 1);
						}
						
//						for (Object obj: ineq.getTerms().get(indexOfNextNetflow)) {
//							if (!(ineq.getTerms().get(chainIndex + 1).contains(obj))) {
//								System.out.println(obj);
//								toBeInflated = (Edge) obj;
//								break;
//							}
//						}
						
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
					
					volume =  volume + getVolume(newF) + "\\right )";
				}
				
				//netflow <= chain
				else{
					int currentIndex = 0;
					
					
					while (currentIndex < ineq.getTerms().size() - 1) {
						

						int currentLength = ineq.getTerms().get(currentIndex + 1).size();
						int prevNetflowIndex = currentIndex;

						//find next netflow term that's greater than this 
						int nextNetflow = 0;
						for (int x = currentLength + 1; x<ineq.getTerms().size(); x++) {
							if (ineq.getEdgeInfo()[x] == false) {
								nextNetflow = x;
								currentIndex = x;
								break;
							}
						}
						
						int finalLength = ineq.getTerms().get(nextNetflow - 1).size();
						int expansion = finalLength - currentLength + 1;
						
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
				
				

				//volume =  volume + getVolume(newF) + "]";
			}
			
			
		//}
		
		return volume;
	}
	static int factorial(int n) {
		int ans = 1;
		for (int i = 1; i<=n; i++) {
			ans = ans * i;
		}
		return ans;
	}
	
	static ArrayList<nInequality> getCases(nInequality i) {
		ArrayList<nInequality> cases = new ArrayList<>();
		List<List<Object> >terms = i.getTerms();
		int numTerms = terms.size();
		
		if (i.getEdgeInfo()[0] == true) {
			if (terms.size() == 2) {
				if (terms.get(1).size() == 1) {
					cases.add(i);
					return cases;
				}
				
				int num = terms.get(1).size();
				for (int j = 0; j<num; j++) {
					List<Object> left = new ArrayList<>();
					List<Object> middle =terms.get(0);
					List<Object> right = new ArrayList<>();
					
					for (int k = 0; k<j; k++) {
						left.add(terms.get(1).get(k));
					}
					for (int k = 0; k<j+1; k++) {
						right.add(terms.get(1).get(k));
					}
					if (left.size() > 0) {
						nInequality partialCase = new nInequality(left, middle, right);
						ArrayList<nInequality> finalCases = getCases(partialCase);
						for (nInequality ineq: finalCases) {
							cases.add(ineq);
						}
					}
					else {
						nInequality partialCase = new nInequality(middle, right);
						ArrayList<nInequality> finalCases = getCases(partialCase);
						
						for (nInequality ineq: finalCases) {
							cases.add(ineq);
						}
					}
					
				}
			}
			
			else {
				if (terms.get(1).size() == 1) {
					cases.add(i);
					return cases;
				}
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

					ArrayList<nInequality> finalCases = getCases(new nInequality(newCase));
					
					for (nInequality ineq: finalCases) {
						cases.add(ineq);
					}
					
				}
				
				
				
				
				
				
			}
			
		}
		
		else {
			int length = terms.get(1).size();
			if (length == 1) {
				cases.add(i);
				return cases;
			}
			
//			if (terms.get(0).size() ==1 && terms.get(0).get(0) instanceof Netflow) {
//				cases.add(i);
//				return cases;
//			}
			
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

class nInequality{
	List<List<Object>> terms;
	boolean[] isEdges;
	
	@Override
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
	
	public FlowPolytope(List<nInequality> a) {
		conditions = a;
	}
	
	public FlowPolytope(nInequality...a) {
		conditions = new ArrayList<>();
		for (nInequality ineq: a) {
			conditions.add(ineq);
		}
	}
	
	
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
	
	public boolean same(Edge e) {
		if (this.start == e.getFirst() && this.end == e.getLast()) {
			return true;
		}
		
		return false;
	}
	public String toString() {
		return "x_{" + start + "" + end + "}";
	}
	int start;
	int end;
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
}

class Netflow{
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


