
import java.util.*;

public class Algorithm {

	public static void main(String[] args) {
	    ArrayList<Object> term1 = new ArrayList<Object>();
		term1.add(new Edge(1,2));
		term1.add(new Edge(1,3));
		term1.add(new Edge(1,4));
		term1.add(new Edge(1,5));
		
		ArrayList<Object> term2 = new ArrayList<Object>();
		term2.add(new Netflow(1));
		nInequality ineq1 = new nInequality(term1, term2);
		
		ArrayList<Object> term3 = new ArrayList<>();
		term3.add(new Edge(2,3));
		term3.add(new Edge(2,4));
		
		ArrayList<Object> term4 = new ArrayList<>();
		term4.add(new Netflow(2));
		term4.add(new Edge(1,2));
		
		ArrayList<Object> term5 = new ArrayList<Object>();
		term5.add(new Edge(3,4));

		ArrayList<Object> term6 = new ArrayList<Object>();
		term6.add(new Netflow(3));
		term6.add(new Edge(1,3));
		term6.add(new Edge(2,3));
		
	
		
		nInequality ineq2 = new nInequality(term3, term4);
		
		nInequality ineq3 = new nInequality(term5, term6);
		
		FlowPolytope f = new FlowPolytope(ineq1, ineq2, ineq3);
		System.out.println(f);
		String s = getVolume(f);
		System.out.println(s.substring(3, s.lastIndexOf('+')));
		
		
	}
	
	static String getVolume(FlowPolytope f) {
		String volume = "";
		ArrayList<nInequality> polytopeDefinition = (ArrayList<nInequality>) f.getInequalities();
		
		int totalInequalities = polytopeDefinition.size();
		for (int i = totalInequalities - 1; i>-1; i--) {
			nInequality currentInequality = f.getInequalities().get(i);
			ArrayList<nInequality> cases = getCases(currentInequality);
		
			for (nInequality ineq: cases) {
				System.out.println(ineq + "hi");
				ArrayList<nInequality> inequalitiesLeft = new ArrayList<>();
				for (int j = 0; j<i; j++) {
					inequalitiesLeft.add(polytopeDefinition.get(j));
				}
				
				FlowPolytope newF = new FlowPolytope(inequalitiesLeft);
				System.out.println(newF);
				if (ineq.getTerms().size() == 2) {
					int chainLength = ineq.getTerms().get(0).size();
					volume = volume + " + \\frac{1}{" +factorial(chainLength) + "} a_" + (i+1) + "^{" + chainLength +  "} [";
				}
				
				
				//chains of length more than 2 that start with sum of edges <= netflow 
				else if (ineq.getEdgeInfo()[0] == true) {
					int currentIndex = 0;
					
					while (currentIndex < ineq.getTerms().size() - 1) {
						System.out.println(currentIndex);
						int chainLength = ineq.getTerms().get(currentIndex).size();
						volume = volume + " + \\frac{1}{" +factorial(chainLength) + "} a_" + (i+1) + "^{" + chainLength +  "} [";
						int chainIndex = currentIndex;
						//find next term of all edges that's greater than this netflow
						int nextTerm = 0;
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
								currentIndex = x;
								break;
							}
						}
						Edge toBeInflated = null;
						for (Object obj: ineq.getTerms().get(indexOfNextNetflow)) {
							if (!(ineq.getTerms().get(chainIndex + 1).contains(obj))) {
								toBeInflated = (Edge) obj;
							}
						}
						
						int inequalityRow = toBeInflated.getFirst();
						//subtract by 1 because the edges start at edge 1
						nInequality toBeChanged = newF.getInequalities().get(inequalityRow - 1);
						List<Object> edges = new ArrayList(toBeChanged.getTerms().get(0));
						for (int x = 0; x<extraTerms; x++) {
							edges.add(toBeInflated);
						}

						
						nInequality newInequality = new nInequality(edges, toBeChanged.getTerms().get(1));
						newF.changeInequality(newInequality, inequalityRow - 1);
					}
				}
				
				//netflow <= chain
				else{
					int currentIndex = 0;
					
					
					while (currentIndex < ineq.getTerms().size() - 1) {
						
						System.out.println(currentIndex);

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
						nInequality toBeChanged = newF.getInequalities().get(inequalityRow - 1);
						List<Object> edges = new ArrayList(toBeChanged.getTerms().get(0));
						for (int x = 0; x<expansion; x++) {
							edges.add(toBeInflated);
						}

						
						nInequality newInequality = new nInequality(edges, toBeChanged.getTerms().get(1));
						newF.changeInequality(newInequality, inequalityRow - 1);
					}
				}
				
				
								
				volume =  volume + getVolume(newF) + "]";
			}
			
			
		}
		
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
		terms = a;
		
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
			terms.add(term);
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

