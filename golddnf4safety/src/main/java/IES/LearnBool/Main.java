package IES.LearnBool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.javatuples.Pair;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;
import net.automatalib.words.impl.Alphabets;

public class Main {
    private Main() {
    }
    
    //variables are encoded as follows:
    	// 1: true
    	// 0: false
    	// -1: don't care

    public static void main(String[] args) throws IOException {

        //Load DFA and alphabet
    	String example = "alkswto";
    	int N = 10;
		int L = 1000;
		
    	Character[] in = null;
    	CompactMealy<Character, Character> target = null;
    	if(example.equals("coffee")) {
    		target = CoffeeExample.constructCoffeeCopy();
    		Character[] in_coffee = {'w','p','b','c'};
    		in = in_coffee;
    	}
    	else if(example.equals("alks")) {
	    	target = ALKSExample.constructALKS();
	    	Character[] in_alks = {'l','r','s'};
	    	in = in_alks;
    	}
    	else if(example.equals("alkswto")){
    		target = wtoALKSExample.constructALKS();
	    	Character[] in_alks = {'l','r','s'};
	    	in = in_alks;
    	}
		Alphabet<Character> alphabet = Alphabets.fromArray(in);
		
		ArrayList<Character> inputToUnsafe = new ArrayList<Character>();
		if(example.equals("coffee")) {
			inputToUnsafe.add('b');
		}
		else {
			inputToUnsafe.add('l');
			inputToUnsafe.add('l');
		}
		ArrayList<Integer> unsafeStates = new ArrayList<Integer>();
		unsafeStates.add(target.getState(inputToUnsafe));
		
		ArrayList<HashMap<Pair<Character,Integer>,Integer>> g = inferBooleanFormula( target,unsafeStates,N,L);
		//Possible extension: add outer loop to adapt number of examples (L)
		
		System.out.println(g);
		System.out.println(g.size());
		double M = Math.pow(in.length, N);
		double x = 0;
		for(HashMap<Pair<Character,Integer>,Integer> clause: g) {
			x += Math.pow(M, N-clause.size());
		}
		System.out.println(x);
		double safety = x / M;
		System.out.println("Safety of Gold's evaluation: " + safety);
		double stochasticSafety = evaluateStochasticSafety(target, unsafeStates, alphabet,N,L);
		System.out.println("Stochastic safety from " + L + " samples: " + stochasticSafety);

        //Visualization.visualize(target, alphabet);
    }
    
    
    /**
     * Main function for DNF formula inference. The algorithm first collects a set of positive examples, then generalizes to a DNF based on these examples by querying a oracle on further derived assignments.
     * Notes for extension: for a generic implementation this could be a function that evaluates whether the behavior after applying an input is safe currently this is the case if an unsafe state is reached.
     * @param automaton System for which to learn a DNF of its safety property
     * @param unsafeStates Set of unsafe states
     * @param n Length of bounded history considered
     * @param L Number of initial examples to sample
     * @return DNF formula of the system's safety property
     */
    static ArrayList<HashMap<Pair<Character,Integer>,Integer>> inferBooleanFormula(CompactMealy<Character, Character> automaton, ArrayList<Integer> unsafeStates, int n, int L){
    	ArrayList<HashMap<Pair<Character,Integer>,Integer>> g = new ArrayList<HashMap<Pair<Character,Integer>,Integer>>();
    	int t = getNumberOfVariables(automaton, n);
    	System.out.println("The number of variables: " + t);
    	
    	for(int i = 0; i < L; i++) {
    		System.out.println("Example " + (i+1) + " of " + L);
    		HashMap<Pair<Character,Integer>,Integer> v = getExample(automaton, unsafeStates,n);
    		System.out.println("Example: " + v);
    		if(checkNotImplies(g,v)) {
    			for(int j = 0; j < t; j++) {
    				for(Pair<Character,Integer> key : v.keySet()) {
    					if(v.get(key) == 1 || v.get(key) == 0) {
    						HashMap<Pair<Character,Integer>,Integer> v_tilde = new HashMap<Pair<Character,Integer>,Integer>(v);
    						v_tilde.put(key, -1); //don't care
    						if(oracle(automaton, v_tilde, unsafeStates,n)) {
    							v = v_tilde;
    						}
    					}
    				}
    			}
				g.add(v);
    		}
    	}
    	
    	return g;
    	
    }
    
    /**
     * Calculate number of variables in the one-hot encoding scheme
     * @param automaton System for which to calculate the number of variables
     * @param n Length of bounded history
     * @return Number of variables in one-hot encoding
     */
    static Integer getNumberOfVariables(CompactMealy<Character, Character> automaton, int n) {
    	return n * automaton.getInputAlphabet().size();
    }
    
    /**
     * Recursively builds all assignments by replacing don't-care assignment by a pair of assignments with the don't-care variable set to 0 and 1, respectively
     * @param results List of all fully defined assignments
     * @param currentAssignment Assignment defined up to position
     * @param position Position where to continue checking for don't-care variables
     */
    static void recursiveBuildAssigments(ArrayList<HashMap<Pair<Character,Integer>,Integer>> results, HashMap<Pair<Character,Integer>,Integer> currentAssignment, Integer position) {
    	if(position == currentAssignment.size()) {
    		results.add(currentAssignment);
    		return;
    	}
    	else {
    		ArrayList<Pair<Character,Integer>> keys = new ArrayList<Pair<Character,Integer>>(currentAssignment.keySet());
    		Integer currentVal = currentAssignment.get(keys.get(position));
    		if(currentVal == 0 || currentVal == 1) {
    			recursiveBuildAssigments(results,currentAssignment,position+1);
    		}
    		else {
    			HashMap<Pair<Character,Integer>,Integer> newAssignment1 = new HashMap<Pair<Character,Integer>,Integer>(currentAssignment);
    			newAssignment1.put(keys.get(position), 1);
    			recursiveBuildAssigments(results,newAssignment1,position+1);
    			HashMap<Pair<Character,Integer>,Integer> newAssignment0 = new HashMap<Pair<Character,Integer>,Integer>(currentAssignment);
    			newAssignment0.put(keys.get(position), 0);
    			recursiveBuildAssigments(results,newAssignment0,position+1);
    		}
    	}
    }
    
    static ArrayList<HashMap<Pair<Character,Integer>,Integer>> getAllDeterminedAssignments(HashMap<Pair<Character,Integer>,Integer> v){
    	ArrayList<HashMap<Pair<Character,Integer>,Integer>> results = new ArrayList<HashMap<Pair<Character,Integer>,Integer>>();
    	recursiveBuildAssigments(results, v, 0);
		return results;
    }
    
    /**
     * Evaluates a DNF expression for a given boolean assignment vector
     * @param g DNF expression
     * @param v boolean assignment
     * @return true, if the assignment is a satisfying assignment, false otherwise
     */
    static Boolean evaluateDNF(ArrayList<HashMap<Pair<Character,Integer>,Integer>> g, HashMap<Pair<Character,Integer>,Integer> v) {
    	for(HashMap<Pair<Character,Integer>, Integer> monomial : g) {
    		Boolean contains = true;
    		for(HashMap.Entry<Pair<Character,Integer>, Integer> entry : monomial.entrySet()) {
    			if(!v.containsKey(entry.getKey()) || ((entry.getValue() == 0 && v.get(entry.getKey()) == 1))) {
    				contains = false;
    			}
    			if(!(v.containsKey(entry.getKey())) || (entry.getValue() == 1 && v.get(entry.getKey()) == 0)) {
    				contains = false;
    			}
    		}
    		if(contains) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Checks whether a boolean assignment is already implied by the boolean expression
     * This function includes a negation by definition -> the result is true if the monomial is NOT implied
     * @param g Boolean expression
     * @param v Vector of boolean assignment
     * @return true, if any of the assignments is implied, false otherwise
     */
    static Boolean checkNotImplies(ArrayList<HashMap<Pair<Character,Integer>,Integer>> g, HashMap<Pair<Character,Integer>,Integer> v) {
    	ArrayList<HashMap<Pair<Character,Integer>,Integer>> assignments = getAllDeterminedAssignments(v);
    	
    	for(HashMap<Pair<Character,Integer>,Integer> assignment : assignments) {
    		Boolean result = evaluateDNF(g,assignment);
    		if(result == false) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Generates positive examples, i.e., sequences that are safe on the system.
     * @param automaton System to generate examples from
     * @param unsafeStates Set of unsafe states that should not be reached with the example
     * @param n Length of examples, i.e., length of bounded history
     * @return boolean assignment vector
     */
    static HashMap<Pair<Character,Integer>,Integer> getExample(CompactMealy<Character, Character> automaton, ArrayList<Integer> unsafeStates, int n){
    	while(true) {
    		Word<Character> in = Helpers.generateRandomSequence(automaton.getInputAlphabet(), n);
    		if(!unsafeStates.contains(automaton.getState(in))) {
    			HashMap<Pair<Character,Integer>,Integer> vec = Helpers.convertToBoolVec(in,automaton.getInputAlphabet());
    			return vec;
    		}
    	}
    }
    
    /**
     * For a given vector of boolean assignment, whether this corresponds to any unsafe sequence in the system
     * @param automaton System on which to evaluate the safety
     * @param vec Vector of boolean assignment
     * @param unsafeStates Set of unsafe states that should not be reached by unsafe inputs
     * @param n Length of the bounded history on the system
     * @return false, if any of the sequences is unsafe, true otherwise
     */
    static Boolean oracle(CompactMealy<Character, Character> automaton, HashMap<Pair<Character,Integer>,Integer> vec, ArrayList<Integer> unsafeStates, int n) {
    	ArrayList<Word<Character>> I_s = Helpers.convertToInputSequences(vec,automaton.getInputAlphabet(),n);
    	for(Word<Character> in : I_s) {
    		if(!unsafeStates.contains(automaton.getState(in))) {
    			return false;
    		}
    	}
    	return true;
    }
    
    static double evaluateStochasticSafety(CompactMealy<Character, Character> automaton, ArrayList<Integer> unsafeStates, Alphabet<Character> alphabet, int n, int L) {
    	int safeCount = 0;
    	
    	for(int i = 0; i < L; i++) {
    		Word<Character> word = Helpers.generateRandomSequence(alphabet, n);
    		if(!unsafeStates.contains(automaton.getState(word))) {
    			safeCount++;
    		}
    	}
    	
    	return (double) safeCount / L;
    }

}
