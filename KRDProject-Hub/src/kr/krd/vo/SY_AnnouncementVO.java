package kr.krd.vo;

public class SY_AnnouncementVO {
	private long annId;
	private int agyId;
	private String title;
	private int reannYn;
	private String pmContact;
	private int recruitCap;
	private String startDt;
	private String endDt;
	private String status;
	private String field;
	private String createdBy;
	private long totalBudget;
	private int hiddenYn;
	private String annDesc;
	public long getAnnId() {
		return annId;
	}
	public void setAnnId(long annId) {
		this.annId = annId;
	}
	public int getAgyId() {
		return agyId;
	}
	public void setAgyId(int agyId) {
		this.agyId = agyId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getReannYn() {
		return reannYn;
	}
	public void setReannYn(int reannYn) {
		this.reannYn = reannYn;
	}
	public String getPmContact() {
		return pmContact;
	}
	public void setPmContact(String pmContact) {
		this.pmContact = pmContact;
	}
	public int getRecruitCap() {
		return recruitCap;
	}
	public void setRecruitCap(int recruitCap) {
		this.recruitCap = recruitCap;
	}
	public String getStartDt() {
		return startDt;
	}
	public void setStartDt(String startDt) {
		this.startDt = startDt;
	}
	public String getEndDt() {
		return endDt;
	}
	public void setEndDt(String endDt) {
		this.endDt = endDt;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public long getTotalBudget() {
		return totalBudget;
	}
	public void setTotalBudget(long totalBudget) {
		this.totalBudget = totalBudget;
	}
	public int getHiddenYn() {
		return hiddenYn;
	}
	public void setHiddenYn(int hiddenYn) {
		this.hiddenYn = hiddenYn;
	}
	public String getAnnDesc() {
		return annDesc;
	}
	public void setAnnDesc(String annDesc) {
		this.annDesc = annDesc;
	}
}
