package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.vo.RR_AnnouncementVO;
import kr.util.DBUtil;

public class SY_ApplicationDAO {

	//신청서 제출
	public int insertApplication(int annId, String userId, String attachPath, String statusCd, long budgetAmt) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		
		//applied_at, upadated_at은 기본값 sysdate 명시
		try {
			conn = DBUtil.getConnection();
			sql = 	"INSERT INTO applications (application_id, application_ann_id, application_user_id, application_applied_at, application_updated_at, "
					+ "application_attach_path, application_status_cd, application_budget_amt) VALUES (app_seq.nextval, ?, ?, SYSDATE, SYSDATE, ?, ?, ?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, annId);
			pstmt.setString(2, userId);
			pstmt.setString(3, attachPath);
			pstmt.setString(4, statusCd);
			pstmt.setLong(5, budgetAmt);
			
			return pstmt.executeUpdate(); //1이면 성공
		}catch (Exception e) {
			throw new RuntimeException("신청서 제출 실패", e);
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}
	
	// //기관 담당자/ 과제 조회 
	//공고 과제 목록 현황 조회
    public List<RR_AnnouncementVO> getProjectList() {
        List<RR_AnnouncementVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            conn = DBUtil.getConnection();

            sql = "SELECT a.announcement_ann_id, " +
            	      "       a.announcement_title, " +
            	      "       a.announcement_total_budget, " +
            	      "       NVL((SELECT COUNT(ap.application_id) " +
            	      "            FROM Application ap " +
            	      "            WHERE ap.announcement_ann_id = a.announcement_ann_id), 0) AS cnt " +
            	      "FROM Announcement a";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
            	while (rs.next()) {
            	    RR_AnnouncementVO vo = new RR_AnnouncementVO();

            	    vo.setAnnId(rs.getInt("announcement_ann_id"));
            	    vo.setTitle(rs.getString("announcement_title"));
            	    vo.setTotalBudget(rs.getLong("announcement_total_budget"));
            	    vo.setApplicantCount(rs.getInt("cnt"));

            	    list.add(vo);
            	}
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
        return list;
    }
	
	//공고 과제 상세 조회-> 선정팀 조회로 이어짐
    public RR_AnnouncementVO getProjectDetail(int ann_id){
    	Connection conn= null;
    	PreparedStatement pstmt=null;
    	ResultSet rs= null;
    	String sql=null;
    	RR_AnnouncementVO vo=null; 
    	
    	
    	try {
    		conn=DBUtil.getConnection();
    		
    		 sql = "SELECT a.announcement_ann_id, " +
           	      "       a.announcement_title, " +
           	      "       a.announcement_total_budget, " +
           	      "       NVL((SELECT COUNT(ap.application_id) " +
           	      "            FROM Application ap " +
           	      "            WHERE ap.announcement_ann_id = a.announcement_ann_id), 0) AS cnt " +
           	      "FROM Announcement a WHERE a.announcement_ann_id = ?"; 
    		 
    		 pstmt = conn.prepareStatement(sql);
             rs = pstmt.executeQuery();
             pstmt.setInt(1, ann_id);
             
             if (rs.next()) {   
                 vo = new RR_AnnouncementVO();
                 vo.setAnnId(rs.getInt("announcement_ann_id"));
                 vo.setTitle(rs.getString("announcement_title"));
                 vo.setTotalBudget(rs.getLong("announcement_total_budget"));
                 vo.setApplicantCount(rs.getInt("cnt"));
                 vo.setApplicantCount(rs.getInt("cnt"));
             }
    		 
    	}catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    	return vo; 
    }
    
    //선정팀 조회
    /*
    public List<RR_AnnouncementVO> getSelectTeam(int project_id){
    	//List<RR_AnnouncementVO> list=new Arraylist<>();
    	Connection conn= null;
    	PreparedStatement pstmt=null;
    	ResultSet rs= null;
    	String sql=null;
    	
    	try {
    		conn=DBUtil.getConnection();
    		
    		//sql="SELECT "
    	}catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }
    */
}
