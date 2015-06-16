package pds.situationAnalysis;

/**
 * A struct-like container for the *Status output stores
 * @author Chris Guinnup
 */
public class StatusStores {

	public ImmediateStore immedStore;
	public AggregateStore aggStore;
	
	public StatusStores() {
		immedStore = new ImmediateStore();
		aggStore = new AggregateStore();
	}
}
