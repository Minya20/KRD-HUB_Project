package kr.krd.vo;

public class USH_AnnSummaryVO {
	public final int annId;
	public final String title;
	public final String statusCd;
	public final String endDt;
	public final String startDt;
	public final String agencyName;
	
	public USH_AnnSummaryVO(int annId, String title, String statusCd, String endDt, String startDt, String agencyName) {
		this.annId = annId;
		this.title = title;
		this.statusCd = statusCd;
		this.endDt = endDt;
		this.startDt = startDt;
		this.agencyName = agencyName;
	}
}
