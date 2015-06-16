/**
The thread controller, consisting of the main program loop which launches image
processing tasks. It uses CountDownLatches to coordinate threads, and keeps the most recent two
frames so that it can easily pass them to frame-comparative jobs.
*/
package pds;
//for JavaDocs