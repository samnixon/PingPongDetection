/**
Analyzes the situation from object data provided by cvStores and decides what the ping pong
table's current status is. This module looks not only at the immediate results, but also at an
aggregation of the data. This way a singular erroneous result should be smoothed out by taking
the rest of the data into account. Upon detecting a true change in status, it noties the output 
module.
*/
package pds.situationAnalysis;
//for JavaDocs