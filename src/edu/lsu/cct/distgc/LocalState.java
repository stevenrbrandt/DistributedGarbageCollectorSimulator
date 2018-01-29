package edu.lsu.cct.distgc;

/**
 * This class keeps track of state
 * that is only needed while a message
 * is being processed.
 * @author sbrandt
 */
public class LocalState {

    boolean same = false;
    boolean parent_was_set = false;
    int sent_to_parent = -1;
    int original_parent = -1;
    int old_weight;
    boolean returned_to_sender = false;
    boolean returned_to_parent = false;
    boolean in_collection_message = false;
}
