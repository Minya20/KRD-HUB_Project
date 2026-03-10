package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.krd.constant.RR_FundingConst;
import kr.util.DBUtil;

public class RR_FundingDAO {

    // 프로젝트의 희망 연구비(APPLICATION_BUDGET_AMT)
    public long getRequestedBudgetAmt(int projectId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql =
                "SELECT NVL(ap.APPLICATION_BUDGET_AMT,0) AS BUDGET_AMT " +
                "FROM PROJECTS p " +
                "JOIN APPLICATIONS ap ON ap.APPLICATION_ID = p.PROJECT_APPLICATION_ID " +
                "WHERE p.PROJECT_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getLong("BUDGET_AMT");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
        return 0;
    }

    // 해당 프로젝트의 특정 회차(1/2/3) 지급 완료 여부
    public boolean isRoundPaid(int projectId, int roundNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql =
                "SELECT COUNT(*) CNT " +
                "FROM FUNDING " +
                "WHERE FUNDING_PROJECT_ID = ? " +
                "  AND FUNDING_PAY_ROUND = ? " +
                "  AND FUNDING_APPROVED_YN = 1";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, roundNo);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("CNT") > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
        return false;
    }

    // 협약 체결 여부(1차 지급 조건)
    public boolean isAgreementSigned(int projectId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql =
                "SELECT NVL(MAX(AGREEMENT_STATUS_CD),'PENDING') AS ST " +
                "FROM AGREEMENT " +
                "WHERE AGREEMENT_PROJECT_ID = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            rs = pstmt.executeQuery();
            if (rs.next()) return "SIGNED".equalsIgnoreCase(rs.getString("ST"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
        return false;
    }

    // 중간/최종 보고 승인 여부(2/3차 조건)
    public boolean isReportApproved(int projectId, String typeCd /* MID / FINAL */) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql =
                "SELECT COUNT(*) CNT " +
                "FROM REPORTS " +
                "WHERE REPORT_PROJECT_ID = ? " +
                "  AND REPORT_TYPE_CD = ? " +
                "  AND REPORT_STATUS_CD = 'APPROVED'";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, projectId);
            pstmt.setString(2, typeCd);
            rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("CNT") > 0;
        } catch (Exception e) {
            // REPORTS가 아직 없거나 컬럼이 다르면 false로 처리(지급 막힘)
            return false;
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
        return false;
    }

    // 지급 승인 upSERT
    public int insertPaidFunding(int projectId, int roundNo, long amount, String approverId) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();

            // 1) 이미 (프로젝트, 회차) 레코드가 있으면 UPDATE로 승인 처리
            String updSql =
                "UPDATE FUNDING SET " +
                "  FUNDING_STATUS_CD = ?, " +
                "  FUNDING_AMOUNT_AMT = ?, " +
                "  FUNDING_REQUESTED_AT = SYSDATE, " +
                "  FUNDING_APPROVED_YN = 1, " +
                "  FUNDING_APPROVED_BY = ?, " +
                "  FUNDING_APPROVED_AT = SYSDATE " +
                "WHERE FUNDING_PROJECT_ID = ? " +
                "  AND FUNDING_PAY_ROUND = ?";

            pstmt = conn.prepareStatement(updSql);
            pstmt.setString(1, RR_FundingConst.STATUS_PAID);
            pstmt.setLong(2, amount);
            pstmt.setString(3, approverId);
            pstmt.setInt(4, projectId);
            pstmt.setInt(5, roundNo);

            int updated = pstmt.executeUpdate();
            pstmt.close();

            if (updated > 0) return updated; // 기존행 승인처리 완료

            // 2) 없으면 INSERT
            String insSql =
                "INSERT INTO FUNDING ( " +
                "  FUNDING_FUND_ID, FUNDING_PROJECT_ID, FUNDING_STATUS_CD, FUNDING_AMOUNT_AMT, " +
                "  FUNDING_REQUESTED_AT, FUNDING_APPROVED_YN, FUNDING_APPROVED_BY, FUNDING_APPROVED_AT, FUNDING_PAY_ROUND " +
                ") VALUES ( " +
                "  'F' || LPAD(FUNDING_SEQ.NEXTVAL, 19, '0'), ?, ?, ?, SYSDATE, 1, ?, SYSDATE, ? " +
                ")";

            pstmt = conn.prepareStatement(insSql);
            pstmt.setInt(1, projectId);
            pstmt.setString(2, RR_FundingConst.STATUS_PAID);
            pstmt.setLong(3, amount);
            pstmt.setString(4, approverId);
            pstmt.setInt(5, roundNo);

            return pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }
    
    
    
    
}