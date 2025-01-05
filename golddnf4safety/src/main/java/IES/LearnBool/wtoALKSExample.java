package IES.LearnBool;

import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

public class wtoALKSExample {


	/**
	 * Instantiates a simple Automated Lane Keeping System (ALKS)
	 * @return mealy machine model
	 */
	public static CompactMealy<Character, Character> constructALKS(){
		final Character OUT_OK = '1';
	    final Character OUT_ALARM = '2';
	    
	    final Character IN_LEFT = 'l';
	    final Character IN_RIGHT = 'r';
	    final Character IN_STRAIGHT = 's';
	    
	    Character[] in = {IN_LEFT, IN_RIGHT, IN_STRAIGHT};
	    
	    Alphabet<Character> inputs = Alphabets.fromArray(in);
	
	    final CompactMealy<Character, Character> machine = new CompactMealy<>(inputs);
	    
		return AutomatonBuilders.forMealy(machine)
	            .withInitial("C")
	            .from("C")
	                .on(IN_LEFT).withOutput(OUT_OK).to("L")
	                .on(IN_RIGHT).withOutput(OUT_OK).to("R")
	                .on(IN_STRAIGHT).withOutput(OUT_OK).to("C")
                .from("L")
	                .on(IN_LEFT).withOutput(OUT_ALARM).to("A")
	                .on(IN_RIGHT).withOutput(OUT_OK).to("C")
	                .on(IN_STRAIGHT).withOutput(OUT_OK).to("L")
                .from("R")
	                .on(IN_LEFT).withOutput(OUT_OK).to("C")
	                .on(IN_RIGHT).withOutput(OUT_ALARM).to("A")
	                .on(IN_STRAIGHT).withOutput(OUT_OK).to("R")
                .from("A")
	                .on(IN_LEFT).withOutput(OUT_ALARM).to("A")
	                .on(IN_RIGHT).withOutput(OUT_ALARM).to("A")
	                .on(IN_STRAIGHT).withOutput(OUT_ALARM).to("A")
	            .create();
	}
}
