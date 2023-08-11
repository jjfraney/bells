package org.flyboy.bells.belfry;

/**
 * @author John J. Franey
 */
public class MpdCommandException extends BelfryException {
    final MpdResponse.Ack ack;

    MpdCommandException(MpdResponse.Ack ack) {
        this.ack = ack;
    }

    @Override
    public String getMessage() {
        return "Mpd command error: code = " + ack.getError() + ", text = " + ack.getMessageText();
    }

    public MpdResponse.Ack getAck() {
        return ack;
    }
}
