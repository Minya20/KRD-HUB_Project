package kr.krd.vo;

public class USH_BudgetUsageSummary {
	public final int projectId;
	public final String announcementTitle;
	public final long totalBudget;
	public final long usedBudget;
	public final long remainingBudget;
	public final double usedPct;
	
	public USH_BudgetUsageSummary(int projectId, String announcementTitle, long totalBudget, long usedBudget, long remainingBudget,
								  double usedPct) {
		this.projectId = projectId;
		this.announcementTitle = announcementTitle;
		this.totalBudget = totalBudget;
		this.usedBudget = usedBudget;
		this.remainingBudget = remainingBudget;
		this.usedPct = usedPct;
	}
}
