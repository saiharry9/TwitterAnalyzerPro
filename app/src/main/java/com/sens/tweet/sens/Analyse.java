package com.sens.tweet.sens;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;




/*
 * (Really simple-dumb) Sentiment analysis for a lucene index of 1 million Tweets!
 * Based on http://jeffreybreen.wordpress.com/2011/07/04/twitter-text-mining-r-slides/
 *
 */

public class Analyse {

    public Twitter twitter;
    public QueryResult result;
    String input[] ;
    int c=0;
    public static String key_word;
    public static String positive_tweets="";
    public static String negative_tweets="";
    public static String neutral_tweets="";

    public TextView sens_text;
    public TextView tweet_text;
    public int comments;
    public int positive;
    public int negative;
    public int neutral;
    public ProgressBar scroll;
    public static Thread thread;
    public Boolean allow;
    public static int positive_count=0;
    public static int negative_count=0;
    public static int neutral_count=0;



    // used to store positive and negative words for scoring
    static List<String> posWords = new ArrayList<String>();
    static List<String> negWords = new ArrayList<String>();

    // keep some stats! [-1 / 0 / 1 / not english / foursquare / no text to
    // classify]
    static int[] stats = new int[6];
    private Activity activity;


    public Analyse(Activity activity,String key_word_) {
        this.activity = activity;
        this.key_word=key_word_;

        String consumerkey = "Zd1Hf3IoXJEdpb2msHT2FaY5j";
        String consumersecretkey = "SG6srJRkq1iQEkED7guP8pNMCk4GHhn2ke0Z6CvHbGw1JiglLZ";

        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(consumerkey);
        builder.setOAuthConsumerSecret(consumersecretkey);
        Configuration configuration = builder.build();
        TwitterFactory factory = new TwitterFactory(configuration);
        twitter = factory.getInstance();
        String accessToken = "599905252-eZXmszh8LognYTvEG3FJPlrWOs1OQ9lYtFCAvFiH"; /* enter your twitter access token */
        String accessTokenSecret = "LG69ZlXTHp2yx8CUqeyMGAbkOpXttOtMkNAhcDmwxxILQ"; //enter your twitter secret token
        AccessToken oathAccessToken = new AccessToken(accessToken, accessTokenSecret);
        twitter.setOAuthAccessToken(oathAccessToken);


        try {
            analyseSentiment();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TwitterException e) {
            e.printStackTrace();
        }

    }

    public void analyseSentiment() throws IOException, TwitterException {


        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    allow=true;
                    //Your code goes here
                    Query query = new Query(key_word);
                    QueryResult result = twitter.search(query);
                    input=new String[result.getTweets().size()];

                    for (Status status : result.getTweets()) {
                        input[c] = "@" + status.getUser().getScreenName() + ":" + status.getText();
                        c++;
                        if (c==result.getTweets().size()) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i=0;i<c;i++) {
                                        try {
                                            getSentiment(input[i]);
                                            if (positive > negative) {
                                                positive_count++;
                                                positive_tweets = positive_tweets + input[i] + "\n\n\n\n";
                                            }  else if (positive==negative){
                                                neutral_count++;
                                                neutral_tweets = neutral_tweets + input[i] + "\n\n\n\n";
                                            }
                                            else if (positive < negative) {
                                                negative_count++;
                                                negative_tweets = negative_tweets + input[i] + "\n\n\n\n";
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    negative_count=Math.abs(c-positive_count-neutral_count);
                                    Log.i("positive",positive_count+":"+positive_tweets);
                                    Log.i("negative",negative_count+":"+negative_tweets);
                                    Log.i("neutral",neutral_count+":"+neutral_tweets+"null");
                                    Listener lis=(Listener)activity;
                                    lis.onComplpete();



                                }
                            });

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();







    }
    public int getSentiment(String input) throws IOException {


        // source: www.cs.uic.edu/~liub/FBS/sentiment-analysis.html
        BufferedReader negReader = new BufferedReader(new InputStreamReader(activity.getAssets().open("negative-words.txt")));
        BufferedReader posReader = new BufferedReader(new InputStreamReader(activity.getAssets().open("positive-words.txt")));

        // currently read word
        String word;

        // add words to comparison list
        while ((word = negReader.readLine()) != null) {
            negWords.add(word);
        }
        while ((word = posReader.readLine()) != null) {
            posWords.add(word);
        }

        // cleanup
        negReader.close();
        posReader.close();

        //System.out.println("FINISH: reading file list");


        String[] a = input.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");

  /*  for(int i=0;i<a.length;i++){
        System.out.println(a[i]);
    }*/
        int posCounter=0;
        int negCounter=0;
        for(int i=0;i<a.length;i++){
            for(int j=0;j<posWords.size();j++){



                if(a[i].equals(posWords.get(j))){
                    posCounter++;

                }

            }
            for(int k=0;k<negWords.size();k++){
                if(a[i].equals(negWords.get(k))){
                    negCounter++;
                }
            }
        }

        positive=posCounter;
        negative=negCounter;
        neutral=comments-posCounter-negCounter;
        System.out.println("pos"+posCounter);
        System.out.println("neg"+negCounter);


        if(posCounter>negCounter){

            System.out.println("positive");
            return 1;
        }
        else if(negCounter>posCounter){
            System.out.println("negative");
            return -1;
        }
        else{
            System.out.println("neutral");
            return 0;
        }
    }

}
