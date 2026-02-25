package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.vo.RR_AnnouncementVO;
import kr.util.DBUtil;

public class RR_AnnouncementDAO {

    // 1) 공고 등록
	public int insertAnnouncement(RR_AnnouncementVO vo) {
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    String sql = null;
	    int count = 0;

	    try {
	        conn = DBUtil.getConnection();

	        // PK(ANNOUNCEMENT_ANN_ID)를 시퀀스로 직접 넣음
	        sql = "INSERT INTO ANNOUNCEMENT ("
	            + "ANNOUNCEMENT_ANN_ID, "  // ← PK 컬럼 추가
	            + "ANNOUNCEMENT_AGY_ID, ANNOUNCEMENT_TITLE, ANNOUNCEMENT_DESC, "
	            + "ANNOUNCEMENT_REANN_YN, ANNOUNCEMENT_PM_CONTACT, ANNOUNCEMENT_RECRUIT_CAP, "
	            + "ANNOUNCEMENT_START_DT, ANNOUNCEMENT_END_DT, ANNOUNCEMENT_STATUS, "
	            + "ANNOUNCEMENT_FIELD, ANNOUNCEMENT_CREATED_BY, ANNOUNCEMENT_TOTAL_BUDGET, "
	            + "ANNOUNCEMENT_HIDDEN_YN"
	            + ") VALUES ("
	            + "ANNOUNCEMENT_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?" // ← 값 추가
	            + ")";

	        pstmt = conn.prepareStatement(sql);

	        int idx = 1;
	        pstmt.setInt(idx++, vo.getAgyId());
	        pstmt.setString(idx++, vo.getTitle());
	        pstmt.setString(idx++, vo.getDesc());
	        pstmt.setInt(idx++, vo.getReannYn());
	        pstmt.setString(idx++, vo.getPmContact());
	        pstmt.setInt(idx++, vo.getRecruitCap());
	        pstmt.setString(idx++, vo.getStartDt());   // VARCHAR2(10)
	        pstmt.setString(idx++, vo.getEndDt());     // VARCHAR2(10)
	        pstmt.setString(idx++, vo.getStatus());    // 공고중
	        pstmt.setString(idx++, vo.getField());
	        pstmt.setString(idx++, vo.getCreatedBy());
	        pstmt.setLong(idx++, vo.getTotalBudget());
	        pstmt.setInt(idx++, vo.getHiddenYn());

	        count = pstmt.executeUpdate();

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        DBUtil.executeClose(null, pstmt, conn);
	    }

	    return count;
	}

    // 2) 기관별 공고 목록 조회 (신청팀 수 포함)
    public List<RR_AnnouncementVO> getAnnouncementListByAgency(int agyId) {
        List<RR_AnnouncementVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            conn = DBUtil.getConnection();

            sql = "SELECT a.ANNOUNCEMENT_ANN_ID, a.ANNOUNCEMENT_TITLE, a.ANNOUNCEMENT_STATUS, "
                + "       a.ANNOUNCEMENT_TOTAL_BUDGET, a.ANNOUNCEMENT_RECRUIT_CAP, a.ANNOUNCEMENT_END_DT, "
                + "       NVL((SELECT COUNT(*) "
                + "              FROM APPLICATIONS ap "
                + "             WHERE ap.APPLICATION_ANN_ID = a.ANNOUNCEMENT_ANN_ID), 0) AS APP_CNT "
                + "FROM ANNOUNCEMENT a "
                + "WHERE a.ANNOUNCEMENT_AGY_ID = ? "
                + "  AND a.ANNOUNCEMENT_HIDDEN_YN = 0 "
                + "ORDER BY a.ANNOUNCEMENT_ANN_ID DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, agyId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RR_AnnouncementVO vo = new RR_AnnouncementVO();
                vo.setAnnId(rs.getInt("ANNOUNCEMENT_ANN_ID"));
                vo.setTitle(rs.getString("ANNOUNCEMENT_TITLE"));
                vo.setStatus(rs.getString("ANNOUNCEMENT_STATUS"));
                vo.setTotalBudget(rs.getLong("ANNOUNCEMENT_TOTAL_BUDGET"));
                vo.setRecruitCap(rs.getInt("ANNOUNCEMENT_RECRUIT_CAP"));
                vo.setEndDt(rs.getString("ANNOUNCEMENT_END_DT"));
                vo.setApplicantCount(rs.getInt("APP_CNT"));
                list.add(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return list;
    }

    // 3) 특정 공고 상세 조회 (수정/삭제용)
    public RR_AnnouncementVO getAnnouncementDetail(int annId, int agyId) {
        RR_AnnouncementVO vo = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            conn = DBUtil.getConnection();

            sql = "SELECT a.ANNOUNCEMENT_ANN_ID, a.ANNOUNCEMENT_AGY_ID, a.ANNOUNCEMENT_TITLE, "
                + "       a.ANNOUNCEMENT_DESC, a.ANNOUNCEMENT_REANN_YN, a.ANNOUNCEMENT_PM_CONTACT, "
                + "       a.ANNOUNCEMENT_RECRUIT_CAP, a.ANNOUNCEMENT_START_DT, a.ANNOUNCEMENT_END_DT, "
                + "       a.ANNOUNCEMENT_STATUS, a.ANNOUNCEMENT_FIELD, a.ANNOUNCEMENT_CREATED_BY, "
                + "       a.ANNOUNCEMENT_TOTAL_BUDGET, a.ANNOUNCEMENT_HIDDEN_YN, "
                + "       NVL((SELECT COUNT(*) "
                + "              FROM APPLICATIONS ap "
                + "             WHERE ap.APPLICATION_ANN_ID = a.ANNOUNCEMENT_ANN_ID), 0) AS APP_CNT "
                + "FROM ANNOUNCEMENT a "
                + "WHERE a.ANNOUNCEMENT_ANN_ID = ? "
                + "  AND a.ANNOUNCEMENT_AGY_ID = ? "
                + "  AND a.ANNOUNCEMENT_HIDDEN_YN = 0";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, annId);
            pstmt.setInt(2, agyId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                vo = new RR_AnnouncementVO();
                vo.setAnnId(rs.getInt("ANNOUNCEMENT_ANN_ID"));
                vo.setAgyId(rs.getInt("ANNOUNCEMENT_AGY_ID"));
                vo.setTitle(rs.getString("ANNOUNCEMENT_TITLE"));
                vo.setDesc(rs.getString("ANNOUNCEMENT_DESC"));
                vo.setReannYn(rs.getInt("ANNOUNCEMENT_REANN_YN"));
                vo.setPmContact(rs.getString("ANNOUNCEMENT_PM_CONTACT"));
                vo.setRecruitCap(rs.getInt("ANNOUNCEMENT_RECRUIT_CAP"));
                vo.setStartDt(rs.getString("ANNOUNCEMENT_START_DT"));
                vo.setEndDt(rs.getString("ANNOUNCEMENT_END_DT"));
                vo.setStatus(rs.getString("ANNOUNCEMENT_STATUS"));
                vo.setField(rs.getString("ANNOUNCEMENT_FIELD"));
                vo.setCreatedBy(rs.getString("ANNOUNCEMENT_CREATED_BY"));
                vo.setTotalBudget(rs.getLong("ANNOUNCEMENT_TOTAL_BUDGET"));
                vo.setHiddenYn(rs.getInt("ANNOUNCEMENT_HIDDEN_YN"));
                vo.setApplicantCount(rs.getInt("APP_CNT"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return vo;
    }

    // 4) 공고 수정
    // fieldNo: 1=과제명, 2=과제설명, 3=예산, 4=선정팀수, 5=마감일
    public int updateAnnouncementField(int annId, int agyId, int fieldNo, String newValue) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = null;
        int count = 0;

        try {
            conn = DBUtil.getConnection();

            switch (fieldNo) {
                case 1:
                    sql = "UPDATE ANNOUNCEMENT "
                        + "SET ANNOUNCEMENT_TITLE = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, newValue);
                    pstmt.setInt(2, annId);
                    pstmt.setInt(3, agyId);
                    break;

                case 2:
                    sql = "UPDATE ANNOUNCEMENT "
                        + "SET ANNOUNCEMENT_DESC = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, newValue);
                    pstmt.setInt(2, annId);
                    pstmt.setInt(3, agyId);
                    break;

                case 3:
                    sql = "UPDATE ANNOUNCEMENT "
                        + "SET ANNOUNCEMENT_TOTAL_BUDGET = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setLong(1, Long.parseLong(newValue));
                    pstmt.setInt(2, annId);
                    pstmt.setInt(3, agyId);
                    break;

                case 4:
                    sql = "UPDATE ANNOUNCEMENT "
                        + "SET ANNOUNCEMENT_RECRUIT_CAP = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, Integer.parseInt(newValue));
                    pstmt.setInt(2, annId);
                    pstmt.setInt(3, agyId);
                    break;

                case 5:
                    sql = "UPDATE ANNOUNCEMENT "
                        + "SET ANNOUNCEMENT_END_DT = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, newValue); // VARCHAR2(10)
                    pstmt.setInt(2, annId);
                    pstmt.setInt(3, agyId);
                    break;

                default:
                    return 0;
            }

            count = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }

        return count;
    }

    // 5) 공고 논리 삭제
    public int softDeleteAnnouncement(int annId, int agyId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = null;
        int count = 0;

        try {
            conn = DBUtil.getConnection();

            sql = "UPDATE ANNOUNCEMENT "
                + "SET ANNOUNCEMENT_HIDDEN_YN = 1 "
                + "WHERE ANNOUNCEMENT_ANN_ID = ? "
                + "  AND ANNOUNCEMENT_AGY_ID = ? "
                + "  AND ANNOUNCEMENT_HIDDEN_YN = 0";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, annId);
            pstmt.setInt(2, agyId);

            count = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }

        return count;
    }
}