package kr.krd.vo;

public class RR_ApplicationVO {
    private int applicationId;
    private int announcementAnnId;
    private String userId;
    private String userName;
    private String appliedAt;     // YYYY-MM-DD
    private String statusCd;
    private String attachPath;
    private long budgetAmt;

    private Double avgScore;      // 평가 평균점수 (없으면 null)

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public int getAnnouncementAnnId() { return announcementAnnId; }
    public void setAnnouncementAnnId(int announcementAnnId) { this.announcementAnnId = announcementAnnId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getAppliedAt() { return appliedAt; }
    public void setAppliedAt(String appliedAt) { this.appliedAt = appliedAt; }

    public String getStatusCd() { return statusCd; }
    public void setStatusCd(String statusCd) { this.statusCd = statusCd; }

    public String getAttachPath() { return attachPath; }
    public void setAttachPath(String attachPath) { this.attachPath = attachPath; }

    public long getBudgetAmt() { return budgetAmt; }
    public void setBudgetAmt(long budgetAmt) { this.budgetAmt = budgetAmt; }

    public Double getAvgScore() { return avgScore; }
    public void setAvgScore(Double avgScore) { this.avgScore = avgScore; }
}