package com.oj.platform.dto;

import java.util.HashMap;
import java.util.Map;

public class SupportAnalyticsDto {

    private long totalTickets;
    private long openTickets;
    private long assignedTickets;
    private long inProgressTickets;
    private long waitingForUserTickets;
    private long resolvedTickets;
    private long closedTickets;

    private long highPriorityCount;
    private long criticalPriorityCount;
    private long mediumPriorityCount;
    private long lowPriorityCount;

    private double averageResolutionTimeHours;
    private double averageSatisfactionRating; // e.g. 4.8 / 5
    private long totalRatedTickets;

    private Map<String, Long> ticketsByCategory = new HashMap<>();
    private Map<String, Long> ticketsByPriority = new HashMap<>();
    private Map<String, Long> ticketsByStatus = new HashMap<>();
    private Map<Integer, Long> ratingDistribution = new HashMap<>(); // 1-star -> count, 2-star -> count, etc.

    public SupportAnalyticsDto() {}

    public long getTotalTickets() { return totalTickets; }
    public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }

    public long getOpenTickets() { return openTickets; }
    public void setOpenTickets(long openTickets) { this.openTickets = openTickets; }

    public long getAssignedTickets() { return assignedTickets; }
    public void setAssignedTickets(long assignedTickets) { this.assignedTickets = assignedTickets; }

    public long getInProgressTickets() { return inProgressTickets; }
    public void setInProgressTickets(long inProgressTickets) { this.inProgressTickets = inProgressTickets; }

    public long getWaitingForUserTickets() { return waitingForUserTickets; }
    public void setWaitingForUserTickets(long waitingForUserTickets) { this.waitingForUserTickets = waitingForUserTickets; }

    public long getResolvedTickets() { return resolvedTickets; }
    public void setResolvedTickets(long resolvedTickets) { this.resolvedTickets = resolvedTickets; }

    public long getClosedTickets() { return closedTickets; }
    public void setClosedTickets(long closedTickets) { this.closedTickets = closedTickets; }

    public long getHighPriorityCount() { return highPriorityCount; }
    public void setHighPriorityCount(long highPriorityCount) { this.highPriorityCount = highPriorityCount; }

    public long getCriticalPriorityCount() { return criticalPriorityCount; }
    public void setCriticalPriorityCount(long criticalPriorityCount) { this.criticalPriorityCount = criticalPriorityCount; }

    public long getMediumPriorityCount() { return mediumPriorityCount; }
    public void setMediumPriorityCount(long mediumPriorityCount) { this.mediumPriorityCount = mediumPriorityCount; }

    public long getLowPriorityCount() { return lowPriorityCount; }
    public void setLowPriorityCount(long lowPriorityCount) { this.lowPriorityCount = lowPriorityCount; }

    public double getAverageResolutionTimeHours() { return averageResolutionTimeHours; }
    public void setAverageResolutionTimeHours(double averageResolutionTimeHours) { this.averageResolutionTimeHours = averageResolutionTimeHours; }

    public double getAverageSatisfactionRating() { return averageSatisfactionRating; }
    public void setAverageSatisfactionRating(double averageSatisfactionRating) { this.averageSatisfactionRating = averageSatisfactionRating; }

    public long getTotalRatedTickets() { return totalRatedTickets; }
    public void setTotalRatedTickets(long totalRatedTickets) { this.totalRatedTickets = totalRatedTickets; }

    public Map<String, Long> getTicketsByCategory() { return ticketsByCategory; }
    public void setTicketsByCategory(Map<String, Long> ticketsByCategory) { this.ticketsByCategory = ticketsByCategory; }

    public Map<String, Long> getTicketsByPriority() { return ticketsByPriority; }
    public void setTicketsByPriority(Map<String, Long> ticketsByPriority) { this.ticketsByPriority = ticketsByPriority; }

    public Map<String, Long> getTicketsByStatus() { return ticketsByStatus; }
    public void setTicketsByStatus(Map<String, Long> ticketsByStatus) { this.ticketsByStatus = ticketsByStatus; }

    public Map<Integer, Long> getRatingDistribution() { return ratingDistribution; }
    public void setRatingDistribution(Map<Integer, Long> ratingDistribution) { this.ratingDistribution = ratingDistribution; }
}
