/**
Receives program-terminating input and triggers a graceful exit. If the
user enters input that is not the quit command, input will inform the user of the proper quit command. 
This thread will run throughout the application's execution. Component 
main will check the termination status periodically.
*/
package pds.input;
//for JavaDocs