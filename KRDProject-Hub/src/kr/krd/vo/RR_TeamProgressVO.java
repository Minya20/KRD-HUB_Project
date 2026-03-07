package kr.krd.vo;

public class RR_TeamProgressVO {

    // 목록/상세 공통
    private int projectId;
    private int applicationId;

    private String userId;
    private String userName;

    private String projectStatusCd;      // ONGOING/STOPPED/COMPLETED...
    private String agreementStatusCd;    // SIGNED/PENDING

    private int paidRound;               // 0~3 (FUNDING 승인된 최대 회차)
    private long paidTotalAmt;           // 총 지급액

    private String midStatusCd;          // SUBMITTED/APPROVED/REJECTED/null
    private String finalStatusCd;

    // 상세에서만
    private Double selectionScore;       // SELECTION_FINAL_SCORE
    private String selectionApprovedAt;  // YYYY-MM-DD
    private String agreementSignedAt;    // YYYY-MM-DD

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getProjectStatusCd() { return projectStatusCd; }
    public void setProjectStatusCd(String projectStatusCd) { this.projectStatusCd = projectStatusCd; }

    public String getAgreementStatusCd() { return agreementStatusCd; }
    public void setAgreementStatusCd(String agreementStatusCd) { this.agreementStatusCd = agreementStatusCd; }

    public int getPaidRound() { return paidRound; }
    public void setPaidRound(int paidRound) { this.paidRound = paidRound; }

    public long getPaidTotalAmt() { return paidTotalAmt; }
    public void setPaidTotalAmt(long paidTotalAmt) { this.paidTotalAmt = paidTotalAmt; }

    public String getMidStatusCd() { return midStatusCd; }
    public void setMidStatusCd(String midStatusCd) { this.midStatusCd = midStatusCd; }

    public String getFinalStatusCd() { return finalStatusCd; }
    public void setFinalStatusCd(String finalStatusCd) { this.finalStatusCd = finalStatusCd; }

    public Double getSelectionScore() { return selectionScore; }
    public void setSelectionScore(Double selectionScore) { this.selectionScore = selectionScore; }

    public String getSelectionApprovedAt() { return selectionApprovedAt; }
    public void setSelectionApprovedAt(String selectionApprovedAt) { this.selectionApprovedAt = selectionApprovedAt; }

    public String getAgreementSignedAt() { return agreementSignedAt; }
    public void setAgreementSignedAt(String agreementSignedAt) { this.agreementSignedAt = agreementSignedAt; }
}