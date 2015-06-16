/**
 * Hooks into the provided video decoding library and provides video frame data to the rest of the 
 * program. The functionality hides behind the interface VideoSvcIF. To decode video, the program 
 *currently uses software called VLC Player through the access library VLCJ.
 */
package pds.videoService;
// for JavaDocs