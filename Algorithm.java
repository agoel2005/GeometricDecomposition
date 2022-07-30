
import java.util.*;

public class Algorithm {

	public static void main(String[] args) {
	    ArrayList<Object> maybe = new ArrayList<Object>();
		maybe.add(new Edge(1,2));
		maybe.add(new Edge(1,3));
		//maybe.add(new Edge(1,4));
		
		ArrayList<Object> maybe2 = new ArrayList<Object>();
		maybe2.add(new Netflow(1));
		maybe2.add(new Edge(0,1));
		maybe2.add(new Edge(-1,1));
		nInequality whatIf = new nInequality(maybe, maybe2);
		
		System.out.println(getCases(whatIf));
		System.out.println(getCases(whatIf).size() + " cases");

		
		ArrayList<ArrayList<Integer>> volume = new ArrayList<>();
		
		//go through the inequalities from bottom to top 
//		for (int i = f.getInequalities().size() - 1; i>=0; i--) {
//			
//			Inequality original = f.getInequalities().get(i);
//			
//			int leftLength = original.less().size();
//			
//			//subtract by 1 because the first element is the netflow
//			int rightLength = original.greater().size() - 1;
//			
//		
//		}

	}
	
	
	static ArrayList<nInequality> getCases(nInequality i) {
		ArrayList<nInequality> cases = new ArrayList<>();
		List<List<Object> >terms = i.getTerms();
		int numTerms = terms.size();
		
		if (i.getEdgeInfo().get(0) == true) {
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
							newCase.add(left);
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
	List<Boolean> isEdges;
	
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
		isEdges = new ArrayList<>();
		for (int i = 0; i<terms.size(); i++) {
			for(Object obj: terms.get(i)) {
				if (!(obj instanceof Edge)) {
					isEdges.add(false);
					break;
				}
			}
			isEdges.add(true);
		}
	}
	
	public nInequality(List<Object>...a ) {
		terms = new ArrayList<>();
		for (List<Object> term: a) {
			terms.add(term);
		}
		
		isEdges = new ArrayList<>();
		for (int i = 0; i<terms.size(); i++) {
			for(Object obj: terms.get(i)) {
				if (!(obj instanceof Edge)) {
					isEdges.add(false);
					break;
				}
			}
			isEdges.add(true);
		}
	}
	
	public List<List<Object>> getTerms(){
		return terms;
	}
	
	public List<Boolean> getEdgeInfo(){
		return isEdges;
	}
	
	
	
}



class Inequality{
	List<Object> left;
	List<Object> right;
	public Inequality(List<Object> a, List<Object> b) {
		left = a;
		right = b;
	}
	public void printInequality() {
		for (Object element : left) {
		}
		System.out.println(left + " <= " + right);
	}
	
	public List<Object> less(){
		return left;
	}
	
	public List<Object> greater(){
		return right;
	}
	
	
}

	
class FlowPolytope{
	List<Inequality> conditions;
	
	public FlowPolytope() {
		conditions = new ArrayList<Inequality>();
	}
	
	public FlowPolytope(List<Inequality> a) {
		conditions = a;
	}
	
	public void addInequality(Inequality a) {
		conditions.add(a);
	}
	public void printPolytope() {
		for(Inequality i: conditions) {
			i.printInequality();
		}
	}

	public List<Inequality> getInequalities(){
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

