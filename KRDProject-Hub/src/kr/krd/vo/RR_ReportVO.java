package kr.krd.vo;

public class RR_ReportVO {
    private int reportRptId;        // REPORT_RPT_ID
    private int reportProjectId;    // REPORT_PROJECT_ID
    private String reportTypeCd;    // REPORT_RPT_TYPE_CD

    private String submittedAt;     // YYYY-MM-DD
    private String statusCd;        // REPORT_STATUS_CD

    private String rejectReason;    // REPORT_REJECT_REASON
    private String content;         // REPORT_CONTENT
    private String keywords;        // REPORT_KEYWORDS
    private Integer progressRate;   // REPORT_PROGRESS_RATE (NULL 가능)

    private String approvedBy;      // REPORT_APPROVED_BY
    private String approvedAt;      // YYYY-MM-DD

    public int getReportRptId() { return reportRptId; }
    public void setReportRptId(int reportRptId) { this.reportRptId = reportRptId; }

    public int getReportProjectId() { return reportProjectId; }
    public void setReportProjectId(int reportProjectId) { this.reportProjectId = reportProjectId; }

    public String getReportTypeCd() { return reportTypeCd; }
    public void setReportTypeCd(String reportTypeCd) { this.reportTypeCd = reportTypeCd; }

    public String getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }

    public String getStatusCd() { return statusCd; }
    public void setStatusCd(String statusCd) { this.statusCd = statusCd; }

    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public Integer getProgressRate() { return progressRate; }
    public void setProgressRate(Integer progressRate) { this.progressRate = progressRate; }

    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }

    public String getApprovedAt() { return approvedAt; }
    public void setApprovedAt(String approvedAt) { this.approvedAt = approvedAt; }
}