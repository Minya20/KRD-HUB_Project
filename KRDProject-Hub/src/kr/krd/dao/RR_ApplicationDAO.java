package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.vo.RR_ApplicationVO;
import kr.util.DBUtil;

public class RR_ApplicationDAO {

    // 공고별 신청 목록 조회 (신청자 이름 포함 + 평균점수(있으면))
    public List<RR_ApplicationVO> getApplicationsByAnnouncement(int annId) {
        List<RR_ApplicationVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            conn = DBUtil.getConnection();

            // ✅ 평균점수 계산: EVALUATION_APPLICATION_ID(신청ID 참조) 기준
            sql =
              "SELECT ap.APPLICATION_ID, ap.APPLICATION_ANN_ID, ap.APPLICATION_USER_ID, u.USER_NAME, "
            + "       TO_CHAR(ap.APPLICATION_APPLIED_AT, 'YYYY-MM-DD') AS APPLIED_AT, "
            + "       ap.APPLICATION_STATUS_CD, ap.APPLICATION_ATTACH_PATH, NVL(ap.APPLICATION_BUDGET_AMT,0) AS BUDGET_AMT, "
            + "       ev.AVG_SCORE "
            + "FROM APPLICATIONS ap "
            + "JOIN USERINFO u ON u.USER_ID = ap.APPLICATION_USER_ID "
            + "LEFT JOIN ( "
            + "   SELECT e.EVALUATION_APPLICATION_ID AS APPLICATION_ID, "
            + "          ROUND(AVG(e.EVALUATION_SCORE), 2) AS AVG_SCORE "
            + "   FROM EVALUATIONS e "
            + "   GROUP BY e.EVALUATION_APPLICATION_ID "
            + ") ev ON ev.APPLICATION_ID = ap.APPLICATION_ID "
            + "WHERE ap.APPLICATION_ANN_ID = ? "
            + "ORDER BY ap.APPLICATION_ID";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, annId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                RR_ApplicationVO vo = new RR_ApplicationVO();
                vo.setApplicationId(rs.getInt("APPLICATION_ID"));
                vo.setAnnouncementAnnId(rs.getInt("APPLICATION_ANN_ID"));
                vo.setUserId(rs.getString("APPLICATION_USER_ID"));
                vo.setUserName(rs.getString("USER_NAME"));
                vo.setAppliedAt(rs.getString("APPLIED_AT"));
                vo.setStatusCd(rs.getString("APPLICATION_STATUS_CD"));
                vo.setAttachPath(rs.getString("APPLICATION_ATTACH_PATH"));
                vo.setBudgetAmt(rs.getLong("BUDGET_AMT"));

                double score = rs.getDouble("AVG_SCORE");
                vo.setAvgScore(rs.wasNull() ? null : score);

                list.add(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return list;
    }

    // 신청 상세 조회
    public RR_ApplicationVO getApplicationDetail(int applicationId) {
        RR_ApplicationVO vo = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = null;

        try {
            conn = DBUtil.getConnection();

            sql =
              "SELECT ap.APPLICATION_ID, ap.APPLICATION_ANN_ID, ap.APPLICATION_USER_ID, u.USER_NAME, "
            + "       TO_CHAR(ap.APPLICATION_APPLIED_AT, 'YYYY-MM-DD') AS APPLIED_AT, "
            + "       ap.APPLICATION_STATUS_CD, ap.APPLICATION_ATTACH_PATH, NVL(ap.APPLICATION_BUDGET_AMT,0) AS BUDGET_AMT, "
            + "       ev.AVG_SCORE "
            + "FROM APPLICATIONS ap "
            + "JOIN USERINFO u ON u.USER_ID = ap.APPLICATION_USER_ID "
            + "LEFT JOIN ( "
            + "   SELECT e.EVALUATION_APPLICATION_ID AS APPLICATION_ID, "
            + "          ROUND(AVG(e.EVALUATION_SCORE), 2) AS AVG_SCORE "
            + "   FROM EVALUATIONS e "
            + "   GROUP BY e.EVALUATION_APPLICATION_ID "
            + ") ev ON ev.APPLICATION_ID = ap.APPLICATION_ID "
            + "WHERE ap.APPLICATION_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, applicationId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                vo = new RR_ApplicationVO();
                vo.setApplicationId(rs.getInt("APPLICATION_ID"));
                vo.setAnnouncementAnnId(rs.getInt("APPLICATION_ANN_ID"));
                vo.setUserId(rs.getString("APPLICATION_USER_ID"));
                vo.setUserName(rs.getString("USER_NAME"));
                vo.setAppliedAt(rs.getString("APPLIED_AT"));
                vo.setStatusCd(rs.getString("APPLICATION_STATUS_CD"));
                vo.setAttachPath(rs.getString("APPLICATION_ATTACH_PATH"));
                vo.setBudgetAmt(rs.getLong("BUDGET_AMT"));

                double score = rs.getDouble("AVG_SCORE");
                vo.setAvgScore(rs.wasNull() ? null : score);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return vo;
    }
}