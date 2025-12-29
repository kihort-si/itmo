package ru.itmo.is.transaction;

public interface TwoPhaseCommitParticipant {
    boolean prepare() throws Exception;
    void commit() throws Exception;
    void rollback() throws Exception;
}

