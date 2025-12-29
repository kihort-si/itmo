package ru.itmo.is.transaction;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.function.Supplier;

@Dependent
public class DatabaseParticipant implements TwoPhaseCommitParticipant {

    @Inject
    private EntityManager entityManager;

    private Supplier<Void> databaseOperation;
    private boolean prepared = false;

    public void setOperation(Supplier<Void> operation) {
        this.databaseOperation = operation;
    }

    @Override
    public boolean prepare() throws Exception {
        try {
            prepared = true;
            return true;
        } catch (Exception e) {
            prepared = false;
            throw e;
        }
    }

    @Override
    public void commit() throws Exception {
        if (!prepared) {
            throw new IllegalStateException("Cannot commit: participant not prepared");
        }
        if (databaseOperation != null) {
            databaseOperation.get();
        }
    }

    @Override
    public void rollback() throws Exception {
        prepared = false;
    }
}

