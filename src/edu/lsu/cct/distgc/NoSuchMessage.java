package edu.lsu.cct.distgc;

public class NoSuchMessage extends RuntimeException {
    public NoSuchMessage() { super(); }
    public NoSuchMessage(String msg) { super(msg); }
}
