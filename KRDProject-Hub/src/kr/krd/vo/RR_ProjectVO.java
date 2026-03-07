package kr.krd.vo;

public class RR_ProjectVO {
    private int projectId;
    private int applicationId;

    private String announcementTitle;
    private String projectStatusCd;     // IN_PROGRESS / STOPPED / COMPLETED ...
    private String agreementStatusCd;  // SIGNED / PENDING (없으면 PENDING 취급)

    private String userId;
    private String userName;

    public int getProjectId() { return projectId; }
    public void setProjectId(int projectId) { this.projectId = projectId; }

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getAnnouncementTitle() { return announcementTitle; }
    public void setAnnouncementTitle(String announcementTitle) { this.announcementTitle = announcementTitle; }

    public String getProjectStatusCd() { return projectStatusCd; }
    public void setProjectStatusCd(String projectStatusCd) { this.projectStatusCd = projectStatusCd; }

    public String getAgreementStatusCd() { return agreementStatusCd; }
    public void setAgreementStatusCd(String agreementStatusCd) { this.agreementStatusCd = agreementStatusCd; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}