package kr.krd.vo;

public class RR_AnnouncementVO {
    private int annId;               // ANNOUNCEMENT_ANN_ID
    private int agyId;               // ANNOUNCEMENT_AGY_ID
    private String title;            // ANNOUNCEMENT_TITLE
    private String desc;             // ANNOUNCEMENT_DESC
    private int reannYn;             // ANNOUNCEMENT_REANN_YN
    private String pmContact;        // ANNOUNCEMENT_PM_CONTACT
    private int recruitCap;          // ANNOUNCEMENT_RECRUIT_CAP
    private String startDt;          // ANNOUNCEMENT_START_DT (VARCHAR2)
    private String endDt;            // ANNOUNCEMENT_END_DT (VARCHAR2)
    private String status;           // ANNOUNCEMENT_STATUS
    private String field;            // ANNOUNCEMENT_FIELD
    private String createdBy;        // ANNOUNCEMENT_CREATED_BY
    private long totalBudget;        // ANNOUNCEMENT_TOTAL_BUDGET
    private int hiddenYn;            // ANNOUNCEMENT_HIDDEN_YN
    private int applicantCount;      // 조회용

    public int getAnnId() { return annId; }
    public void setAnnId(int annId) { this.annId = annId; }

    public int getAgyId() { return agyId; }
    public void setAgyId(int agyId) { this.agyId = agyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public int getReannYn() { return reannYn; }
    public void setReannYn(int reannYn) { this.reannYn = reannYn; }

    public String getPmContact() { return pmContact; }
    public void setPmContact(String pmContact) { this.pmContact = pmContact; }

    public int getRecruitCap() { return recruitCap; }
    public void setRecruitCap(int recruitCap) { this.recruitCap = recruitCap; }

    public String getStartDt() { return startDt; }
    public void setStartDt(String startDt) { this.startDt = startDt; }

    public String getEndDt() { return endDt; }
    public void setEndDt(String endDt) { this.endDt = endDt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public long getTotalBudget() { return totalBudget; }
    public void setTotalBudget(long totalBudget) { this.totalBudget = totalBudget; }

    public int getHiddenYn() { return hiddenYn; }
    public void setHiddenYn(int hiddenYn) { this.hiddenYn = hiddenYn; }

    public int getApplicantCount() { return applicantCount; }
    public void setApplicantCount(int applicantCount) { this.applicantCount = applicantCount; }
}