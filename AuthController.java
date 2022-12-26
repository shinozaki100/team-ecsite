package jp.co.internous.utopia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import jp.co.internous.utopia.model.domain.MstUser;
import jp.co.internous.utopia.model.form.UserForm;
import jp.co.internous.utopia.model.mapper.MstUserMapper;
import jp.co.internous.utopia.model.mapper.TblCartMapper;
import jp.co.internous.utopia.model.session.LoginSession;

@RestController
@RequestMapping("/utopia/auth")
public class AuthController {

	private Gson gson = new Gson();
	
	@Autowired
	private LoginSession loginSession;
	@Autowired
	private MstUserMapper userMapper;
	@Autowired
	private TblCartMapper cartMapper;

	@RequestMapping("/login")
	public String login(@RequestBody UserForm f) {
		MstUser user = userMapper.findByUserNameAndPassword(f.getUserName(), f.getPassword());
		
		int guestId = loginSession.getGuestId();
		
		if(user !=null && guestId != 0) {
			int count = cartMapper.findCountByUserId(guestId);
			if (count > 0) {
				cartMapper.updateUserId(user.getId(),guestId);
			}
		}
		
		if(user != null) {	
			loginSession.setId(user.getId());
			loginSession.setGuestId(0);
			loginSession.setUserName(user.getUserName());
			loginSession.setPassword(user.getPassword());
			loginSession.setLoginflg(true);		
		} else {			
			loginSession.setId(0);
			loginSession.setUserName(null);
			loginSession.setPassword(null);
			loginSession.setLoginflg(false);		
		}
		
		return gson.toJson(user);
	}
	
	@RequestMapping("/logout")
	public String logout() {
				
		loginSession.setId(0);
		loginSession.setGuestId(0);
		loginSession.setUserName(null);
		loginSession.setPassword(null);
		loginSession.setLoginflg(false);		
		
		return "";
	}

	@PostMapping("/resetPassword")
	public String resetpassword(@RequestBody UserForm f) {
		String newPassword = f.getNewPassword();
		String newPasswordConfirm = f.getNewPasswordConfirm();
		
		MstUser user = userMapper.findByUserNameAndPassword(f.getUserName(), f.getPassword());
		if (user == null) {
			return "現在のパスワードが正しくありません。";
		}
		if (user.getPassword().equals(newPassword)) {
			return "現在のパスワードと同一文字列が入力されました。";
		}
		if (!newPassword.equals(newPasswordConfirm)) {
			return "新パスワードと確認用パスワードが一致しません。";
		}

		userMapper.updatedPassword(user.getUserName(), f.getNewPassword());
		loginSession.setPassword(f.getNewPassword());
				
		return "パスワードが再設定されました。";
	}
}