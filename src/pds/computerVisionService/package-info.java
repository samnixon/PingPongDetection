/**
Attempts to detect objects such as the table, ping pong ball, and ping pong rackets. Currently
we use the OpenCV library to accomplish this. Each computer vision algorithm can also run in
its own thread for performance optimization. Thus inputs and outputs are passed as construction
parameters, and each class is Runnable.
*/
package pds.computerVisionService;
//for JavaDocs