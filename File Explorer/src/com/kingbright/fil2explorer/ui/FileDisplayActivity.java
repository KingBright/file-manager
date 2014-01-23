package com.kingbright.fil2explorer.ui;

import java.util.HashMap;
import java.util.Map;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.kingbright.fil2explorer.R;
import com.kingbright.fil2explorer.extention.Extentions;
import com.kingbright.fil2explorer.file.FileScanner;

public class FileDisplayActivity extends FragmentActivity implements
		ActionBar.TabListener {

	private SectionsPagerAdapter mSectionsPagerAdapter;

	private ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_file_display);

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						mSectionsPagerAdapter.getFragment(position)
								.onSelected();
					}
				});

		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		Extentions.getIntance(this).init();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings: {
			return true;
		}
		case R.id.menu_exit: {
			exit();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (!mSectionsPagerAdapter.getFragment(mViewPager.getCurrentItem())
				.onBackPressed()) {
			exit();
		}
	}

	public void exit() {
		FileScanner.clear();
		Extentions.clear();
		super.onBackPressed();
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private Map<Integer, BasicFragment> mFragmentMap = new HashMap<Integer, BasicFragment>();

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		public BasicFragment getFragment(int position) {
			BasicFragment fragment = mFragmentMap.get(position);
			return fragment;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0: {
				BasicFragment bf = new FileListFragment();
				mFragmentMap.put(position, bf);
				return bf;
			}
			case 1: {
				BasicFragment bf = new FavouriteFileFragment();
				mFragmentMap.put(position, bf);
				return bf;
			}
			case 2: {
				BasicFragment bf = new ClassifiedFileFragment();
				mFragmentMap.put(position, bf);
				return bf;
			}
			}
			return null;
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0: {
				return getString(R.string.title_section1).toUpperCase();
			}
			case 1: {
				return getString(R.string.title_section2).toUpperCase();
			}
			case 2: {
				return getString(R.string.title_section3).toUpperCase();
			}
			}
			return null;
		}
	}
}
