package kr.krd.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

import kr.util.DBUtil;



public class CMY_MemberDAOOrigin {
	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	String cust_id;						//사용자 아이디
	String role;						//사용자 권한
	String field;						//사용자 분야
	LocalDate today = LocalDate.now();	//현재날짜를 저장하는 변수
	LocalDate deadline;					//데드라인의 값을 저장할 변수



	//로그인 메서드
	public String userLogin(String user_id, String user_pw) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_id = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT * FROM USERINFO WHERE USER_ID = ? AND USER_PWD = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			pstmt.setString(2, user_pw);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				String user_name = rs.getString("USER_NAME");
				System.out.println(user_name + "님 환영합니다.");
				real_id = rs.getString("user_id");	//일치하면 real_id에 유저 아이디를 넣는다

			}else {
				System.out.println("아이디 혹은 비밀번호가 일치 하지 않습니다.");
				real_id = "0";	//틀리면 "0"을 넣는다.

			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_id;
	}

	//회원가입 메서드(일반회원 버전)
	public void insertMember() {

		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		//비밀번호 검사 정규식 : 특수문자1개 포함하여 8자 이상
		String passwordRegex =
				"^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";

		try {
			//회원가입 내용 표시
			System.out.println("회원가입을 진행합니다.");
			System.out.println("=".repeat(50));
			System.out.print("ID:_________________");
			String user_id = br.readLine();
			String user_pw;
			while(true) {
				System.out.println("※비밀번호 규칙 : 특수문자1개포함, 8자 이상");
				System.out.print("Password:_________________");
				user_pw = br.readLine();	
				if(user_pw.matches(passwordRegex)) {
					break;
				}else {
					System.out.println("비밀번호 규칙에 위배되었습니다.");
				}
			}
			System.out.print("Name:_________________");
			String user_name = br.readLine();
			System.out.print("e-mail:_________________");
			String user_email = br.readLine();
			String user_birthDate = null;
			while(true) {
				System.out.println("BirthDate:_________________");
				System.out.println("ex)1998-03-13");
				System.out.print(">>");
				user_birthDate = br.readLine();
				if(user_birthDate.length() != 10) {
					System.out.println("생년월일을 예시에 맞춰서 입력해주세요");
					continue;
				}
				break;
			}
			String user_phoneNo=null;
			while(true) {
				System.out.println("PhoneNumber:_________________");
				System.out.println("ex)01012345678");
				System.out.print(">>");
				user_phoneNo = br.readLine();
				if(user_phoneNo.length() != 11) {
					System.out.println("전화번호를 예시에 맞게 입력해주세요");
					continue;
				}
				break;
			}
			int your_Country;
			while(true) {
				System.out.println("		Country");
				System.out.println("[1] 한국 [2] 미국 [3] 일본 [4] 중국");
				System.out.println("[5] 대만 [6] 캐나다 [7] 영국");
				System.out.print("Choose one >>");
				
				try {
					your_Country = Integer.parseInt(br.readLine());
					if(your_Country > 7 || your_Country < 1) {
						System.out.println("1~7 사이의 숫자를 입력해주세요!");
						System.out.println();
						continue;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력하세요");
				}

			}
			System.out.print("Address:_________________");
			String user_addr = br.readLine();
			int your_Gender;
			while(true) {
				System.out.println("		Gender");
				System.out.println("	[1] male [2] female");
				System.out.println(">>");
				
				try {
					your_Gender = Integer.parseInt(br.readLine());
					if(your_Gender > 2 || your_Gender < 1) {
						System.out.println("1 or 2를 입력해주세요");
						System.out.println();
						continue;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력해주세요");
				}
			}
			int your_affiliation;
			while(true) {
				System.out.println("		Choose your Affiliation");
				System.out.println("[1] DGIST [2] GIST [3] KAIST [4] POSTECH [5] UNIST");
				System.out.println("[6] 고려대학교 [7] 과학기술정보통신부 [8] 교육부 [9] 국토교통부");
				System.out.println("[10] 농림축산식품부 [11] 문화체육관광부 [12] 보건복지부 [13] 산업통상자원부");
				System.out.println("[14] 서울대학교 [15] 성균관대학교 [16] 연세대학교 [17] 중소벤처기업부");
				System.out.println("[18] 한국전자통신연구원 [19] 한양대학교 [20] 해양수산부 [21] 환경부 [22] 없음");
				System.out.print(">>");
				try {
					your_affiliation = Integer.parseInt(br.readLine());
					if(your_affiliation > 22 || your_affiliation < 1) {
						System.out.println("1 ~ 21 사이 숫자를 입력 해주세요");
						System.out.println();
						continue;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력 해주세요");
				}


			}
			int your_field;
			while(true) {
				System.out.println("		Choose your Field");
				System.out.println("[1]의료 [2] 바이오 [3] 로보틱스 [4] 데이터 [5] 기획");
				System.out.println("[6] 국방 [7] 교통 [8] 감사 [9] AI [10] 환경 [11] 협약");
				System.out.println("[12] 평가 [13] 제도 [14] 정책 [15] 정산 [16] 전기");
				System.out.println("[17] 예산 [18] 역사 [19] 에너지 [20] 소재 [21] 반도체");
				System.out.println("[22] 운영 [23] 성과");
				System.out.print(">>");

				try {
					your_field = Integer.parseInt(br.readLine());
					if(your_field > 23 || your_field < 1) {
						System.out.println("1~ 23 사이의 숫자를 입력 해주세요.");
						System.out.println();
						continue;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력 해주세요");
				}
			}

			//JDBC 수행 1,2단계
			conn = DBUtil.getConnection();
			//SQL 문 작성
			sql = "INSERT INTO USERINFO (user_id, user_pwd, user_name, user_email, "
					+ "user_birth_dt, user_phone_no, user_country_cd, user_addr, " +
					"user_gender_cd, user_role_cd, "
					+ "user_acct_status_cd, user_affiliation, user_field) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?)";


			//JDBC 수행 3단계
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id); //ID
			pstmt.setString(2, user_pw); //PW
			pstmt.setString(3, user_name); //Name
			pstmt.setString(4, user_email); //Email
			pstmt.setString(5, user_birthDate); //Birth
			pstmt.setString(6, user_phoneNo); //Phone

			//국가 대입 메서드
			pstmt.setString(7, getCountry(your_Country)); //Country
			pstmt.setString(8, user_addr); //Address

			if(your_Gender == 1) {
				pstmt.setInt(9, 1); //Gender : male
			}else if(your_Gender == 2) {
				pstmt.setInt(9, 2);//Gender : female
			}

			pstmt.setString(10, "GST"); //Role : 일반 회원
			pstmt.setString(11, "ACTIVE"); //Status : ACTIVE
			pstmt.setString(12, getAffiliation(your_affiliation)); //Affiliation

			//분야 대입 메서드
			pstmt.setString(13, getField(your_field)); //Field


			System.out.println();
			System.out.println();
			//JDBC 수행 4단계
			int count = pstmt.executeUpdate();
			System.out.println("=".repeat(30));
			System.out.println(count+"개의 회원 정보를 저장했습니다.");	

		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

	//국가 대입 메서드
	public String getCountry(int no) {
		String country = null;
		switch (no) {
		case 1:
			country = "KOREA";
			break;
		case 2:
			country = "USA";
			break;
		case 3:
			country = "JAPAN";
			break;
		case 4:
			country = "CHINA";
			break;

		case 5:
			country = "TAIWAN";
			break;
		case 6:
			country = "CANADA";
			break;
		case 7:
			country = "ENGLAND";
			break;
		}

		return country;
	}

	//필드 분야 대입 메서드
	public String getField(int no) {
		String field = null;

		switch(no) {
		case 1:
			field = "의료";
			break;
		case 2:
			field = "바이오";
			break;
		case 3:
			field = "로보틱스";
			break;
		case 4:
			field = "데이터";
			break;
		case 5:
			field = "기획";
			break;
		case 6:
			field = "국방";
			break;
		case 7:
			field = "교통";
			break;
		case 8:
			field = "감사";
			break;
		case 9:
			field = "AI";
			break;
		case 10:
			field = "환경";
			break;
		case 11:
			field = "협약";
			break;
		case 12:
			field = "평가";
			break;
		case 13:
			field = "제도";
			break;
		case 14:
			field = "정책";
			break;
		case 15:
			field = "정산";
			break;
		case 16:
			field = "전기";
			break;
		case 17:
			field = "예산";
			break;
		case 18:
			field = "역사";
			break;
		case 19:
			field = "에너지";
			break;
		case 20:
			field = "소재";
			break;
		case 21:
			field = "반도체";
			break;
		case 22:
			field = "운영";
			break;
		case 23:
			field = "성과";
			break;

		}

		return field;
	}

	public String getAffiliation(int no) {
		String affiliation = null;

		switch(no) {
		case 1:
			affiliation = "DGIST";
			break;
		case 2:
			affiliation = "GIST";
			break;
		case 3:
			affiliation = "KAIST";
			break;
		case 4:
			affiliation = "POSTECH";
			break;
		case 5:
			affiliation = "UNIST";
			break;
		case 6:
			affiliation = "고려대학교";
			break;
		case 7:
			affiliation = "과학기술정보통신부";
			break;
		case 8:
			affiliation = "교육부";
			break;
		case 9:
			affiliation = "국토교통부";
			break;
		case 10:
			affiliation = "농림축산식품부";
			break;
		case 11:
			affiliation = "문화체육관광부";
			break;
		case 12:
			affiliation = "보건복지부";
			break;
		case 13:
			affiliation = "산업통상자원부";
			break;
		case 14:
			affiliation = "서울대학교";
			break;
		case 15:
			affiliation = "성균관대학교";
			break;
		case 16:
			affiliation = "연세대학교";
			break;
		case 17:
			affiliation = "중소벤처기업부";
			break;
		case 18:
			affiliation = "한국전자통신연구원";
			break;
		case 19:
			affiliation = "한양대학교";
			break;
		case 20:
			affiliation = "해양수산부";
			break;
		case 21:
			affiliation = "환경부";
			break;
		case 22:
			affiliation = "무소속";
			break;
		}

		return affiliation;
	}

	//비밀번호 재설정
	public void resetPassword() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		//비밀번호 검사 정규식 
		String passwordRegex =
				"^(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$";
		
		try {
			System.out.println();
			System.out.println("[비밀번호 재설정]");
			System.out.print("아이디 : ");
			String userId = br.readLine();
			System.out.print("이름 : ");
			String userName = br.readLine();
			System.out.print("이메일 : ");
			String userEmail = br.readLine();
			
			conn = DBUtil.getConnection();
			sql = "SELECT user_id FROM userInfo WHERE user_id = ? AND user_name = ? AND user_email = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userId);
			pstmt.setString(2, userName);
			pstmt.setString(3, userEmail);
			
			rs = pstmt.executeQuery();
			
			if(!rs.next()) {
				System.out.println("일치하는 회원 정보가 없습니다.");
				return;
			}
			
			rs.close();
			pstmt.close();
			
			String newPw;
			while(true) {
				System.out.println("※ 새 비밀번호 규칙 : 특수문자 1개 포함, 8자 이상");
				System.out.print("새 비밀번호 : ");
				newPw = br.readLine();
				
				if(!newPw.matches(passwordRegex)) {
					System.out.println("비밀번호 규칙에 맞지 않습니다.");
					continue;
				}
				
				System.out.print("새 비밀번호 확인 : ");
				String confirmPw = br.readLine();
				
				if(!newPw.equals(confirmPw)) {
					System.out.println("비밀번호 확인이 일치하지 않습니다.");
					continue;
				}
				break;
			}
			
			sql = "UPDATE userInfo SET user_pwd = ? WHERE user_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newPw);
			pstmt.setString(2, userId);
			
			int count = pstmt.executeUpdate();
			
			if(count == 1) {
				System.out.println("비밀번호가 성공적으로 변경되었습니다.");
			}else {
				System.out.println("비밀번호 변경에 실패했습니다.");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	//사용자의 권한을 반환하는 메서드
	public String getUserRole(String user_id) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_role = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_role_cd FROM USERINFO WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				real_role = rs.getString("user_role_cd");	//일치하면 real_id에 유저 아이디를 넣는다
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_role;
	}

	//사용자의 분야를 반환하는 메서드
	public String getUserField(String user_id) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		String real_field = null;

		try {
			conn = DBUtil.getConnection();
			sql = "SELECT user_field FROM USERINFO WHERE USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				real_field = rs.getString("user_field");	//일치하면 real_id에 유저 아이디를 넣는다
			}
		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return real_field;
	}


	//평가 배정 목록 조회
	//평가 테이블에 행이 있어야 조회 함
	public void readEval() {
		while(true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			String sql = null;
			ResultSet rs = null;
			//DB 행값을 담을 변수를 선언
			int eval_id=0;				//평가번호
			int eval_app_id=0	;	//신청번호
			String eval_rev_id="";	//평가위원ID
			String eval_field="";			//평가분야
			Date eval_assigned_dt =null;	//평가배정일
			int eval_score=0;	
			String eval_comment="";			//평가 코멘트//평가 점수
			String eval_status="";		//평가 상태

			Date ann_start_dt =null;		//공고(평가)시작일
			Date ann_end_dt =null;			//공고(평가)마감일
			Date eval_deadline = null;		//평가마감일
			String ann_title="";			//공고명
			String ann_desc="";			//공고 설명
			String ann_budget="";	//총 예산

			int app_budget_amt=0;		//신청자의 요구 예산
			String app_user_id="";		//신청자 아이디
			String app_attach="";	//신청자 첨부파일

			String ag_name="";		//기관명

			try {
				conn = DBUtil.getConnection();
				sql = "SELECT  e.EVALUATION_ID,\r\n"
						+ "		e.EVALUATION_APPLICATION_ID,\r\n"
						+ "		e.EVALUATION_REVIEWER_ID,\r\n"
						+ "		e.EVALUATION_FIELD,\r\n"
						+ "		e.EVALUATION_ASSIGNED_at,\r\n"
						+ "		e.EVALUATION_SCORE,\r\n"
						+ "		e.EVALUATION_STATUS_CD,\r\n"
						+ "		e.EVALUATION_IS,\r\n"
						+ "		e.EVALUATION_DEADLINE_AT,\r\n"
						+ "		a.APPLICATION_ID,\r\n"
						+ "		a.APPLICATION_BUDGET_AMT,\r\n"
						+ "		a.APPLICATION_USER_ID,\r\n"
						+ "        a.APPLICATION_ATTACH_PATH,\r\n"
						+ "        ag.AGENCY_AGY_NAME,\r\n"
						+ "		an.ANNOUNCEMENT_START_DT,\r\n"
						+ "        an.ANNOUNCEMENT_END_DT,\r\n"
						+ "		an.ANNOUNCEMENT_TITLE,\r\n"
						+ "		an.ANNOUNCEMENT_DESC,\r\n"
						+ "        an.ANNOUNCEMENT_TOTAL_BUDGET\r\n"
						+ "FROM EVALUATIONS e\r\n"
						+ "JOIN APPLICATIONS a \r\n"
						+ "ON e.EVALUATION_APPLICATION_ID = a.APPLICATION_ID\r\n"
						+ "JOIN ANNOUNCEMENT an\r\n"
						+ "ON a.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ann_ID\r\n"
						+ "JOIN AGENCY ag\r\n"
						+ "ON an.ANNOUNCEMENT_AGY_ID = ag.AGENCY_AGY_ID\r\n"
						+ "WHERE e.EVALUATION_REVIEWER_ID = ?";
				pstmt = conn.prepareStatement(sql);

				pstmt.setString(1, cust_id);
				//pstmt.setString(2, field);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					//내용들 대충
					System.out.println("");
					System.out.print("평가번호\t");
					System.out.print("과제명\t\t\t");
					System.out.print("신청자\t");
					System.out.print("기관명\t\t");
					System.out.print("분야\t");
					System.out.print("평가 상태\t\t");
					System.out.print("평가 마감일\t");
					System.out.print("연구 수행 기간\n");
					System.out.println("=".repeat(100));
					do {
						eval_id = rs.getInt("EVALUATION_ID");						//평가번호
						eval_app_id = rs.getInt("EVALUATION_APPLICATION_ID");		//신청번호
						eval_rev_id = rs.getString("EVALUATION_REVIEWER_ID");	//평가위원ID
						eval_field = rs.getString("EVALUATION_FIELD");			//평가분야
						eval_assigned_dt = rs.getDate("EVALUATION_ASSIGNED_AT");	//평가배정일
						eval_score = rs.getInt("EVALUATION_SCORE");	
						eval_comment = rs.getString("EVALUATION_IS");			//평가 코멘트//평가 점수
						eval_status = rs.getString("EVALUATION_STATUS_CD");		//평가 상태
						eval_deadline = rs.getDate("EVALUATION_DEADLINE_AT");	//평가마감일

						ann_start_dt = rs.getDate("ANNOUNCEMENT_START_DT");		//공고(평가)시작일
						ann_end_dt = rs.getDate("ANNOUNCEMENT_END_DT");			//공고(평가)마감일			
						ann_title = rs.getString("ANNOUNCEMENT_TITLE");			//공고명
						ann_desc = rs.getString("ANNOUNCEMENT_DESC");			//공고 설명
						ann_budget = rs.getString("ANNOUNCEMENT_TOTAL_BUDGET");	//총 예산

						app_budget_amt = rs.getInt("APPLICATION_BUDGET_AMT");		//신청자의 요구 예산
						app_user_id = rs.getString("APPLICATION_USER_ID");		//신청자 아이디
						app_attach = rs.getString("APPLICATION_ATTACH_PATH");	//신청자 첨부파일

						ag_name = rs.getString("AGENCY_AGY_NAME");				//기관명

						if(eval_comment == null) {
							eval_comment = "";
						}
						System.out.print(eval_id+"\t");
						System.out.print(ann_title+"\t");
						System.out.print(app_user_id+"\t");
						System.out.print(ag_name+"\t\t");
						System.out.print(eval_field+"\t");
						System.out.print(eval_status+"\t");
						System.out.print(eval_deadline+"\t");
						System.out.print(ann_start_dt+"-"+ann_end_dt+"\n");
					}
					while(rs.next());
					System.out.println();
					System.out.println();
					System.out.println("[1]상세보기");
					System.out.println("[2]나가기");
					int eval_no = Integer.parseInt(br.readLine());
					if(eval_no == 1) {
						System.out.println();
						System.out.println("상세 보기할 대상의 번호를 입력 하세요.");
						String do_eval_no = br.readLine();
						//상세 보기 메서드
						viewAppDetail(do_eval_no);
					}else if(eval_no == 2) {
						//
						System.out.println("목록에서 메뉴로");
						break;
					}
				}else {
					System.out.println("┌───────────────────────────────┐");
					System.out.println("│평가할 대상 목록이 없습니다.		│");
					System.out.println("└───────────────────────────────┘");
					return;
				}
				System.out.println();
				System.out.println();

			}catch(NumberFormatException e) {
				System.out.println("숫자를 입력하세요");
			}
			catch(Exception e){e.printStackTrace();}
			finally {DBUtil.executeClose(rs, pstmt, conn);}
		}//end of while

	}

	//상세 조회 메서드 (평가 테이블에서 보고 싶은 값을 인자로 받아온다.)
	public void viewAppDetail(String eval_no) 
	{
		mother:while(true) {
			Connection conn = null;
			PreparedStatement pstmt = null;
			String sql = null;
			ResultSet rs = null;
			//DB 행값을 담을 변수를 선언
			int eval_id=0;				//평가번호
			int eval_app_id=0	;	//신청번호
			String eval_rev_id="";	//평가위원ID
			String eval_field="";			//평가분야
			Date eval_assigned_dt =null;	//평가배정일
			int eval_score=0;	
			String eval_comment="";			//평가 코멘트//평가 점수
			String eval_status="";		//평가 상태

			Date ann_start_dt =null;		//공고(평가)시작일
			Date ann_end_dt =null;			//공고(평가)마감일
			Date eval_deadline = null;		//평가마감일
			String ann_title="";			//공고명
			String ann_desc="";			//공고 설명
			String ann_budget="";	//총 예산
			String ann_field="";

			int app_budget_amt=0;		//신청자의 요구 예산
			String app_user_id="";		//신청자 아이디
			String app_attach="";	//신청자 첨부파일

			String ag_name="";		//기관명

			try {
				conn = DBUtil.getConnection();
				sql = "SELECT  e.EVALUATION_ID,\r\n"
						+ "		e.EVALUATION_APPLICATION_ID,\r\n"
						+ "		e.EVALUATION_REVIEWER_ID,\r\n"
						+ "		e.EVALUATION_FIELD,\r\n"
						+ "		e.EVALUATION_ASSIGNED_at,\r\n"
						+ "		e.EVALUATION_SCORE,\r\n"
						+ "		e.EVALUATION_STATUS_CD,\r\n"
						+ "		e.EVALUATION_IS,\r\n"
						+ "		e.EVALUATION_DEADLINE_AT,\r\n"
						+ "		a.APPLICATION_ID,\r\n"
						+ "		a.APPLICATION_BUDGET_AMT,\r\n"
						+ "		a.APPLICATION_USER_ID,\r\n"
						+ "     a.APPLICATION_ATTACH_PATH,\r\n"
						+ "     ag.AGENCY_AGY_NAME,\r\n"
						+ "		an.ANNOUNCEMENT_START_DT,\r\n"
						+ "     an.ANNOUNCEMENT_END_DT,\r\n"
						+ "		an.ANNOUNCEMENT_TITLE,\r\n"
						+ "		an.ANNOUNCEMENT_FIELD,\r\n"
						+ "		an.ANNOUNCEMENT_DESC,\r\n"
						+ "     an.ANNOUNCEMENT_TOTAL_BUDGET\r\n"
						+ "FROM EVALUATIONS e\r\n"
						+ "JOIN APPLICATIONS a \r\n"
						+ "ON e.EVALUATION_APPLICATION_ID = a.APPLICATION_ID\r\n"
						+ "JOIN ANNOUNCEMENT an\r\n"
						+ "ON a.APPLICATION_ANN_ID = an.ANNOUNCEMENT_ann_ID\r\n"
						+ "JOIN AGENCY ag\r\n"
						+ "ON an.ANNOUNCEMENT_AGY_ID = ag.AGENCY_AGY_ID\r\n"
						+ "WHERE e.EVALUATION_ID = ? AND e.EVALUATION_REVIEWER_ID = ?"
						+ "AND an.ANNOUNCEMENT_HIDDEN_YN = 0";	//히든 여부를 나타내는 where절 부분
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, eval_no);
				pstmt.setString(2, cust_id);
				rs = pstmt.executeQuery();
				if(rs.next()) {
					//내용들 대충

					eval_id = rs.getInt("EVALUATION_ID");						//평가번호
					eval_app_id = rs.getInt("EVALUATION_APPLICATION_ID");		//신청번호
					eval_rev_id = rs.getString("EVALUATION_REVIEWER_ID");	//평가위원ID
					eval_field = rs.getString("EVALUATION_FIELD");			//평가분야
					eval_assigned_dt = rs.getDate("EVALUATION_ASSIGNED_AT");	//평가배정일
					eval_score = rs.getInt("EVALUATION_SCORE");	
					eval_comment = rs.getString("EVALUATION_IS");			//평가 코멘트//평가 점수
					eval_status = rs.getString("EVALUATION_STATUS_CD");		//평가 상태
					eval_deadline = rs.getDate("EVALUATION_DEADLINE_AT");	//평가마감일
					deadline = eval_deadline.toLocalDate();

					ann_start_dt = rs.getDate("ANNOUNCEMENT_START_DT");		//공고(평가)시작일
					ann_end_dt = rs.getDate("ANNOUNCEMENT_END_DT");			//공고(평가)마감일			
					ann_title = rs.getString("ANNOUNCEMENT_TITLE");			//공고명
					ann_desc = rs.getString("ANNOUNCEMENT_DESC");			//공고 설명
					ann_budget = rs.getString("ANNOUNCEMENT_TOTAL_BUDGET");	//총 예산
					ann_field = rs.getString("ANNOUNCEMENT_FIELD");			//신청 분야

					app_budget_amt = rs.getInt("APPLICATION_BUDGET_AMT");		//신청자의 요구 예산
					app_user_id = rs.getString("APPLICATION_USER_ID");		//신청자 아이디
					app_attach = rs.getString("APPLICATION_ATTACH_PATH");	//신청자 첨부파일

					ag_name = rs.getString("AGENCY_AGY_NAME");				//기관명

					if(eval_comment == null) {
						eval_comment = "";
					}
					System.out.println("");
					System.out.println("=".repeat(50));
					System.out.print("과제명 : "+ ann_title +"\n");
					System.out.print("신청자 ID : " + app_user_id + "\n");
					
					System.out.print("기관명 : "+ ag_name +"\t\t\t");
					System.out.printf("신청예산 : %,d원\n",app_budget_amt);
					System.out.print("신청 분야 : "+ ann_field +"\n");
					System.out.print("평가 상태 : "+ eval_status +"\n");
					System.out.print("평가 마감일 : " + deadline + "\n");
					System.out.print("평가 점수 : "+ eval_score +" \n");
					System.out.println("-".repeat(70));
					System.out.println("	평가 의견");
					int commentLeng = eval_comment.length();
					int lengstartcnt = 0;
					for(int i = 0; i < eval_comment.length(); i += 40) {
					    int end = Math.min(i + 40, eval_comment.length());
					    System.out.println(eval_comment.substring(i, end));
					}
					System.out.println("=".repeat(50));
				}else {
					System.out.println("┌────────────────────────────────────┐");
					System.out.println("│본인의 평가목록에 존재하는 번호를 입력하세요! │");
					System.out.println("└────────────────────────────────────┘");
					System.out.println();
					return;
				}
				//오늘날짜가 데드라인 이후 이면
				if(today.isAfter(deadline)) {
					System.out.println();
					System.out.println();
					System.out.println("[평가가 마감되었습니다.]");
					System.out.println("[2]나가기");
					int do_eval_no = Integer.parseInt(br.readLine());
					if(do_eval_no == 2) {
						System.out.println();
						return;
					}else {
						System.out.println("숫자 2만 입력해주세요!");
					}
				}

				System.out.println();
				System.out.println();
				System.out.println("[1]평가하기");
				System.out.println("[2]나가기");
				int do_eval_no = Integer.parseInt(br.readLine());
				if(do_eval_no == 1) {



					//재평가 여부를 검사하는 메서드
					//해당하는 평가번호,스테이터스,마감일을 검사하고
					//결과 값이 있으면 true를 반환하는 메서드다
					//반환 값이 true 값이면 재평가여부를 묻고
					//재평가하면 평가하기 메서드를 실행
					//아니면 브레이크
					//false 값이면 그대로 아래 임시평가 체크 메서드를 진행함
					if(reEval(eval_id)) {
						System.out.println("이미 해당 과제를 평가하였습니다. 재평가를 진행하시겠습니까?");
						System.out.println("[1]재평가하기 [2]돌아가기");
						int choose_no = Integer.parseInt(br.readLine());
						if(choose_no == 1) {
							//재평가대상을 위한 전용 평가 메서드
							//해당 메서드에서는 평가작성후 나갈 시 임시저장 여부를 묻지 않는다.
							reSubmiteval(eval_id);

							return;
						}else if(choose_no == 2) {
							break;
						}
					}

					//임시 평가 체크 메서드
					boolean checkYN = checkTempEval(eval_id);
					//checkTempEval메서드에서 임시평가를 사용하면 checkYN에 True값 리턴
					//임시평가 값이 존재하지 않거나 임시평가를 사용하지 않으면 false로 리턴함
					System.out.println(checkYN + ":값");
					if(!checkYN) {
						//평가하기
						submiteval(eval_id);
						return;
					}else {
						//임시평가의 값을 사용하여 평가테이블에 그대로 저장하는 메서드
						useSaveEval(eval_id);
						return;
					}
					//평가 메서드
				}else if(do_eval_no == 2) {
					//
					System.out.println();
					return;

				}
			}
			catch(Exception e){e.printStackTrace();}
			finally {DBUtil.executeClose(rs, pstmt, conn);}	
		}//end of while

	}
	//기본 평가하기 메서드
	//해당 평가를 작성하기 위해 필요한 평가 번호를 가져온다.
	public void submiteval(int eval_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		int score = 0;
		String comment = null;

		try {
			//점수, 평가 부분
			score = checkScore("점수 :"); //점수가 0~100 으로만 입력하게 해주는 메서드
			System.out.println("코멘트 작성");
			comment = br.readLine();

			//DB연결 부분
			conn = DBUtil.getConnection();
			sql = "update evaluations set EVALUATION_SCORE = ?, EVALUATION_IS = ?, EVALUATION_STATUS_CD = ? where EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, score);			//점수 저장
			pstmt.setString(2, comment);	//평가코멘트 저장
			pstmt.setInt(4, eval_id);		//업데이트에 사용할 평가 번호 삽입
			System.out.println();
			System.out.println();

			mother:while(true) {
				System.out.println("[1]평가제출");
				System.out.println("[2]나가기");
				int save_eval_no = Integer.parseInt(br.readLine());
				if(save_eval_no == 1) {
					String submit = "SUBMITTED";
					pstmt.setString(3, submit);
					int cnt = pstmt.executeUpdate();
					System.out.println(cnt + "건의 평가를 작성 완료 하였습니다");
					evalCount(cust_id);
					break;
				}else if(save_eval_no == 2) {
					//나가기를 했을 때 작성한 평가에 대해 임시저장 여부를 묻는다.
					while(true) {
						System.out.println("작성한 평가를 임시저장 하시겠습니까?");
						System.out.print("[1] 저장한다.  [2] 나가기 >>");
						int saveTemp = Integer.parseInt(br.readLine());
						if(saveTemp == 1) {
							//임시저장하는 메서드, 평가번호,점수,코멘트를 인자로 받아들인다.
							saveTempEval(eval_id,score,comment);
							break mother;
						}
						if(saveTemp == 2) {
							//그냥 나감(평가상세로 나가기)
							System.out.println("이전으로 돌아갑니다.");
							return;
						}
						else {
							System.out.println("1 혹은 2를 입력 해주세요.");
						}
					}

				}else {
					System.out.println("1 혹은 2를 입력 해주세요");
				}
			}

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}	
	}

	//해당 메서드 역시 동일하게 평가 제출 메서드지만
	//재평가 대상을 위한 전용 메서드로 만약 점수와 코멘트를 입력하고
	//제출하지 않고 나갈시 임시저장 여부를 묻지 않고 바로 종료 한다.
	public void reSubmiteval(int eval_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		int score = 0;
		String comment = null;

		try {
			//점수, 평가 부분
			score = checkScore("점수 :"); //점수가 0~100 으로만 입력하게 해주는 메서드
			System.out.println("코멘트 작성");
			comment = br.readLine();

			//DB연결 부분
			conn = DBUtil.getConnection();
			sql = "update evaluations set EVALUATION_SCORE = ?, EVALUATION_IS = ?, EVALUATION_STATUS_CD = ? where EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, score);			//점수 저장
			pstmt.setString(2, comment);	//평가코멘트 저장
			pstmt.setInt(4, eval_id);		//업데이트에 사용할 평가 번호 삽입
			System.out.println();
			System.out.println();

			while(true) {
				System.out.println("[1]평가제출");
				System.out.println("[2]나가기");
				int save_eval_no = Integer.parseInt(br.readLine());
				if(save_eval_no == 1) {
					String submit = "SUBMITTED";
					pstmt.setString(3, submit);
					int cnt = pstmt.executeUpdate();
					System.out.println(cnt + "건의 평가를 작성 완료 하였습니다");
					break;
				}else if(save_eval_no == 2) {
					//그냥 나감(평가상세로 나가기)
					System.out.println("이전으로 돌아갑니다.");
					return;
				}else {
					System.out.println("1 혹은 2를 입력 해주세요");
				}
			}

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}	
	}


	//임시평가 내용을 그대로 사용하는 메서드
	public void useSaveEval(int eval_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		int temp_eval_no = 0;
		int temp_eval_eval_no = 0;
		int temp_eval_score = 0;
		String temp_eval_comment = null;

		try {

			//DB연결 부분
			conn = DBUtil.getConnection();
			//임시저장 테이블에서
			sql = "select * from TEMP_EVAL where TEMP_EVAL_EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, eval_id);		
			rs = pstmt.executeQuery();

			//임시평가 값을 가져옴
			if(rs.next()) {
				temp_eval_no = rs.getInt("TEMP_EVAL_ID");
				temp_eval_eval_no = rs.getInt("TEMP_EVAL_EVALUATION_ID");
				temp_eval_score = rs.getInt("TEMP_EVAL_SCORE");
				temp_eval_comment = rs.getString("TEMP_EVAL_IS");
			}else {
				System.out.println("sql구문 오류일듯?");
				return;
			}
			pstmt.close();
			sql = "update EVALUATIONS set EVALUATION_SCORE=?,EVALUATION_IS=?,EVALUATION_STATUS_CD=?"
					+ " where EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, temp_eval_score);
			pstmt.setString(2, temp_eval_comment);
			pstmt.setString(3,"SUBMITTED");
			pstmt.setInt(4, eval_id);

			int cnt = pstmt.executeUpdate();
			System.out.println(cnt+"개 임시저장한 값을 사용하여 평가에 반영하였습니다.");
			pstmt.close();
			sql="delete from temp_eval where temp_eval_evaluation_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, eval_id);
			pstmt.executeUpdate();	//평가에 반영후 임시저장에 있는 값은 삭제 된다.
			pstmt.close();
			
			evalCount(cust_id); //평가위원의 평가 카운트를 1 늘리는 메서드

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}	
	}
	
	public void evalCount(String rev_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		int revCount =0;
		
		try {
			conn = DBUtil.getConnection();
			sql = "select EVALUATOR_EVAL_CNT from evaluator where evaluator_reviewer_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, rev_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				revCount = rs.getInt("EVALUATOR_EVAL_CNT");
			}
			rs.close();
			pstmt.close();
			
			sql = "update EVALUATOR set EVALUATOR_EVAL_CNT = ? where EVALUATOR_REVIEWER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, ++revCount);
			pstmt.setString(2, rev_id);
			pstmt.executeUpdate();
		}
		catch(Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(null, pstmt, conn);
		}
	}

	//평가하기에서 점수를 올바르게 작성했는지 검사하는 메서드
	public int checkScore(String scoreInfo) {
		int score;
		while(true) {
			System.out.print(scoreInfo);	
			try {
				score = Integer.parseInt(br.readLine());
				if(score > 100 || score < 0) {// 점수가 100 초과 혹은 0 미만인 경우
					System.out.println("점수를 0~100점 사이로 입력해주세요!");
					continue;
				}else {
					break;
				}
			}
			catch(IOException e) {e.printStackTrace();}
			catch(NumberFormatException e) {System.out.println("숫자를 입력해주세요.");}
		}
		return score;
	}


	//재평가 체크 메서드
	public boolean reEval(int reEval_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		Boolean resultbn = false;


		try {
			conn = DBUtil.getConnection();
			//평가테이블을 검색하는데 해당하는 평가번호와, 제출현황이 submitted인거 그리고 현재 날짜가 데드라인을 넘겼는지 
			sql = "select * from EVALUATIONS WHERE EVALUATION_ID = ? and EVALUATION_STATUS_CD = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, reEval_id);
			pstmt.setString(2, "SUBMITTED"); //제출이 완료 되었는지
			rs = pstmt.executeQuery();

			if(rs.next()) {
				//
				resultbn = true; //값이 존재하면 (이미 평가를 하였고 아직 마감일 지나지 않았으니 true 대입)
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {DBUtil.executeClose(rs, pstmt, conn);}
		return resultbn;
	}



	//임시평가 체크 메서드
	public boolean checkTempEval(int eval_id) {

		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;
		ResultSet rs = null;
		boolean checkFlag = false;

		try {
			conn = DBUtil.getConnection();
			//sql은 해당 평가번호를 임시평가 테이블에 검색한다
			sql = "select * from EVALUATIONS WHERE EVALUATION_ID"
					+ "= ? and EVALUATION_STATUS_CD = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, eval_id);
			pstmt.setString(2, "SAVED"); //스테이터스가 SAVED 인지(즉 임시저장 상태인지 검사)
			rs = pstmt.executeQuery();

			if(rs.next()) {
				checkFlag = true;
			}
			while(checkFlag) {//true면 임시평가한 값이 있음 false면 없음
				//검색 결과가 있다는것 = 임시저장을 한 경우
				System.out.println("해당 과제에 대해 임시평가 기록이 존재합니다 해당 평가를 그대로 평가에 사용하시겠습니까?");
				System.out.println("[1] 사용한다.  [2]사용하지 않는다.(임시평가 내용이 지워집니다!)");
				System.out.print(">");
				int temp_choose = Integer.parseInt(br.readLine());
				if(temp_choose == 1) {
					break;
				}
				else if(temp_choose == 2) {
					//임시평가에 해당 행을 삭제하고 평가하기 메서드를 실행하는 메서드 작성
					rs.close();
					pstmt.close();

					sql = "delete from TEMP_EVAL where TEMP_EVAL_EVALUATION_ID = ?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setInt(1, eval_id);
					int delcnt = pstmt.executeUpdate();
					System.out.println(delcnt + "건의 임시평가를 제거 하였습니다.");
					System.out.println();
					checkFlag = false;
				}
				else {
					System.out.println("1 또는 2를 입력하여 주세요.");
					System.out.println();
				}

			}

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(rs, pstmt, conn);}	
		return checkFlag;
	}

	//임시저장 메서드
	//평가 제출중 제출하지 않고 나가기를 눌렀을 때
	//값을 임시저장하려는 경우 작동하는 메서드
	public void saveTempEval(int eval_id, int score, String comment) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		String sql = null;

		try {
			conn = DBUtil.getConnection();

			sql = "insert into temp_eval(TEMP_EVAL_ID,TEMP_EVAL_EVALUATION_ID,"
					+ "TEMP_EVAL_SCORE,TEMP_EVAL_IS) "
					+ "values(temp_eval_seq.nextval,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, eval_id);
			pstmt.setInt(2, score);
			pstmt.setString(3, comment);

			int cnt = pstmt.executeUpdate();
			System.out.println(cnt+"개의 평가데이터를 임시저장 하였습니다.");
			pstmt.close();
			sql = "update evaluations set EVALUATION_STATUS_CD = ? where EVALUATION_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "SAVED");
			pstmt.setInt(2, eval_id);
			cnt = pstmt.executeUpdate();
			System.out.println(cnt+"개의 평가데이터의 상태를 SAVED로 변경하였습니다.");

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}	
	}

	
	public void myInfo(String myCust_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		int choose_no;
		
		try {
			conn = DBUtil.getConnection();
			sql = "select u.user_id, u.user_name, u.user_email, u.user_birth_dt, \r\n"
					+ "u.user_addr, u.user_country_cd, u.user_gender_cd, u.user_affiliation, \r\n"
					+ "u.user_field, e.EVALUATOR_EVAL_CNT, u.user_created_at from userinfo u\r\n"
					+ "join evaluator e\r\n"
					+ "on u.user_id = e.EVALUATOR_REVIEWER_ID\r\n"
					+ "where u.user_id = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, myCust_id);
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				System.out.println();
				System.out.println("    		내 정보");
				System.out.println("-".repeat(40));
				System.out.println("ID : " + rs.getString("user_id"));
				System.out.println("이름 : " + rs.getString("user_name"));
				System.out.println("이메일 : " + rs.getString("user_email"));
				
				String birth = rs.getString("user_birth_dt");
				System.out.println("생년월일 : "
				+birth.substring(0, 4)+"년 "+birth.substring(4, 6)
				+ "월 " + birth.substring(6, 8) + "일");
				
				System.out.println("주소 : " + rs.getString("user_addr"));
				System.out.println("국적 : " + rs.getString("user_country_cd"));
				
				int gender = rs.getInt("user_gender_cd");
				if(gender == 1) {
					System.out.println("성별 : 남성");
				}else {
					System.out.println("성별 : 여성");
				}
				if(rs.getString("user_affiliation") == null) {
					System.out.println("소속 : 무소속");
				}
				System.out.println("소속 : " + rs.getString("user_affiliation"));
				System.out.println("분야 : " + rs.getString("user_field"));
				System.out.println("평가 횟수 : " + rs.getInt("evaluator_eval_cnt"));
				String myDate = rs.getString("user_created_at");
				System.out.println("계정 생성 날짜 : " + myDate.substring(0, 4) + "년 " +
						myDate.substring(5, 7) + "월 " + myDate.substring(8, 10) + 
						"일 " + myDate.substring(11, 13) + "시 " + 
						myDate.substring(14, 16) + "분 " + myDate.substring(17, 19) +
						"초");
				
				while(true) {
					System.out.println();
					System.out.println();
					System.out.println("[1] 비밀번호 변경");
					System.out.println("[2] 나가기");
					try {
						choose_no = Integer.parseInt(br.readLine());
						if(choose_no > 2 || choose_no < 1) {
							System.out.println("1 혹은 2를 입력 해주세요.");
							continue;
						}
						break;
					}
					catch(NumberFormatException e) {
						System.out.println("숫자를 입력 해주세요.");
					}
					
				}
				switch(choose_no) {
				case 1:
					resetPassword();
					break;
				
				case 2:
					return;
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
	}

	//평가목록 화면 메서드
	public boolean callReviewerMenu(String myCust_id, String myRole, String myField) {
		cust_id = myCust_id; //UserMain에서 가져온 사용자 ID를 MemberDAO에 있는 cust_id로 삽입
		role = myRole;	//UserMain에서 가져온 사용자의 권한을 role에 삽입
		field = myField;
		while(true) {
			System.out.println("┌────────────────────────────────────────────────────────┐");
			System.out.println("│							 │");
			System.out.println("│	국가 연구과제 관리 프로그램	「KRD Hubs」		 │");
			System.out.println("│							 │");
			System.out.println("│	1. 평가배정목록조회					 │");
			System.out.println("│	2. 내정보						 │");
			System.out.println("│	3. 로그아웃					 │");
			System.out.println("│	4. 종료						 │");
			System.out.println("│							 │");
			System.out.println("│							 │");
			System.out.println("│등급 : 평가위원					ver.1.0	 │");
			System.out.println("└────────────────────────────────────────────────────────┘");
			System.out.println("［원하시는 메뉴를 선택하세요 ]");
			System.out.print(">>");
			try {
				int rev_choose = Integer.parseInt(br.readLine());
				if(rev_choose == 1) {
					//평가배정목록조회
					readEval();
				}else if(rev_choose == 2) {
					myInfo(cust_id);
				}else if(rev_choose == 3) {
					//로그아웃
					return true;
				}else if(rev_choose == 4) {
					System.out.println("프로그램 종료");
					System.exit(0);
				}
			}catch(NumberFormatException e) {
				System.out.println("숫자를 입력하세요.");
			}
			catch(Exception e) {e.printStackTrace();}
			//finally {if(br != null)try{br.close();}catch(IOException e) {}}
		}
	}

	//일반회원 화면 메서드
	public void callGuestMenu(String myCust_id, String myRole, String myField) {
		cust_id = myCust_id; //UserMain에서 가져온 사용자 ID를 MemberDAO에 있는 cust_id로 삽입
		role = myRole;	//UserMain에서 가져온 사용자의 권한을 role에 삽입
		field = myField;
		System.out.println("전달된 cust_id = [" + cust_id + "]");
		while(true) {
			System.out.println("┌────────────────────────────────────────────────────────┐");
			System.out.println("│							 │");
			System.out.println("│	국가 연구과제 관리 프로그램	「KRD Hubs」		 │");
			System.out.println("│							 │");
			System.out.println("│	1. 공고조회					 │");
			System.out.println("│	2. 권한신청					 │");
			System.out.println("│	3. 내정보						 │");
			System.out.println("│	4. 로그아웃					 │");
			System.out.println("│	5. 종료						 │");
			System.out.println("│							 │");
			System.out.println("│등급 : 일반회원					ver.1.0	 │");
			System.out.println("└────────────────────────────────────────────────────────┘");
			System.out.println("［원하시는 메뉴를 선택하세요 ]");
			System.out.print(">>");
			try {
				int gst_choose = Integer.parseInt(br.readLine());
				if(gst_choose == 1) {
					//공고조회
					System.out.println("공고조회임");
				}else if(gst_choose == 2) {
					//권한신청
					applyRole(cust_id);
				}else if(gst_choose == 3) {
					//내정보
				}else if(gst_choose == 4) {
					//로그아웃
					return;
				}else if(gst_choose == 5) {
					System.out.println("프로그램 종료");
					System.exit(0);
				}
			}
			catch(Exception e) {e.printStackTrace();}
			//finally {if(br != null)try{br.close();}catch(IOException e) {}}
		}
	}
	//일반 회원 권한 신청
	public void applyRole(String user_id) {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		String role = null;
		
		try {
			conn = DBUtil.getConnection();
			sql = "select role_app_user_id from role_application"
					+ " where ROLE_APP_USER_ID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			rs = pstmt.executeQuery();
			if(rs.next()) {
				System.out.println("[이미 권한 신청이 진행되고 있습니다.]");
				System.out.println();
				return;	//만약에 해당 사용자의 권한 신청이 조회가 된다면 리턴함
			}
			rs.close();
			pstmt.close();
			
			while(true) {
				System.out.println();
				System.out.println("=".repeat(20));
				System.out.println("권한 신청을 진행합니다.");
				System.out.println("신청할 권한");
				System.out.println("[1] 연구자(개인) [2] 연구자(단체) [3] 기관 담당자 [4] 평가위원");
				try {
					int your_role = Integer.parseInt(br.readLine());
					if(your_role > 4 || your_role < 1) {
						System.out.println("제시된 권한을 보고 올바른 숫자를 입력해주세요.");
						continue;
					}
					switch(your_role) {
					case 1:
						role = "RESI";	//개인 연구원
						break;
					case 2:
						role = "RESO";	//단체 연구원
						break;
					case 3:
						role = "AGY";	//기관 담당자
						break;
					case 4:
						role = "REV";	//평가위원
						break;
					}
					break;
				}catch(NumberFormatException e) {
					System.out.println("숫자를 입력하세요.");
				}
			}
			System.out.println("	평가 사유[엔터를 누르지 않고 입력 해주세요]");
			String reason = br.readLine();
			
			sql = "insert into role_application("
					+ "ROLE_APP_ID,ROLE_APP_USER_ID,"
					+ "ROLE_APP_ROLE_CD, ROLE_APP_APPLY_REASON) "
					+ "values(role_app_seq.nextval,?,?,?)";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, user_id);
			pstmt.setString(2, role);
			pstmt.setString(3, reason);

			int cnt = pstmt.executeUpdate();
			if(cnt == 1) {
				System.out.println("권한신청이 완료 되었습니다");
			}
			

		}
		catch(Exception e){e.printStackTrace();}
		finally {DBUtil.executeClose(null, pstmt, conn);}
	}
	//내 정보 보기 메서드

	//로그아웃 메서드
	public boolean logout() {
		return false;
	}
}
