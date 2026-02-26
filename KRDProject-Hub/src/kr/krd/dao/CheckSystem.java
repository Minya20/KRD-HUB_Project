package kr.krd.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import kr.util.DBUtil;

public class CheckSystem {

    public void applyAnnouncement(int annId, String userId) {

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            /*공고 상태 + 모집 정원 조회 */
            String checkSql =
                "SELECT ANNOUNCEMENT_STATUS, ANNOUNCEMENT_RECRUIT_CAP " +
                "FROM ANNOUNCEMENT WHERE ANNOUNCEMENT_ANN_ID = ?";

            pstmt = conn.prepareStatement(checkSql);
            pstmt.setInt(1, annId);
            rs = pstmt.executeQuery();

            if (!rs.next()) {
                System.out.println("공고가 존재하지 않습니다.");
                return;
            }

            String status = rs.getString("ANNOUNCEMENT_STATUS");
            int cap = rs.getInt("ANNOUNCEMENT_RECRUIT_CAP");

            if (!"OPEN".equals(status)) {
                System.out.println("현재 OPEN 상태가 아닙니다.");
                return;
            }

            rs.close();
            pstmt.close();


            /*진행중(SELECTED) 과제 수 체크 */
            String countSql =
                "SELECT COUNT(*) FROM APPLICATION " +
                "WHERE USER_ID = ? AND APPLICATION_STATUS_CD = 'SELECTED'";

            pstmt = conn.prepareStatement(countSql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            rs.next();

            int selectedCount = rs.getInt(1);

            if (selectedCount >= 5) {
                System.out.println("진행중 과제가 5개 이상입니다.");
                return;
            }

            rs.close();
            pstmt.close();


            /*신청 INSERT */
            String insertSql =
                "INSERT INTO APPLICATION (ANNOUNCEMENT_ANN_ID, USER_ID, APPLICATION_STATUS_CD) " +
                "VALUES (?, ?, 'OPEN')";

            pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, annId);
            pstmt.setString(2, userId);
            pstmt.executeUpdate();

            pstmt.close();


            /*현재 신청 수 체크 */
            String applyCountSql =
                "SELECT COUNT(*) FROM APPLICATION " +
                "WHERE ANNOUNCEMENT_ANN_ID = ? " +
                "AND APPLICATION_STATUS_CD = 'OPEN'";
            /*
             * 위에 sql문은 어플리케이션에서 같은아이디 조회후 그어플에 신청한
             * 팀을 조회후 카운팅하고 그수와 신청가능팀 수를 비교하는 sql문입니
             */

            pstmt = conn.prepareStatement(applyCountSql);
            pstmt.setInt(1, annId);
            rs = pstmt.executeQuery();
            rs.next();

            int applyCount = rs.getInt(1);

            rs.close();
            pstmt.close();


            /*정원 초과 시 공고 CLOSED */
            if (applyCount >= cap) {

                String updateSql =
                    "UPDATE ANNOUNCEMENT SET ANNOUNCEMENT_STATUS = 'CLOSED' " +
                    "WHERE ANNOUNCEMENT_ANN_ID = ?";

                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, annId);
                pstmt.executeUpdate();

                pstmt.close();
            }

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