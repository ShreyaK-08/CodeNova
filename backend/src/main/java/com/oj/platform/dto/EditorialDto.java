package com.oj.platform.dto;

public class EditorialDto {
    private boolean unlocked;
    private boolean hasEditorial;
    private int unlockAfterAttempts;
    private long currentFailedAttempts;
    private String title;
    private String approach;
    private String algorithm;
    private String complexity;
    private String solution;
    private String message;

    public EditorialDto() {
    }

    public static EditorialDto locked(boolean hasEditorial, int unlockAfterAttempts, long currentFailedAttempts) {
        EditorialDto dto = new EditorialDto();
        dto.setUnlocked(false);
        dto.setHasEditorial(hasEditorial);
        dto.setUnlockAfterAttempts(unlockAfterAttempts);
        dto.setCurrentFailedAttempts(currentFailedAttempts);
        dto.setMessage(String.format("Editorial is locked. Attempt the problem %d times to unlock. Current failed attempts: %d",
                unlockAfterAttempts, currentFailedAttempts));
        return dto;
    }

    public static EditorialDto unlocked(String title, String approach, String algorithm, String complexity, String solution,
                                        int unlockAfterAttempts, long currentFailedAttempts) {
        EditorialDto dto = new EditorialDto();
        dto.setUnlocked(true);
        dto.setHasEditorial(true);
        dto.setTitle(title);
        dto.setApproach(approach);
        dto.setAlgorithm(algorithm);
        dto.setComplexity(complexity);
        dto.setSolution(solution);
        dto.setUnlockAfterAttempts(unlockAfterAttempts);
        dto.setCurrentFailedAttempts(currentFailedAttempts);
        dto.setMessage("Editorial unlocked.");
        return dto;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isHasEditorial() {
        return hasEditorial;
    }

    public void setHasEditorial(boolean hasEditorial) {
        this.hasEditorial = hasEditorial;
    }

    public int getUnlockAfterAttempts() {
        return unlockAfterAttempts;
    }

    public void setUnlockAfterAttempts(int unlockAfterAttempts) {
        this.unlockAfterAttempts = unlockAfterAttempts;
    }

    public long getCurrentFailedAttempts() {
        return currentFailedAttempts;
    }

    public void setCurrentFailedAttempts(long currentFailedAttempts) {
        this.currentFailedAttempts = currentFailedAttempts;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getApproach() {
        return approach;
    }

    public void setApproach(String approach) {
        this.approach = approach;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getComplexity() {
        return complexity;
    }

    public void setComplexity(String complexity) {
        this.complexity = complexity;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
