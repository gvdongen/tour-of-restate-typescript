package dev.restate.tour.auxiliary;

public class EmailClient {
    public static EmailClient get() {
        return new EmailClient();
    }

    public boolean notifyUserOfPaymentSuccess(String userId) {
        System.out.println("Notifying user " + userId + " of payment success");
        // send the email
        return true;
    }

    public boolean notifyUserOfPaymentFailure(String userId) {
        System.out.println("Notifying user " + userId + " of payment failure");
        // send the email
        return true;
    }
}