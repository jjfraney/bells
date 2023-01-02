package org.flyboy.bells.tower;

/**
 * @author John J. Franey
 */
public class MpdCommandException extends BelltowerException {
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
