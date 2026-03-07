package kr.krd.vo;

public class RR_SelectionVO {
    private int applicationId;
    private String userId;
    private String userName;

    private double avgScore;      // SUBMITTED 점수 평균
    private int submittedCnt;     // SUBMITTED 개수(평가완료 개수)
    private long budgetAmt;       // 희망 연구비(APPLICATION_BUDGET_AMT)

    // 결과 조회용
    private String resultCd;      // SELECTED / REJECTED
    private String approvedAt;    // YYYY-MM-DD

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }

    public int getSubmittedCnt() { return submittedCnt; }
    public void setSubmittedCnt(int submittedCnt) { this.submittedCnt = submittedCnt; }

    public long getBudgetAmt() { return budgetAmt; }
    public void setBudgetAmt(long budgetAmt) { this.budgetAmt = budgetAmt; }

    public String getResultCd() { return resultCd; }
    public void setResultCd(String resultCd) { this.resultCd = resultCd; }

    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }
}