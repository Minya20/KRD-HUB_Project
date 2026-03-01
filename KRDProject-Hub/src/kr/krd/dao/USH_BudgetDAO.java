package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.vo.USH_BudgetHistRow;
import kr.util.DBUtil;

public class USH_BudgetDAO {
	
	//예산 변경 이력 조회 (프로젝트ID/기간/조회건수 옵션)
	public List<USH_BudgetHistRow> findBudgetHist(Long projectIdOrNull, String startDtOrNull, String endDtOrNull, int limit) {
		List<USH_BudgetHistRow> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = new StringBuilder();
			sql.append("SELECT * FROM (");
			sql.append("SELECT ");
			sql.append("budget_hist_id, ");
			sql.append("budget_hist_project_id, ");
			sql.append("budget_hist_before_amt, ");
			sql.append("budget_hist_after_amt, ");
			sql.append("budget_hist_changed_at, ");
			sql.append("budget_hist_changed_by, ");
			sql.append("budget_hist_reason ");
			sql.append("FROM budget_hist ");
			sql.append("WHERE 1=1 ");
			
			List<Object> params = new ArrayList<>();
			
			if(projectIdOrNull != null) {
				sql.append("AND budget_hist_project_id = ? ");
				params.add(projectIdOrNull);
			}
			if(startDtOrNull != null && !startDtOrNull.isBlank()) {
				sql.append("AND TRUNC(budget_hist_changed_at) >= TO_DATE(?, 'YYYY-MM-DD') ");
				params.add(startDtOrNull);
			}
			if(endDtOrNull != null && !endDtOrNull.isBlank()) {
				sql.append("AND TRUNC(budget_hist_changed_at) <= TO_DATE(?, 'YYYY-MM-DD') ");
				params.add(endDtOrNull);
			}
			
			sql.append("ORDER BY budget_hist_changed_at DESC, budget_hist_id DESC ");
			sql.append(") WHERE ROWNUM <= ? ");
			
			params.add(limit);
			
			pstmt = conn.prepareStatement(sql.toString());
			
			for(int i=0;i<params.size();i++) {
				pstmt.setObject(i + 1, params.get(i));
			}
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				list.add(new USH_BudgetHistRow(
						rs.getLong("budget_hist_id"),
						rs.getLong("budget_hist_project_id"),
						rs.getBigDecimal("budget_hist_before_amt"),
						rs.getBigDecimal("budget_hist_after_amt"),
						rs.getTimestamp("budget_hist_changed_at"),
						rs.getString("budget_hist_changed_by"),
						rs.getString("budget_hist_reason")
						));
			}
			
			return list;
			
		}catch (Exception e) {
			e.printStackTrace();
			return list;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

}
