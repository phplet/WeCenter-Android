package org.iflab.wc.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.iflab.wc.adapter.FollowAdapter;
import org.iflab.wc.app.WecenterApi;
import org.iflab.wc.common.GlobalVariables;
import org.iflab.wc.model.FollowModel;
import org.iflab.wc.topic.imageload.ImageDownLoader;
import org.iflab.wc.topic.imageload.ImageDownLoader.onImageLoaderListener;
import org.iflab.wc.userinfo.UserInfoShowActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.iflab.wc.R;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FollowFrament extends Fragment {
	private String uid;
	private boolean isFirstEnter;
	private int mFirstVisibleItem;
	private int mVisibleItemCount;
	private int totalItem;
	private List<FollowModel> attentionUserModels;
	private int currentPage = 1;
	private ImageDownLoader imageDownLoader;
	private FollowAdapter adapter;
	private ListView listView;
	private int per_page = 10;
	private int all_pages = 1;
	private LinearLayout footerLinearLayout;
	private TextView footText;
	private String url;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.attention_listview, container, false);
		Intent intent = getActivity().getIntent();
		String UserOrMe = intent.getStringExtra("userorme");
		uid = intent.getStringExtra("uid");
		if (UserOrMe.equals(GlobalVariables.ATTENEION_ME)) {
			url = WecenterApi.ATTENTIONME;
		}else {
			url = WecenterApi.ATTENTIONUSER;
		}
		attentionUserModels = new ArrayList<FollowModel>();
		imageDownLoader = new ImageDownLoader(getActivity());
		footerLinearLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.next_page_footer,null);
		footText = (TextView)footerLinearLayout.findViewById(R.id.footer_text);
		isFirstEnter = true;
		init(view);
		getInformation("1");
		return view;
	}

	private void init(View view) {
		// TODO Auto-generated method stub
		listView = (ListView)view.findViewById(R.id.attention_user);
		listView.addFooterView(footerLinearLayout,"",false);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
					Intent intent = new Intent(getActivity(),UserInfoShowActivity.class);
					intent.putExtra("uid", attentionUserModels.get(position).getUid());
					intent.putExtra("status", GlobalVariables.DISAVAILABLE_EDIT);
					startActivity(intent);
			}
		});
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE&&(mFirstVisibleItem + mVisibleItemCount == totalItem)) {
					if (currentPage<=all_pages) {
						getInformation(String.valueOf(currentPage));
					}else {
						footText.setText("没有更多数据了");
						//footerLinearLayout.setVisibility(View.GONE);
						//listView.removeFooterView(footerLinearLayout);
					}
				}
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
					showImage(mFirstVisibleItem, mVisibleItemCount);
				} else {
					cancleTask();
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				mFirstVisibleItem = firstVisibleItem;
				mVisibleItemCount = visibleItemCount;
				totalItem = totalItemCount;
				if (isFirstEnter && visibleItemCount > 0) {
					showImage(mFirstVisibleItem, mVisibleItemCount);
					isFirstEnter = false;
				}
			}
		});
	}

	private void showImage(int firstVisibleItem, int visibleItemCount) {
		for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount
				- 1; i++) {
			String mImageUrl = attentionUserModels.get(i).getUserImageUrl();
			//System.out.println(mImageUrl);
			if (!mImageUrl.equals("") ) {
				mImageUrl =WecenterApi.IMAGE_BASE_URL+mImageUrl;
				//System.err.println(mImageUrl);
				final ImageView mImageView = (ImageView) listView
						.findViewWithTag(mImageUrl);
				imageDownLoader.getBitmap(mImageUrl, new onImageLoaderListener() {

					public void onImageLoader(Bitmap bitmap, String url) {
						//System.out.println(bitmap+")(");
						if (mImageView != null && bitmap != null) {
							mImageView.setImageBitmap(bitmap);
						}
					}
				});
			}else {
				continue;
			}
		}
	}
		
	private void getInformation(String page) {
		// TODO Auto-generated method stub
		RequestParams params = new RequestParams();
		AsyncHttpClient client = new AsyncHttpClient();
		params.put("uid", uid);
		params.put("page", page);
		params.put("perpage", String.valueOf(per_page));
		client.get(url, params,
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						// TODO Auto-generated method stub
						try {
							JSONObject jsonObject = new JSONObject(new String(
									arg2));
							int errno = jsonObject.getInt("errno");
							if (errno == -1) {
								String err = jsonObject.getString("err");
								Toast.makeText(getActivity(), err,
										Toast.LENGTH_SHORT).show();
							} else {
								String rsm = jsonObject.getString("rsm");
								JSONObject jsonObject2 = new JSONObject(rsm);
								String total_rows = jsonObject2.getString("total_rows");
								if (currentPage == 1) {
									int totalPages = Integer.parseInt(total_rows);
									int a = totalPages%per_page;
									if (a == 0) {
										all_pages = totalPages/per_page;
									}else {
										all_pages = totalPages/per_page+1;
										//System.out.println(allPages);
									}
								}
								String rows = jsonObject2.getString("rows");
								JSONArray jsonArray = new JSONArray(rows);
								for (int i = 0; i < jsonArray.length(); i++) {
									FollowModel userModel = new FollowModel();
									JSONObject jsonObject3 = jsonArray.getJSONObject(i);
									String uid = jsonObject3.getString("uid");
									String user_name = jsonObject3.getString("user_name");
									String avatar_file = jsonObject3.getString("avatar_file");
									String signature = jsonObject3.getString("signature");
									userModel.setUid(uid);
									userModel.setUserName(user_name);
									userModel.setSingnature(signature);
									userModel.setUserImageUrl(avatar_file);
									attentionUserModels.add(userModel);
								}
							}
							if (currentPage == 1) {
								adapter = new FollowAdapter(getActivity(), imageDownLoader, attentionUserModels);
								listView.setAdapter(adapter);
								if (attentionUserModels.size() < per_page) {
									footText.setText("没有更多数据了！");
								}
								currentPage ++;
							}else {
								adapter.notifyDataSetChanged();
								currentPage ++;
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub

					}
				});
	}
	public void cancleTask(){
		imageDownLoader.cacelTask();
	}
}