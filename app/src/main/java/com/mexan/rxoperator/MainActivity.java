package com.mexan.rxoperator;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @InjectView(R.id.username)
    EditText mUsername;

    @InjectView(R.id.password)
    EditText mPassword;

    @InjectView(R.id.btn_login)
    Button mBtnLogin;

    @InjectView(R.id.txt_status)
    TextView mStatus;

    @InjectView(R.id.prog_login)
    ProgressBar mProfLogin;

    private static final String USERNAME = "admin";
    private static final String PASSWORD = "admin";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        prepareLogin();
    }


    private void prepareLogin() {

        Observable<Boolean> usernameNotEmptyObservable = WidgetObservable.text(mUsername)
           .distinctUntilChanged()
           .map(onTextChangeEvent -> !TextUtils.isEmpty(onTextChangeEvent.text()));


        Observable<Boolean> passwordNotEmpty = WidgetObservable.text(mPassword)
           .distinctUntilChanged()
           .map(onTextChangeEvent -> !TextUtils.isEmpty(onTextChangeEvent.text()));

        Observable.combineLatest(usernameNotEmptyObservable, passwordNotEmpty,
           (aBoolean, aBoolean2) -> aBoolean && aBoolean2)
           .flatMap(aBoolean -> {

               if (aBoolean) {
                   showProgBar();
                   return checkIfNameCorrect(mUsername.getText().toString())
                           .distinctUntilChanged()
                           .doOnCompleted(() -> {
                               hideProgBar();
                           })
                           .doOnNext(success ->{
                               if (success){
                                   mUsername.setTextColor(Color.BLUE);
                               }else{
                                   mUsername.setTextColor(Color.RED);
                               }
                           });
               } else {
                   return Observable.just(false);
               }

           })
           .distinctUntilChanged()
           .subscribe(aBoolean -> {
               mBtnLogin.setEnabled(aBoolean);
           }, throwable -> {
               throwable.printStackTrace();
           });
    }


    private Observable<Boolean> checkIfNameCorrect(String key) {
        Log.d(TAG, "Requested to Server - " + key);
        return Observable
                .just(key.equals(USERNAME))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void showProgBar() {
        mProfLogin.setVisibility(View.VISIBLE);
        Log.d(TAG, "Show Progress");
    }

    private void hideProgBar() {
        mProfLogin.setVisibility(View.GONE);
        Log.d(TAG, "Hide Progress");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
