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



public class MemberDAO {
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
	

	
	//아이디 찾기
	public void findUserId() {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String sql = null;
		
		try {
			System.out.println();
			System.out.println("[아이디 찾기]");
			System.out.print("이름 : ");
			String userName = br.readLine();
			System.out.print("이메일 : ");
			String userEmail = br.readLine();
			
			conn = DBUtil.getConnection();
			sql = "SELECT user_id FROM userInfo WHERE user_name = ? AND user_email = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userName);
			pstmt.setString(2, userEmail);
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				String foundId = rs.getString("user_id");
				System.out.println("회원님의 아이디는 [" + foundId + "] 입니다.");
			}else {
				System.out.println("일치하는 회원 정보가 없습니다.");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			DBUtil.executeClose(rs, pstmt, conn);
		}
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
	
	//회원가입 메서드(권한 선택 버전)
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
	
	//평가목록 화면 메서드
	public void callReviewerMenu(String myCust_id, String myRole, String myField) {
		cust_id = myCust_id; //UserMain에서 가져온 사용자 ID를 MemberDAO에 있는 cust_id로 삽입
		role = myRole;	//UserMain에서 가져온 사용자의 권한을 role에 삽입
		field = myField;
		System.out.println("전달된 cust_id = [" + cust_id + "]");
		while(true) {
			System.out.println("┌────────────────────────────────────────────────────────┐");
			System.out.println("│							 │");
			System.out.println("│	국가 연구과제 관리 프로그램	「KRD Hubs」		 │");
			System.out.println("│							 │");
			System.out.println("│	1. 평가배정목록조회					 │");
			System.out.println("│	2. 평가기록조회					 │");
			System.out.println("│	3. 내정보						 │");
			System.out.println("│	4. 로그아웃					 │");
			System.out.println("│	5. 종료						 │");
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
					//평가기록조회
				}else if(rev_choose == 3) {
					//내정보
				}else if(rev_choose == 4) {
					//로그아웃
					return;
				}else if(rev_choose == 5) {
					System.out.println("프로그램 종료");
					System.exit(0);
				}
			}
			catch(Exception e) {e.printStackTrace();}
			//finally {if(br != null)try{br.close();}catch(IOException e) {}}
		}
	}
}
