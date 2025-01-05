package IES.LearnBool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import org.javatuples.Pair;

import net.automatalib.words.Alphabet;
import net.automatalib.words.Word;

public class Helpers {
	static int seed = 42;
	static Random random = new Random(seed);
	
	/**
	 * generate random sequences from a given alphabet of symbols
	 * @param <T> Symbol type
	 * @param alphabet Alphabet of symbols
	 * @param length Length of the to be generated sequence
	 * @return A sequence of length l from symbols of alphabet
	 */
	public static <T> Word<T> generateRandomSequence(Alphabet<T> alphabet, int length) { 	
    	Vector<T> vec = new Vector<T>();
        for (int i = 0; i < length; i++) {
            vec.addElement(alphabet.getSymbol(random.nextInt(alphabet.size())));
        }
        Word<T> sequence = Word.fromList(vec);
        return sequence;
	}
	
	/**
	 * Encoding of an input sequence to a boolean assignment
	 * @param <T> Symbol type
	 * @param in Input sequence of symbols
	 * @param alphabet Alphabet of symbols
	 * @return Boolean vector encoding the input sequence
	 */
	public static <T> HashMap<Pair<T,Integer>,Integer> convertToBoolVec(Word<T> in, Alphabet<T> alphabet){
		HashMap<Pair<T,Integer>,Integer> vec = new HashMap<Pair<T,Integer>,Integer>();
		int n = in.length();
		for(int i = 0; i < n; i++) {
			//one-hot encoding: set variable for input value and position to true
			vec.put(new Pair<T,Integer>(in.getSymbol(i),i), 1);
			//one-hot encoding: set all other variables for the same position to false
			for(T elem: alphabet) {
				if(!vec.containsKey(new Pair<T,Integer>(in.getSymbol(i),i))) {
					vec.put(new Pair<T,Integer>(elem,i), 0);
				}
			}
		}
		return vec;
	}
	
	/**
	 * Generate all possible sequences from a set of symbols per timestep, i.e., position
	 * @param <T> Type of symbols
	 * @param inputPerTimestep Set of possible Inputs per time step, i.e., position in the sequence
	 * @param timestep Current time step, i.e., position in the sequence
	 * @param currentWord Sequence of the past time steps
	 * @param result Resulting set of sequences
	 */
	public static <T> void getAllCombinationsRecursive(ArrayList<ArrayList<T>> inputPerTimestep, int timestep, Word<T> currentWord, ArrayList<Word<T>> result) {
		if(timestep == inputPerTimestep.size()) {
			result.add(currentWord);
			return;
		}
		
		for(int i = 0; i < inputPerTimestep.get(timestep).size(); i++) {
			getAllCombinationsRecursive(inputPerTimestep,timestep+1,currentWord.append(inputPerTimestep.get(timestep).get(i)),result);
		}
	}
	
	/**
	 * Get all inputs per position that are possible (1 or don't care)
	 * All combinations of all inputs at each position gives the set of sequences
	 * @param <T> Type of symbols
	 * @param vec Boolean vector of assignment
	 * @param alphabet Alphabet of symbols
	 * @param n Length of bounded history
	 * @return Set of input sequences corresponding to the Boolean assignment vector
	 */
	public static <T> ArrayList<Word<T>> convertToInputSequences(HashMap<Pair<T,Integer>,Integer> vec, Alphabet<T> alphabet, int n){
		ArrayList<Word<T>> I_s = new ArrayList<Word<T>>();
		ArrayList<ArrayList<T>> inputPerTimestep = new ArrayList<ArrayList<T>>();
		
		for(int i = 0; i < n; i++) {
			ArrayList<T> inputList = new ArrayList<T>();
			for(T input: alphabet) {
				Pair<T,Integer> pair = new Pair<T,Integer>(input,i);
				if(!vec.containsKey(pair) || vec.get(pair) == 1 || vec.get(pair) == -1) {
					inputList.add(input);
				}
			}
			inputPerTimestep.add(inputList);
		}
		
		Word<T> currentWord = Word.epsilon();
		getAllCombinationsRecursive(inputPerTimestep,0,currentWord,I_s);
		return I_s;
	}
}
