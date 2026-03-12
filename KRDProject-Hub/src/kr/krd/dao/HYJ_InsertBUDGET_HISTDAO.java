package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HYJ_InsertBUDGET_HISTDAO {

    public void insertBudgetHist(int annId, long afterAmt) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        String selectSql = null;
        String insertSql = null;

        try {
            conn = DBUtil.getConnection();

            // ======================
            // 기존 금액 조회
            // ======================
            selectSql =
                "SELECT ANNOUNCEMENT_TOTAL_BUDGET "
              + "FROM ANNOUNCEMENT "
              + "WHERE ANNOUNCEMENT_ANN_ID = ?";

            pstmt = conn.prepareStatement(selectSql);
            pstmt.setInt(1, annId);

            rs = pstmt.executeQuery();

            long beforeAmt = 0;
            if (rs.next()) {
                beforeAmt = rs.getLong(1);
            }

            DBUtil.executeClose(rs, pstmt, null);

            // ======================
            // HIST INSERT
            // ======================
            insertSql =
                "INSERT INTO BUDGET_HIST ("
              + " BUDGET_HIST_ID, "
              + " BUDGET_HIST_BEFORE_AMT, "
              + " BUDGET_HIST_AFTER_AMT, "
              + " BUDGET_HIST_CHANGED_AT, "
              + " BUDGET_HIST_PROJECT_ID, "
              + " BUDGET_HIST_CHANGED_BY "
              + ") VALUES (SEQ_BUDGET_HIST.NEXTVAL, ?, ?, SYSDATE, ?, ?)";

            pstmt = conn.prepareStatement(insertSql);
            pstmt.setLong(1, beforeAmt);
            pstmt.setLong(2, afterAmt);
            pstmt.setInt(3, annId);
            pstmt.setString(4, "SYSTEM");

            pstmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(null, pstmt, conn);
        }
    }
}