package com.sens.tweet.sens;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import static android.R.id.input;
import static com.sens.tweet.sens.Analyse.negWords;
import static com.sens.tweet.sens.Analyse.posWords;
import static com.sens.tweet.sens.R.id.scroll;
import static com.sens.tweet.sens.R.id.sens_text;
import static com.sens.tweet.sens.R.id.tweet_text;
import static com.sens.tweet.sens.StartupActivity.thread;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link sentimentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link sentimentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class sentimentFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public Twitter twitter;
    public QueryResult result;
    String input = "";
    int c=0;

    public static String key_word="";
    public EditText key_edit;
    public Button go;
    public TextView sens_text;
    public TextView tweet_text;
    public int comments;
    public int positive;
    public int negative;
    public int neutral;
    public ProgressBar scroll;
    public static Thread thread;
    public Boolean allow;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public sentimentFragment() {
        // Required empty public constructor
    }


    public static sentimentFragment newInstance(String param1, String param2) {
        sentimentFragment fragment = new sentimentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sentiment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tweet_text=(TextView)view.findViewById(R.id.tweet_text);
        sens_text=(TextView)view.findViewById(R.id.sens_text);
        scroll=(ProgressBar) view.findViewById(R.id.scroll);

        String consumerkey ="Zd1Hf3IoXJEdpb2msHT2FaY5j";
        String consumersecretkey="SG6srJRkq1iQEkED7guP8pNMCk4GHhn2ke0Z6CvHbGw1JiglLZ";

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

        Bundle bundle=getArguments();
        String keyword=bundle.getString("key");
        try {
            analyseSentiment(keyword);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TwitterException e) {
            e.printStackTrace();
        }


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
           // throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public int getSentiment(String input) throws IOException {


        // source: www.cs.uic.edu/~liub/FBS/sentiment-analysis.html
        BufferedReader negReader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("negative-words.txt")));
        BufferedReader posReader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("positive-words.txt")));

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


    public void sentimentAnalyse(String key_word_){


        this.key_word=key_word_;

        try  {
            allow=true;
            //Your code goes here
            Query query = new Query(key_word);
            QueryResult result = twitter.search(query);
            for (Status status : result.getTweets()) {
                input = input +"@" + status.getUser().getScreenName() + ":" + status.getText()+"\n\n\n\n";
                c++;
                if (c==result.getTweets().size()){

                    comments=c;

                    try {
                        getSentiment(input);
                        sens_text.setText("Comments : "+comments+" Positive : "+positive+" Negative "+negative);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    tweet_text.setText(input);
                    scroll.setVisibility(View.INVISIBLE);




                    Log.i("input",input);
                    Log.i("sens",getSentiment(input)+"");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public void analyseSentiment(String key_word_) throws IOException, TwitterException {



        this.key_word=key_word_;
        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    allow=true;
                    //Your code goes here
                    Query query = new Query(key_word);
                    QueryResult result = twitter.search(query);
                    for (Status status : result.getTweets()) {
                        input = input +"@" + status.getUser().getScreenName() + ":" + status.getText()+"\n\n\n\n";
                        c++;
                        if (c==result.getTweets().size()){

                            comments=c;
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        getSentiment(input);
                                        sens_text.setText("Comments : "+comments+" Positive : "+positive+" Negative "+negative);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    tweet_text.setText(input);
                                    scroll.setVisibility(View.INVISIBLE);
                                    StartupActivity.progressBar.setVisibility(View.INVISIBLE);
                                }
                            });


                            Log.i("input",input);
                            Log.i("sens",getSentiment(input)+"");
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();







    }
}
