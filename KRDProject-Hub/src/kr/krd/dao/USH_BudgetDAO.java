package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.vo.USH_BudgetHistRow;
import kr.krd.vo.USH_BudgetUsageSummary;
import kr.krd.vo.USH_FundingLine;
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

	//예산 사용 현황(목록)
	public List<USH_BudgetUsageSummary> findBudgetUsageAll() {
		List<USH_BudgetUsageSummary> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT p.project_id, an.announcement_title, NVL(an.announcement_total_budget, 0) AS total_budget, "
					+ "NVL(SUM(CASE WHEN NVL(f.funding_approved_yn, 0) = 1 THEN f.funding_amount_amt ELSE 0 END), 0) AS used_budget "
					+ "FROM projects p "
					+ "JOIN applications a ON a.application_id = p.project_application_id "
					+ "JOIN announcement an ON an.announcement_ann_id = a.application_ann_id "
					+ "LEFT JOIN funding f ON f.funding_project_id = p.project_id "
					+ "GROUP BY p.project_id, an.announcement_title, an.announcement_total_budget "
					+ "ORDER BY p.project_id";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				int projectId = rs.getInt("project_id");
				String title = rs.getString("announcement_title");
				long total = rs.getLong("total_budget");
				long used = rs.getLong("used_budget");
				long remain = total - used;
				double pct = (total == 0) ? 0.0 : Math.round((used * 10000.0 / total)) / 100.0;
				
				list.add(new USH_BudgetUsageSummary(projectId, title, total, used, remain, pct));
			}
			return list;
		}catch (Exception e) {
			e.printStackTrace();
			return list;
		} finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	
	//예산 사용 현황(프로젝트 1건 요약)
	public USH_BudgetUsageSummary findBudgetUsageByProjectId(int projectId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT p.project_id, an.announcement_title, NVL(an.announcement_total_budget, 0) AS total_budget, "
					+ "NVL(SUM(CASE WHEN NVL(f.funding_approved_yn, 0) = 1 THEN f.funding_amount_amt ELSE 0 END), 0) AS used_budget "
					+ "FROM projects p "
					+ "JOIN applications a ON a.application_id = p.project_application_id "
					+ "JOIN announcement an ON an.announcement_ann_id = a.application_ann_id "
					+ "LEFT JOIN funding f ON f.funding_project_id = p.project_id "
					+ "WHERE p.project_id = ? "
					+ "GROUP BY p.project_id, an.announcement_title, an.announcement_total_budget";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, projectId);
			rs = pstmt.executeQuery();
			
			if(!rs.next()) return null;
			
			String title = rs.getString("announcement_title");
			long total = rs.getLong("total_budget");
			long used = rs.getLong("used_budget");
			long remain = total = used;
			double pct = (total == 0) ? 0.0 : Math.round((used * 10000.0 / total)) / 100.0;
			
			return new USH_BudgetUsageSummary(projectId, title, total, used, remain, pct);
			
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	
	//프로젝트별 집행(지급) 내역
	public List<USH_FundingLine> findFundingLinesByProjectId(int projectId) {
		List<USH_FundingLine> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT funding_pay_round, funding_amount_amt, funding_status_cd, funding_requested_at, funding_approved_yn, "
					+ "funding_approved_by, funding_approved_at "
					+ "FROM funding WHERE funding_project_id = ? ORDER BY funding_pay_round";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, projectId);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				list.add(new USH_FundingLine(
						rs.getInt("funding_pay_round"),
						rs.getLong("funding_amount_amt"),
						rs.getString("funding_status_cd"),
						rs.getDate("funding_requested_at"),
						rs.getInt("funding_approved_yn"),
						rs.getString("funding_approved_by"),
						rs.getDate("funding_approved_at")
						));
			}
			return list;
		}catch(Exception e) {
			e.printStackTrace();
			return list;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
}
