package ru.itmo.is.transaction;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@ApplicationScoped
public class TwoPhaseCommitCoordinator {

    private static final Logger logger = Logger.getLogger(TwoPhaseCommitCoordinator.class.getName());

    public void execute(List<TwoPhaseCommitParticipant> participants) throws Exception {
        if (participants == null || participants.isEmpty()) {
            return;
        }

        List<TwoPhaseCommitParticipant> preparedParticipants = new ArrayList<>();
        boolean allPrepared = true;

        logger.info("Starting two-phase commit: Prepare phase");
        for (TwoPhaseCommitParticipant participant : participants) {
            try {
                if (participant.prepare()) {
                    preparedParticipants.add(participant);
                    logger.info("Participant prepared successfully: " + participant.getClass().getSimpleName());
                } else {
                    allPrepared = false;
                    logger.warning("Participant preparation failed: " + participant.getClass().getSimpleName());
                    break;
                }
            } catch (Exception e) {
                allPrepared = false;
                logger.severe("Error during prepare phase for " + participant.getClass().getSimpleName() + ": " + e.getMessage());
                rollbackParticipants(preparedParticipants);
                throw new Exception("Prepare phase failed", e);
            }
        }

        if (!allPrepared) {
            logger.warning("Not all participants prepared successfully. Rolling back.");
            rollbackParticipants(preparedParticipants);
            throw new Exception("Two-phase commit failed: not all participants prepared");
        }

        logger.info("All participants prepared. Starting commit phase");
        List<TwoPhaseCommitParticipant> committedParticipants = new ArrayList<>();
        try {
            for (TwoPhaseCommitParticipant participant : preparedParticipants) {
                try {
                    participant.commit();
                    committedParticipants.add(participant);
                    logger.info("Participant committed successfully: " + participant.getClass().getSimpleName());
                } catch (Exception e) {
                    logger.severe("Error during commit phase for " + participant.getClass().getSimpleName() + ": " + e.getMessage());
                    rollbackParticipants(committedParticipants);
                    throw new Exception("Commit phase failed", e);
                }
            }
            logger.info("Two-phase commit completed successfully");
            cleanupParticipants(committedParticipants);
        } catch (Exception e) {
            logger.severe("Error during commit phase: " + e.getMessage());
            rollbackParticipants(committedParticipants);
            throw e;
        }
    }

    private void cleanupParticipants(List<TwoPhaseCommitParticipant> participants) {
        for (TwoPhaseCommitParticipant participant : participants) {
            if (participant instanceof MinioParticipant) {
                ((MinioParticipant) participant).cleanup();
            }
        }
    }

    private void rollbackParticipants(List<TwoPhaseCommitParticipant> participants) {
        for (TwoPhaseCommitParticipant participant : participants) {
            try {
                participant.rollback();
                logger.info("Participant rolled back: " + participant.getClass().getSimpleName());
            } catch (Exception e) {
                logger.severe("Error during rollback for " + participant.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
    }
}

