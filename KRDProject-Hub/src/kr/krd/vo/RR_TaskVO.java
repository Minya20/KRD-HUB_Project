package kr.krd.vo;

public class RR_TaskVO {
    private int annId;
    private String title;
    private String taskStatus;   // 진행중/중단/완료 등(표시용)
    private int teamCount;       // 선정된 팀(프로젝트) 수

    public int getAnnId() { return annId; }
    public void setAnnId(int annId) { this.annId = annId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }

    public int getTeamCount() { return teamCount; }
    public void setTeamCount(int teamCount) { this.teamCount = teamCount; }
}