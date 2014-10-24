package com.kenai.camera;


import com.kenai.function.message.XToast;
import com.kenai.function.mzstore.XMZyanzheng;
import com.kenai.function.setting.XSetting;
import com.kenai.function.state.XState;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import cc.kenai.common.ad.KenaiTuiguang;

public class KenaiActivity extends PreferenceActivity {
	Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		/**
		 * 适配4.0的主题
		 */
		if (XState.get_issdk14())
			setTheme(16974123);
		context = getBaseContext();
		super.onCreate(savedInstanceState);
		if(XState.get_is_need_first_reset(this)){
			XSetting.xset_string_int(this, "camerafloat_x", "150");
			XSetting.xset_string_int(this, "camerafloat_y", "100");
			XSetting.xset_string_int(this, "cameracontrol_y", "600");
			XSetting.xset_string_int(this, "cameracontrol_x", "150");
			XSetting.xset_string_int(this, "camerafloat_size", "300");
		}
		if(XState.get_isfirst(this)){
			onCreateDialog(1).show();
			mainhelp();
		}
		addPreferencesFromResource(R.xml.settings);
		
		load_button();

		

		// ///////////////////////////////////////////////ceshi测试ceshi测试ceshi测试ceshi测试ceshi测试ceshi测试ceshi测试ceshi测试/////////////////////////////////////////////////
		XState.xSetTestModel(true);// 是否测试模式
		XMZyanzheng.False = true;// 是否测试模式
		// ///////////////////////////////////////ceshi测试ceshi测试ceshi测试ceshi测试ceshi测试ceshi测试ceshi测试ceshi测试//v//////////////////////////////////////////////////////////

		if (!XMZyanzheng.get_isLawful_zhengshiban(context)) {
			XMZyanzheng.xBind(this);
			if (!XMZyanzheng.get_isLawful(context))
				XToast.xToast(context, "请点击下方评价购买,刷新证书");
		} else {

		} 

		/**
		 * 推广
		 */
		if (!XMZyanzheng.get_isLawful_zhengshiban(context)) {
			try {
				PackageInfo info = context.getPackageManager().getPackageInfo(
						context.getPackageName(), 0);

				int versionCode = info.versionCode;

				if (XSetting.xget_int(context, "kenai_check_add_fuzhu") != versionCode) {
					new AlertDialog.Builder(this)
							.setIcon(R.drawable.ic_launcher)
							.setTitle("延长试用")
							.setMessage("支持全功能试用3天\n点击评价，说出您的评价还能获得额外试用哦！")
							.setPositiveButton("I know",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											XMZyanzheng
													.xcheck(KenaiActivity.this);
											if (TextUtils.equals(
													XMZyanzheng
															.get_isLawful_string(context),
													""))
												buy.setSummary("未验证权限");
											if (XMZyanzheng
													.get_isLawful_zhengshiban(context)) {
												buy.setTitle("评价作者");
												buy.setSummary(XMZyanzheng
														.get_isLawful_string(context));
											} else if (XMZyanzheng
													.get_isLawful(context)) {
												buy.setTitle("评价购买");
												buy.setSummary(XMZyanzheng
														.get_isLawful_string(context));
											} else {
												buy.setTitle("评价购买");
												buy.setSummary(XMZyanzheng
														.get_isLawful_string(context));

											}
										}
									}).create().show();
				}

			} catch (NameNotFoundException e) {

			}

		}
		
//		XSpeakR.speak(context, R.raw.move_to_up);

		
		
//		PushManager.startWork(this, PushConstants.LOGIN_TYPE_API_KEY, "gcjN5ihaVyN8xXIrPVQRQKHE");
	}

	static boolean isFirstONRESUME = true;

	@Override
	protected void onResume() {
		super.onResume();
		if (!isFirstONRESUME) {
			if (XMZyanzheng.xisHasBind()) {
				XMZyanzheng.xcheck(this);
			}
		} else {
			isFirstONRESUME = false;
		}
		if (TextUtils.equals(XMZyanzheng.get_isLawful_string(context), "")) {
			buy.setSummary("未验证权限");
		} else {
			if (XMZyanzheng.get_isLawful_zhengshiban(context)) {
				buy.setTitle("评价作者");
				buy.setSummary(XMZyanzheng.get_isLawful_string(context));
			} else if (XMZyanzheng.get_isLawful(context)) {
				buy.setTitle("评价购买");
				buy.setSummary(XMZyanzheng.get_isLawful_string(context));
			} else {
				buy.setTitle("评价购买");
				buy.setSummary(XMZyanzheng.get_isLawful_string(context));

			}
		}

	}

	@Override
	protected void onPause() {

		if (XMZyanzheng.xisHasBind()) {
			XMZyanzheng.xcheck(this);
		}
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		if (!XMZyanzheng.get_isLawful_zhengshiban(context)) {
			XMZyanzheng.xUnBind(this);
		}
		super.onDestroy();
	}

	
	
	
	protected void mainhelp() {
		final WindowManager wm = (WindowManager) getApplicationContext()
				.getSystemService("window");
		WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();
		wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		wmParams.format = PixelFormat.RGBA_8888;
		wmParams.flags =WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		DisplayMetrics metric = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metric);
		int width = metric.widthPixels;
		int height = metric.heightPixels;
		wmParams.width = width;
		wmParams.height = height;
		final ImageView my = new ImageView(this);
		my.setBackgroundColor(Color.WHITE);
		my.setBackgroundResource(R.drawable.help);
		wm.addView(my, wmParams);
		final OnClickListener l = new OnClickListener() {
			int n=0;
			public void onClick(View v) {
				if(n>=1){
				wm.removeView(my);
				}else{
					n++;
					my.setBackgroundResource(R.drawable.help2);
				}
			}
		};
		my.setOnClickListener(l);

	}
	
	PreferenceScreen buy;

	
	
	private void load_button() {
        //载入推广按钮事件
        findPreference("tuiguang").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                KenaiTuiguang.show(getBaseContext());
                return true;
            }
        });
		PreferenceScreen tuijian_dream = (PreferenceScreen) findPreference("tuijian_dream");
		tuijian_dream
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {

						startActivity(new Intent(
								"android.intent.action.VIEW",
								Uri.parse("mstore:http://app.meizu.com/phone/apps/dba0d8630ada406dbab446434568bd32")));
						return true;
					}
				});
		PreferenceScreen statebarCamera = (PreferenceScreen) findPreference("statebarCamera");
		statebarCamera
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					int n = 0;

					public boolean onPreferenceClick(Preference preference) {
						if (n % 2 == 0) {
							startService(new Intent(KenaiActivity.this,
									StatebarService.class));
						} else {
							stopService(new Intent(KenaiActivity.this,
									StatebarService.class));
						}
						n++;
						return true;
					}

				});
		PreferenceScreen cameraService_start = (PreferenceScreen) findPreference("cameraService_start");
		cameraService_start
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(context, KenaiService.class);
						startService(intent);
						return true;
					}

				});
		PreferenceScreen cameraService_stop = (PreferenceScreen) findPreference("cameraService_stop");
		cameraService_stop
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(context, KenaiService.class);
						stopService(intent);
						// sendBroadcast(new Intent("com.kenai.camera.face"));
						return true;
					}

				});
		PreferenceScreen cameraTime = (PreferenceScreen) findPreference("cameraTime");
		cameraTime
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						Intent intent = new Intent(context, CameraTime.class);
						startService(intent);
						return true;
					}

				});
		PreferenceScreen cameraHelp = (PreferenceScreen) findPreference("cameraHelp");
		cameraHelp
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						mainhelp();
						return true;
					}

				});
		PreferenceScreen cameraCare = (PreferenceScreen) findPreference("cameraCare");
		cameraCare
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						onCreateDialog(3).show();
						return true;
					}

				});
		PreferenceScreen call = (PreferenceScreen) findPreference("call");
		call.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Uri emailUri = Uri.parse("mailto:lx_yjq@qq.com");
				Intent returnIt = new Intent(Intent.ACTION_SENDTO, emailUri);
				startActivity(returnIt);

				return false;
			}

		});
		buy = (PreferenceScreen) findPreference("buy");
		buy.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				if (XMZyanzheng.get_isLawful_zhengshiban(context)) {
					buy.setTitle("评价作者");
					buy.setSummary("正式版本");
				} else if (XMZyanzheng.get_isLawful(context)) {
					buy.setTitle("评价购买");
					buy.setSummary(XMZyanzheng.get_isLawful_string(context));
				} else {
					buy.setTitle("评价购买");
					buy.setSummary(XMZyanzheng.get_isLawful_string(context));

				}
				return true;
			}
		});
		buy.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {

				startActivity(new Intent(
						"android.intent.action.VIEW",
						Uri.parse("mstore:http://app.meizu.com/phone/apps/246d7a4fb9b24b2fb868c096ad37404a")));
				if (!XMZyanzheng.get_isLawful_string(context).equals("")
						&& !XMZyanzheng.get_isLawful_string(context).equals(
								"无对应的证书")
						&& !XMZyanzheng.get_isLawful_string(context).equals(
								"证书无效"))
					XMZyanzheng.kenai_check_add(context);
				return true;
			}

		});
		PreferenceScreen Esc = (PreferenceScreen) findPreference("Esc");
		Esc.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(context, KenaiService.class);
				stopService(intent);
				finish();
				return true;
			}

		});

	}



	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 2:
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle("使用帮助")
					.setMessage(context.getString(R.string.help))
					.setPositiveButton("完毕",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		case 3:
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle("注意事项")
					.setMessage(context.getString(R.string.care))
					.setPositiveButton("完毕",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		case 1:
		default:
			return new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle("免责声明")
					.setMessage(context.getString(R.string.load))
					.setPositiveButton("完毕",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).create();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

	}
}
