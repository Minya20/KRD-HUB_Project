package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class HJY_CheckSystem {

    public void applyAnnouncement(int annId,
                                  String userId,
                                  String attachPath,
                                  int budgetAmt) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            /* 1 공고 상태 확인 */
            String checkSql =
                    "SELECT ANNOUNCEMENT_STATUS " +
                    "FROM ANNOUNCEMENT " +
                    "WHERE ANNOUNCEMENT_ANN_ID = ?";

            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, annId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("공고가 존재하지 않습니다.");
                conn.rollback();
                return;
            }

            String status = rs.getString("ANNOUNCEMENT_STATUS");

            if (!"공고중".equals(status)) {
                System.out.println("현재 모집중인 공고가 아닙니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();

           /*2 신청 체크 */
            String dupSql =
                    "SELECT COUNT(*) FROM APPLICATION " +
                    "WHERE APPLICATION_ANN_ID = ? " +
                    "AND APPLICATION_USER_ID = ?";

            pstmt = conn.prepareStatement(dupSql);
            pstmt.setInt(1, annId);
            pstmt.setString(2, userId);
            rs = pstmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                System.out.println("이미 신청한 공고입니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();


            /* 3. 진행중(SELECTED) 과제 5개 제한 */
            String countSql =
                    "SELECT COUNT(*) FROM APPLICATION " +
                    "WHERE APPLICATION_USER_ID = ? " +
                    "AND APPLICATION_STATUS_CD = 'SELECTED'";

            pstmt = conn.prepareStatement(countSql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            rs.next();

            if (rs.getInt(1) >= 5) {
                System.out.println("진행중 과제가 5개 이상입니다.");
                conn.rollback();
                return;
            }

            rs.close();
            pstmt.close();


            /* 4.APPLICATION INSERT */
            String insertSql =
                    "INSERT INTO APPLICATION (" +
                    "APPLICATION_ANN_ID, " +
                    "APPLICATION_USER_ID, " +
                    "APPLICATION_APPLIED_AT, " +
                    "APPLICATION_UPDATED_AT, " +
                    "APPLICATION_ATTACH_PATH, " +
                    "APPLICATION_STATUS_CD, " +
                    "APPLICATION_BUDGET_AMT" +
                    ") VALUES (?, ?, SYSDATE, SYSDATE, ?, 'APPLIED', ?)";

            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, annId);
            pstmt.setString(2, userId);
            pstmt.setString(3, attachPath);
            pstmt.setInt(4, budgetAmt);

            pstmt.executeUpdate();
            pstmt.close();

            conn.commit();
            System.out.println("신청 완료!");

        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (Exception ex) {}
            e.printStackTrace();
        } finally {
            DBUtil.executeClose(rs, pstmt, conn);
        }
    }
}