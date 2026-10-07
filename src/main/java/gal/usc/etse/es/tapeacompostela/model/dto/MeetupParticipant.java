package gal.usc.etse.es.tapeacompostela.model.dto;

import gal.usc.etse.es.tapeacompostela.model.entity.MeetupParticipant.Status;

import java.time.LocalDateTime;

public record MeetupParticipant(
        UserSummary user,
        Status status,
        LocalDateTime invitedAt,
        LocalDateTime respondedAt
) {
    public static MeetupParticipant from(gal.usc.etse.es.tapeacompostela.model.entity.MeetupParticipant participant) {
        return new MeetupParticipant(
                UserSummary.from(participant.getUser()),
                participant.getStatus(),
                participant.getInvitedAt(),
                participant.getRespondedAt()
        );
    }
}
