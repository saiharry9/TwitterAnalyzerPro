package com.sens.tweet.sens;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import static android.R.attr.fragment;
import static android.R.attr.key;
import static android.R.attr.visibility;
import static android.os.Build.VERSION_CODES.N;
import static com.sens.tweet.sens.Analyse.negWords;
import static com.sens.tweet.sens.Analyse.posWords;


public class StartupActivity extends AppCompatActivity implements Listener {

    public Twitter twitter;

    public EditText key_edit;
    public Button go;
    public static Thread thread;
    public Boolean destroy;
    public static ProgressBar progressBar;

    private Toolbar toolbar;
    public TabLayout tabLayout;
    public TextView guide;
    private ViewPager viewPager;
    private Analyse analyse;
    int red1,blue1,green1,color1,red2,green2,blue2,color2;
    // Tab titles
    private String[] tabs = { "Positive", "Negative", "Neutral" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        destroy=false;
        key_edit=(EditText)findViewById(R.id.key_edit);
        go=(Button) findViewById(R.id.go_btn);
        progressBar=(ProgressBar)findViewById(R.id.progress_bar);
        guide=(TextView) findViewById(R.id.guideline);

        guide.setText("Search for a keyword to see how everyone are reacting.");

        /*
        // Generate color1 before starting the thread
        red1 = (int)(Math.random() * 128 + 127);
         green1 = (int)(Math.random() * 128 + 127);
         blue1 = (int)(Math.random() * 128 + 127);
         color1 = 0xff << 24 | (red1 << 16) |
                (green1 << 8) | blue1;


        new Thread() {
            public void run() {
                while(true) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {

                            //generate color 2

                            red2 = (int)(Math.random() * 128 + 127);
                            green2 = (int)(Math.random() * 128 + 127);
                            blue2 = (int)(Math.random() * 128 + 127);
                            color2 = 0xff << 24 | (red2 << 16) |
                                    (green2 << 8) | blue2;

                            //start animation
                            View v = findViewById(R.id.guideline);
                            ObjectAnimator anim = ObjectAnimator.ofInt(v, "backgroundColor", color1, color2);


                            anim.setEvaluator(new ArgbEvaluator());
                            anim.setRepeatCount(ValueAnimator.INFINITE);
                            anim.setRepeatMode(ValueAnimator.REVERSE);
                            anim.setDuration(10000);
                            anim.start();

                            // Now set color1 to color2
                            // This way, the background will go from
                            // the previous color to the next color
                            // smoothly
                            color1 = color2;

                        }
                    });
                }
            }
        }.start();

*/

        toolbar = (Toolbar) findViewById(R.id.toolbar);
       // setSupportActionBar(toolbar);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager = (ViewPager) findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        //setupViewPager(viewPager,adapter);

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        //tabLayout.setupWithViewPager(viewPager);





/*
        String consumerkey ="Zd1Hf3IoXJEdpb2msHT2FaY5j";
        String consumersecretkey="SG6srJRkq1iQEkED7guP8pNMCk4GHhn2ke0Z6CvHbGw1JiglLZ";

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(consumerkey);
        builder.setOAuthConsumerSecret(consumersecretkey);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
        String accessToken = "599905252-eZXmszh8LognYTvEG3FJPlrWOs1OQ9lYtFCAvFiH";
        String accessTokenSecret = "LG69ZlXTHp2yx8CUqeyMGAbkOpXttOtMkNAhcDmwxxILQ"; //enter your twitter secret token
        AccessToken oathAccessToken = new AccessToken(accessToken, accessTokenSecret);
        twitter.setOAuthAccessToken(oathAccessToken);
*/


        key_edit.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                go.setVisibility(View.VISIBLE);
                return false;
            }
        });


        key_edit.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()!=KeyEvent.ACTION_DOWN)
                    return false;
                if(keyCode == KeyEvent.KEYCODE_ENTER ){

                    progressBar.setVisibility(View.VISIBLE);
                    go.setVisibility(View.INVISIBLE);
                    guide.setVisibility(View.INVISIBLE);
                    tabLayout.setupWithViewPager(null);

                        setupViewPager(viewPager,null);



                  /*  if (destroy){
                        getFragmentManager().beginTransaction().remove(sentimentFragment).commit();
                    }
                    sentimentFragment=new sentimentFragment();
                    Bundle bundle=new Bundle();
                    bundle.putString("key",key_edit.getText().toString());
                    sentimentFragment.setArguments(bundle);
                    getFragmentManager().beginTransaction().replace(R.id.frame,sentimentFragment).commit();
                    destroy=true;*/

                    Analyse.neutral_count=0;
                    Analyse.positive_count=0;
                    Analyse.neutral_count=0;

                    Analyse.negative_tweets="";
                    Analyse.positive_tweets="";
                    Analyse.neutral_tweets="";





                     analyse=new Analyse(StartupActivity.this,key_edit.getText().toString());

                    return true;
                }
                return false;
            }
        });

        go.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
        key_edit.setText("");
        //go.setVisibility(View.INVISIBLE);
    }
});

    }

    private void setupViewPager(ViewPager viewPager,ViewPagerAdapter adapter) {
        if (adapter!=null) {
            adapter.addFragment(new PositiveFragment(), "POSITIVE");
            adapter.addFragment(new NegativeFragment(), "NEGATIVE");
            adapter.addFragment(new NeutralFragment(), "NEUTRAL");
        }
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onComplpete() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        setupViewPager(viewPager,adapter);
        tabLayout.setupWithViewPager(viewPager);
        progressBar.setVisibility(View.INVISIBLE);
        analyse=null;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}



