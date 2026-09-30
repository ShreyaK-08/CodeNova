package com.oj.platform.dto;

public class UserSupportSummaryDto {

    private long totalTickets;
    private long openTickets;
    private long inProgressTickets;
    private long waitingForUserTickets;
    private long resolvedTickets;
    private long closedTickets;

    public UserSupportSummaryDto() {}

    public UserSupportSummaryDto(long totalTickets, long openTickets, long inProgressTickets, long waitingForUserTickets, long resolvedTickets, long closedTickets) {
        this.totalTickets = totalTickets;
        this.openTickets = openTickets;
        this.inProgressTickets = inProgressTickets;
        this.waitingForUserTickets = waitingForUserTickets;
        this.resolvedTickets = resolvedTickets;
        this.closedTickets = closedTickets;
    }

    public long getTotalTickets() { return totalTickets; }
    public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }

    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }

    public long getInProgressTickets() { return inProgressTickets; }
    public void setInProgressTickets(long inProgressTickets) { this.inProgressTickets = inProgressTickets; }

    public long getWaitingForUserTickets() { return waitingForUserTickets; }
    public void setWaitingForUserTickets(long waitingForUserTickets) { this.waitingForUserTickets = waitingForUserTickets; }

    public long getResolvedTickets() { return resolvedTickets; }
    public void setResolvedTickets(long resolvedTickets) { this.resolvedTickets = resolvedTickets; }

    public long getClosedTickets() { return closedTickets; }
    public void setClosedTickets(long closedTickets) { this.closedTickets = closedTickets; }
}
