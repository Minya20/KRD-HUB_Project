package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.krd.constant.RR_ReportConst;
import kr.krd.vo.RR_ReportVO;
import kr.util.DBUtil;

public class RR_ReportDAO {

    // 프로젝트별 최신 보고서 1건(중간/최종)
    public RR_ReportVO getLatestReport(int projectId, String typeCd) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "SELECT * FROM ( " +
                "  SELECT REPORT_RPT_ID, REPORT_PROJECT_ID, REPORT_RPT_TYPE_CD, " +
                "         TO_CHAR(REPORT_SUBMITTED_AT,'YYYY-MM-DD') AS SUBMITTED_AT, " +
                "         REPORT_STATUS_CD, REPORT_REJECT_REASON, REPORT_CONTENT, REPORT_KEYWORDS, REPORT_PROGRESS_RATE, " +
                "         REPORT_APPROVED_BY, TO_CHAR(REPORT_APPROVED_AT,'YYYY-MM-DD') AS APPROVED_AT " +
                "  FROM REPORTS " +
                "  WHERE REPORT_PROJECT_ID = ? AND REPORT_RPT_TYPE_CD = ? " +
                "  ORDER BY REPORT_SUBMITTED_AT DESC NULLS LAST, REPORT_RPT_ID DESC " +
                ") WHERE ROWNUM = 1";

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setString(2, typeCd);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                RR_ReportVO vo = new RR_ReportVO();
                vo.setReportRptId(rs.getInt("REPORT_RPT_ID"));
                vo.setReportProjectId(rs.getInt("REPORT_PROJECT_ID"));
                vo.setReportTypeCd(rs.getString("REPORT_RPT_TYPE_CD"));

                vo.setSubmittedAt(rs.getString("SUBMITTED_AT"));
                vo.setStatusCd(rs.getString("REPORT_STATUS_CD"));

                vo.setRejectReason(rs.getString("REPORT_REJECT_REASON"));
                vo.setContent(rs.getString("REPORT_CONTENT"));
                vo.setKeywords(rs.getString("REPORT_KEYWORDS"));

                int pr = rs.getInt("REPORT_PROGRESS_RATE");
                vo.setProgressRate(rs.wasNull() ? null : pr);

                vo.setApprovedBy(rs.getString("REPORT_APPROVED_BY"));
                vo.setApprovedAt(rs.getString("APPROVED_AT"));
                return vo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
        return null;
    }

    // 승인
    public int approveReport(int reportRptId, String approverId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "UPDATE REPORTS " +
                "SET REPORT_STATUS_CD = ?, " +
                "    REPORT_APPROVED_BY = ?, " +
                "    REPORT_APPROVED_AT = SYSDATE, " +
                "    REPORT_REJECT_REASON = NULL " +
                "WHERE REPORT_RPT_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, RR_ReportConst.ST_APPROVED);
            pstmt.setString(2, approverId);
            pstmt.setInt(3, reportRptId);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }

    // 반려 (사유 저장)
    public int rejectReport(int reportRptId, String approverId, String reason) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            String sql =
                "UPDATE REPORTS " +
                "SET REPORT_STATUS_CD = ?, " +
                "    REPORT_APPROVED_BY = ?, " +
                "    REPORT_APPROVED_AT = SYSDATE, " +
                "    REPORT_REJECT_REASON = ? " +
                "WHERE REPORT_RPT_ID = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, RR_ReportConst.ST_REJECTED);
            pstmt.setString(2, approverId);
            pstmt.setString(3, reason);
            pstmt.setInt(4, reportRptId);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }
}