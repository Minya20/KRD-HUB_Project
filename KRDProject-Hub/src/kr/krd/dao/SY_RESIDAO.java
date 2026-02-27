package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import kr.krd.vo.SY_AnnouncementVO;
import kr.util.DBUtil;

public class SY_RESIDAO {
	

    //공고 목록(데이터만 리턴)
	public ArrayList<SY_AnnouncementVO> selectAnnList() {
		ArrayList<SY_AnnouncementVO> list = new ArrayList<>();
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT announcement_ann_id, announcement_title, announcement_total_budget, announcement_start_dt, "
					+ "announcement_end_dt FROM announcement WHERE announcement_hidden_yn = 0 ORDER BY announcement_ann_id DESC";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				SY_AnnouncementVO vo = new SY_AnnouncementVO();
				vo.setAnnId(rs.getInt("announcement_ann_id"));
				vo.setTitle(rs.getString("announcement_title"));
				vo.setTotalBudget(rs.getLong("announcement_total_budget"));
				vo.setStartDt(rs.getString("announcement_start_dt"));
				vo.setEndDt(rs.getString("announcement_end_dt"));
				list.add(vo);
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
		return list;
	}
    
	//공고 상세(데이터만 리턴)
	public SY_AnnouncementVO selectAnnDetail(int annId) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs =null;
		String sql = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM announcement WHERE announcement_ann_id = ? AND announcement_hidden_yn = 0";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, annId);
			rs = pstmt.executeQuery();
			
			if(!rs.next()) return null;
			
			SY_AnnouncementVO vo = new SY_AnnouncementVO();
			vo.setAnnId(rs.getInt("announcement_ann_id"));
			vo.setAgyId(rs.getInt("announcement_agy_id"));
			vo.setTitle(rs.getString("announcement_title"));
			vo.setReannYn(rs.getInt("announcement_reann_yn"));
			vo.setPmContact(rs.getString("announcement_pm_contact"));
			vo.setRecruitCap(rs.getInt("announcement_recruit_cap"));
			vo.setStartDt(rs.getString("announcement_start_dt"));
			vo.setEndDt(rs.getString("announcement_end_dt"));
			vo.setStatus(rs.getString("announcement_status"));
			vo.setField(rs.getString("announcement_field"));
			vo.setCreatedBy(rs.getString("announcement_created_by"));
			vo.setTotalBudget(rs.getLong("announcement_total_budget"));
			vo.setHiddenYn(rs.getInt("announcement_hidden_yn"));
			vo.setAnnDesc(rs.getString("announcement_desc"));
			
			return vo;
			
			
		}catch (Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
		
		return null; //컴파일/실행 안정
	}
	
}