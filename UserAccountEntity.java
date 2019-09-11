package cn.jiuzhuang.modules.sys.entity;

import cn.jiuzhuang.common.exception.JzException;
import cn.jiuzhuang.common.utils.Constant;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会员账户
 * 
 * @author Sing
 * @email h_j_xiao@foxmail.com
 * @date 2019-04-26 13:45:52
 */
@Data
@TableName("tb_user_account")
public class UserAccountEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * ID
	 */
	@TableId
	private Long id;
	/**
	 * 用户ID
	 */
	private Long userId;
	/**
	 * 用户角色(1-会员，2-分享经销商，3-区域代理商)
	 */
	private Integer userType;
	/**
	 * 推荐二维码
	 */
	private String inviteQrCode;
	/**
	 * 姓名
	 */
	private String name;
	/**
	 * 手机号码
	 */
	private String phone;
	/**
	 * 省
	 */
	private String province;
	/**
	 * 市
	 */
	private String city;
	/**
	 * 区
	 */
	private String area;
	/**
	 * 详细地址
	 */
	private String address;
	/**
	 * 返点总额
	 */
	private Long totalCommissionCash;
	/**
	 * 已提现金额
	 */
	private Long totalMentionCash;
	/**
	 * 校验串
	 */
	private String checkString;
	/**
	 * 
	 */
	private Date updateTime;
	/**
	 * 积分
	 */
	private Long integral;

	public UserAccountEntity() {
		super();
	}

	public UserAccountEntity(Long userId, String nickName, String inviteQrCode) {
		super();
		this.userId = userId;
		this.name = nickName;
		this.userType = Constant.UserType.VIP.getValue();
		this.inviteQrCode = inviteQrCode;
		this.totalCommissionCash = 0L;
		this.totalMentionCash = 0L;
		this.updateTime = new Date();
		this.integral = 0L;
	}

	public void review(UserDistributorApplicationEntity form, Date nowTime) {
		if (form.getType() == 1) {
			this.userType = Constant.UserType.DISTRIBUTOR.getValue();
		} else if (form.getType() == 2) {
			this.userType = Constant.UserType.AGENT.getValue();
		} else {
			throw new JzException("用户类型错误，请检查申请内容！！！");
		}
		this.name = form.getName();
		this.phone = form.getPhone();
		this.province = form.getProvince();
		this.city = form.getCity();
		this.area = form.getArea();
		this.address = form.getAddress();
		this.updateTime = nowTime;
	}

	public UserAccountEntity(Long id, String inviteQrCode) {
		super();
		this.id = id;
		this.inviteQrCode = inviteQrCode;
	}
}
