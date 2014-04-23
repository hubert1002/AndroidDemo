package cn.dong.demo.widget;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;
import cn.dong.demo.R;
import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.Platform.ShareParams;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.weibo.TencentWeibo;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

public class SharePage implements Callback, OnClickListener, PlatformActionListener {
	private static final String TAG = "Share";
	private static final int COMPLETE = 1;
	private static final int ORROR = 2;
	private static final int CANCEL = 3;

	private Handler mHandler;
	private Activity context;
	private boolean finishing;
	private boolean sharing;
	private PopupWindow popupWindow;
	private View parentView;
	private View popupView;
	private HashMap<String, Object> data;
	private PlatformActionListener callback;

	public SharePage(Activity context) {
		this.context = context;
		this.parentView = context.findViewById(android.R.id.content);
		init();
		initPopupWindow();
		initView();
	}

	public void show() {
		popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
	}

	/**
	 * text分享文本，支持全平台
	 */
	public void setText(String text) {
		data.put("text", text);
	}

	/**
	 * title标题，微信使用
	 */
	public void setTitle(String title) {
		data.put("title", title);
	}

	/**
	 * 设置自定义的外部回调
	 */
	public void setCallback(PlatformActionListener callback) {
		this.callback = callback;
	}

	private void init() {
		ShareSDK.initSDK(context);
		data = new HashMap<String, Object>();
		mHandler = new Handler(this);
		callback = this;
		finishing = false;
		sharing = false;
	}

	private void initPopupWindow() {
		popupView = LayoutInflater.from(context).inflate(R.layout.share_page_layout, null);
		popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT, true);
		popupWindow.setTouchable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
	}

	private void initView() {
		popupView.setOnClickListener(this);
		Button btn_cancel = (Button) popupView.findViewById(R.id.share_cancel);
		btn_cancel.setOnClickListener(this);
		View[] buttons = new View[6];
		buttons[0] = popupView.findViewById(R.id.share_sinaweibo);
		buttons[1] = popupView.findViewById(R.id.share_tencentweibo);
		buttons[2] = popupView.findViewById(R.id.share_qq);
		buttons[3] = popupView.findViewById(R.id.share_wechat);
		buttons[4] = popupView.findViewById(R.id.share_wechatmoments);
		buttons[5] = popupView.findViewById(R.id.share_email);
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setOnClickListener(this);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.share_cancel) {
			dismiss();
		} else if (v.getId() == R.id.share_main) {
			dismiss();
		} else {
			share(v.getId());
		}
	}

	private void share(int id) {
		// if (sharing) {
		// return;
		// }
		sharing = true;
		ShareParams sp = new ShareParams(data);
		Platform platform = null;
		switch (id) {
		case R.id.share_sinaweibo:
			platform = ShareSDK.getPlatform(context, SinaWeibo.NAME);
			break;
		case R.id.share_tencentweibo:
			platform = ShareSDK.getPlatform(context, TencentWeibo.NAME);
			break;
		case R.id.share_qq:
			platform = ShareSDK.getPlatform(context, QQ.NAME);
			if (!platform.isValid()) {
				Log.d(TAG, "qq isValid");
				Toast.makeText(context, R.string.qq_client_inavailable, Toast.LENGTH_SHORT).show();
				return;
			}
			break;
		case R.id.share_wechat:
			Log.d(TAG, "wechat onClick");
			platform = ShareSDK.getPlatform(context, Wechat.NAME);
			if (!platform.isValid()) {
				Log.d(TAG, "wechat isValid");
				Toast.makeText(context, R.string.wechat_client_inavailable, Toast.LENGTH_SHORT)
						.show();
				return;
			}
			break;
		case R.id.share_wechatmoments:
			platform = ShareSDK.getPlatform(context, WechatMoments.NAME);
			if (!platform.isValid()) {
				Toast.makeText(context, R.string.wechat_client_inavailable, Toast.LENGTH_SHORT)
						.show();
				return;
			}
			break;
		default:
			break;
		}
		platform.setPlatformActionListener(callback);
		platform.share(sp);
		sharing = false;
	}

	@Override
	public void onCancel(Platform platform, int action) {
		mHandler.sendEmptyMessage(CANCEL);
	}

	@Override
	public void onComplete(Platform platform, int action, HashMap<String, Object> res) {
		mHandler.sendEmptyMessage(COMPLETE);
	}

	@Override
	public void onError(Platform platform, int action, Throwable t) {
		mHandler.sendEmptyMessage(ORROR);
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case COMPLETE:
			Toast.makeText(context, R.string.share_complete, Toast.LENGTH_SHORT).show();
			break;
		case ORROR:
			Toast.makeText(context, R.string.share_error, Toast.LENGTH_SHORT).show();
			break;
		case CANCEL:

			break;
		}
		dismiss();
		return true;
	}

	public void dismiss() {
		if (finishing) {
			return;
		}
		finishing = true;
		popupWindow.dismiss();
		finishing = false;
	}
}