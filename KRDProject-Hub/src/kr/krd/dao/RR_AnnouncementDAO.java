package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.constant.RR_AnnouncementStatus;
import kr.krd.vo.RR_AnnouncementVO;
import kr.util.DBUtil;

public class RR_AnnouncementDAO {

    // ===== 공고 등록 =====
    public int insertAnnouncement(RR_AnnouncementVO vo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int cnt = 0;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "INSERT INTO ANNOUNCEMENT ("
              + " ANNOUNCEMENT_ANN_ID, ANNOUNCEMENT_AGY_ID, ANNOUNCEMENT_TITLE, ANNOUNCEMENT_DESC, "
              + " ANNOUNCEMENT_REANN_YN, ANNOUNCEMENT_PM_CONTACT, ANNOUNCEMENT_RECRUIT_CAP, "
              + " ANNOUNCEMENT_START_DT, ANNOUNCEMENT_END_DT, ANNOUNCEMENT_STATUS, "
              + " ANNOUNCEMENT_FIELD, ANNOUNCEMENT_CREATED_BY, ANNOUNCEMENT_TOTAL_BUDGET, "
              + " ANNOUNCEMENT_HIDDEN_YN"
              + ") VALUES ("
              + " ANNOUNCEMENT_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?"
              + ")";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, vo.getAgyId());
            pstmt.setString(2, vo.getTitle());
            pstmt.setString(3, vo.getDesc());
            pstmt.setInt(4, vo.getReannYn());
            pstmt.setString(5, vo.getPmContact());
            pstmt.setInt(6, vo.getRecruitCap());
            pstmt.setString(7, vo.getStartDt());
            pstmt.setString(8, vo.getEndDt());
            pstmt.setString(9, vo.getStatus());
            pstmt.setString(10, vo.getField());
            pstmt.setString(11, vo.getCreatedBy());
            pstmt.setLong(12, vo.getTotalBudget());
            pstmt.setInt(13, vo.getHiddenYn());

            cnt = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }

        return cnt;
    }

    // ===== 기관별 공고 목록 조회 =====
    public List<RR_AnnouncementVO> getAnnouncementListByAgency(int agyId) {
        List<RR_AnnouncementVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT a.ANNOUNCEMENT_ANN_ID, "
              + "       a.ANNOUNCEMENT_TITLE, "
              + "       a.ANNOUNCEMENT_STATUS, "
              + "       a.ANNOUNCEMENT_END_DT, "
              + "       COUNT(ap.APPLICATION_ID) AS APPLICANT_COUNT "
              + "FROM ANNOUNCEMENT a "
              + "LEFT JOIN APPLICATIONS ap "
              + "       ON a.ANNOUNCEMENT_ANN_ID = ap.APPLICATION_ANN_ID "
              + "WHERE a.ANNOUNCEMENT_AGY_ID = ? "
              + "  AND a.ANNOUNCEMENT_HIDDEN_YN = 0 "
              + "GROUP BY a.ANNOUNCEMENT_ANN_ID, "
              + "         a.ANNOUNCEMENT_TITLE, "
              + "         a.ANNOUNCEMENT_STATUS, "
              + "         a.ANNOUNCEMENT_END_DT "
              + "ORDER BY a.ANNOUNCEMENT_ANN_ID DESC";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, agyId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RR_AnnouncementVO vo = new RR_AnnouncementVO();
                vo.setAnnId(rs.getInt("ANNOUNCEMENT_ANN_ID"));
                vo.setTitle(rs.getString("ANNOUNCEMENT_TITLE"));
                vo.setStatus(rs.getString("ANNOUNCEMENT_STATUS"));
                vo.setEndDt(rs.getString("ANNOUNCEMENT_END_DT"));
                vo.setApplicantCount(rs.getInt("APPLICANT_COUNT"));
                list.add(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return list;
    }

    // ===== 공고 상세 조회 =====
    public RR_AnnouncementVO getAnnouncementDetail(int annId, int agyId) {
        RR_AnnouncementVO vo = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT ANNOUNCEMENT_ANN_ID, ANNOUNCEMENT_AGY_ID, ANNOUNCEMENT_TITLE, ANNOUNCEMENT_DESC, "
              + "       ANNOUNCEMENT_REANN_YN, ANNOUNCEMENT_PM_CONTACT, ANNOUNCEMENT_RECRUIT_CAP, "
              + "       ANNOUNCEMENT_START_DT, ANNOUNCEMENT_END_DT, ANNOUNCEMENT_STATUS, "
              + "       ANNOUNCEMENT_FIELD, ANNOUNCEMENT_CREATED_BY, ANNOUNCEMENT_TOTAL_BUDGET, "
              + "       ANNOUNCEMENT_HIDDEN_YN "
              + "FROM ANNOUNCEMENT "
              + "WHERE ANNOUNCEMENT_ANN_ID = ? "
              + "  AND ANNOUNCEMENT_AGY_ID = ? "
              + "  AND ANNOUNCEMENT_HIDDEN_YN = 0";

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
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return vo;
    }

    // ===== 공고 수정 =====
    public int updateAnnouncementField(int annId, int agyId, int fieldNo, String newValue) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int cnt = 0;

        try {
            conn = DBUtil.getConnection();

            String sql = null;

            switch (fieldNo) {
                case 1:
                    sql = "UPDATE ANNOUNCEMENT SET ANNOUNCEMENT_TITLE = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    break;
                case 2:
                    sql = "UPDATE ANNOUNCEMENT SET ANNOUNCEMENT_DESC = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    break;
                case 3:
                    sql = "UPDATE ANNOUNCEMENT SET ANNOUNCEMENT_TOTAL_BUDGET = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    break;
                case 4:
                    sql = "UPDATE ANNOUNCEMENT SET ANNOUNCEMENT_RECRUIT_CAP = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    break;
                case 5:
                    sql = "UPDATE ANNOUNCEMENT SET ANNOUNCEMENT_END_DT = ? "
                        + "WHERE ANNOUNCEMENT_ANN_ID = ? AND ANNOUNCEMENT_AGY_ID = ? AND ANNOUNCEMENT_HIDDEN_YN = 0";
                    break;
                default:
                    return 0;
            }

            pstmt = conn.prepareStatement(sql);

            if (fieldNo == 3 || fieldNo == 4) {
                pstmt.setLong(1, Long.parseLong(newValue));
            } else {
                pstmt.setString(1, newValue);
            }

            pstmt.setInt(2, annId);
            pstmt.setInt(3, agyId);

            cnt = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }

        return cnt;
    }

    // ===== 공고 논리 삭제 =====
    public int softDeleteAnnouncement(int annId, int agyId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int cnt = 0;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "UPDATE ANNOUNCEMENT "
              + "SET ANNOUNCEMENT_HIDDEN_YN = 1 "
              + "WHERE ANNOUNCEMENT_ANN_ID = ? "
              + "  AND ANNOUNCEMENT_AGY_ID = ? "
              + "  AND ANNOUNCEMENT_HIDDEN_YN = 0";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, annId);
            pstmt.setInt(2, agyId);

            cnt = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }

        return cnt;
    }

    // ===== 마감 -> 선정대기 상태 변경 =====
    public int promoteClosedToSelectPending(int agyId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int cnt = 0;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "UPDATE ANNOUNCEMENT a "
              + "SET a.ANNOUNCEMENT_STATUS = ? "
              + "WHERE a.ANNOUNCEMENT_AGY_ID = ? "
              + "  AND a.ANNOUNCEMENT_HIDDEN_YN = 0 "
              + "  AND a.ANNOUNCEMENT_STATUS = ? "
              + "  AND EXISTS ( "
              + "      SELECT 1 FROM APPLICATIONS ap "
              + "      WHERE ap.APPLICATION_ANN_ID = a.ANNOUNCEMENT_ANN_ID "
              + "  ) "
              + "  AND NOT EXISTS ( "
              + "      SELECT 1 FROM SELECTION s "
              + "      WHERE s.SELECTION_ANN_ID = a.ANNOUNCEMENT_ANN_ID "
              + "  )";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, RR_AnnouncementStatus.SELECT_PENDING);
            pstmt.setInt(2, agyId);
            pstmt.setString(3, RR_AnnouncementStatus.CLOSED);

            cnt = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }

        return cnt;
    }

    // ===== 선정 결과가 이미 있는 공고를 선정완료 상태로 보정 =====
    /*public int syncSelectedDoneAnnouncements(int agyId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        int cnt = 0;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "UPDATE ANNOUNCEMENT a "
              + "SET a.ANNOUNCEMENT_STATUS = ? "
              + "WHERE a.ANNOUNCEMENT_AGY_ID = ? "
              + "  AND a.ANNOUNCEMENT_HIDDEN_YN = 0 "
              + "  AND a.ANNOUNCEMENT_STATUS <> ? "
              + "  AND EXISTS ( "
              + "      SELECT 1 FROM SELECTION s "
              + "      WHERE s.SELECTION_ANN_ID = a.ANNOUNCEMENT_ANN_ID "
              + "  )";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, RR_AnnouncementStatus.SELECT_DONE);
            pstmt.setInt(2, agyId);
            pstmt.setString(3, RR_AnnouncementStatus.SELECT_DONE);

            cnt = pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }

        return cnt;
    }*/
}