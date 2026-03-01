package kr.krd.vo;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class USH_BudgetHistRow {
	public final long histId;
	public final long projectId;
	public final BigDecimal beforeAmt;
	public final BigDecimal afterAmt;
	public final Timestamp changedAt;
	public final String changedBy;
	public final String reason;
	
	public USH_BudgetHistRow(long histId, long projectId, BigDecimal beforeAmt, BigDecimal afterAmt, Timestamp changedAt,
							 String changedBy, String reason) {
		this.histId = histId;
		this.projectId = projectId;
		this.beforeAmt = beforeAmt;
		this.afterAmt = afterAmt;
		this.changedAt = changedAt;
		this.changedBy = changedBy;
		this.reason = reason;
	}
}
