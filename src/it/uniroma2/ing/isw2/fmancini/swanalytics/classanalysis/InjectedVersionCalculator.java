package it.uniroma2.ing.isw2.fmancini.swanalytics.classanalysis;

import java.util.Queue;
import org.apache.commons.collections4.queue.CircularFifoQueue;

/**
 * Estimate the injected version of a bug using the Proportion Moving Window method
 * @author fmancini
 *
 */
public class InjectedVersionCalculator {
	private Integer windowSize;
	private Queue<Integer> proportions;
	
	public InjectedVersionCalculator(Integer numBugs) {
		this.windowSize = (int) (numBugs * 0.01);
		this.proportions = new CircularFifoQueue<>(this.windowSize);
	}	
	
	public void updateProportionValue(Integer injectedVersion, Integer openingVersion, Integer fixedVersion) {
		if (fixedVersion.equals(openingVersion)) {
			this.proportions.add(0);
		} else {
			this.proportions.add((fixedVersion - injectedVersion) / (fixedVersion - openingVersion));
		}
	}
	
	public Integer computeAffectedVersion(Integer openingVersion, Integer fixedVersion) {
		Integer pSum = 0;
		for (Integer proportion: this.proportions) {
			pSum += proportion;
		}
		Integer proportion = (this.proportions.isEmpty()) ? 0 : pSum/this.proportions.size();
		Integer injectedVersion = fixedVersion - (fixedVersion - openingVersion) * proportion;
		return (injectedVersion <= openingVersion) ? injectedVersion : openingVersion;
		
	}
	
}
