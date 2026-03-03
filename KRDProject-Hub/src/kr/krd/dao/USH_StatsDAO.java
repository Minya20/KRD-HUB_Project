package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

import kr.util.DBUtil;
import kr.krd.vo.USH_YearCountVO;
import kr.krd.vo.USH_AgencyCountVO;

public class USH_StatsDAO {
	
	//연도별 선정 건수
	public List<USH_YearCountVO> findSelectedCountByYear() {
		List<USH_YearCountVO> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT EXTRACT(YEAR FROM selection_approved_at) AS yr, COUNT(selection_id) AS cnt FROM selection "
					+ "WHERE selection_result_cd = 'SELECTED' AND selection_approved_at IS NOT NULL "
					+ "GROUP BY EXTRACT(YEAR FROM selection_approved_at) ORDER BY yr";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				int year = rs.getInt("yr");
				int cnt = rs.getInt("cnt");
				list.add(new USH_YearCountVO(year,cnt));
			}
			return list;
			
		}catch (Exception e) {
			e.printStackTrace();
			return list;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	
	//기관별 선정 건수
	public List<USH_AgencyCountVO> findSelectedCountByAgency() {
		List<USH_AgencyCountVO> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT ag.agency_agy_name AS agency_name, COUNT(*) AS cnt FROM selection s "
					+ "JOIN applications a ON a.application_id = s.selection_application_id "
					+ "JOIN announcement an ON an.announcement_ann_id = a.application_ann_id "
					+ "JOIN agency ag ON ag.agency_agy_id = an.announcement_agy_id WHERE s.selection_result_cd = 'SELECTED' "
					+ "AND s.selection_approved_at IS NOT NULL GROUP BY ag.agency_agy_name ORDER BY cnt DESC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				String name = rs.getString("agency_name");
				int cnt = rs.getInt("cnt");
				list.add(new USH_AgencyCountVO(name,cnt));
			}
			return list;
		}catch (Exception e) {
			e.printStackTrace();
			return list;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
				
	}
	
	//평균 경쟁률
	public double findAvgCompetitionRate() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT AVG(app_cnt / NULLIF(recruit_cap,0)) AS avg_rate "
					+ "FROM (SELECT an.announcement_ann_id AS ann_id, an.announcement_recruit_cap AS recruit_cap, "
					+ "COUNT(a.application_id) AS app_cnt "
					+ "FROM announcement an LEFT JOIN applications a ON a.application_ann_id = an.announcement_ann_id "
					+ "WHERE an.announcement_recruit_cap IS NOT NULL "
					+ "AND an.announcement_recruit_cap > 0 "
					+ "GROUP BY an.announcement_ann_id, an.announcement_recruit_cap)";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(!rs.next()) return 0.0;
			return rs.getDouble("avg_rate");
			
		}catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
}
