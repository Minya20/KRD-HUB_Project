package kr.krd.vo;

public class RR_TaskProgressVO {
    private int annId;
    private String title;
    private long totalBudget;
    private int teamCount;
    private String taskStatus; // 진행중/중단/완료/대기

    public int getAnnId() { return annId; }
    public void setAnnId(int annId) { this.annId = annId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public long getTotalBudget() { return totalBudget; }
    public void setTotalBudget(long totalBudget) { this.totalBudget = totalBudget; }

    public int getTeamCount() { return teamCount; }
    public void setTeamCount(int teamCount) { this.teamCount = teamCount; }

    public String getTaskStatus() { return taskStatus; }
    public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
}