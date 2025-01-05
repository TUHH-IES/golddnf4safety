package IES.LearnBool;

import java.io.IOException;

import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFA;
import de.learnlib.algorithms.lstar.dfa.ClassicLStarDFABuilder;
import de.learnlib.api.oracle.MembershipOracle.DFAMembershipOracle;
import de.learnlib.datastructure.observationtable.OTUtils;
import de.learnlib.datastructure.observationtable.writer.ObservationTableASCIIWriter;
import de.learnlib.filter.statistic.oracle.DFACounterOracle;
import de.learnlib.oracle.equivalence.DFAWMethodEQOracle;
import de.learnlib.oracle.membership.SimulatorOracle.DFASimulatorOracle;
import de.learnlib.util.Experiment.DFAExperiment;
import de.learnlib.util.statistics.SimpleProfiler;
import net.automatalib.automata.fsa.DFA;
import net.automatalib.automata.fsa.impl.compact.CompactDFA;
import net.automatalib.automata.transducers.MealyMachine;
import net.automatalib.automata.transducers.impl.compact.CompactMealy;
import net.automatalib.util.automata.builders.AutomatonBuilders;
import net.automatalib.visualization.Visualization;
import net.automatalib.words.Alphabet;
import net.automatalib.words.impl.Alphabets;

/*
 * A first example that instantiates and visualizes the coffee machine example
 */
public final class CoffeeExample {


	/**
	 * Instantiates a copy of the Coffee machine example from Merten et.al. with a character alphabet
	 * @return mealy machine model
	 */
	public static CompactMealy<Character, Character> constructCoffeeCopy(){
		final Character OUT_OK = '1';
	    final Character OUT_ERROR = '2';
	    final Character OUT_COFFEE = '3';
	    
	    final Character IN_WATER = 'w';
	    final Character IN_POD = 'p';
	    final Character IN_BUTTON = 'b';
	    final Character IN_CLEAN = 'c';
	    
	    Character[] in = {IN_WATER, IN_POD, IN_BUTTON, IN_CLEAN};
	    
	    Alphabet<Character> inputs = Alphabets.fromArray(in);
	
	    final CompactMealy<Character, Character> machine = new CompactMealy<>(inputs);
	    
		return AutomatonBuilders.forMealy(machine)
	            .withInitial("a")
	            .from("a")
	                .on(IN_WATER).withOutput(OUT_OK).to("c")
	                .on(IN_POD).withOutput(OUT_OK).to("b")
	                .on(IN_BUTTON).withOutput(OUT_ERROR).to("f")
	                .on(IN_CLEAN).withOutput(OUT_OK).loop()
	            .from("b")
	                .on(IN_WATER).withOutput(OUT_OK).to("d")
	                .on(IN_POD).withOutput(OUT_OK).loop()
	                .on(IN_BUTTON).withOutput(OUT_ERROR).to("f")
	                .on(IN_CLEAN).withOutput(OUT_OK).to("a")
	            .from("c")
	                .on(IN_WATER).withOutput(OUT_OK).loop()
	                .on(IN_POD).withOutput(OUT_OK).to("d")
	                .on(IN_BUTTON).withOutput(OUT_ERROR).to("f")
	                .on(IN_CLEAN).withOutput(OUT_OK).to("a")
	            .from("d")
	                .on(IN_WATER, IN_POD).withOutput(OUT_OK).loop()
	                .on(IN_BUTTON).withOutput(OUT_COFFEE).to("e")
	                .on(IN_CLEAN).withOutput(OUT_OK).to("a")
	            .from("e")
	                .on(IN_WATER, IN_POD, IN_BUTTON).withOutput(OUT_ERROR).to("f")
	                .on(IN_CLEAN).withOutput(OUT_OK).to("a")
	            .from("f")
	                .on(IN_WATER, IN_POD, IN_BUTTON, IN_CLEAN).withOutput(OUT_ERROR).loop()
	            .create();
	}
}
