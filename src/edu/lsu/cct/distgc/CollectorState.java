package edu.lsu.cct.distgc;

/**
 * The "Collector State" is defined by the last message sent to all outgoing
 * links. Healthy means no message has been sent.
 */
public enum CollectorState {
    healthy_state, phantom_state, recover_state, build_state, infected_state, dead_state;
}
