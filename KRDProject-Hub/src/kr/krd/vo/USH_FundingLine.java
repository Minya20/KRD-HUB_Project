package kr.krd.vo;

import java.sql.Date;


public class USH_FundingLine {
	public final int payRound;
	public final long amount;
	public final String stautsCd;
	public final Date requestedDt;
	public final int approvedYn;
	public final String approvedBy;
	public final Date approvedDt;
	
	public USH_FundingLine(int payRound, long amount, String statusCd, Date requestedDt, int approvedYn, String approvedBy,
						   Date approvedDt) {
		this.payRound = payRound;
		this.amount = amount;
		this.stautsCd = statusCd;
		this.requestedDt = requestedDt;
		this.approvedYn = approvedYn;
		this.approvedBy = approvedBy;
		this.approvedDt = approvedDt;
	}
}
