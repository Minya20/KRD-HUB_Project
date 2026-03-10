package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.vo.USH_AnnSummaryVO;
import kr.krd.vo.USH_AnnStatusInfo;
import kr.util.DBUtil;

public class USH_AnnouncementDAO {
	
	//공고 목록 조회(요약)
	public List<USH_AnnSummaryVO> findAnnSummaryList() {
		List<USH_AnnSummaryVO> list = new ArrayList<>();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT an.announcement_ann_id AS ann_id, an.announcement_title AS title,"
					+ " an.announcement_status AS status_cd, an.announcement_start_dt AS start_dt,"
					+ " an.announcement_end_dt AS end_dt, ag.agency_agy_name AS agency_name "
					+ "FROM announcement an LEFT JOIN agency ag ON ag.agency_agy_id = an.announcement_agy_id "
					+ "WHERE announcement_hidden_yn <> '1' "
					+ "ORDER BY an.announcement_end_dt DESC NULLS LAST, an.announcement_ann_id";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				list.add(new USH_AnnSummaryVO(rs.getInt("ann_id"),
											  rs.getString("title"),
											  rs.getString("status_cd"),
											  rs.getString("end_dt"),
											  rs.getString("start_dt"),
											  rs.getString("agency_name")
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
	
	public USH_AnnStatusInfo findAnnStatusInfo(int annId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT announcement_status AS status_cd, announcement_end_dt AS end_dt "
					+ "FROM announcement WHERE announcement_ann_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, annId);
			rs = pstmt.executeQuery();
			
			if(!rs.next()) return null;
			
			String status = rs.getString("status_cd");
			java.sql.Date endDate = rs.getDate("end_dt");
			java.time.LocalDate endDt = (endDate == null) ? null : endDate.toLocalDate();
			
			return new USH_AnnStatusInfo(status == null ? null : status.trim().toUpperCase(),endDt);
		}catch(Exception e) {
			e.printStackTrace();
			return null;
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}
	
	public int updateAnnStatus(int annId, String newStatusCd) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "UPDATE announcement SET announcement_status = ? "
					+ "WHERE announcement_ann_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newStatusCd);
			pstmt.setInt(2, annId);
			return pstmt.executeUpdate();
		}catch(Exception e) {
			e.printStackTrace();
			return 0;
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}
}
