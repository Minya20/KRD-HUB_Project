package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import kr.krd.constant.RR_AnnouncementStatus;
import kr.krd.vo.RR_TaskProgressVO;
import kr.krd.vo.RR_TeamProgressVO;
import kr.util.DBUtil;

public class RR_TaskProgressDAO {

    // 6-1) "선정된 과제만" 목록 (공고 단계 제외)
	public List<RR_TaskProgressVO> getTaskProgressList(int agyId) {
	    List<RR_TaskProgressVO> list = new ArrayList<>();
	    Connection conn = null;
	    PreparedStatement pstmt = null;
	    ResultSet rs = null;

	    try {
	        conn = DBUtil.getConnection();

	        String sql =
	            "SELECT an.ANNOUNCEMENT_ANN_ID AS ANN_ID, "
	          + "       an.ANNOUNCEMENT_TITLE AS TITLE, "
	          + "       NVL(an.ANNOUNCEMENT_TOTAL_BUDGET, 0) AS TOTAL_BUDGET, "
	          + "       COUNT(p.PROJECT_ID) AS TEAM_COUNT, "
	          + "       CASE "
	          + "         WHEN SUM(CASE WHEN p.PROJECT_STATUS_CD IN ('ONGOING','IN_PROGRESS') THEN 1 ELSE 0 END) > 0 "
	          + "              THEN '진행중' "
	          + "         WHEN SUM(CASE WHEN p.PROJECT_STATUS_CD = 'COMPLETED' THEN 1 ELSE 0 END) = COUNT(p.PROJECT_ID) "
	          + "              THEN '완료' "
	          + "         WHEN SUM(CASE WHEN p.PROJECT_STATUS_CD = 'STOPPED' THEN 1 ELSE 0 END) = COUNT(p.PROJECT_ID) "
	          + "              THEN '중단' "
	          + "         ELSE '진행중' "
	          + "       END AS TASK_STATUS "
	          + "FROM ANNOUNCEMENT an "
	          + "JOIN APPLICATIONS ap "
	          + "  ON ap.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ANN_ID "
	          + "JOIN PROJECTS p "
	          + "  ON p.PROJECT_APPLICATION_ID = ap.APPLICATION_ID "
	          + "WHERE an.ANNOUNCEMENT_AGY_ID = ? "
	          + "  AND an.ANNOUNCEMENT_HIDDEN_YN = 0 "
	          + "GROUP BY an.ANNOUNCEMENT_ANN_ID, an.ANNOUNCEMENT_TITLE, an.ANNOUNCEMENT_TOTAL_BUDGET "
	          + "ORDER BY an.ANNOUNCEMENT_ANN_ID DESC";

	        pstmt = conn.prepareStatement(sql);
	        pstmt.setInt(1, agyId);
	        rs = pstmt.executeQuery();

	        while (rs.next()) {
	            RR_TaskProgressVO vo = new RR_TaskProgressVO();
	            vo.setAnnId(rs.getInt("ANN_ID"));
	            vo.setTitle(rs.getString("TITLE"));
	            vo.setTotalBudget(rs.getLong("TOTAL_BUDGET"));
	            vo.setTeamCount(rs.getInt("TEAM_COUNT"));
	            vo.setTaskStatus(rs.getString("TASK_STATUS"));
	            list.add(vo);
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        DBUtil.executeClose(rs, pstmt, conn);
	    }

	    return list;
	}
    // 6-2) 과제(annId) 선택 시: 팀(프로젝트) 진행 현황 목록
    public List<RR_TeamProgressVO> getTeamProgressList(int agyId, int annId) {
        List<RR_TeamProgressVO> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT p.PROJECT_ID, ap.APPLICATION_ID, ap.APPLICATION_USER_ID AS USER_ID, u.USER_NAME, "
              + "       p.PROJECT_STATUS_CD, "
              + "       NVL((SELECT MAX(ag.AGREEMENT_STATUS_CD) KEEP (DENSE_RANK LAST ORDER BY ag.AGREEMENT_SIGNED_AT NULLS LAST, ag.AGREEMENT_ID) "
              + "            FROM AGREEMENT ag WHERE ag.AGREEMENT_PROJECT_ID = p.PROJECT_ID), 'PENDING') AS AG_ST, "
              + "       NVL((SELECT MAX(f.FUNDING_PAY_ROUND) FROM FUNDING f WHERE f.FUNDING_PROJECT_ID = p.PROJECT_ID AND f.FUNDING_APPROVED_YN = 1), 0) AS PAID_ROUND, "
              + "       NVL((SELECT SUM(f.FUNDING_AMOUNT_AMT) FROM FUNDING f WHERE f.FUNDING_PROJECT_ID = p.PROJECT_ID AND f.FUNDING_APPROVED_YN = 1), 0) AS PAID_TOTAL, "
              + "       (SELECT MAX(r.REPORT_STATUS_CD) KEEP (DENSE_RANK LAST ORDER BY r.REPORT_SUBMITTED_AT NULLS LAST, r.REPORT_RPT_ID) "
              + "          FROM REPORTS r WHERE r.REPORT_PROJECT_ID = p.PROJECT_ID AND r.REPORT_RPT_TYPE_CD='MID') AS MID_ST, "
              + "       (SELECT MAX(r.REPORT_STATUS_CD) KEEP (DENSE_RANK LAST ORDER BY r.REPORT_SUBMITTED_AT NULLS LAST, r.REPORT_RPT_ID) "
              + "          FROM REPORTS r WHERE r.REPORT_PROJECT_ID = p.PROJECT_ID AND r.REPORT_RPT_TYPE_CD='FINAL') AS FIN_ST "
              + "FROM ANNOUNCEMENT an "
              + "JOIN APPLICATIONS ap ON ap.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ANN_ID "
              + "JOIN PROJECTS p ON p.PROJECT_APPLICATION_ID = ap.APPLICATION_ID "
              + "JOIN USERINFO u ON u.USER_ID = ap.APPLICATION_USER_ID "
              + "WHERE an.ANNOUNCEMENT_AGY_ID = ? "
              + "  AND an.ANNOUNCEMENT_ANN_ID = ? "
              + "  AND an.ANNOUNCEMENT_HIDDEN_YN = 0 "
              + "ORDER BY p.PROJECT_ID";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, agyId);
            pstmt.setInt(2, annId);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                RR_TeamProgressVO vo = new RR_TeamProgressVO();
                vo.setProjectId(rs.getInt("PROJECT_ID"));
                vo.setApplicationId(rs.getInt("APPLICATION_ID"));
                vo.setUserId(rs.getString("USER_ID"));
                vo.setUserName(rs.getString("USER_NAME"));
                vo.setProjectStatusCd(rs.getString("PROJECT_STATUS_CD"));
                vo.setAgreementStatusCd(rs.getString("AG_ST"));
                vo.setPaidRound(rs.getInt("PAID_ROUND"));
                vo.setPaidTotalAmt(rs.getLong("PAID_TOTAL"));
                vo.setMidStatusCd(rs.getString("MID_ST"));
                vo.setFinalStatusCd(rs.getString("FIN_ST"));
                list.add(vo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return list;
    }

    // 6-3) 팀(프로젝트) 상세
    public RR_TeamProgressVO getTeamProgressDetail(int projectId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT p.PROJECT_ID, ap.APPLICATION_ID, ap.APPLICATION_USER_ID AS USER_ID, u.USER_NAME, "
              + "       p.PROJECT_STATUS_CD, "
              + "       s.SELECTION_FINAL_SCORE, TO_CHAR(s.SELECTION_APPROVED_AT,'YYYY-MM-DD') AS SEL_AT, "
              + "       NVL((SELECT MAX(ag.AGREEMENT_STATUS_CD) KEEP (DENSE_RANK LAST ORDER BY ag.AGREEMENT_SIGNED_AT NULLS LAST, ag.AGREEMENT_ID) "
              + "            FROM AGREEMENT ag WHERE ag.AGREEMENT_PROJECT_ID = p.PROJECT_ID), 'PENDING') AS AG_ST, "
              + "       TO_CHAR((SELECT MAX(ag.AGREEMENT_SIGNED_AT) FROM AGREEMENT ag WHERE ag.AGREEMENT_PROJECT_ID = p.PROJECT_ID AND ag.AGREEMENT_STATUS_CD='SIGNED'), 'YYYY-MM-DD') AS AG_AT, "
              + "       NVL((SELECT MAX(f.FUNDING_PAY_ROUND) FROM FUNDING f WHERE f.FUNDING_PROJECT_ID = p.PROJECT_ID AND f.FUNDING_APPROVED_YN = 1), 0) AS PAID_ROUND, "
              + "       NVL((SELECT SUM(f.FUNDING_AMOUNT_AMT) FROM FUNDING f WHERE f.FUNDING_PROJECT_ID = p.PROJECT_ID AND f.FUNDING_APPROVED_YN = 1), 0) AS PAID_TOTAL, "
              + "       (SELECT MAX(r.REPORT_STATUS_CD) KEEP (DENSE_RANK LAST ORDER BY r.REPORT_SUBMITTED_AT NULLS LAST, r.REPORT_RPT_ID) "
              + "          FROM REPORTS r WHERE r.REPORT_PROJECT_ID = p.PROJECT_ID AND r.REPORT_RPT_TYPE_CD='MID') AS MID_ST, "
              + "       (SELECT MAX(r.REPORT_STATUS_CD) KEEP (DENSE_RANK LAST ORDER BY r.REPORT_SUBMITTED_AT NULLS LAST, r.REPORT_RPT_ID) "
              + "          FROM REPORTS r WHERE r.REPORT_PROJECT_ID = p.PROJECT_ID AND r.REPORT_RPT_TYPE_CD='FINAL') AS FIN_ST "
              + "FROM PROJECTS p "
              + "JOIN APPLICATIONS ap ON ap.APPLICATION_ID = p.PROJECT_APPLICATION_ID "
              + "JOIN USERINFO u ON u.USER_ID = ap.APPLICATION_USER_ID "
              + "LEFT JOIN SELECTION s ON s.SELECTION_APPLICATION_ID = ap.APPLICATION_ID "
              + "WHERE p.PROJECT_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                RR_TeamProgressVO vo = new RR_TeamProgressVO();
                vo.setProjectId(rs.getInt("PROJECT_ID"));
                vo.setApplicationId(rs.getInt("APPLICATION_ID"));
                vo.setUserId(rs.getString("USER_ID"));
                vo.setUserName(rs.getString("USER_NAME"));
                vo.setProjectStatusCd(rs.getString("PROJECT_STATUS_CD"));

                double score = rs.getDouble("SELECTION_FINAL_SCORE");
                vo.setSelectionScore(rs.wasNull() ? null : score);
                vo.setSelectionApprovedAt(rs.getString("SEL_AT"));

                vo.setAgreementStatusCd(rs.getString("AG_ST"));
                vo.setAgreementSignedAt(rs.getString("AG_AT"));

                vo.setPaidRound(rs.getInt("PAID_ROUND"));
                vo.setPaidTotalAmt(rs.getLong("PAID_TOTAL"));

                vo.setMidStatusCd(rs.getString("MID_ST"));
                vo.setFinalStatusCd(rs.getString("FIN_ST"));
                return vo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }

        return null;
    }
}